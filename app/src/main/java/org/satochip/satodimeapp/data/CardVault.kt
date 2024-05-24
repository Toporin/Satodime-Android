package org.satochip.satodimeapp.data

import android.util.Log
import android.content.Context
import android.content.Context.MODE_PRIVATE
import org.satochip.javacryptotools.coins.Asset
import org.satochip.javacryptotools.coins.AssetType
import org.satochip.javacryptotools.explorers.CoinCombined
import org.satochip.satodimeapp.BuildConfig.DEBUG
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.util.SatodimePreferences
import org.satochip.satodimeapp.util.apiKeys
import org.satochip.satodimeapp.util.getBalanceDouble
import org.satochip.satodimeapp.util.newBaseCoin
import java.nio.ByteBuffer
import java.util.logging.Level

private const val TAG = "CardVault"
private const val DEBUG_EXPLORER = true
private val USE_MOCKUP_ADDRESSS = (DEBUG && DEBUG_EXPLORER) // use mockup address, only available in DEBUG mode!

final class CardVault (val cardSlot: CardSlot, val context: Context) {
    // preferences
    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
    val debugMode = prefs.getBoolean(SatodimePreferences.VERBOSE_MODE.name,false)
    val logLevel = if (debugMode) {Level.CONFIG} else {Level.WARNING}

    private val keySlip44: ByteArray = cardSlot.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int

    val isTestnet = keySlip44[0].toInt() and 0x80 == 0x00
    val state = cardSlot.slotState // UNINITIALIZED, SEALED, UNSEALED;
    val index: Int = cardSlot.index

    val baseCoin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)
    init {
        baseCoin.setLoggerLevel(logLevel)
        SatoLog.d(TAG,"CardVault constructor $keySlip44Int")
    }

    var nativeAsset: Asset = Asset()
    init {
        nativeAsset.type = AssetType.Coin
        nativeAsset.name = baseCoin.display_name
        nativeAsset.symbol = baseCoin.coin_symbol
        nativeAsset.address = if (cardSlot.pubkeyBytes != null) baseCoin.pubToAddress(cardSlot.pubkeyBytes) else {"undefined"}
        nativeAsset.explorerLink = baseCoin.getAddressWeburl(nativeAsset.address)
    }

    val priceExplorer = CoinCombined( nativeAsset.symbol, apiKeys, logLevel)

    // meta info such as icon
    val coin: Coin = try {
        if (nativeAsset.symbol == "ROP")
            Coin.ETH
        else if (nativeAsset.symbol == "MATIC") {
            Coin.MATIC
        }
        else
            Coin.valueOf(nativeAsset.symbol.take(3)) //todo: clean?
    } catch (e: IllegalArgumentException) {
        Coin.UNKNOWN
    }

    // from settings
    var selectedFirstCurrency: String? = nativeAsset.symbol //prefs.getString("firstCurrency", "BTC"); // native
    var selectedSecondCurrency: String? = prefs.getString(SatodimePreferences.SELECTED_CURRENCY.name, "USD");

    // asset list
    var tokenList: List<Asset> = emptyList()
    var nftList: List<Asset> = emptyList()

    fun fetchBalance(): Double?{
        SatoLog.d(TAG, "fetchBalance: START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (USE_MOCKUP_ADDRESSS) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            SatoLog.w(TAG, "fetchBalance: using mockup address $addressCopy instead of ${nativeAsset.address}")
        }

        try {
            var balance = baseCoin.getBalance(addressCopy)
            SatoLog.d(TAG, "fetchBalance balance: $balance")
            nativeAsset.balance = balance.toString()
            nativeAsset.decimals = "0" // no divisor
            return balance
        } catch (e: Exception) {
            nativeAsset.balance = null
            nativeAsset.decimals = null
            SatoLog.e(TAG, "fetchBalance exception: $e")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            return null
        }
    }

    fun fetchAssetValue(asset: Asset){
        SatoLog.d(TAG, "fetchAssetValue: START $asset")
        // note: valueInFirstCurrency is not used currently

        // testnet coins & assets have zero value!
        if (isTestnet){
            SatoLog.d(TAG, "fetchAssetValue: for a testnet is 0!")
            asset.rate = 0.0
            asset.rateCurrency = selectedSecondCurrency
            asset.rateAvailable = true
            asset.valueInFirstCurrency = "0"
            asset.firstCurrency = selectedFirstCurrency
            asset.valueInSecondCurrency = "0"
            asset.secondCurrency = selectedSecondCurrency
            return
        }

        // fetch base rate (usually USD) if not yet available
        if (!asset.rateAvailable) {
            if (asset.type == AssetType.Coin) {
                // fetch exchange rate
                val exchangeRate = priceExplorer.get_exchange_rate_between(asset.symbol, selectedSecondCurrency)
                SatoLog.d(TAG, "fetchAssetValue: exchange rate: ${asset.symbol} = $exchangeRate $selectedSecondCurrency")
                if (exchangeRate != null && exchangeRate>=0){
                    asset.rate = exchangeRate
                    asset.rateCurrency = selectedSecondCurrency
                    asset.rateAvailable = true
                }
            } else {
                // for token, rate is typically fetched when info is populated but with no guarantee
                // TODO: try to fetch from priceExplorer?
            }
        }

        // get exchange rate for selected currency and token value in this currency
        if (asset.rateAvailable){

            if (asset.rateCurrency == selectedSecondCurrency){
                // no need to fetch exchange rate to seconCurrency!
                val balanceDouble = getBalanceDouble(asset.balance, asset.decimals)
                if (balanceDouble != null && asset.rate != null) {
                    val valueDouble = balanceDouble * asset.rate
                    asset.valueInSecondCurrency = valueDouble.toString()
                    asset.secondCurrency = selectedSecondCurrency
                    SatoLog.d(TAG, "fetchAssetValue: exchange rate: ${asset.symbol} = ${asset.rate} $selectedSecondCurrency")
                    SatoLog.d(TAG, "fetchAssetValue: value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            } else {
                // fetch exchange rate between available rateCurrency and desired selectedSecondCurrency
                val exchangeRate = priceExplorer.get_exchange_rate_between(asset.rateCurrency, selectedSecondCurrency)
                SatoLog.d(TAG, "fetchAssetValue: exchange rate: ${asset.rateCurrency} = $exchangeRate $selectedSecondCurrency")

                val balanceDouble = getBalanceDouble(asset.balance, asset.decimals)
                if (balanceDouble != null && asset.rate != null && exchangeRate != null && exchangeRate >=0) {
                    val valueDouble = balanceDouble * asset.rate / exchangeRate
                    asset.valueInSecondCurrency = valueDouble.toString()
                    asset.secondCurrency = selectedSecondCurrency
                    SatoLog.d(TAG, "fetchAssetValue: value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            }

        } else {
            asset.valueInSecondCurrency = null
            asset.secondCurrency = null
            SatoLog.w(TAG, "fetchAssetValue: exchangeRate unavailable!")
        }
    }

    fun fetchTokenList(): List<Asset> {
        SatoLog.d(TAG, "fetchTokenList: START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (USE_MOCKUP_ADDRESSS) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            SatoLog.w(TAG, "fetchTokenList: using mockup address $addressCopy instead of ${nativeAsset.address}")
        }

        try {
            //TODO: check if token/NFT supported by blockchain
            tokenList = baseCoin.getAssetList(addressCopy)
            SatoLog.d(TAG, "fetchTokenList: tokenList: $tokenList")

            if (tokenList != null) {
                return tokenList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            SatoLog.e(TAG, "fetchTokenList: exception for $addressCopy: $e")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            return emptyList()
        }
    }

    fun fetchNftList(): List<Asset> {
        SatoLog.d(TAG, "fetchNftList: START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (USE_MOCKUP_ADDRESSS) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            SatoLog.w(TAG, "fetchNftList: using mockup address $addressCopy instead of ${nativeAsset.address}")
        }

        try {
            nftList = baseCoin.getNftList(addressCopy)
            SatoLog.d(TAG, "fetchNftList: nftList: $nftList")

            if (nftList != null) {
                return nftList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            SatoLog.e(TAG, "fetchNftList: exception for $addressCopy + $e")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            return emptyList()
        }
    }

}

private fun getMockupAddressForDebug(coin_symbol: String): String? {
    //for debug purpose only
    var addressCopy : String?= null
    if (coin_symbol == "XCP") {
        addressCopy = "1Do5kUZrTyZyoPJKtk4wCuXBkt5BDRhQJ4"
    } else if (coin_symbol == "ETH") {
        //addressCopy = "0xd5b06c8c83e78e92747d12a11fcd0b03002d48cf"
        //addressCopy = "0x86b4d38e451c707e4914ffceab9479e3a8685f98"
        //addressCopy = "0xE71a126D41d167Ce3CA048cCce3F61Fa83274535" // cryptopunk
        //addressCopy = "0xed1bf53Ea7fD8a290A3172B6c00F1Fb3657D538F" // usdt
        //addressCopy = "0x2c4ebd4b21736e992f3efeb55de37ae66457199d" // grolex nft
        addressCopy = "0x15a9300158A7fd97CF50eF54E976185e0e1F2771" // nod.i
    } else if (coin_symbol == "BTC") {
        addressCopy = "bc1ql49ydapnjafl5t2cp9zqpjwe6pdgmxy98859v2" // whale
    } else if (coin_symbol == "BNB") {
        addressCopy = "0x560eE56e87256E69AC6CC7aA00c54361fFe9af94" // usdc
    } else if (coin_symbol == "BCH") {
        addressCopy = "bitcoincash:pqtdjp63swypep62kyfxzh2k6kpq5weydvfqs9wpk2"
    } else if (coin_symbol == "LTC") {
        addressCopy = "ltc1qr07zu594qf63xm7l7x6pu3a2v39m2z6hh5pp4t"
    } else if (coin_symbol == "MATIC") {
        //addressCopy = "0x8db853Aa2f01AF401e10dd77657434536735aC62"
        //addressCopy = "0x86d22A8219De3683CF188778CDAdEE62D1442033"
        addressCopy = "0xE976c3052Df18cc2Dc878b9bc3191Bba68Ef3d80" // DolZ nft
        //addressCopy = "0x440D4955a914D5e29F861aC024A608aE41c56cB6" // PookyBall nft contract
        //addressCopy = "0xd7f1cbca340c831d77c0d8d3dc843a07873ade44" // PookyBall nft vault
        //addressCopy = "0xF977814e90dA44bFA03b6295A0616a897441aceC" // Binance hot wallet with USDT
    }
    return addressCopy
}


package org.satochip.satodime.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import org.satochip.javacryptotools.coins.Asset
import org.satochip.javacryptotools.coins.AssetType
import org.satochip.javacryptotools.explorers.CoinCombined
import org.satochip.satodime.util.SatodimePreferences
import org.satochip.satodime.util.apiKeys
import org.satochip.satodime.util.getBalanceDouble
import org.satochip.satodime.util.newBaseCoin
import java.nio.ByteBuffer
import java.util.logging.Level

private const val TAG = "CardVault"
private const val DEBUG_EXPLORER = true // use mockup address

// todo: add context?
final class CardVault (val cardSlot: CardSlot, val context: Context) {
    // preferences
    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
    val debugMode = prefs.getBoolean(SatodimePreferences.VERBOSE_MODE.name,false)
    val logLevel = if (debugMode) {Level.CONFIG} else {Level.WARNING}

    // TODO: clean and remove unnecessary/redundant fields
    private val keySlip44: ByteArray = cardSlot.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int

    val isTestnet = keySlip44[0].toInt() and 0x80 == 0x00
    val state = cardSlot.slotState // UNINITIALIZED, SEALED, UNSEALED;
    val index: Int = cardSlot.index

    val baseCoin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)
    init {
        baseCoin.setLoggerLevel(logLevel)
        Log.d(TAG,"CardVault constructor $keySlip44Int")
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
        if (nativeAsset.symbol == "ROP") Coin.ETH else Coin.valueOf(nativeAsset.symbol.take(3)) //todo: clean?
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
        Log.d(TAG, "DEBUG CardVault fetchBalance START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            Log.w(TAG, "Using mockup address $addressCopy instead of ${nativeAsset.address}")
        }

        try {
            Log.d(TAG, "fetchBalance address: $addressCopy")
            var balance = baseCoin.getBalance(addressCopy)
            Log.d(TAG, "fetchBalance balance: $balance")
            nativeAsset.balance = balance.toString()
            nativeAsset.decimals = "0" // no divisor
            return balance
        } catch (e: Exception) {
            nativeAsset.balance = null
            nativeAsset.decimals = null
            Log.e(TAG, "fetchBalance exception: $e")
            Log.e(TAG, Log.getStackTraceString(e))
            return null
        }
    }

    fun fetchAssetValue(asset: Asset){
        Log.d(TAG, "CardVault fetchAssetValue START $asset")
        // note: valueInFirstCurrency is not used currently

        // testnet coins & assets have zero value!
        if (isTestnet){
            Log.d(TAG, "CardVault fetchAssetValue for a testnet is 0!")
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
                Log.d(TAG, "CardVault fetchAssetValue exchange rate: ${asset.symbol} = $exchangeRate $selectedSecondCurrency")
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
                    Log.d(TAG, "CardVault fetchAssetValue exchange rate: ${asset.symbol} = ${asset.rate} $selectedSecondCurrency")
                    Log.d(TAG, "CardVault fetchAssetValue value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            } else {
                // fetch exchange rate between available rateCurrency and desired selectedSecondCurrency
                val exchangeRate = priceExplorer.get_exchange_rate_between(asset.rateCurrency, selectedSecondCurrency)
                Log.d(TAG, "CardVault fetchAssetValue exchange rate: ${asset.rateCurrency} = $exchangeRate $selectedSecondCurrency")

                val balanceDouble = getBalanceDouble(asset.balance, asset.decimals)
                if (balanceDouble != null && asset.rate != null && exchangeRate != null && exchangeRate >=0) {
                    val valueDouble = balanceDouble * asset.rate / exchangeRate
                    asset.valueInSecondCurrency = valueDouble.toString()
                    asset.secondCurrency = selectedSecondCurrency
                    Log.d(TAG, "CardVault fetchAssetValue value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            }

        } else {
            asset.valueInSecondCurrency = null
            asset.secondCurrency = null
            Log.d(TAG, "CardVault fetchAssetValue exchangeRate unavailable!")
        }
    }


    // todo list token + get balance...
    fun fetchTokenList(): List<Asset> {
        Log.d(TAG, "CardVault getAssetList START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            Log.w(TAG, "CardVault getAssetList using mockup address $addressCopy instead of ${nativeAsset}")
        }

        try {
            //TODO: check if token/NFT supported by blockchain
            Log.d(TAG, "address to fetch: $addressCopy")
            tokenList = baseCoin.getAssetList(addressCopy)
            Log.d(TAG, "tokenList: $tokenList")

            if (tokenList != null) {
                return tokenList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "CardVault getAssetList exception for $addressCopy: $e")
            Log.e(TAG, Log.getStackTraceString(e))
            return emptyList()
        }
    }

    fun fetchNftList(): List<Asset> {
        Log.d(TAG, "CardVault fetchNftList START ${nativeAsset.address}")

        var addressCopy = nativeAsset.address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol) ?: nativeAsset.address
            Log.w(TAG, "CardVault fetchNftList using mockup address $addressCopy instead of ${nativeAsset.address}")
        }

        try {
            Log.d(TAG, "CardVault fetchNftList address to fetch: $addressCopy")
            nftList = baseCoin.getNftList(addressCopy)
            Log.d(TAG, "nftList: $nftList")

            if (nftList != null) {
                return nftList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "CardVault fetchNftList exception for $addressCopy + $e")
            Log.e(TAG, Log.getStackTraceString(e))
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
    }
    return addressCopy
}


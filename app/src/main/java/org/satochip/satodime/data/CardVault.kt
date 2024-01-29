package org.satochip.satodime.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import org.satochip.javacryptotools.coins.Asset
import org.satochip.javacryptotools.coins.AssetType
import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Constants.BCH
import org.satochip.javacryptotools.coins.Constants.BTC
import org.satochip.javacryptotools.coins.Constants.ETH
import org.satochip.javacryptotools.coins.Constants.LTC
import org.satochip.javacryptotools.coins.Constants.XCP
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.javacryptotools.explorers.CoinCombined
import org.satochip.javacryptotools.explorers.Coingecko
import org.satochip.satodime.util.SatodimePreferences
import org.satochip.satodime.util.apiKeys
import org.satochip.satodime.util.getBalanceDouble
import java.nio.ByteBuffer
import java.util.logging.Level

private const val TAG = "CardVault"
private const val DEBUG_EXPLORER = true // use mockup address

// todo: add context?
public final class CardVault (val cardSlot: CardSlot, val context: Context) {
    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
    val debugMode = prefs.getBoolean(SatodimePreferences.DEBUG_MODE.name,false)
    val logLevel = if (debugMode) {Level.INFO} else {Level.WARNING}
    init {
        println("DEBUG in CardVault constructor START")
    }

    // TODO: clean and remove unnecessary/redundant fields
    private val keySlip44: ByteArray = cardSlot.slotStatus.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int
    val isTestnet = keySlip44[0].toInt() and 0x80 == 0x00 // to remove
    val baseCoin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)

    init {
        println("DEBUG in CardVault constructor START2")
        baseCoin.setLoggerLevel(logLevel)
        Log.d(TAG, "APIKEYS: $apiKeys")
    }

    var nativeAsset: Asset = Asset()
    // todo: populate asset
    init {
        println("DEBUG in CardVault constructor START4")
        nativeAsset.type = AssetType.Coin
        nativeAsset.name = baseCoin.display_name
        nativeAsset.symbol = baseCoin.coin_symbol
        nativeAsset.address = if (cardSlot.pubkey != null) baseCoin.pubToAddress(cardSlot.pubkey) else {"undefined"}
        nativeAsset.explorerLink = baseCoin.getAddressWeburl(nativeAsset.address)
    }

    val priceExplorer = CoinCombined( nativeAsset.symbol, apiKeys, logLevel)

    // TODO: legacy
    val coin: Coin = try {
        if (cardSlot.coinSymbol == "ROP") Coin.ETH else Coin.valueOf(cardSlot.coinSymbol.take(3))
    } catch (e: IllegalArgumentException) {
        Coin.UNKNOWN
    }
    val isSealed: Boolean = (cardSlot.slotState == SlotState.SEALED) // todo replace with state?!
    val displayName: String = baseCoin.display_name  //todo in nativeAsset
    val state = cardSlot.slotState // UNINITIALIZED, SEALED, UNSEALED;

    // privkey info (available on user request only)
    val privateKey: String? = "priv0000" // TODO: remove
    val entropy: String? = "entropy0000" // todo: remove
    val privateKeyHex: String? = null
    val privateKeyWif: String? = null
    val privateKeyEntropy: String? = null

    init {
        println("DEBUG in CardVault constructor displayName $displayName")
        println("DEBUG in CardVault constructor START3")
    }

    // TODO: add balance & assets info
    val index: Int = cardSlot.index
    init {
        println("DEBUG in CardVault constructor index: $index")
    }

    init {
        println("DEBUG in CardVault constructor cardSlot.pubkey: ${cardSlot.pubkey}")
    } //todo in nativeAsset
    var address: String = if (cardSlot.pubkey != null) baseCoin.pubToAddress(cardSlot.pubkey) else {"undefined"}
    init {
        println("DEBUG in CardVault constructor address: $address")
    }

    //todo in nativeAsset
    var addressLink: String = baseCoin.getAddressWeburl(address) // explorer url
    init {
        println("DEBUG in CardVault constructor addressLink: $addressLink")
    }

    // from settings
    var selectedFirstCurrency: String? = nativeAsset.symbol //prefs.getString("firstCurrency", "BTC"); // native
    var selectedSecondCurrency: String? = prefs.getString(SatodimePreferences.SELECTED_CURRENCY.name, "USD");
//    var selectedFirstCurrency: String? = "TODO:firstCurrency"
//    var selectedSecondCurrency: String? = "TODO:secondCurrency"
    var coinValueInFirstCurrency: Double? = null //todo in nativeAsset
    var coinValueInSecondCurrency: Double? = null //todo in nativeAsset

    var balance: Double? = null //getBalance() //null // async value //todo in nativeAsset
    init {
        println("DEBUG in CardVault constructor START5")
    }
    val currencyAmount: String = "$balance" //TODO improve display format //todo in nativeAsset
    init {
        println("DEBUG in CardVault constructor START6")
    }

    // asset list
    //var assetList: List<Asset> = emptyList()
    var tokenList: List<Asset> = emptyList()
    var nftList: List<Asset> = emptyList()
//    var tokenList: MutableMap<String, String>? = null
//    var nftList: MutableMap<String, String>? = null

    init {
        println("DEBUG in CardVault constructor END")
    }

    fun fetchBalance(): Double?{
        Log.d(TAG, "DEBUG CardVault fetchBalance START $address")

        var addressCopy = address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol)
            Log.w(TAG, "Using mockup address $addressCopy for vault $index")
        }

        try {
            Log.d(TAG, "fetchBalance address: $addressCopy")
            balance = baseCoin.getBalance(addressCopy)
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
        //Log.d(TAG, "DEBUG CardVault fetchAssetValue START $asset")
        // note: valueInFirstCurrency is not used currently

        // testnet coins & assets have zero value!
        if (isTestnet){
            Log.d(TAG, "DEBUG CardVault fetchAssetValue for a testnet is 0!")
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
                Log.d(TAG, "DEBUG CardVault fetchAssetValue exchange rate: ${asset.symbol} = $exchangeRate $selectedSecondCurrency")
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
                    Log.d(TAG, "DEBUG CardVault fetchAssetValue exchange rate: ${asset.symbol} = ${asset.rate} $selectedSecondCurrency")
                    Log.d(TAG, "DEBUG CardVault fetchAssetValue value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            } else {
                // fetch exchange rate between available rateCurrency and desired selectedSecondCurrency
                val exchangeRate = priceExplorer.get_exchange_rate_between(asset.rateCurrency, selectedSecondCurrency)
                Log.d(TAG, "DEBUG CardVault fetchAssetValue exchange rate: ${asset.rateCurrency} = $exchangeRate $selectedSecondCurrency")

                val balanceDouble = getBalanceDouble(asset.balance, asset.decimals)
                if (balanceDouble != null && asset.rate != null && exchangeRate != null && exchangeRate >=0) {
                    val valueDouble = balanceDouble * asset.rate / exchangeRate
                    asset.valueInSecondCurrency = valueDouble.toString()
                    asset.secondCurrency = selectedSecondCurrency
                    Log.d(TAG, "DEBUG CardVault fetchAssetValue value: ${valueDouble} = ${valueDouble.toString()} $selectedSecondCurrency")
                } else {
                    asset.valueInSecondCurrency = null
                    asset.secondCurrency = null
                }
            }

        } else {
            asset.valueInSecondCurrency = null
            asset.secondCurrency = null
            Log.d(TAG, "DEBUG CardVault fetchAssetValue exchangeRate unavailable!")
        }
    }


    // todo list token + get balance...
    fun fetchTokenList(): List<Asset> {
        Log.d(TAG, "DEBUG CardVault getAssetList START $address")

        var addressCopy = address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol)
            Log.w(TAG, "Using mockup address $addressCopy for vault $index")
        }

        try {
            //TODO: check if token/NFT supported by blockchain
            Log.d(TAG, "address to fetch: $addressCopy")
            tokenList = baseCoin.getAssetList(addressCopy)
            Log.d(TAG, "tokenList: $tokenList")
//            nftList = baseCoin.getNftList(addressCopy)
//            Log.d(TAG, "nftList: $nftList")

            if (tokenList != null) {
                return tokenList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch tokenList for $addressCopy!! $e")
            Log.e(TAG, Log.getStackTraceString(e))
            return emptyList()
        }
    }

    fun fetchNftList(): List<Asset> {
        Log.d(TAG, "DEBUG CardVault fetchNftList START $address")

        var addressCopy = address
        if (DEBUG_EXPLORER) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol)
            Log.w(TAG, "Using mockup address $addressCopy for vault $index")
        }

        try {
            Log.d(TAG, "address to fetch: $addressCopy")
            nftList = baseCoin.getNftList(addressCopy)
            Log.d(TAG, "nftList: $nftList")

            if (nftList != null) {
                return nftList
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch nftList for $addressCopy!! +$e")
            Log.e(TAG, Log.getStackTraceString(e))
            return emptyList()
        }
    }

}

private fun newBaseCoin(
    keySlip44Int: Int,
    isTestnet: Boolean,
    apiKeys: Map<String, String>
): BaseCoin {
    return when (keySlip44Int or -0x80000000) { // switch first bit (ignore testnet or mainnet)
        BTC -> Bitcoin(isTestnet, apiKeys)
        LTC -> Litecoin(isTestnet, apiKeys)
        BCH -> BitcoinCash(isTestnet, apiKeys)
        ETH -> Ethereum(isTestnet, apiKeys)
        XCP -> Counterparty(isTestnet, apiKeys)
        else -> UnsupportedCoin(isTestnet, apiKeys)
    }
}

private fun getMockupAddressForDebug(coin_symbol: String): String {
    //for debug purpose only
    var addressCopy = ""
    if (coin_symbol == "XCP") {
        addressCopy = "1Do5kUZrTyZyoPJKtk4wCuXBkt5BDRhQJ4"
    } else if (coin_symbol == "ETH") {
        //addressCopy = "0xd5b06c8c83e78e92747d12a11fcd0b03002d48cf"
        //addressCopy = "0x86b4d38e451c707e4914ffceab9479e3a8685f98"
        addressCopy = "0xE71a126D41d167Ce3CA048cCce3F61Fa83274535" // cryptopunk
        //addressCopy = "0xed1bf53Ea7fD8a290A3172B6c00F1Fb3657D538F" // usdt
        //addressCopy = "0x2c4ebd4b21736e992f3efeb55de37ae66457199d" // grolex nft
    } else if (coin_symbol == "BTC") {
        addressCopy = "bc1ql49ydapnjafl5t2cp9zqpjwe6pdgmxy98859v2" // whale
    } else if (coin_symbol == "BNB") {
        addressCopy = "0x560eE56e87256E69AC6CC7aA00c54361fFe9af94" // usdc
    }
    return addressCopy
}


package org.satochip.satodime.data

import org.satochip.javacryptotools.coins.Constants.BCH
import org.satochip.javacryptotools.coins.Constants.BTC
import org.satochip.javacryptotools.coins.Constants.ETH
import org.satochip.javacryptotools.coins.Constants.LTC
import org.satochip.javacryptotools.coins.Constants.XCP
import org.satochip.satodime.util.apiKeys
import java.nio.ByteBuffer
import android.util.Log
import org.satochip.javacryptotools.coins.Asset
import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import java.util.logging.Level


private const val TAG = "CardVault"
private const val DEBUG = true

// todo: add context?
public final class CardVault (val cardSlot: CardSlot) {

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
        if (DEBUG) {
            baseCoin.setLoggerLevel(Level.INFO)
        }
        Log.d(TAG, "APIKEYS: $apiKeys")
    }

    // TODO: legacy
    val coin: Coin = try {
        if (cardSlot.coinSymbol == "ROP") Coin.ETH else Coin.valueOf(cardSlot.coinSymbol.take(3))
    } catch (e: IllegalArgumentException) {
        Coin.UNKNOWN
    }
    val isSealed: Boolean = (cardSlot.slotState == SlotState.SEALED) // todo replace with state?!
    val displayName: String = baseCoin.display_name
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
    }
    var address: String = if (cardSlot.pubkey != null) baseCoin.pubToAddress(cardSlot.pubkey) else {"undefined"}
    init {
        println("DEBUG in CardVault constructor address: $address")
    }

    var addressLink: String = baseCoin.getAddressWeburl(address) // explorer url
    init {
        println("DEBUG in CardVault constructor addressLink: $addressLink")
    }

    // from settings
//    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
//    var selectedFirstCurrency: String? = prefs.getString("firstCurrency", "USD");
//    var selectedSecondCurrency: String? = prefs.getString("secondCurrency", "USD");
    var selectedFirstCurrency: String? = "TODO:firstCurrency"
    var selectedSecondCurrency: String? = "TODO:secondCurrency"
    var coinValueInFirstCurrency: Double? = null
    var coinValueInSecondCurrency: Double? = null

    init {
        println("DEBUG in CardVault constructor START4")
    }

    var balance: Double? = null //getBalance() //null // async value
    init {
        println("DEBUG in CardVault constructor START5")
    }
    val currencyAmount: String = "$balance" //TODO improve display format
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

    fun getBalanceDebug(): Double?{
        println("DEBUG CardVault getBalanceDebug START $address")

        var addressCopy = address
        if (DEBUG) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol)
            Log.w(TAG, "Using mockup address $addressCopy for vault $index")
        }

        try {
            println("address to fetch: $addressCopy")
            balance = baseCoin.getBalance(addressCopy)
            println("Balance: $balance")
            return balance
        } catch (e: Exception) {
            println("Failed to fetch balance!!")
            Log.e(TAG, Log.getStackTraceString(e))
            return null
        }
    }

    // todo list token + get balance...
    fun fetchTokenList(): List<Asset> {
        Log.d(TAG, "DEBUG CardVault getAssetList START $address")

        var addressCopy = address
        if (DEBUG) {
            addressCopy = getMockupAddressForDebug(baseCoin.coin_symbol)
            Log.w(TAG, "Using mockup address $addressCopy for vault $index")
        }

        try {
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
        Log.d(TAG, "DEBUG CardVault getAssetList START $address")

        var addressCopy = address
        if (DEBUG) {
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


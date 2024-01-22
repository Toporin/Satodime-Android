package org.satochip.satodime.data

import org.satochip.javacryptotools.BaseCoin
import org.satochip.javacryptotools.Bitcoin
import org.satochip.javacryptotools.BitcoinCash
import org.satochip.javacryptotools.Counterparty
import org.satochip.javacryptotools.Ethereum
import org.satochip.javacryptotools.Litecoin
import org.satochip.javacryptotools.UnsupportedCoin
import org.satochip.javacryptotools.coins.Constants.BCH
import org.satochip.javacryptotools.coins.Constants.BTC
import org.satochip.javacryptotools.coins.Constants.ETH
import org.satochip.javacryptotools.coins.Constants.LTC
import org.satochip.javacryptotools.coins.Constants.XCP
import org.satochip.satodime.util.apiKeys
import java.nio.ByteBuffer
import android.util.Log


private const val TAG = "CardVault"

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

    var balance: Double? = getBalanceDebug() //null // async value
    init {
        println("DEBUG in CardVault constructor START5")
    }
    val currencyAmount: String = "$balance" //TODO improve display format
    init {
        println("DEBUG in CardVault constructor START6")
    }

    // asset list
    var tokenList: MutableMap<String, String>? = null
    var nftList: MutableMap<String, String>? = null

    init {
        println("DEBUG in CardVault constructor END")
    }

    fun getBalanceDebug(): Double?{
        println("DEBUG CardVault getBalanceDebug START")
        try {
            println("address to fetch: $address")
            var balance = baseCoin.getBalance(address)
            println("Balance: $balance")
            return balance
        } catch (e: Exception) {
            println("Failed to fetch balance!!")
            Log.e(TAG, Log.getStackTraceString(e))
            return null
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


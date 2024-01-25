package org.satochip.satodime.util

import android.icu.text.NumberFormat
import android.util.Log
import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.javacryptotools.coins.Constants
import org.satochip.satodime.BuildConfig
import org.satochip.satodime.data.Coin
import org.satochip.satodime.data.Currency
import org.satochip.satodime.data.Token
import java.util.Locale

private const val TAG = "Utils"

val apiKeys = hashMapOf(
    Pair("API_KEY_ETHERSCAN", BuildConfig.API_KEY_ETHERSCAN),
    Pair("API_KEY_ETHPLORER", BuildConfig.API_KEY_ETHPLORER),
    Pair("API_KEY_BSCSCAN", BuildConfig.API_KEY_BSCSCAN),
    Pair("API_KEY_RARIBLE", BuildConfig.API_KEY_RARIBLE),
)

fun getCurrencyAmount(coin: Coin, isTestnet: Boolean, balance: Double, currency: Currency) : String {
    return try {
        val rate = getCoinExplorer(coin, isTestnet).get_exchange_rate_between(currency.name.lowercase())
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())//TODO platform specific
        formatter.maximumFractionDigits = 2
        val amount = formatter.format(balance * rate)
        when(currency) {
            Currency.USD -> "$$amount"
            Currency.EUR -> "$amount€"
            Currency.BTC -> "₿$amount"
            Currency.ETH -> "$amount ETH"
        }
    } catch (e: Exception) {
        Log.e(TAG, Log.getStackTraceString(e))
        "N/A"
    }
}

fun getCoinBalance(coin: Coin, isTestnet: Boolean, address: String): Double? {
    return try {
        getCoinExplorer(coin, isTestnet).getBalance(address)
    } catch (e: Exception) {
        Log.e(TAG, Log.getStackTraceString(e))
        null
    }
}

fun getTokenBalance(coin: Coin, isTestnet: Boolean, token: Token, address: String): Double? {
    return try {
        getCoinExplorer(coin, isTestnet).getTokenBalance(address, token.contractAddress)
    } catch (e: Exception) {
        Log.e(TAG, Log.getStackTraceString(e))
        null
    }
}

fun getTokenCurrencyAmount(coin: Coin, isTestnet: Boolean, balance: Double, token: Token, currency: Currency) : String {
    return try {
        val rate = getCoinExplorer(coin, isTestnet).get_token_exchange_rate_between(
            token.contractAddress,
            currency.name.lowercase()
        )
        val fiatAmount = balance * rate
        "$ $fiatAmount"
    } catch (e: Exception) {
        Log.e(TAG, Log.getStackTraceString(e))
        "N/A"
    }
}

// todo: move in own file?
fun coinToSlip44Bytes(coinSymbol: String, isTestnet: Boolean): ByteArray {
    var slip44Int = when (coinSymbol){
        "BTC" -> Constants.BTC
        "LTC" -> Constants.LTC
        "BCH" -> Constants.BCH
        "ETH" -> Constants.ETH
        "XCP" -> Constants.XCP
        else -> Constants.BTC // should not happen
    }

    if (isTestnet){
        slip44Int = (slip44Int and 0x7fffffff) // set first bit to 0
    }

    // convert to ByterArray
    var slip44Bytes = intToBytes(slip44Int)
    Log.d(TAG, "coinToSlip44Bytes: $coinSymbol $slip44Int $slip44Bytes")

    return slip44Bytes
}

fun intToBytes(int: Int): ByteArray {
    Log.d(TAG, "Utils intToBytes int: $int")
    var valint = int
    var bytes = ByteArray(4)
    //for (index in 0..3){
    for (index in 3 downTo 0){ //
        bytes[index] = (valint and 0xff).toByte()
        valint = (valint shr 8 )
    }
    Log.d(TAG, "Utils intToBytes bytes: ${bytes[0]} ${bytes[1]} ${bytes[2]} ${bytes[3]}")
    return bytes
}
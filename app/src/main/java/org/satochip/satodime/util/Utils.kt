package org.satochip.satodime.util

import android.icu.text.NumberFormat
import android.util.Log
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
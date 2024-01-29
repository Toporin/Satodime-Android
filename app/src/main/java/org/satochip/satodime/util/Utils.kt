package org.satochip.satodime.util

import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
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
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.pow

private const val TAG = "Utils"

val apiKeys = hashMapOf(
    Pair("API_KEY_ETHERSCAN", BuildConfig.API_KEY_ETHERSCAN),
    Pair("API_KEY_ETHPLORER", BuildConfig.API_KEY_ETHPLORER),
    Pair("API_KEY_BSCSCAN", BuildConfig.API_KEY_BSCSCAN),
    Pair("API_KEY_RARIBLE", BuildConfig.API_KEY_RARIBLE),
)

/** Convert an IPFS address to IPFS gateway (https) address */
fun sanitizeNftImageUrlString(link: String): String {
    if (link == null){
        return ""
    }
    var nftImageUrlString = link
    // check if IPFS? => use ipfs.io gateway
    // todo: support ipfs protocol
    if (nftImageUrlString.startsWith("ipfs://ipfs/")) {
        //ipfs://ipfs/bafybeia4kfavwju5gjjpilerm2azdoxvpazff6fmtatqizdpbmcolpsjci/image.png
        //https://ipfs.io/ipfs/bafybeia4kfavwju5gjjpilerm2azdoxvpazff6fmtatqizdpbmcolpsjci/image.png
        nftImageUrlString = nftImageUrlString.removePrefix("ipfs:/");
        nftImageUrlString = "https://ipfs.io" + nftImageUrlString
    } else if (nftImageUrlString.startsWith("ipfs://"))  {
        // ipfs://QmZ2ddtVUV1brVGjpq6vgrG6jEgEK3CqH19VURKzdwCSRf
        // https://ipfs.io/ipfs/QmZ2ddtVUV1brVGjpq6vgrG6jEgEK3CqH19VURKzdwCSRf
        nftImageUrlString = nftImageUrlString.removePrefix("ipfs:/");
        nftImageUrlString = "https://ipfs.io/ipfs" + nftImageUrlString
    } else {
        // do nothing
        return nftImageUrlString
    }
    Log.d(TAG, "Converted link: $link to: $nftImageUrlString")
    return nftImageUrlString
}

fun getBalanceDouble(balanceString: String?, decimalsString: String?): Double?{

    if (balanceString == null){
        return null
    }

    // convert decimals to double value or 0.0
    val decimalsDouble = (decimalsString?:"0").toDoubleOrNull() ?: 0.0
    val balanceDouble = balanceString.toDoubleOrNull() ?: return null

    //
    //val balance = balanceDouble / (10.0).pow(decimalsDouble)
    val balance = balanceDouble / (10.0).pow(decimalsDouble)
    return balance
}

fun formatBalance(balanceString: String?, decimalsString: String?, symbol: String?, maxFractionDigit: Int = 8): String{
    val balanceDouble = getBalanceDouble(balanceString, decimalsString)
    return formatBalance(balanceDouble, symbol, maxFractionDigit)
}

fun formatBalance(balanceDouble: Double?, symbol: String?, maxFractionDigit: Int = 8): String{
    val symbolString = symbol?:""

    if (balanceDouble == null){
        return ""// "? $symbolString"
    }

    // set the number of significant digits depending on coin
    val decimalFormat: DecimalFormat = when(symbolString){
        "BTC" -> DecimalFormat("###.########")
        "ETH" -> DecimalFormat("###.######")
        else -> {DecimalFormat("###.##")}
    }

    val balance = decimalFormat.format(balanceDouble)

    // todo: format number
    return "$balance $symbolString"
}

/////

//
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
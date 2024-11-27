package org.satochip.satodimeapp.util

import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Constants
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.Polygon
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.satodimeapp.BuildConfig
import org.satochip.satodimeapp.services.SatoLog
import java.text.DecimalFormat
import kotlin.math.pow

private const val TAG = "Utils"

val apiKeys = hashMapOf(
    Pair("API_KEY_ETHERSCAN", BuildConfig.API_KEY_ETHERSCAN),
    Pair("API_KEY_ETHPLORER", BuildConfig.API_KEY_ETHPLORER),
    Pair("API_KEY_BSCSCAN", BuildConfig.API_KEY_BSCSCAN),
    Pair("API_KEY_RARIBLE", BuildConfig.API_KEY_RARIBLE),
    Pair("API_KEY_COVALENT", BuildConfig.API_KEY_COVALENT),
    Pair("API_KEY_PAYBIS_ID", BuildConfig.API_KEY_PAYBIS_ID),
    Pair("API_KEY_PAYBIS_HMAC", BuildConfig.API_KEY_PAYBIS_HMAC),
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
    SatoLog.d(TAG, "sanitizeNftImageUrlString: converted link: $link to: $nftImageUrlString")
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
    val balance = balanceDouble / (10.0).pow(decimalsDouble)
    return balance
}

fun formatBalance(balanceString: String?, decimalsString: String?, symbol: String?, maxFractionDigit: Int = 8): String{
    val balanceDouble = getBalanceDouble(balanceString, decimalsString)
    return formatBalance(balanceDouble, symbol, maxFractionDigit)
}

fun formatBalance(balanceDouble: Double?, symbol: String?, maxFractionDigit: Int = 8): String{
    val symbolString = symbol?:""

    if (balanceDouble == null || balanceDouble < 0){
        return "" // "? $symbolString"
    }

    // set the number of significant digits depending on coin
    val decimalFormat: DecimalFormat = when(symbolString){
        "BTC" -> DecimalFormat("###.########")
        "ETH" -> DecimalFormat("###.######")
        "MATIC" -> DecimalFormat("###.######")
        "BCH" -> DecimalFormat("###.########")
        else -> {DecimalFormat("###.##")}
    }

    val balance = decimalFormat.format(balanceDouble)

    return "$balance $symbolString"
}

fun newBaseCoin(
    keySlip44Int: Int,
    isTestnet: Boolean,
    apiKeys: Map<String, String>
): BaseCoin {
    return when (keySlip44Int or -0x80000000) { // switch first bit (ignore testnet or mainnet)
        Constants.BTC -> Bitcoin(isTestnet, apiKeys)
        Constants.LTC -> Litecoin(isTestnet, apiKeys)
        Constants.BCH -> BitcoinCash(isTestnet, apiKeys)
        Constants.ETH -> Ethereum(isTestnet, apiKeys)
        Constants.MATIC -> Polygon(isTestnet, apiKeys)
        Constants.XCP -> Counterparty(isTestnet, apiKeys)
        else -> UnsupportedCoin(isTestnet, apiKeys)
    }
}

fun coinToSlip44Bytes(coinSymbol: String, isTestnet: Boolean): ByteArray {
    var slip44Int = when (coinSymbol){
        "BTC" -> Constants.BTC
        "LTC" -> Constants.LTC
        "BCH" -> Constants.BCH
        "ETH" -> Constants.ETH
        "XCP" -> Constants.XCP
        "MATIC" -> Constants.MATIC
        else -> Constants.BTC // should not happen
    }

    if (isTestnet){
        slip44Int = (slip44Int and 0x7fffffff) // set first bit to 0
    }

    // convert to ByterArray
    var slip44Bytes = intToBytes(slip44Int)
    SatoLog.d(TAG, "coinToSlip44Bytes: $coinSymbol $slip44Int $slip44Bytes")

    return slip44Bytes
}

fun intToBytes(int: Int): ByteArray {
    SatoLog.d(TAG, "intToBytes int: $int")
    var valint = int
    var bytes = ByteArray(4)
    for (index in 3 downTo 0){ //
        bytes[index] = (valint and 0xff).toByte()
        valint = (valint shr 8 )
    }
    SatoLog.d(TAG, "intToBytes bytes: ${bytes[0]} ${bytes[1]} ${bytes[2]} ${bytes[3]}")
    return bytes
}
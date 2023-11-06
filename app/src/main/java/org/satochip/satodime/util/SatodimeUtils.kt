package org.satochip.satodime.util

import org.bouncycastle.util.encoders.Hex
import org.satochip.client.Constants.NFTSET
import org.satochip.client.Constants.TOKENSET
import org.satochip.javacryptotools.coins.Constants.MAP_SLIP44_BY_SYMBOL
import java.math.BigInteger//TODO platform specific
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

// -------------------------- FROM PREVIOUS SATODIME APP ------------------------//TODO improve exception management

fun getSlip44(coinName: String, isTestnet: Boolean = false) : ByteArray {
    val slip44Int = if (isTestnet) {
        MAP_SLIP44_BY_SYMBOL[coinName]!! and 0x7FFFFFFF
    } else MAP_SLIP44_BY_SYMBOL[coinName]!!
    val buffer = ByteBuffer.allocate(4)
    buffer.putInt(slip44Int) // big endian
    return buffer.array()
}

fun getContractByteTLV(contract: String, coinName: String, asset: String) : ByteArray {
    val isToken = TOKENSET.contains(asset)
    val isNFT = NFTSET.contains(asset)
    val contractBytes: ByteArray = if (isToken || isNFT) {
        checkContractFieldToBytes(contract, coinName)
    } else {
        ByteArray(0) // ignore contract value
    }
    val contractByteTLV = ByteArray(34)
    contractByteTLV[0] = 0.toByte()
    contractByteTLV[1] = contractBytes.size.toByte()
    System.arraycopy(contractBytes, 0, contractByteTLV, 2, contractBytes.size)
    return contractByteTLV
}

fun getTokenIdByteTLV(tokenId: String, asset: String) : ByteArray {
    val tokenIdBytes: ByteArray = if (NFTSET.contains(asset)) {
        checkTokenidFieldToBytes(tokenId)
    } else {
        ByteArray(0) // ignore tokenID
    }
    val tokenIdByteTLV = ByteArray(34)
    tokenIdByteTLV[0] = 0.toByte()
    tokenIdByteTLV[1] = tokenIdBytes.size.toByte()
    System.arraycopy(tokenIdBytes, 0, tokenIdByteTLV, 2, tokenIdBytes.size)
    return  tokenIdByteTLV
}

@Throws(Exception::class)
private fun checkContractFieldToBytes(pContract: String, coin: String): ByteArray {
    var contract = pContract
    if (contract == "") return ByteArray(0)
    val contractBytes: ByteArray?
    val hexCoinList: List<String> = mutableListOf("ETH", "ETC", "BSC")
    return if (hexCoinList.contains(coin)) {
        if (!contract.matches("^(0x)?[0-9a-fA-F]{40}$".toRegex())) {
            throw Exception("wrong contract address format")
        }
        contract = contract.replaceFirst("^0x".toRegex(), "") // remove "0x" if present
        contractBytes = try {
            Hex.decode(contract)
        } catch (e: Exception) {
            throw Exception("wrong contract address hex format")
        }
        if (contractBytes!!.size > 20) {
            throw Exception("contract field size is too long")
        }
        contractBytes
    } else if (coin == "XCP") {
        var asset = contract
        var subAsset = ""
        if (asset.contains(".")) {
            // contains subasset
            val parts = asset.split(".".toRegex(), limit = 2).toTypedArray()
            asset = parts[0]
            subAsset = parts[1]
            val maxLength = (250 - asset.length - 1).toString()
            val pattern = "^[a-zA-Z0-9.-_@!]+$maxLength}$"
            if (!subAsset.matches(pattern.toRegex())) {
                throw Exception("wrong contract address format") // "Wrong subasset format: {subasset} (check for length and unauthorized characters)"
            }
        }
        if (asset.startsWith("A") || asset.startsWith("a")) {
            // numeric asset
            asset = asset.uppercase() // a=>A
            val nbrString = asset.substring(1) // remove the "A"
            val nbrBig: BigInteger = try {
                BigInteger(nbrString)
            } catch (e: Exception) {
                throw Exception("Wrong numeric asset format: {asset} is not an integer")
            }
            val minBound = BigInteger("26").pow(12).add(BigInteger.ONE)
            val maxBound = BigInteger("256").pow(8)
            if (nbrBig < minBound || nbrBig > maxBound) {
                throw Exception("Wrong numeric asset format: {asset} (numeric value outside of bounds)")
            }
        } else {
            // named asset
            asset = asset.uppercase()
            if (!asset.matches("^[A-Z]{4,12}$".toRegex())) {
                throw Exception("Wrong named asset format: {asset} (should be 4-12 uppercase latin characters)")
            }
        }
        // encode
        contract = if (subAsset == "") {
            asset
        } else {
            "$asset.$subAsset"
        }
        contractBytes = contract.toByteArray(StandardCharsets.UTF_8)
        if (contractBytes.size > 32) {
            throw Exception("Unfortunately, Satodime supports only asset name smaller than 32 char")
        }
        contractBytes
    } else {
        throw Exception("Unsupported blockchain$coin")
    }
}

@Throws(Exception::class)
private fun checkTokenidFieldToBytes(pTokenId: String): ByteArray {
    var tokenid = pTokenId
    if (tokenid == "") tokenid = "0" // default
    val tokenIdBig = BigInteger(tokenid) // tokenid is decimal-formated
    var tokenIdHexString = tokenIdBig.toString(16) // convert to hex string
    if (tokenIdHexString.length % 2 == 1) {
        tokenIdHexString = "0$tokenIdHexString" // must have an even number of chars
    }
    val tokenIdBytes = Hex.decode(tokenIdHexString) // convert to bytes
    if (tokenIdBytes.size > 32) {
        throw Exception("tokenid field size is too long!")
    }
    return tokenIdBytes
}
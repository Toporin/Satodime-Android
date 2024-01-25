package org.satochip.satodime.data

import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.javacryptotools.coins.Constants
import org.satochip.satodime.util.apiKeys
import java.nio.ByteBuffer

data class CardPrivkey(
    val slip44: ByteArray = ByteArray(4),
    val privateKeyInfos: Map<String, ByteArray>? = null,
) {

    //private val keySlip44: ByteArray = slotStatus.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(slip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int

    // TODO: clean and remove unnecessary/redundant fields
    val isTestnet = slip44[0].toInt() and 0x80 == 0x00 // to remove
    val coin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)

    val privkeyBytes = privateKeyInfos?.get("privkey")
    val privkeyHex = if (privkeyBytes == null) null else "0x" + SatochipParser.toHexString(privkeyBytes)
    val privkeyWif = if (privkeyBytes == null) null else coin.encodePrivkey(privkeyBytes)
    val entropyBytes = privateKeyInfos?.get("entropy")
    val entropyHex = if (privkeyBytes == null) null else SatochipParser.toHexString(entropyBytes)

}

// todo: move in own file?
private fun newBaseCoin(
    keySlip44Int: Int,
    isTestnet: Boolean,
    apiKeys: Map<String, String>
): BaseCoin {
    return when (keySlip44Int or -0x80000000) { // switch first bit (ignore testnet or mainnet)
        Constants.BTC -> Bitcoin(isTestnet, apiKeys)
        Constants.LTC -> Litecoin(isTestnet, apiKeys)
        Constants.BCH -> BitcoinCash(isTestnet, apiKeys)
        Constants.ETH -> Ethereum(isTestnet, apiKeys)
        Constants.XCP -> Counterparty(isTestnet, apiKeys)
        else -> UnsupportedCoin(isTestnet, apiKeys)
    }
}
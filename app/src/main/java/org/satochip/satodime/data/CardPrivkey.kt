package org.satochip.satodime.data

import org.satochip.client.SatochipParser
import org.satochip.satodime.util.apiKeys
import org.satochip.satodime.util.newBaseCoin
import java.nio.ByteBuffer

data class CardPrivkey(
    val slip44: ByteArray = ByteArray(4),
    val privateKeyInfos: Map<String, ByteArray>? = null,
) {

    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(slip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int

    // needed to compute WIF format (blockchain dependent)
    val isTestnet = slip44[0].toInt() and 0x80 == 0x00
    val coin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)

    val privkeyBytes = privateKeyInfos?.get("privkey")
    val privkeyHex = if (privkeyBytes == null) null else "0x" + SatochipParser.toHexString(privkeyBytes)
    val privkeyWif = if (privkeyBytes == null) null else coin.encodePrivkey(privkeyBytes)
    val entropyBytes = privateKeyInfos?.get("entropy")
    val entropyHex = if (privkeyBytes == null) null else SatochipParser.toHexString(entropyBytes)

}
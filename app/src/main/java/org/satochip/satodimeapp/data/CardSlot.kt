package org.satochip.satodimeapp.data

import java.nio.ByteBuffer

data class CardSlot(
    val index: Int,
    val keySlip44: ByteArray,
    var keysState: Byte,
    val pubkeyBytes: ByteArray? = null,
) {
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    val keySlip44Int = wrappedKeySlip44.int // todo convert using utils ?

    val slotState = SlotState.byteAsSlotState(keysState)

    //var pubkeyHex: String? = if (pubKeyBytes!=null) {SatochipParser.toHexString(pubKeyBytes)} else {null}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CardSlot

        if (keysState != other.keysState) return false
        if (keySlip44Int != other.keySlip44Int) return false
        if (pubkeyBytes != null) {
            if (other.pubkeyBytes == null) return false
            if (!pubkeyBytes.contentEquals(other.pubkeyBytes)) return false
        } else if (other.pubkeyBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keysState.hashCode()
        result = 37 * result + keySlip44Int
        result = 31 * result + (pubkeyBytes?.contentHashCode() ?: 0)
        return result
    }

}


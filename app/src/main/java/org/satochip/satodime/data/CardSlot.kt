package org.satochip.satodime.data

import org.satochip.client.Constants.MAP_ASSET_BY_CODE
import org.satochip.client.Constants.NFTSET
import org.satochip.client.Constants.TOKENSET
import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
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

data class CardSlot(
    val slotStatus: SatodimeKeyslotStatus,
    val keysState: Byte,
    val publicKeyBytes: ByteArray? = null,
    val privateKeyInfos: Map<String, ByteArray>? = null,
) {
    private val keySlip44: ByteArray = slotStatus.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    private val keySlip44Int = wrappedKeySlip44.int

    val isTestnet = keySlip44[0].toInt() and 0x80 == 0x00
    val coin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)
    val coinSymbol: String = coin.coin_symbol
    val coinDisplayName: String = coin.display_name
    val assetName = MAP_ASSET_BY_CODE[slotStatus.keyAsset.toInt()]
    val isToken = TOKENSET.contains(assetName)
    val isNFT = NFTSET.contains(assetName)
    val publicKeyHexString: String = SatochipParser.toHexString(publicKeyBytes)
    val address: String? = if (publicKeyBytes == null) null else coin.pubToAddress(publicKeyBytes)
    val slotState = SlotState.byteAsSlotState(keysState)
    val privateKeyBytes = privateKeyInfos?.get("privkey")
    val privateKeyHex = "0x" + SatochipParser.toHexString(privateKeyBytes)
    val privateKeyWif = if (privateKeyBytes == null) null else coin.encodePrivkey(privateKeyBytes)
    val entropyHex: String = SatochipParser.toHexString(privateKeyInfos?.get("entropy"))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CardSlot

        if (slotStatus != other.slotStatus) return false
        if (keysState != other.keysState) return false
        if (publicKeyBytes != null) {
            if (other.publicKeyBytes == null) return false
            if (!publicKeyBytes.contentEquals(other.publicKeyBytes)) return false
        } else if (other.publicKeyBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slotStatus.hashCode()
        result = 31 * result + keysState
        result = 31 * result + (publicKeyBytes?.contentHashCode() ?: 0)
        return result
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


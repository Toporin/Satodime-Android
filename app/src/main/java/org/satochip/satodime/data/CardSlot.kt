package org.satochip.satodime.data

import org.satochip.client.Constants.MAP_ASSET_BY_CODE
import org.satochip.client.Constants.NFTSET
import org.satochip.client.Constants.TOKENSET
import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.javacryptotools.coins.Constants.BCH
import org.satochip.javacryptotools.coins.Constants.BTC
import org.satochip.javacryptotools.coins.Constants.ETH
import org.satochip.javacryptotools.coins.Constants.LTC
import org.satochip.javacryptotools.coins.Constants.XCP
import org.satochip.satodime.util.apiKeys
import java.nio.ByteBuffer

data class CardSlot(
    val slotStatus: SatodimeKeyslotStatus,
    var keysState: Byte,
    val publicKeyBytes: ByteArray? = null,
    val privateKeyInfos: Map<String, ByteArray>? = null, // todo remove
) {
    private val keySlip44: ByteArray = slotStatus.keySlip44
    private val wrappedKeySlip44: ByteBuffer = ByteBuffer.wrap(keySlip44)//TODO platform specific
    val keySlip44Int = wrappedKeySlip44.int // todo convert using utils ?

    // TODO: clean and remove unnecessary/redundant fields
    val isTestnet = keySlip44[0].toInt() and 0x80 == 0x00 // to remove
    val coin = newBaseCoin(keySlip44Int, isTestnet, apiKeys)

    // TODO: remove & compute as needed
    val coinSymbol: String = coin.coin_symbol
    val coinDisplayName: String = coin.display_name
    val assetName = MAP_ASSET_BY_CODE[slotStatus.keyAsset.toInt()] // deprecated
    val isToken = TOKENSET.contains(assetName) //deprecated
    val isNFT = NFTSET.contains(assetName) // deprecated
    val publicKeyHexString: String = SatochipParser.toHexString(publicKeyBytes)
    //val address: String? = if (publicKeyBytes == null) null else coin.pubToAddress(publicKeyBytes)
    val slotState = SlotState.byteAsSlotState(keysState)
    val privateKeyBytes = privateKeyInfos?.get("privkey")
    val privateKeyHex = "0x" + SatochipParser.toHexString(privateKeyBytes)
    val privateKeyWif = if (privateKeyBytes == null) null else coin.encodePrivkey(privateKeyBytes)
    val entropyHex: String = SatochipParser.toHexString(privateKeyInfos?.get("entropy"))

    // TODO: add balance & assets info
    val index: Int = 0
    var pubkey: ByteArray? = publicKeyBytes
    var balance: Double? = null // async value
    var address: String? = "(undefined)"
    var addressLink: String? = null // explorer url

    var selectedFirstCurrency: String? = null
    var selectedSecondCurrency: String? = null
    var coinValueInFirstCurrency: Double? = null
    var coinValueInSecondCurrency: Double? = null

    // asset list
    var tokenList: MutableMap<String, String>? = null
    var nftList: MutableMap<String, String>? = null
    // private info
    var privkey: ByteArray? = null
    var entropy: ByteArray? = null

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

// todo: move in own file?
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


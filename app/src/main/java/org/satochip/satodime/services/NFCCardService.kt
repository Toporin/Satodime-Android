package org.satochip.satodime.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.satochip.client.Constants.MAP_CODE_BY_ASSET
import org.satochip.client.SatochipCommandSet
import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
import org.satochip.client.SatodimeStatus
import org.satochip.io.APDUResponse
import org.satochip.satodime.data.Asset
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.Coin
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.util.getContractByteTLV
import org.satochip.satodime.util.getSlip44
import org.satochip.satodime.util.getTokenIdByteTLV
import java.security.SecureRandom
import kotlin.concurrent.thread

private const val TAG = "NFCCardService"

object NFCCardService {

    var isConnected = MutableLiveData(false)
    var isReadingFinished = MutableLiveData(true)
    var waitForSetup = MutableLiveData(false)
    var isOwner = MutableLiveData(false)
    var isAuthentic = MutableLiveData(false)
    var cardSlots = MutableLiveData<List<CardSlot>>()
    var authenticationKeyHex: String? = null
    var unlockSecret: String? = null
    var certificate: String? = null

    private var cmdSet: SatochipCommandSet? = null
    private var satodimeStatus: SatodimeStatus? = null

    fun initialize(cmdSet: SatochipCommandSet) {
        NFCCardService.cmdSet = cmdSet
        cmdSet.cardSelect("satodime")
        readCard()
    }

    fun readCard() {
        try {
            authenticationKeyHex = SatochipParser.toHexString(cmdSet?.authentikey)
            cmdSet?.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet?.applicationStatus ?: return
            waitForSetup.postValue(!cardStatus.isSetupDone)
            Log.d(TAG, "Need setup (has no owner) : " + !cardStatus.isSetupDone)
            satodimeStatus = cmdSet?.satodimeStatus
            Log.d(TAG, "Authentication key : $authenticationKeyHex")
            updateCardSlots()
            isAuthentic.postValue(verifyAuthenticity())
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown while reading the card.")
            Log.e(TAG, Log.getStackTraceString(e))
        }
        Log.i(TAG, "Card reading finished")
        isReadingFinished.postValue(true)
    }

    fun acceptOwnership() {
        Log.d(TAG, "Accept ownership")
        unlockSecret = try {
            val secret = takeOwnershipAndRetrieveUnlockSecret()
            waitForSetup.postValue(false)
            isOwner.postValue(true)
            secret
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown while taking ownership.")
            Log.e(TAG, Log.getStackTraceString(e))
            null
        }
        Log.d(TAG, "Unlock secret $unlockSecret")
    }

    fun dismissOwnership() {
        Log.d(TAG, "Dismiss ownership")
        waitForSetup.postValue(false)
        isOwner.postValue(false)
    }

    fun transferOwnership(): Boolean {
        if(cmdSet == null) return false
        if(cmdSet!!.satodimeInitiateOwnershipTransfer().isOK) {
            isOwner.postValue(false)
            thread {
                runBlocking {
                    delay(5000)// to not display immediately the accept ownership view
                    waitForSetup.postValue(true)
                }
            }
            return true
        }
        return false
    }

    fun seal(
        slot: Int,
        coin: Coin,
        asset: Asset = Asset.Coin,
        contract: String = "",
        tokenId: String = "",
        entropy: String = "",
        isTestnet: Boolean = false
    ): Boolean {
        try {
            val rapduSeal = cmdSet!!.satodimeSealKey(slot, entropy.toByteArray().copyOf(32))
            if (rapduSeal.isOK) {
                val slip44 = getSlip44(coin.name, isTestnet)
                val rapduSetInfo = cmdSet!!.satodimeSetKeyslotStatusPart0(
                    slot,
                    0,
                    0,
                    MAP_CODE_BY_ASSET[asset.name]!!,
                    slip44,
                    getContractByteTLV(contract, coin.name, asset.name),
                    getTokenIdByteTLV(tokenId, asset.name)
                )
                if (rapduSetInfo.isOK) {
                    satodimeStatus = cmdSet!!.satodimeStatus
                    updateCardSlots()
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown on slot $slot sealing.")
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    fun unseal(slot: Int): Boolean {
        try {
            val rapduUnseal = cmdSet!!.satodimeUnsealKey(slot)
            if (rapduUnseal.isOK) {
                val rapduPrivkey = cmdSet!!.satodimeGetPrivkey(slot)
                if (rapduPrivkey.isOK) {
                    satodimeStatus = cmdSet!!.satodimeStatus
                    updateCardSlots()
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown on slot $slot unsealing.")
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    fun reset(slot: Int): Boolean {
        try {
            val rapduUnseal = cmdSet!!.satodimeResetKey(slot)
            if (rapduUnseal.isOK) {
                satodimeStatus = cmdSet!!.satodimeStatus
                updateCardSlots()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown on slot $slot reset.")
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    fun isOwner(): Boolean {
        if (satodimeStatus == null) return false
        return satodimeStatus!!.isOwner
    }

    fun unlockCard(unlockSecret: String?) {
        if (cmdSet != null && !unlockSecret.isNullOrEmpty()) {
            cmdSet!!.satodimeUnlockSecret = SatochipParser.fromHexString(unlockSecret)
            NFCCardService.unlockSecret = unlockSecret
            isOwner.postValue(isOwner())
        }
    }

    private fun takeOwnershipAndRetrieveUnlockSecret(): String? {
        if (cmdSet == null) return null
        val random = SecureRandom()
        val pinTries0 = 5.toByte()
        val pin0 = ByteArray(8)
        random.nextBytes(pin0)
        val rapduSetup: APDUResponse = cmdSet!!.cardSetup(pinTries0, pin0)
        return if (rapduSetup.isOK) {
            SatochipParser.toHexString(cmdSet!!.satodimeUnlockSecret)
        } else {
            null
        }
    }

    private fun verifyAuthenticity(): Boolean {
        if (cmdSet == null) return false
        return try {
            val authResponse = cmdSet!!.cardVerifyAuthenticity()
            val result = authResponse[0]
            val ca = authResponse[1]
            val subCa = authResponse[2]
            val device = authResponse[3]
            val error = authResponse[4]
            certificate = "CA :\n $ca\n Sub CA :\n $subCa\n Device :\n $device\n" + if (error.isNotEmpty()) {
                "Error :\n $error"
            } else ""
            return result.equals("OK")
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown while verifying card's authenticity.")
            Log.e(TAG, Log.getStackTraceString(e))
            false
        }
    }

    private fun updateCardSlots() {
        val updatedCardSlots = mutableListOf<CardSlot>()
        val keyState = satodimeStatus?.keysState ?: return
        for (i in keyState.indices) {
            Log.d(TAG, "Slot $i : ---------------")
            var pubKey: ByteArray? = null
            try {
                pubKey = cmdSet!!.parser.parseSatodimeGetPubkey(cmdSet!!.satodimeGetPubkey(i))
            } catch (e: Exception) {
                Log.e(TAG, "An exception has been thrown while reading public keys.")
                Log.e(TAG, Log.getStackTraceString(e))
            }

            var privateKeyInfos: Map<String, ByteArray>? = null
            if (SlotState.byteAsSlotState(keyState[i]) == SlotState.UNSEALED) {
                try {
                    val rapduPrivkey = cmdSet!!.satodimeGetPrivkey(i)
                    if (rapduPrivkey.isOK) {
                        privateKeyInfos = cmdSet!!.parser.parseSatodimeGetPrivkey(rapduPrivkey)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "An exception has been thrown while reading private keys.")
                    Log.e(TAG, Log.getStackTraceString(e))
                }
            }
            try {
                val response = cmdSet!!.satodimeGetKeyslotStatus(i)
                val keySlotStatus = SatodimeKeyslotStatus(response)
                if (keySlotStatus.keySlip44 == null) {
                    Log.e(TAG, "Failed to retrieve slot $i status.")
                    return
                }
                val cardSlot = CardSlot(keySlotStatus, keyState[i], pubKey, privateKeyInfos)
                updatedCardSlots += cardSlot
                Log.d(TAG, "slot state : ------- ${cardSlot.slotState}")
                Log.d(TAG, "symbol : ------- ${cardSlot.coinSymbol}")
                Log.d(TAG, "name : ------- ${cardSlot.coinDisplayName}")
                Log.d(TAG, "is token : ------- ${cardSlot.isToken}")
                Log.d(TAG, "is NFT : ------- ${cardSlot.isNFT}")
                Log.d(TAG, "privateKeyBytes : ------- ${cardSlot.privateKeyBytes}")
                Log.d(TAG, "privateKeyHex : ------- ${cardSlot.privateKeyHex}")
                Log.d(TAG, "privateKeyWif : ------- ${cardSlot.privateKeyWif}")
                Log.d(
                    TAG,
                    "Asset : " + cardSlot.assetName
                            + ", Public key : " + cardSlot.publicKeyHexString
                            + ", Address: " + cardSlot.address
                )
            } catch (e: Exception) {
                Log.e(TAG, "An exception has been thrown while getting key slot status.")
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
        cardSlots.postValue(updatedCardSlots)
    }
}

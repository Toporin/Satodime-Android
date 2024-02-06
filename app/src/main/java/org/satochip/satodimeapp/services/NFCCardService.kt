package org.satochip.satodimeapp.services

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.nfc.NfcAdapter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.satochip.android.NFCCardManager
import org.satochip.client.ApplicationStatus
import org.satochip.client.SatochipCommandSet
import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
import org.satochip.client.SatodimeStatus
import org.satochip.io.APDUResponse
import org.satochip.satodimeapp.BuildConfig.DEBUG
import org.satochip.satodimeapp.data.AuthenticityStatus
import org.satochip.satodimeapp.data.CardPrivkey
import org.satochip.satodimeapp.data.CardSlot
import org.satochip.satodimeapp.data.NfcActionType
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.data.SlotState
import java.security.SecureRandom
import kotlin.concurrent.thread

private const val TAG = "NFCCardService"

// singleton
object NFCCardService {

    lateinit var context: Context // initialized in sharedViewModel init() // = application.applicationContext
    var activity: Activity? = null

    //
    var isConnected = MutableLiveData(false) // the app is connected to a card // updated in SatodimeCardListener
    var isCardDataAvailable = MutableLiveData(false)

    // Card state
    var waitForSetup = MutableLiveData(false) // card requires setup // TODO: rename to isSetupDone?
    var cardSlots = MutableLiveData<List<CardSlot>>()
    var unlockSecretHex: String? = null
    var unlockSecretBytes: ByteArray? = null

    var ownershipStatus = MutableLiveData<OwnershipStatus>(OwnershipStatus.Unknown)
    var authenticityStatus = MutableLiveData<AuthenticityStatus>(AuthenticityStatus.Unknown)

    var authentikeyHex : String? = null
    var cardPrivkeys = MutableLiveData<List<CardPrivkey?>>()

    // certificate
    var certificateList = MutableLiveData<MutableList<String>>() // todo: not livedata?
    var cardAppletVersion: String = "undefined"

    private lateinit var cmdSet: SatochipCommandSet
    private var parser: SatochipParser?= null
    private var satodimeStatus: SatodimeStatus? = null

    // to define action to perform
    var actionType: NfcActionType = NfcActionType.DoNothing
    var actionIndex: Int = 0
    var actionEntropy: ByteArray = byteArrayOf() // for seal
    var actionSlip44: ByteArray = byteArrayOf() // for seal
    var resultMsg: String = ""
    var resultCodeLive = MutableLiveData<NfcResultCode>(NfcResultCode.Busy) //NfcResultCode = NfcResultCode.Ok

    fun scanCardForAction(activity: Activity){
        SatoLog.d(TAG, "scanCardForAction thread START")
        this.activity = activity
        val cardManager = NFCCardManager()
        cardManager.setCardListener(SatodimeCardListenerForAction)
        cardManager.start()

        NFCCardService.resultCodeLive.postValue(NfcResultCode.Busy)

        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) //context)
        nfcAdapter?.enableReaderMode(
            activity,
            cardManager,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        SatoLog.d(TAG, "scanCardForAction thread END")
    }

    fun disableScanForAction(){
        SatoLog.d(TAG, "disableScanForAction Start")
        if (activity != null) {
            if (activity?.isFinishing() == true) {
                SatoLog.e(TAG, "NFCCardService disableScanForAction activity isFinishing()")
                return;
            }
            val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            nfcAdapter?.disableReaderMode(activity)
            SatoLog.d(TAG, "disableScanForAction disableReaderMode!")
        }
    }

    fun initialize(cmdSet: SatochipCommandSet) {
        SatoLog.d(TAG, "initialize START")
        NFCCardService.cmdSet = cmdSet
        NFCCardService.parser = cmdSet.parser
        SatoLog.d(TAG, "initialize action: $actionType")
        SatoLog.d(TAG, "initialize index: $actionIndex")
        resultCodeLive.postValue(NfcResultCode.Busy)
        if (actionType == NfcActionType.ScanCard) {
            readCard()
        } else if (actionType == NfcActionType.TakeOwnership) {
            takeOwnership()
        } else if (actionType == NfcActionType.ReleaseOwnership) {
            releaseOwnership()
        } else if (actionType == NfcActionType.SealSlot) {
            seal(actionIndex, actionSlip44, actionEntropy)
        } else if (actionType == NfcActionType.UnsealSlot) {
            unseal(actionIndex)
        }else if (actionType == NfcActionType.ResetSlot) {
            reset(actionIndex)
        }else if (actionType == NfcActionType.GetPrivkey) {
            getPrivkey(actionIndex)
        }else {
           // do nothing?
        }
    }

    fun readCard() {
        SatoLog.d(TAG, "readCard START")
        try {
            // reinitialize card state
            ownershipStatus.postValue(OwnershipStatus.Unknown)
            authenticityStatus.postValue(AuthenticityStatus.Unknown)
            isCardDataAvailable.postValue(false)

            val rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            val rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            SatoLog.d(TAG, "readCard cardStatus: $cardStatus")
            //
            val versionInt = getCardVersionInt(rapduStatus)
            val versionString = getCardVersionString(rapduStatus)
            SatoLog.d(TAG, "readCard CardVersionString: $versionString")
            cardAppletVersion = versionString

            // check if setupDone
            if (!cardStatus.isSetupDone) {
                SatoLog.d(TAG, "readCard card needs setup (it has no owner)")
                ownershipStatus.postValue(OwnershipStatus.Unclaimed)

                // check version: v0.1-0.1 cannot proceed further without setup first
                if (versionInt <= 0x00010001) {
                    waitForSetup.postValue(true) // show accept ownership immediatly
                    resultCodeLive.postValue(NfcResultCode.RequireSetup)
                    resultMsg = "Satodime v0.1-0.1 requires user to claim ownership to continue!"
                    SatoLog.w(TAG,"readCard Satodime v0.1-0.1 requires user to claim ownership to continue!")
                    // return early
                    return
                } else {
                    // add a delay to not display immediately the accept ownership view
                    thread {
                        runBlocking {
                            delay(5000)
                            waitForSetup.postValue(true)
                        }
                    }
                }
            }

            // authenticity
            var authResults = cmdSet.cardVerifyAuthenticity()
            if (authResults != null) {
                if (authResults[0].compareTo("OK") == 0){
                    authenticityStatus.postValue(AuthenticityStatus.Authentic)
                } else {
                    authenticityStatus.postValue(AuthenticityStatus.NotAuthentic)
                    SatoLog.w(TAG,"readCard failed to authenticate card!")
                }
                certificateList.postValue(authResults.toMutableList())
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "readCard satodimeStatus: $satodimeStatus");

            // get authentikey
            authentikeyHex= cmdSet.authentikeyHex
            SatoLog.d(TAG, "readCard authentikey: $authentikeyHex");

            // check for ownership
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "readCard found an unlockSecret for card $authentikeyHex");
                ownershipStatus.postValue(OwnershipStatus.Owner)
                unlockSecretHex = prefs.getString(authentikeyHex, "");
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes);
            } else {
                SatoLog.d(TAG, "readCard found no unlockSecret for card $authentikeyHex");
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
            }

            // Fetch Vaults info
            updateCardSlots()

            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Card scan successful!"
            NFCCardService.isCardDataAvailable.postValue(true)
            SatoLog.d(TAG, "readCard: Card reading successful")
        } catch (e: Exception) {
            SatoLog.e(TAG, "readCard exception: $e")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.UnknownError)
            resultMsg = "readCard exception: ${e.localizedMessage}"
        }
    }

    fun takeOwnership() {
        SatoLog.d(TAG, "takeOwnership: START")
        try {
            val rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            val rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check authentikey
            // for v0.1-0.1, authentikeyHex is not available until ownership is accepted, so this check cannot be done
            val versionInt = getCardVersionInt(rapduStatus)
            if (versionInt > 0x00010001) {
                val rapduAuthkey = cmdSet.cardGetAuthentikey()
                var authentikeyHex2 = cmdSet.authentikeyHex
                // check that authentikey match with previous tap
                if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                    SatoLog.e(TAG, "takeOwnership: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                    resultCodeLive.postValue(NfcResultCode.CardMismatch)
                    resultMsg = "Authentikey mismatch"
                    return // throw?
                }
            }

            // check if setupDone
            if (cardStatus.isSetupDone == false) {
                try {
                    val random = SecureRandom()
                    val pinTries0 = 5.toByte()
                    val pin0 = ByteArray(8)
                    random.nextBytes(pin0)
                    val rapduSetup: APDUResponse = cmdSet.cardSetup(pinTries0, pin0).checkOK()
                    val unlockSecretHex = SatochipParser.toHexString(cmdSet.satodimeUnlockSecret)

                    // set authentikey if null (this can happen for card v0.1-0.1)
                    if (authentikeyHex == null){
                        authentikeyHex = cmdSet.authentikeyHex
                    }
                    // save in prefs
                    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
                    prefs.edit().putString(authentikeyHex, unlockSecretHex).apply();
                    SatoLog.d(TAG,"takeOwnership: Saved unlockSecret for card ${authentikeyHex}")
                    // update status
                    ownershipStatus.postValue(OwnershipStatus.Owner)
                    resultCodeLive.postValue(NfcResultCode.Ok)
                    resultMsg = "Card ownership claimed successfully for $authentikeyHex!"
                    SatoLog.d(TAG,"takeOwnership: ownership claimed successfully for ${authentikeyHex}")
                    // add a delay to not leave the view immediately
                    thread {
                        runBlocking {
                            delay(4000)
                            waitForSetup.postValue(false)
                        }
                    }

                } catch (e: Exception) {
                    SatoLog.e(TAG,"takeOwnership: failed to take ownership: ${e.localizedMessage}")
                    SatoLog.e(TAG, Log.getStackTraceString(e))
                    ownershipStatus.postValue(OwnershipStatus.NotOwner)
                    resultCodeLive.postValue(NfcResultCode.FailedToTakeOwnership)
                    resultMsg = "Failed to take ownership: ${e.localizedMessage}"
                    return // throw?
                }
            } else {
                // setup already done on card, so not possible to take ownership
                SatoLog.w(TAG,"takeOwnership: Card ownership already claimed for $authentikeyHex!")
                resultCodeLive.postValue(NfcResultCode.OwnershipAlreadyClaimed)
                resultMsg = "Card ownership already claimed for (authentikeyHex)!"
                return // throw?
            }

        } catch (e: Exception) {
            SatoLog.e(TAG, "takeOwnership: failed to take ownership with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToTakeOwnership)
            resultMsg = "Failed to take ownership with error: ${e.localizedMessage}"
        }
    }

    fun dismissOwnership() {
        SatoLog.d(TAG, "dismissOwnership: START")
        waitForSetup.postValue(false)
    }

    fun releaseOwnership() {
        SatoLog.d(TAG, "releaseOwnership: START")
        try {
            var rapdu = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check that authentikey match with previous tap
            cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                SatoLog.e(TAG, "releaseOwnership: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "releaseOwnership: satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "releaseOwnership: found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                SatoLog.d(TAG, "releaseOwnership: found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to release ownership: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // releaseOwnership
            rapdu = cmdSet.satodimeInitiateOwnershipTransfer().checkOK()
            SatoLog.d(TAG, "releaseOwnership: card ownership released successfully for $authentikeyHex!")
            // TODO: remove unlockSecret from prefs
            if (prefs.contains(authentikeyHex)) {
                prefs.edit().remove(authentikeyHex).apply()
                SatoLog.d(TAG, "releaseOwnership: removed unlockSecret for card $authentikeyHex")
            }
            ownershipStatus.postValue(OwnershipStatus.Unclaimed)
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Card ownership released successfully for $authentikeyHex!"

        } catch (e: Exception) {
            SatoLog.e(TAG, "releaseOwnership: failed to release ownership with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToReleaseOwnership)
            resultMsg = "Failed to release ownership with error: ${e.localizedMessage}"
        }

    }

    fun seal(
        slot: Int,
        slip44Bytes: ByteArray,
        entropyBytes: ByteArray){

        SatoLog.d(TAG, "seal: START")
        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                SatoLog.e(TAG, "seal: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // get satodime status (this also sets correct unlock counter)
            // todo check state is uninitialized
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "seal: satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "seal: found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                SatoLog.d(TAG, "seal: found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to release ownership: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // seal vault
            val rapduSeal = cmdSet.satodimeSealKey(slot, entropyBytes).checkOK()
            // set slot data info
            var contractBytes = ByteArray(34)
            contractBytes[1] = 32
            var tokenidBytes = ByteArray(34)
            tokenidBytes[1] = 32
            val rapduSetInfo = cmdSet.satodimeSetKeyslotStatusPart0(
                slot,
                0,
                0,
                0,
                slip44Bytes,
                contractBytes, // deprecated, mainly used for backward compatibility
                tokenidBytes //deprecated, mainly used for backward compatibility
            ).checkOK()

            // partially update status
            val updatedCardSlots = cardSlots.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedCardSlots != null && slot<updatedCardSlots.size) {
                val newCardSlot = getCardSlot(index = slot)
                if (newCardSlot != null) {
                    updatedCardSlots[slot] = newCardSlot
                    cardSlots.postValue(updatedCardSlots)
                }
            }

            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault sealed successfully for $authentikeyHex!"
            SatoLog.d(TAG, "seal: vault sealed successfully for $authentikeyHex!")

        } catch (e: Exception) {
            SatoLog.e(TAG, "seal: failed to seal vault with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToSealVault)
            resultMsg = "Failed to seal vault with error: ${e.localizedMessage}"
        }
    }

    fun unseal(slotIndex: Int){
        SatoLog.d(TAG, "unseal: START");
        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                SatoLog.e(TAG, "unseal: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "unseal: satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "unseal: found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                SatoLog.d(TAG, "unseal: found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to release ownership: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // TODO: check slot is sealed!

            // unseal vault
            val rapduUnseal = cmdSet.satodimeUnsealKey(slotIndex).checkOK()

            // partially update status
            val updatedCardSlots = cardSlots.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedCardSlots != null && slotIndex<updatedCardSlots.size && updatedCardSlots[slotIndex] != null){
                var oldCardSlot = updatedCardSlots[slotIndex]
                var newCardSlot = CardSlot(
                    index = slotIndex,
                    keySlip44 = oldCardSlot.keySlip44,
                    keysState= 0x02,
                    pubkeyBytes = oldCardSlot.pubkeyBytes,
                )
                updatedCardSlots[slotIndex] = newCardSlot
            }
            cardSlots.postValue(updatedCardSlots)

            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault unsealed successfully for $authentikeyHex!"
            SatoLog.d(TAG, "unseal: vault unsealed successfully for $authentikeyHex!")

        } catch (e: Exception) {
            SatoLog.e(TAG, "unseal: failed to unseal vault with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToUnsealVault)
            resultMsg = "Failed to unseal vault with error: ${e.localizedMessage}"
        }
    }

    fun reset(slotIndex: Int){
        SatoLog.d(TAG, "reset: START slot: ${slotIndex}")
        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                SatoLog.e(TAG, "reset: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "reset: satodimeStatus: $satodimeStatus");

            // todo: check slot is unsealed!

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "reset: found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                SatoLog.d(TAG, "reset: found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCodeLive.postValue(NfcResultCode.FailedToResetVault)
                resultMsg = "Failed to reset vault: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // reset vault
            val rapduReset = cmdSet.satodimeResetKey(slotIndex)

            // update just cardSlot for specific vault
            val updatedCardSlots = cardSlots.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedCardSlots != null && slotIndex<updatedCardSlots.size && updatedCardSlots[slotIndex] != null){
                var oldCardSlot = updatedCardSlots[slotIndex]
                var keySlip44 = oldCardSlot.keySlip44
                var newCardSlot = CardSlot(
                    index = slotIndex,
                    keySlip44 = keySlip44,
                    keysState= 0x00, // uninitialized
                    pubkeyBytes = null,
                )
                updatedCardSlots[slotIndex] = newCardSlot
            }
            cardSlots.postValue(updatedCardSlots)

            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault reset successfully for $authentikeyHex!"
            SatoLog.d(TAG, "reset: vault reset successfully for $authentikeyHex!")

        } catch (e: Exception) {
            SatoLog.e(TAG, "reset: failed to reset vault with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToResetVault)
            resultMsg = "Failed to reset vault with error: ${e.localizedMessage}"
        }
    }

    fun getPrivkey(slot: Int){
        SatoLog.d(TAG, "getPrivkey: Start slot: ${slot}")
        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                SatoLog.e(TAG, "getPrivkey: card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            SatoLog.d(TAG, "getPrivkey: satodimeStatus: $satodimeStatus");
            // check vault status is unsealed
            val keyState = satodimeStatus?.keysState ?: return
            if (SlotState.byteAsSlotState(keyState[slot]) != SlotState.UNSEALED) {
                SatoLog.w(TAG, "getPrivkey: vault is not unsealed for card $authentikeyHex")
                resultCodeLive.postValue(NfcResultCode.FailedToRecoverPrivkey)
                resultMsg = "Failed to recover privkey: vault is not unsealed for card $authentikeyHex"
                return // throw?
            }

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                SatoLog.d(TAG, "getPrivkey: found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                SatoLog.d(TAG, "getPrivkey: found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to reset vault: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // get privkey
            val rapduPrivkey = cmdSet.satodimeGetPrivkey(slot).checkOK()
            var privateKeyInfos = cmdSet.parser.parseSatodimeGetPrivkey(rapduPrivkey) // Map<String, ByteArray>? = null

            // update list
            val updatedcardPrivkeys = cardPrivkeys.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedcardPrivkeys != null && slot<updatedcardPrivkeys.size){
                var slip44Bytes = cardSlots.value?.get(slot)?.keySlip44 ?: ByteArray(4) //todo check default?
                var newCardPrivkey = CardPrivkey(
                    slip44 = slip44Bytes,
                    privateKeyInfos = privateKeyInfos,
                )
                updatedcardPrivkeys[slot] = newCardPrivkey
            }
            cardPrivkeys.postValue(updatedcardPrivkeys)
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Privkey recovered successfully for $authentikeyHex!"
            SatoLog.d(TAG, "getPrivkey: privkey recovered successfully for $authentikeyHex!")

        } catch (e: Exception) {
            SatoLog.e(TAG, "getPrivkey: failed to recover privkey with error: ${e.localizedMessage}")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            resultCodeLive.postValue(NfcResultCode.FailedToRecoverPrivkey)
            resultMsg = "Failed to recover privkey with error: ${e.localizedMessage}"
        }
    }

    private fun updateCardSlots() {
        SatoLog.d(TAG, "updateCardSlots: START")
        val updatedCardSlots = mutableListOf<CardSlot>()
        val keyState = satodimeStatus?.keysState ?: return
        for (i in keyState.indices) {
            //val cardSlot = getCardSlot(index= i)
            val cardSlot = if (keyState[i] == 0x00.toByte()){
                // uninitialized slot
                CardSlot(i, ByteArray(4), keyState[i], null)
            } else{
                // get info from card
                getCardSlot(index= i)
            }
            updatedCardSlots += cardSlot
        }
        cardSlots.postValue(updatedCardSlots)

        // private keys (empty list to be updated)
        val updatedCardPrivkeys = mutableListOf<CardPrivkey?>()
        for (i in keyState.indices) {
            updatedCardPrivkeys += null
        }
        cardPrivkeys.postValue(updatedCardPrivkeys)
    }

    fun getCardSlot(index: Int): CardSlot {
        SatoLog.d(TAG, "getCardSlot: START Slot: $index")
        var rapdu: APDUResponse? = null
        var pubKey: ByteArray? = null
        try {
            rapdu = cmdSet.satodimeGetPubkey(index)
            pubKey = cmdSet.parser.parseSatodimeGetPubkey(rapdu)
        } catch (e: Exception) {
            SatoLog.e(TAG, "getCardSlot: exception thrown while reading public keys: $e")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            SatoLog.e(TAG, "getCardSlot: exception thrown while reading public keys pubkey: ${rapdu?.bytes}")
        }

        // keysSlotStatus
        val response = cmdSet.satodimeGetKeyslotStatus(index)
        val keySlotStatus = SatodimeKeyslotStatus(response)
        if (keySlotStatus.keySlip44 == null) {
            SatoLog.e(TAG, "getCardSlot: failed to retrieve slot $index status")
        }
        val cardSlot = CardSlot(index, keySlotStatus.keySlip44, keySlotStatus.keyStatus, pubKey)
        SatoLog.d(TAG, "getCardSlot index: $index")
        SatoLog.d(TAG, "getCardSlot keySlip44: ${cardSlot.keySlip44Int}")
        SatoLog.d(TAG, "getCardSlot keyStatus: ${cardSlot.slotState}")
        SatoLog.d(TAG, "getCardSlot pubKey: ${pubKey}")
        return cardSlot
    }

    // util
    fun getCardVersionInt(rapdu: APDUResponse): Int {
        var data = rapdu.data
        val protocol_major_version = data[0]
        val protocol_minor_version = data[1]
        val applet_major_version = data[2]
        val applet_minor_version = data[3]
        val versionInt: Int= (protocol_major_version.toInt() shl 24) +
                                (protocol_minor_version.toInt() shl 16) +
                                (applet_major_version.toInt() shl 8) +
                                applet_minor_version
        return versionInt
    }

    fun getCardVersionString(rapdu: APDUResponse): String {
        var data = rapdu.data
        val protocol_major_version = data[0]
        val protocol_minor_version = data[1]
        val applet_major_version = data[2]
        val applet_minor_version = data[3]
        val versionString= "$protocol_major_version.$protocol_minor_version-$applet_major_version.$applet_minor_version"
        return versionString
    }

}


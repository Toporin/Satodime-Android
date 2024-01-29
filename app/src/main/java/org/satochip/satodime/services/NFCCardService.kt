package org.satochip.satodime.services

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.nfc.NfcAdapter
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.satochip.android.NFCCardManager
import org.satochip.client.ApplicationStatus
import org.satochip.client.Constants.MAP_CODE_BY_ASSET
import org.satochip.client.SatochipCommandSet
import org.satochip.client.SatochipParser
import org.satochip.client.SatodimeKeyslotStatus
import org.satochip.client.SatodimeStatus
import org.satochip.io.APDUResponse
import org.satochip.satodime.BuildConfig.DEBUG
import org.satochip.satodime.data.AuthenticityStatus
//import org.satochip.satodime.data.Asset
import org.satochip.satodime.data.CardPrivkey
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.Coin
import org.satochip.satodime.data.NfcActionType
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.data.OwnershipStatus
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.util.getContractByteTLV
import org.satochip.satodime.util.getSlip44
import org.satochip.satodime.util.getTokenIdByteTLV
import java.security.SecureRandom
import kotlin.concurrent.thread

private const val TAG = "NFCCardService"

// singleton
object NFCCardService {
    //val context = getApplicationContext()
    //lateinit var application: Application
    lateinit var context: Context // initialized in sharedViewModel init() // = application.applicationContext
    var activity: Activity? = null

    // the app is listening to connect to a card
    var isListening = MutableLiveData(false)
    // the app is connected to a card
    var isConnected = MutableLiveData(false) // updated in SatodimeCardListener
    // todo deprecate
    var isReadingFinished = MutableLiveData(true)  // todo deprecate, same usage as isConnected
    // todo action has been performed, app can stop listenning for card...
    var isActionFinished = MutableLiveData(true) // todo

    // cards require setup
    var waitForSetup = MutableLiveData(false) // TODO: rename to isSetupDone?
    var isOwner = MutableLiveData(false) // toodo deprecate use ownershipStatus instead
    var isAuthentic = MutableLiveData(false) // todo deprecate use authenticityStatus
    var cardSlots = MutableLiveData<List<CardSlot>>()
    var authenticationKeyHex: String? = null
    var unlockSecret: String? = null
    var unlockSecretHex: String? = null // TODO: remove?
    var unlockSecretBytes: ByteArray? = null // TODO: remove?
    var certificate: String? = null // todo: remove?

    // added
    var ownershipStatus = MutableLiveData<OwnershipStatus>(OwnershipStatus.Unknown)
    var authenticityStatus = MutableLiveData<AuthenticityStatus>(AuthenticityStatus.Unknown)
    var isCardDataAvailable = MutableLiveData(false)
    var cardStatus = MutableLiveData<ApplicationStatus>()
    var satodimeStatusNew = MutableLiveData<SatodimeStatus>()
    var authentikeyHex : String? = null
    var cardPrivkeys = MutableLiveData<List<CardPrivkey?>>()

    // ownership
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
    var resultCode: NfcResultCode = NfcResultCode.Ok
    var resultMsg: String = ""
    var resultCodeLive = MutableLiveData<NfcResultCode>(NfcResultCode.Busy) //NfcResultCode = NfcResultCode.Ok


    fun scanCardForAction(activity: Activity){
        this.activity = activity
        val cardManager = NFCCardManager()
        cardManager.setCardListener(SatodimeCardListenerForAction)
        cardManager.start()

        NFCCardService.isListening.postValue(true)
        NFCCardService.isActionFinished.postValue(false) // deprecate?
        NFCCardService.resultCodeLive.postValue(NfcResultCode.Busy)

        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) //context)
        nfcAdapter?.enableReaderMode(
            activity,
            cardManager,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        Log.d(TAG, "NFCcardService scanCardForAction thread END")
    }

    fun disableScanForAction(){
        Log.d(TAG, "NFCCardService disableScanForAction Start")
        if (activity != null) {
            if (activity?.isFinishing() == true) {
                Log.e(TAG, "NFCCardService disableScanForAction activity isFinishing()")
                return;
            }
            val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            nfcAdapter?.disableReaderMode(activity)
            Log.d(TAG, "NFCCardService disableScanForAction disabled disableReaderMode!")
        }
    }

    fun initialize(cmdSet: SatochipCommandSet) {
        Log.d(TAG, "In NFCCardService initialize START")
        NFCCardService.cmdSet = cmdSet
        NFCCardService.parser = cmdSet.parser
        Log.d(TAG, "In NFCCardService initialize action: $actionType")
        Log.d(TAG, "In NFCCardService initialize index: $actionIndex")
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
        Log.d(TAG, "In NFCCardService readCard START")
        try {
            // reinitialize card state
            ownershipStatus.postValue(OwnershipStatus.Unknown)
            authenticityStatus.postValue(AuthenticityStatus.Unknown)
            isCardDataAvailable.postValue(false)

            val rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            Log.d(TAG, "In NFCCardService readCard card selected!")
            // cardStatus
            val rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.d(TAG, "In NFCCardService readCard cardStatus: $cardStatus")
            //
            val versionInt = getCardVersionInt(rapduStatus)
            val versionString = getCardVersionString(rapduStatus)
            Log.d(TAG, "CardVersionInt: $versionInt")
            Log.d(TAG, "CardVersionString: $versionString")
            cardAppletVersion = versionString

            // check if setupDone
            if (!cardStatus.isSetupDone) {
                Log.d(TAG, "Card needs setup (it has no owner)")
                ownershipStatus.postValue(OwnershipStatus.Unclaimed)

                // check version: v0.1-0.1 cannot proceed further without setup first
                if (versionInt <= 0x00010001) {
                    waitForSetup.postValue(true) // show accept ownership immediatly
                    resultCode = NfcResultCode.RequireSetup
                    resultCodeLive.postValue(NfcResultCode.RequireSetup)
                    resultMsg = "Satodime v0.1-0.1 requires user to claim ownership to continue!"
                    Log.w(TAG,"Satodime v0.1-0.1 requires user to claim ownership to continue!")
                    // return early
                    return
                } else {
                    // add a delay to not display immediately the accept ownership view
                    // TODO: test if correct behavior
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
                var isAuthenticBool: Boolean = (authResults[0].compareTo("OK") == 0) // todo remove
                isAuthentic.postValue(isAuthenticBool) // todo remove
                if (authResults[0].compareTo("OK") == 0){
                    authenticityStatus.postValue(AuthenticityStatus.Authentic)
                } else {
                    authenticityStatus.postValue(AuthenticityStatus.NotAuthentic)
                }
                certificateList.postValue(authResults.toMutableList())
            }
            if (DEBUG) {
                if (authResults != null) {
                    for (index in 0 until authResults.size) {
                        Log.d(TAG, "DEBUGAUTH : " + authResults[index])
                    }
                }
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");

            // get authentikey
            //rapdu = cmdSet.cardGetAuthentikey()
            authentikeyHex= cmdSet.authentikeyHex
            Log.d(TAG, "Satodime authentikey: $authentikeyHex");

            // check for ownership
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex");
                ownershipStatus.postValue(OwnershipStatus.Owner)
                unlockSecretHex = prefs.getString(authentikeyHex, "");
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes);
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex");
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
            }

            // Fetch Vaults info
            updateCardSlots()

            //actionType = NfcActionType.DoNothing
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultCode = NfcResultCode.Ok //deprecate
            isActionFinished.postValue(true) //deprecate
            resultMsg = "Card scan successful!"
            NFCCardService.isCardDataAvailable.postValue(true)
            Log.i(TAG, "NFCCardService readCard: Card reading finished")
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown while reading the card.")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.UnknownError
            resultCodeLive.postValue(NfcResultCode.UnknownError)
            resultMsg = "Error during card scan: ${e.localizedMessage}"
        }
    }

    fun takeOwnership() {
        Log.d(TAG, "In NFCCardService takeOwnership START")
        try {
            val rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            val rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check authentikey
            // for v0.1-0.1, authentikeyHex is not available until ownership is accepted, so this check cannot be done
            val versionInt = getCardVersionInt(rapduStatus)
            println("DEBUG CardVersionInt: $versionInt")
            if (versionInt > 0x00010001) {
                val rapduAuthkey = cmdSet.cardGetAuthentikey()
                var authentikeyHex2 = cmdSet.authentikeyHex
                // check that authentikey match with previous tap
                if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                    Log.e(TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                    resultCode = NfcResultCode.CardMismatch
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
                    Log.d(TAG,"Recovered unlockSecret ${unlockSecretHex}")

                    // set authentikey if null (this can happen for card v0.1-0.1)
                    if (authentikeyHex == null){
                        authentikeyHex = cmdSet.authentikeyHex
                    }
                    // save in prefs
                    val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
                    prefs.edit().putString(authentikeyHex, unlockSecretHex).apply();
                    Log.d(TAG,"Saved unlockSecret ${unlockSecretHex} for card ${authentikeyHex}")
                    // update status
                    ownershipStatus.postValue(OwnershipStatus.Owner)
                    resultCode = NfcResultCode.Ok // todo deprecate
                    resultCodeLive.postValue(NfcResultCode.Ok)
                    resultMsg = "Card ownership claimed successfully for $authentikeyHex!"
                    isActionFinished.postValue(true) // todo remove?
                    //waitForSetup.postValue(false)
                    // add a delay to not leave the view immediately
                    thread {
                        runBlocking {
                            delay(4000)
                            waitForSetup.postValue(false)
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG,"Failed to take ownership: ${e.localizedMessage}")
                    Log.e(TAG, Log.getStackTraceString(e))
                    ownershipStatus.postValue(OwnershipStatus.NotOwner)
                    resultCode = NfcResultCode.FailedToTakeOwnership
                    resultCodeLive.postValue(NfcResultCode.FailedToTakeOwnership)
                    resultMsg = "Failed to take ownership: ${e.localizedMessage}"
                    isActionFinished.postValue(true)
                    return // throw?
                }
            } else {
                // setup already done on card, so not possible to take ownership
                Log.w(TAG,"Card ownership already claimed for $authentikeyHex!")
                resultCode = NfcResultCode.OwnershipAlreadyClaimed
                resultCodeLive.postValue(NfcResultCode.OwnershipAlreadyClaimed)
                resultMsg = "Card ownership already claimed for (authentikeyHex)!"
                isActionFinished.postValue(true)
                return // throw?
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to take ownership with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToTakeOwnership
            resultCodeLive.postValue(NfcResultCode.FailedToTakeOwnership)
            resultMsg = "Failed to take ownership with error: ${e.localizedMessage}"
            // todo isActionFinished.postValue(true)?? or allow rescan??
        }
    }

//    // TODO: remove
//    fun acceptOwnership() { // TODO: rename to takeOwnership
//        Log.d(TAG, "Accept ownership")
//        unlockSecret = try {
//            val secret = takeOwnershipAndRetrieveUnlockSecret()
//            waitForSetup.postValue(false)
//            isOwner.postValue(true)
//            secret
//        } catch (e: Exception) {
//            Log.e(TAG, "An exception has been thrown while taking ownership.")
//            Log.e(TAG, Log.getStackTraceString(e))
//            null
//        }
//        Log.d(TAG, "Unlock secret $unlockSecret")
//    }

    fun dismissOwnership() {
        Log.d(TAG, "Dismiss ownership")
        waitForSetup.postValue(false)
        isOwner.postValue(false) // todo remove?
    }

    fun releaseOwnership() {
        Log.d(TAG, "In NFCCardService releaseOwnership START")
        try {
            var rapdu = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            rapdu = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check that authentikey match with previous tap
            rapdu = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                Log.e(
                    TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCode = NfcResultCode.CardMismatch
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCode = NfcResultCode.NotOwner
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to release ownership: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // releaseOwnership
            rapdu = cmdSet.satodimeInitiateOwnershipTransfer().checkOK()
            Log.d(TAG, "Card ownership released successfully for $authentikeyHex!")
            // TODO: remove unlockSecret from prefs
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Remove unlockSecret for card $authentikeyHex")
                prefs.edit().remove(authentikeyHex).apply()
            }
            ownershipStatus.postValue(OwnershipStatus.Unclaimed)
            resultCode = NfcResultCode.Ok // todo deprecate
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Card ownership released successfully for $authentikeyHex!"

        } catch (e: Exception) {
            Log.e(TAG, "Failed to release ownership with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToReleaseOwnership // todo deprecate
            resultCodeLive.postValue(NfcResultCode.FailedToReleaseOwnership)
            resultMsg = "Failed to release ownership with error: ${e.localizedMessage}"
        }

    }

//    // TODO: remove?
//    fun transferOwnership(): Boolean {
//        if(cmdSet == null) return false
//        if(cmdSet!!.satodimeInitiateOwnershipTransfer().isOK) {
//            isOwner.postValue(false)
//            thread {
//                runBlocking {
//                    delay(5000)// to not display immediately the accept ownership view
//                    waitForSetup.postValue(true)
//                }
//            }
//            return true
//        }
//        return false
//    }

    fun seal(
        slot: Int,
        slip44Bytes: ByteArray,
        entropyBytes: ByteArray){

        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                Log.e(
                    TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCode = NfcResultCode.CardMismatch // todo deprecate
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // get satodime status (this also sets correct unlock counter)
            // todo check state is uninitialized
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCode = NfcResultCode.NotOwner // todo deprecate
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

            resultCode = NfcResultCode.Ok // todo deprecate
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault sealed successfully for $authentikeyHex!"
            Log.e(TAG, "Vault sealed successfully for $authentikeyHex!")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to seal vault with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToSealVault // todo deprecate
            resultCodeLive.postValue(NfcResultCode.FailedToSealVault)
            resultMsg = "Failed to seal vault with error: ${e.localizedMessage}"
        }
    }

//    // TODO: remove
//    fun sealOld(
//        slot: Int,
//        coin: Coin,
//        asset: Asset = Asset.Coin,
//        contract: String = "",
//        tokenId: String = "",
//        entropy: String = "",
//        isTestnet: Boolean = false
//    ): Boolean {
//        try {
//            val rapduSeal = cmdSet!!.satodimeSealKey(slot, entropy.toByteArray().copyOf(32))
//            if (rapduSeal.isOK) {
//                val slip44 = getSlip44(coin.name, isTestnet)
//                val rapduSetInfo = cmdSet!!.satodimeSetKeyslotStatusPart0(
//                    slot,
//                    0,
//                    0,
//                    MAP_CODE_BY_ASSET[asset.name]!!,
//                    slip44,
//                    getContractByteTLV(contract, coin.name, asset.name),
//                    getTokenIdByteTLV(tokenId, asset.name)
//                )
//                if (rapduSetInfo.isOK) {
//                    satodimeStatus = cmdSet!!.satodimeStatus
//                    updateCardSlots()
//                    return true
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "An exception has been thrown on slot $slot sealing.")
//            Log.e(TAG, Log.getStackTraceString(e))
//        }
//        return false
//    }

    fun unseal(slotIndex: Int){

        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                Log.e(
                    TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCode = NfcResultCode.CardMismatch // todo deprecate
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCode = NfcResultCode.NotOwner // todo deprecate
                resultCodeLive.postValue(NfcResultCode.NotOwner)
                resultMsg = "Failed to release ownership: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // TODO: check slot is sealed!

            // unseal vault
            val rapduUnseal = cmdSet.satodimeUnsealKey(slotIndex).checkOK()

            // partially update status
            // TODO? update just cardSlot for specific vault?
            //satodimeStatus = cmdSet.satodimeStatus // todo: update satodimeStatus?
            val updatedCardSlots = cardSlots.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedCardSlots != null && slotIndex<updatedCardSlots.size && updatedCardSlots[slotIndex] != null){
                var oldCardSlot = updatedCardSlots[slotIndex]
                var newKeySlotStatus = oldCardSlot.slotStatus // todo warn: newKeySlotStatus.keyStatus is not correct & should not be used
                var newCardSlot = CardSlot(
                    slotStatus= newKeySlotStatus,
                    keysState= 0x02,
                    publicKeyBytes = oldCardSlot.publicKeyBytes,
                    privateKeyInfos = null,
                )
                updatedCardSlots[slotIndex] = newCardSlot
            }
            cardSlots.postValue(updatedCardSlots)

            resultCode = NfcResultCode.Ok //todo deprecate
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault unsealed successfully for $authentikeyHex!"
            Log.e(TAG, "Vault unsealed successfully for $authentikeyHex!")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to unseal vault with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToUnsealVault //todo deprecate
            resultCodeLive.postValue(NfcResultCode.FailedToUnsealVault)
            resultMsg = "Failed to unseal vault with error: ${e.localizedMessage}"
        }
    }

//    // TODO: remove
//    fun unsealOld(slot: Int): Boolean {
//        try {
//            val rapduUnseal = cmdSet!!.satodimeUnsealKey(slot)
//            if (rapduUnseal.isOK) {
//                val rapduPrivkey = cmdSet!!.satodimeGetPrivkey(slot)
//                if (rapduPrivkey.isOK) {
//                    satodimeStatus = cmdSet!!.satodimeStatus
//                    updateCardSlots()
//                    return true
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "An exception has been thrown on slot $slot unsealing.")
//            Log.e(TAG, Log.getStackTraceString(e))
//        }
//        return false
//    }

    fun reset(slotIndex: Int){

        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                Log.e(
                    TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCode = NfcResultCode.CardMismatch // todo deprecate
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus // todo: optimize? (satodimeStatus already fetched during scan)
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");

            // todo: check slot is unsealed!

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCode = NfcResultCode.FailedToResetVault // todo deprecate
                resultCodeLive.postValue(NfcResultCode.FailedToResetVault)
                resultMsg = "Failed to reset vault: found no unlockSecret for card $authentikeyHex"
                return // throw?
            }

            // reset vault
            val rapduReset = cmdSet.satodimeResetKey(slotIndex)

            // update just cardSlot for specific vault
            //satodimeStatus = cmdSet.satodimeStatus // todo: update satodimeStatus?
            Log.d(TAG, "NFCCardService reset update slots after reset START")
            val updatedCardSlots = cardSlots.value?.toMutableList() //mutableListOf<CardSlot>()
            if (updatedCardSlots != null && slotIndex<updatedCardSlots.size && updatedCardSlots[slotIndex] != null){
                var oldCardSlot = updatedCardSlots[slotIndex]
                var newKeySlotStatus = oldCardSlot.slotStatus // todo warn: newKeySlotStatus.keyStatus is not correct & should not be used
                var newCardSlot = CardSlot(
                    slotStatus= newKeySlotStatus, //todo: set to null or support CardSlot?
                    keysState= 0x00, // uninitialized
                    publicKeyBytes = null,
                    privateKeyInfos = null,
                )
                updatedCardSlots[slotIndex] = newCardSlot
            }
            Log.d(TAG, "NFCCardService reset update slots after reset END")
            cardSlots.postValue(updatedCardSlots)
            Log.d(TAG, "NFCCardService reset update slots after reset UPDATED!")

            resultCode = NfcResultCode.Ok // todo remove
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Vault reset successfully for $authentikeyHex!"
            Log.i(TAG, "Vault reset successfully for $authentikeyHex!")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset vault with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToResetVault // todo deprecate
            resultCodeLive.postValue(NfcResultCode.FailedToResetVault)
            resultMsg = "Failed to reset vault with error: ${e.localizedMessage}"
        }
    }

//    // TODO: remove
//    fun resetOld(slot: Int): Boolean {
//        try {
//            val rapduUnseal = cmdSet!!.satodimeResetKey(slot)
//            if (rapduUnseal.isOK) {
//                satodimeStatus = cmdSet!!.satodimeStatus
//                updateCardSlots()
//                return true
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "An exception has been thrown on slot $slot reset.")
//            Log.e(TAG, Log.getStackTraceString(e))
//        }
//        return false
//    }

    fun getPrivkey(slot: Int){
        try {
            var rapduSelect = cmdSet.cardSelect("satodime").checkOK()
            // cardStatus
            var rapduStatus = cmdSet.cardGetStatus()//To update status if it's not the first reading
            val cardStatus = cmdSet.applicationStatus ?: return
            Log.i(TAG, "")

            // check that authentikey match with previous tap
            var rapduAuthkey = cmdSet.cardGetAuthentikey()
            var authentikeyHex2 = cmdSet.authentikeyHex
            if (authentikeyHex2.compareTo(authentikeyHex ?: "") !=0) {
                Log.e(
                    TAG, "card mismatch: authentikey: $authentikeyHex2 expected: $authentikeyHex")
                resultCode = NfcResultCode.CardMismatch
                resultCodeLive.postValue(NfcResultCode.CardMismatch)
                resultMsg = "Authentikey mismatch"
                return // throw?
            }

            // satodimeStatus
            satodimeStatus = cmdSet.satodimeStatus
            Log.d(TAG, "Satodime satodimeStatus: $satodimeStatus");
            // check vault status is unsealed
            val keyState = satodimeStatus?.keysState ?: return
            if (SlotState.byteAsSlotState(keyState[slot]) != SlotState.UNSEALED) {
                if(DEBUG) Log.d(TAG, "Vault is not unsealed for card $authentikeyHex")
                resultCode = NfcResultCode.FailedToRecoverPrivkey // todo deprecate
                resultCodeLive.postValue(NfcResultCode.FailedToRecoverPrivkey)
                resultMsg = "Failed to recover privkey: vault is not unsealed for card $authentikeyHex"
                return // throw?
            }

            // get unlockSecret
            val prefs = context.getSharedPreferences("satodime", MODE_PRIVATE)
            if (prefs.contains(authentikeyHex)) {
                if(DEBUG) Log.d(TAG, "Found an unlockSecret for card $authentikeyHex")
                unlockSecretHex = prefs.getString(authentikeyHex, "")
                unlockSecretBytes = SatochipParser.fromHexString(unlockSecretHex)
                cmdSet.setSatodimeUnlockSecret(unlockSecretBytes)
            } else {
                if(DEBUG) Log.d(TAG, "Found no unlockSecret for card $authentikeyHex")
                ownershipStatus.postValue(OwnershipStatus.NotOwner)
                resultCode = NfcResultCode.NotOwner // todo deprecate
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
                var slip44Bytes = cardSlots.value?.get(slot)?.slotStatus?.keySlip44 ?: ByteArray(4) //todo check default?
                var newCardPrivkey = CardPrivkey(
                    slip44 = slip44Bytes,
                    privateKeyInfos = privateKeyInfos,
                )
                updatedcardPrivkeys[slot] = newCardPrivkey
            }
            cardPrivkeys.postValue(updatedcardPrivkeys)

            resultCode = NfcResultCode.Ok //todo deprecate
            resultCodeLive.postValue(NfcResultCode.Ok)
            resultMsg = "Privkey recovered successfully for $authentikeyHex!"
            Log.e(TAG, "Privkey recovered successfully for $authentikeyHex!")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to recover privkey with error: ${e.localizedMessage}")
            Log.e(TAG, Log.getStackTraceString(e))
            resultCode = NfcResultCode.FailedToRecoverPrivkey
            resultMsg = "Failed to recover privkey with error: ${e.localizedMessage}"
        }
    }

//    // todo: remove?
//    fun isOwner(): Boolean {
//        if (satodimeStatus == null) return false
//        return satodimeStatus!!.isOwner
//    }

//    // todo: remove? only used in SharedViewModel
//    fun unlockCard(unlockSecret: String?) {
//        if (cmdSet != null && !unlockSecret.isNullOrEmpty()) {
//            cmdSet!!.satodimeUnlockSecret = SatochipParser.fromHexString(unlockSecret)
//            NFCCardService.unlockSecret = unlockSecret
//            isOwner.postValue(isOwner())
//        }
//    }

//    // TODO: remove
//    private fun takeOwnershipAndRetrieveUnlockSecret(): String? {
//        if (cmdSet == null) return null
//        val random = SecureRandom()
//        val pinTries0 = 5.toByte()
//        val pin0 = ByteArray(8)
//        random.nextBytes(pin0)
//        val rapduSetup: APDUResponse = cmdSet!!.cardSetup(pinTries0, pin0)
//        return if (rapduSetup.isOK) {
//            SatochipParser.toHexString(cmdSet!!.satodimeUnlockSecret)
//        } else {
//            null
//        }
//    }

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

            // todo: only extract privkey on user request!
            var privateKeyInfos: Map<String, ByteArray>? = null // todo: remove in CardSlot
//            if (SlotState.byteAsSlotState(keyState[i]) == SlotState.UNSEALED) {
//                try {
//                    val rapduPrivkey = cmdSet!!.satodimeGetPrivkey(i)
//                    if (rapduPrivkey.isOK) {
//                        privateKeyInfos = cmdSet!!.parser.parseSatodimeGetPrivkey(rapduPrivkey)
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "An exception has been thrown while reading private keys.")
//                    Log.e(TAG, Log.getStackTraceString(e))
//                }
//            }
            // keysSlotStatus
            try {
                val response = cmdSet!!.satodimeGetKeyslotStatus(i)
                val keySlotStatus = SatodimeKeyslotStatus(response)
                if (keySlotStatus.keySlip44 == null) {
                    Log.e(TAG, "Failed to retrieve slot $i status.")
                    return
                }
                val cardSlot = CardSlot(keySlotStatus, keyState[i], pubKey, privateKeyInfos)
                updatedCardSlots += cardSlot
                var slip44Bytes = cardSlot.slotStatus.keySlip44
                Log.d(TAG, "slot state : ------- ${cardSlot.slotState}")
                Log.d(TAG, "slip44Bytes : ------- ${cardSlot.slotStatus.keySlip44}")
                Log.d(TAG, "slip44Hex : ------- ${SatochipParser.toHexString(slip44Bytes)}")
                Log.d(TAG, "slip44Int : ------- ${cardSlot.keySlip44Int}")
                Log.d(TAG, "symbol : ------- ${cardSlot.coinSymbol}")
                Log.d(TAG, "name : ------- ${cardSlot.coinDisplayName}")
                Log.d(TAG, "is token : ------- ${cardSlot.isToken}") // todo remove
                Log.d(TAG, "is NFT : ------- ${cardSlot.isNFT}") // todo remove
                Log.d(TAG, "privateKeyBytes : ------- ${cardSlot.privateKeyBytes}") // todo remove
                Log.d(TAG, "privateKeyHex : ------- ${cardSlot.privateKeyHex}") // todo remove
                Log.d(TAG, "privateKeyWif : ------- ${cardSlot.privateKeyWif}") // todo remove
                Log.d(TAG, "Public key : " + cardSlot.publicKeyHexString)
                Log.d(TAG, "Address: " + cardSlot.address)
            } catch (e: Exception) {
                Log.e(TAG, "An exception has been thrown while getting key slot status.")
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
        cardSlots.postValue(updatedCardSlots)

        // private keys (empty list to be updated)
        val updatedCardPrivkeys = mutableListOf<CardPrivkey?>()
        for (i in keyState.indices) {
            updatedCardPrivkeys += null
        }
        cardPrivkeys.postValue(updatedCardPrivkeys)
    }

    fun getCardSlot(index: Int): CardSlot? {

        Log.d(TAG, "NFCCardService getCardSlot Slot $index")
        var pubKey: ByteArray? = null
        try {
            pubKey = cmdSet.parser.parseSatodimeGetPubkey(cmdSet.satodimeGetPubkey(index))
        } catch (e: Exception) {
            Log.e(TAG, "NFCCardService getCardSlot  an exception has been thrown while reading public keys.")
            Log.e(TAG, Log.getStackTraceString(e))
        }

        // we only extract privkey on user request!
        var privateKeyInfos: Map<String, ByteArray>? = null // todo: remove in CardSlot

        // keysSlotStatus
        try {
            val response = cmdSet.satodimeGetKeyslotStatus(index)
            val keySlotStatus = SatodimeKeyslotStatus(response)
            if (keySlotStatus.keySlip44 == null) {
                Log.e(TAG, "Failed to retrieve slot $index status.")
                return null
            }
            keySlotStatus.keyStatus
            val cardSlot = CardSlot(keySlotStatus, keySlotStatus.keyStatus, pubKey, privateKeyInfos)
            Log.d(TAG, "slot state : ------- ${cardSlot.slotState}")
            Log.d(TAG, "symbol : ------- ${cardSlot.coinSymbol}")
            Log.d(TAG, "name : ------- ${cardSlot.coinDisplayName}")
            Log.d(
                TAG,
                "Asset : " + cardSlot.assetName
                        + ", Public key : " + cardSlot.publicKeyHexString
                        + ", Address: " + cardSlot.address //todo remove?
            )
            return cardSlot
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown while getting key slot status.")
            Log.e(TAG, Log.getStackTraceString(e))
            return null
        }

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


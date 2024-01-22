package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.satochip.satodime.data.CardPrivkey
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.NfcActionType
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.util.coinToSlip44Bytes

private const val TAG = "SharedViewModel"

class SharedViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return SharedViewModel(application) as T
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val satodimeStore = SatodimeStore(context)

    var isActionFinished by mutableStateOf(false)
    var isListening by mutableStateOf(false)
    var isCardConnected by mutableStateOf(false) // todo deprecate?
    var isAskingForCardOwnership by mutableStateOf(false) // todo: rename waitForSetup //deprecate?
    var isReadingFinished by mutableStateOf(true)
    var isOwner by mutableStateOf(false)
    var isAuthentic by mutableStateOf(false)

    // DEBUG
    var isCardDataAvailable by mutableStateOf(false)
    var cardSlots = mutableListOf<CardSlot>()
    //var cardVaults: List<CardVault?>? = null
    var cardVaults = MutableLiveData<List<CardVault?>>() //MutableLiveData<MutableList<CardVault?>>() // mutableListOf<CardVault?>()
    var cardPrivkeys = mutableListOf<CardPrivkey?>()
    var selectedVault by mutableIntStateOf(1)
    var resultCodeLive by mutableStateOf(NfcResultCode.Busy)
    var isVaultDataAvailable by mutableStateOf(false)


    init {
        NFCCardService.context = getApplication<Application>().applicationContext
        NFCCardService.isActionFinished.observeForever {
            isActionFinished = it
        }
        NFCCardService.isListening.observeForever {
            isListening = it
        }
        NFCCardService.isConnected.observeForever {
            isCardConnected = it
        }
        NFCCardService.waitForSetup.observeForever {
            isAskingForCardOwnership = it
            //saveCardAuth()
        }
        NFCCardService.isReadingFinished.observeForever { // todo redundant with isConnected
            isReadingFinished = it
        }
        NFCCardService.isAuthentic.observeForever {
            isAuthentic = it
        }
        NFCCardService.isOwner.observeForever {
            isOwner = it
        }
        NFCCardService.resultCodeLive.observeForever {
            resultCodeLive = it

        }
        NFCCardService.isCardDataAvailable.observeForever{
            isCardDataAvailable = it
        }
        // update balances
        NFCCardService.cardSlots.observeForever {
            viewModelScope.launch {
                println("DEBUG SharedViewModel cardSlots UPDATE START")
                updateVaults(it) // TODO uncomment to fetch balance etc
                println("DEBUG SharedViewModel cardSlots UPDATE FINISHED")
            }
            cardSlots = it.toMutableList()
        }
        NFCCardService.cardPrivkeys.observeForever {
            cardPrivkeys = it.toMutableList()
        }
    }

    // Card actions

    fun scanCard(activity: Activity) {
        Log.d(TAG, "SharedViewModel scanCard START")
        NFCCardService.actionType = NfcActionType.ScanCard
        scanCardForAction(activity)
    }

    fun takeOwnership(activity: Activity) {
        Log.d(TAG, "SharedViewModel takeOwnership START")
        NFCCardService.actionType = NfcActionType.TakeOwnership
        scanCardForAction(activity)
    }

    fun dismissCardOwnership() {
        Log.d(TAG, "SharedViewModel dismissCardOwnership START")
        NFCCardService.dismissOwnership()
    }

    fun releaseOwnership(activity: Activity) {
        Log.d(TAG, "SharedViewModel releaseOwnership START")
        NFCCardService.actionType = NfcActionType.ReleaseOwnership
        scanCardForAction(activity)
    }

    // assert 0 <= index < cardSlots.size
    fun sealSlot(activity: Activity, index: Int, coinSymbol: String, isTestnet: Boolean, entropyBytes: ByteArray) {
        Log.d(TAG, "SharedViewModel sealSlot START slot: ${index}")
        NFCCardService.actionType = NfcActionType.SealSlot
        NFCCardService.actionIndex = index
        // check entropy (32bytes)
        NFCCardService.actionEntropy = entropyBytes
        // convert to slip44Bytes
        NFCCardService.actionSlip44 = coinToSlip44Bytes(coinSymbol = coinSymbol, isTestnet = isTestnet)
        scanCardForAction(activity)
    }

    // assert 0 <= index < cardSlots.size
    fun unsealSlot(activity: Activity, index: Int) {
        Log.d(TAG, "SharedViewModel unsealSlot START slot: ${index}")
        NFCCardService.actionType = NfcActionType.UnsealSlot
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    // assert 0 <= index < cardSlots.size
    fun resetSlot(activity: Activity, index: Int) {
        Log.d(TAG, "SharedViewModel resetSlot START slot: ${index}")
        NFCCardService.actionType = NfcActionType.ResetSlot
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    fun recoverSlotPrivkey(activity: Activity, index: Int) {
        Log.d(TAG, "SharedViewModel recoverSlotPrivkey START slot: ${index}")
        NFCCardService.actionType = NfcActionType.GetPrivkey
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    fun scanCardForAction(activity: Activity) {
        Log.d(TAG, "SharedViewModel scanCardForAction START")
        viewModelScope.launch {
            Log.d(TAG, "SharedViewModel scanCardForAction START Thread")
            NFCCardService.scanCardForAction(activity)
            Log.d(TAG, "SharedViewModel scanCardForAction END Thread")
        }
        Log.d(TAG, "In scanCardForAction END")
    }

//    // todo remove?
//    fun acceptCardOwnership() {
//        NFCCardService.acceptOwnership()
//        saveCardAuth()
//        viewModelScope.launch {
//            NFCCardService.readCard()
//        }
//    }

//    // TODO: remove?
//    private fun saveCardAuth() {
//        viewModelScope.launch {
//            NFCCardService.authenticationKeyHex?.let { authKey ->
//                NFCCardService.unlockSecret?.let { unlockSecret ->
//                    val cardAuth = CardAuth(authKey, unlockSecret)
//                    val updatedCardsAuth = satodimeStore.cardsAuthFromDataStore.first().filter {
//                        it.authenticationKey != authKey
//                    }.toMutableList()
//                    updatedCardsAuth += cardAuth
//                    satodimeStore.saveCardAuthToDataStore(updatedCardsAuth)
//                }
//            }
//        }
//    }

//    // todo: remove?
//    private fun unlockCard() {
//        viewModelScope.launch {
//            satodimeStore.cardsAuthFromDataStore.collect {
//                if (NFCCardService.waitForSetup.value == false) {
//                    val unlockSecret = it.firstOrNull { cardAuth ->
//                        cardAuth.authenticationKey == NFCCardService.authenticationKeyHex
//                    }?.unlockSecret
//                    NFCCardService.unlockCard(unlockSecret)
//                }
//            }
//        }
//    }

    /// WEB API

    private suspend fun updateVaults(cardSlots: List<CardSlot>) {
        println("DEBUG SharedViewModel updateVaults START")
        withContext(Dispatchers.IO) {
            println("DEBUG SharedViewModel updateVaults withContext")
            val updatedVaults = mapCardSlotsToVaults(cardSlots)
            cardVaults.postValue(updatedVaults) //postValue(updatedVaults.toMutableList())
            isVaultDataAvailable = true
        }
        println("DEBUG SharedViewModel updateVaults END")
    }

    private fun mapCardSlotsToVaults(cardSlots: List<CardSlot>): List<CardVault?> {
        println("DEBUG SharedViewModel mapCardSlotsToVaults START")
        return cardSlots.map {
            if (it.slotState == SlotState.UNINITIALIZED) {
                println("DEBUG SharedViewModel mapCardSlotsToVaults slot uninitialized")
                return@map null
            }

            println("DEBUG SharedViewModel mapCardSlotsToVaults  it pubkey: ${it.publicKeyHexString}")

//            println("DEBUG SharedViewModel mapCardSlotsToVaults return null vault")
//            return@map null

            println("DEBUG SharedViewModel mapCardSlotsToVaults create cardVault START")
            var cardVault = CardVault(it)
            println("DEBUG SharedViewModel mapCardSlotsToVaults getBalance START")
            // get balance from api
            //val balance = cardVault.getBalanceDebug()
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance: $balance")
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance END")
            // TODO: get Tokens & NFTs

            return@map cardVault
        }
    }

//    // TODO debug remove??
//    fun getActivity(context: Context?): Activity? {
//        Log.d(TAG, "In getActivity START")
//        if (context == null) {
//            Log.d(TAG, "In getActivity context null")
//            return null
//        } else if (context is ContextWrapper) {
//            return if (context is Activity) {
//                Log.d(TAG, "In getActivity context is activity")
//                context
//            } else {
//                Log.d(TAG, "In getActivity context is ContextWrapper")
//                getActivity(context.baseContext)
//            }
//        }
//        Log.d(TAG, "In getActivity context is something else")
//        Log.d(TAG, "In getActivity context is type ${context::class.qualifiedName}")
//        return null
//    }
}
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.satochip.satodime.data.AuthenticityStatus
import org.satochip.satodime.data.CardPrivkey
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.NfcActionType
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.data.OwnershipStatus
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
    var authenticityStatus by mutableStateOf(AuthenticityStatus.Unknown)

    // delayed values
    var ownershipStatusDelayed by mutableStateOf(OwnershipStatus.Unknown)
    var authenticityStatusDelayed by mutableStateOf(AuthenticityStatus.Unknown)

    // dialogs
    //val showNoCardScannedDialog = mutableStateOf(false) // for NoCardScannedDialog
    val showOwnershipDialog = mutableStateOf(true) // for OwnershipDialog
    val showAuthenticityDialog = mutableStateOf(true) // for AuthenticityDialog

    init {
        NFCCardService.context = getApplication<Application>().applicationContext
        NFCCardService.isActionFinished.observeForever {
            isActionFinished = it //todo remove
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
            isOwner = it // todo remove?
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
                Log.d(TAG, "DEBUG SharedViewModel cardSlots UPDATE START")
                updateVaults(it) // TODO uncomment to fetch balance etc
                Log.d(TAG, "DEBUG SharedViewModel cardSlots UPDATE FINISHED")
            }
            cardSlots = it.toMutableList()
        }
        NFCCardService.cardPrivkeys.observeForever {
            cardPrivkeys = it.toMutableList()
        }
        NFCCardService.authenticityStatus.observeForever{
            authenticityStatus = it
        }
        NFCCardService.ownershipStatus.observeForever{
            viewModelScope.launch {
                Log.d(TAG, "DEBUG SharedViewModel ownershipStatusDelayed UPDATE START")
                // add delay so that dialogs do not show at same time...
                delay(5000)
                ownershipStatusDelayed = it
                showOwnershipDialog.value = true
                Log.d(TAG, "DEBUG SharedViewModel ownershipStatusDelayed UPDATE FINISHED")
            }
        }
        NFCCardService.authenticityStatus.observeForever{
            viewModelScope.launch {
                Log.d(TAG, "DEBUG SharedViewModel authenticityStatus UPDATE START")
                // add delay so that dialogs do not show at same time...
                delay(5000)
                authenticityStatusDelayed = it
                showAuthenticityDialog.value = true
                Log.d(TAG, "DEBUG SharedViewModel authenticityStatus UPDATE FINISHED")
            }
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
        Log.d(TAG,"DEBUG SharedViewModel updateVaults START")
        fetchVaultInfoFromSlot(cardSlots)

        // balance
        fetchVaultBalance()

        // coin values & rates
        fetchVaultPrice()

        // asset list
        fetchVaultAssets()

        // asset values
        fetchVaultAssetPrices()

//        withContext(Dispatchers.IO) {
//            println("DEBUG SharedViewModel updateVaults withContext")
//            val updatedVaults = mapCardSlotsToVaults(cardSlots)
//            cardVaults.postValue(updatedVaults) //postValue(updatedVaults.toMutableList())
//            isVaultDataAvailable = true
//        }
//        println("DEBUG SharedViewModel updateVaults END")
    }

    private suspend fun fetchVaultInfoFromSlot(cardSlots: List<CardSlot>) {
        Log.d(TAG,"DEBUG SharedViewModel fetchVaultBalance START")
        withContext(Dispatchers.IO) {
            Log.d(TAG,"DEBUG SharedViewModel fetchVaultInfoFromSlot withContext")
            val updatedVaults = cardSlots.map {
                if (it.slotState == SlotState.UNINITIALIZED) {
                    Log.d(TAG,"DEBUG SharedViewModel fetchVaultInfoFromSlot created uninitialized vault ${it.index}")
                    return@map null
                }

                var cardVault = CardVault(it, context)
                Log.d(TAG,"DEBUG SharedViewModel fetchVaultInfoFromSlot created vault ${cardVault.index}")
                return@map cardVault
            }
            cardVaults.postValue(updatedVaults) //postValue(updatedVaults.toMutableList())
            isVaultDataAvailable = true

        }

    }

    private suspend fun fetchVaultBalance() {
        Log.d(TAG,"DEBUG SharedViewModel fetchVaultBalance START")
        withContext(Dispatchers.IO) {
            Log.d(TAG,"DEBUG SharedViewModel fetchVaultBalance withContext")
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    Log.d(TAG,"DEBUG SharedViewModel fetchVaultBalance uninitialized vault")
                    return@map null
                }

                it.fetchBalance()
                Log.d(TAG,"DEBUG SharedViewModel fetchVaultBalance updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
            //isVaultDataAvailable = true
        }
    }

    private suspend fun fetchVaultPrice(){
        Log.d(TAG,"DEBUG SharedViewModel fetchVaultPrice START")
        withContext(Dispatchers.IO) {
            Log.d(TAG,"DEBUG SharedViewModel fetchVaultPrice withContext")
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    Log.d(TAG,"DEBUG SharedViewModel fetchVaultPrice uninitialized vault")
                    return@map null
                }

                // update asset value/rate fields
                it.fetchAssetValue(it.nativeAsset)
                Log.d(TAG,"DEBUG SharedViewModel fetchVaultPrice updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
            //isVaultDataAvailable = true
        }
    }

    private suspend fun fetchVaultAssets() {
        Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssets START")
        withContext(Dispatchers.IO) {
            Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssets withContext")
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssets uninitialized vault")
                    return@map null
                }

                it.fetchTokenList()
                it.fetchNftList()
                Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssets updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
            //isVaultDataAvailable = true
        }
    }

    private suspend fun fetchVaultAssetPrices() {
        Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssetPrices START")
        withContext(Dispatchers.IO) {
            Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssetPrices withContext")
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssetPrices uninitialized vault")
                    return@map null
                }
                // update asset value/rate fields
                for(asset in it.tokenList){
                    it.fetchAssetValue(asset)
                }
                // update asset value/rate fields for nft
                for(asset in it.nftList){
                    it.fetchAssetValue(asset)
                }
                Log.d(TAG,"DEBUG SharedViewModel fetchVaultAssetPrices updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
            //isVaultDataAvailable = true
        }
    }


    // todo deprecate
    private fun mapCardSlotsToVaults(cardSlots: List<CardSlot>): List<CardVault?> {
        Log.d(TAG,"DEBUG SharedViewModel mapCardSlotsToVaults START")
        return cardSlots.map {
            if (it.slotState == SlotState.UNINITIALIZED) {
                Log.d(TAG,"DEBUG SharedViewModel mapCardSlotsToVaults slot uninitialized")
                return@map null
            }

            Log.d(TAG,"DEBUG SharedViewModel mapCardSlotsToVaults  it pubkey: ${it.publicKeyHexString}")

//            println("DEBUG SharedViewModel mapCardSlotsToVaults return null vault")
//            return@map null

            Log.d(TAG,"DEBUG SharedViewModel mapCardSlotsToVaults create cardVault START")
            var cardVault = CardVault(it, context)
            Log.d(TAG,"DEBUG SharedViewModel mapCardSlotsToVaults getBalance START")
            // get balance from api
            val balance = cardVault.fetchBalance()
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance: $balance")
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance END")
            // TODO: get Tokens & NFTs
            cardVault.fetchTokenList()
            cardVault.fetchNftList()

            return@map cardVault
        }
    }

    // todo deprecate
    private suspend fun updateVaultsOld(cardSlots: List<CardSlot>) {
        Log.d(TAG,"DEBUG SharedViewModel updateVaults START")
        withContext(Dispatchers.IO) {
            println("DEBUG SharedViewModel updateVaults withContext")
            val updatedVaults = mapCardSlotsToVaults(cardSlots)
            cardVaults.postValue(updatedVaults) //postValue(updatedVaults.toMutableList())
            isVaultDataAvailable = true
        }
        println("DEBUG SharedViewModel updateVaults END")
    }

    // todo deprecate?
    private fun mapCardSlotsToVaultsOld(cardSlots: List<CardSlot>): List<CardVault?> {
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
            var cardVault = CardVault(it, context)
            println("DEBUG SharedViewModel mapCardSlotsToVaults getBalance START")
            // get balance from api
            val balance = cardVault.fetchBalance()
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance: $balance")
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance END")
            // TODO: get Tokens & NFTs
            cardVault.fetchTokenList()
            cardVault.fetchNftList()

            return@map cardVault
        }
    }

}
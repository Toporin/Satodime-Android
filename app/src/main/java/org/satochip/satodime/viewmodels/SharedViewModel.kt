package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
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
import org.satochip.satodime.services.SatoLog
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

    var isCardConnected by mutableStateOf(false) // todo deprecate?
    var isAskingForCardOwnership by mutableStateOf(false) // todo: rename waitForSetup //deprecate?

    var isCardDataAvailable by mutableStateOf(false)
    var cardSlots = mutableListOf<CardSlot>()
    var cardVaults = MutableLiveData<List<CardVault?>>() //MutableLiveData<MutableList<CardVault?>>() // mutableListOf<CardVault?>()
    var cardPrivkeys = mutableListOf<CardPrivkey?>()
    var selectedVault by mutableIntStateOf(1)
    var resultCodeLive by mutableStateOf(NfcResultCode.Busy)
    var authenticityStatus by mutableStateOf(AuthenticityStatus.Unknown)
    var ownershipStatus by mutableStateOf(OwnershipStatus.Unknown)

    // dialogs
    val showOwnershipDialog = mutableStateOf(true) // for OwnershipDialog
    val showAuthenticityDialog = mutableStateOf(true) // for AuthenticityDialog

    init {
        NFCCardService.context = getApplication<Application>().applicationContext
        NFCCardService.isConnected.observeForever {
            isCardConnected = it
        }
        NFCCardService.waitForSetup.observeForever {
            isAskingForCardOwnership = it
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
                updateVaults(it)
            }
            cardSlots = it.toMutableList()
        }
        NFCCardService.cardPrivkeys.observeForever {
            cardPrivkeys = it.toMutableList()
        }
        NFCCardService.ownershipStatus.observeForever{
            ownershipStatus = it
        }
        NFCCardService.authenticityStatus.observeForever{
            authenticityStatus = it
        }
        NFCCardService.ownershipStatus.observeForever{
            viewModelScope.launch {
                // add delay so that dialogs do not show at same time...
                delay(5000) //todo remove?
                if (it == OwnershipStatus.NotOwner){
                    showOwnershipDialog.value = true
                }
            }
        }
        NFCCardService.authenticityStatus.observeForever{
            viewModelScope.launch {
                // add delay so that dialogs do not show at same time...
                delay(5000) // todo remove?
                if (it == AuthenticityStatus.NotAuthentic){
                    showAuthenticityDialog.value = true
                }
            }
        }

    }

    // Card actions
    fun scanCard(activity: Activity) {
        NFCCardService.actionType = NfcActionType.ScanCard
        scanCardForAction(activity)
    }

    fun takeOwnership(activity: Activity) {
        NFCCardService.actionType = NfcActionType.TakeOwnership
        scanCardForAction(activity)
    }

    fun dismissCardOwnership() {
        NFCCardService.dismissOwnership()
    }

    fun releaseOwnership(activity: Activity) {
        NFCCardService.actionType = NfcActionType.ReleaseOwnership
        scanCardForAction(activity)
    }

    // assert 0 <= index < cardSlots.size
    fun sealSlot(activity: Activity, index: Int, coinSymbol: String, isTestnet: Boolean, entropyBytes: ByteArray) {
        SatoLog.d(TAG, "sealSlot START slot: ${index}")
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
        SatoLog.d(TAG, "unsealSlot START slot: ${index}")
        NFCCardService.actionType = NfcActionType.UnsealSlot
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    // assert 0 <= index < cardSlots.size
    fun resetSlot(activity: Activity, index: Int) {
        SatoLog.d(TAG, "resetSlot START slot: ${index}")
        NFCCardService.actionType = NfcActionType.ResetSlot
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    fun recoverSlotPrivkey(activity: Activity, index: Int) {
        SatoLog.d(TAG, "recoverSlotPrivkey START slot: ${index}")
        NFCCardService.actionType = NfcActionType.GetPrivkey
        NFCCardService.actionIndex = index
        scanCardForAction(activity)
    }

    fun scanCardForAction(activity: Activity) {
        SatoLog.d(TAG, "scanCardForAction START")
        viewModelScope.launch {
            NFCCardService.scanCardForAction(activity)
        }
        SatoLog.d(TAG, "scanCardForAction END")
    }

    /// WEB API
    private suspend fun updateVaults(cardSlots: List<CardSlot>) {
        SatoLog.d(TAG,"updateVaults START")
        fetchVaultInfoFromSlot(cardSlots)

        // balance
        fetchVaultBalance()

        // coin values & rates
        fetchVaultPrice()

        // asset list
        fetchVaultAssets()

        // asset values
        fetchVaultAssetPrices()

    }

    private suspend fun fetchVaultInfoFromSlot(cardSlots: List<CardSlot>) {
        SatoLog.d(TAG,"fetchVaultBalance START")
        withContext(Dispatchers.IO) {
            val updatedVaults = cardSlots.map {
                if (it.slotState == SlotState.UNINITIALIZED) {
                    SatoLog.d(TAG,"fetchVaultInfoFromSlot created uninitialized vault ${it.index}")
                    return@map null
                }

                var cardVault = CardVault(it, context)
                SatoLog.d(TAG,"fetchVaultInfoFromSlot created vault ${cardVault.index}")
                return@map cardVault
            }
            cardVaults.postValue(updatedVaults) //postValue(updatedVaults.toMutableList())
        }

    }

    private suspend fun fetchVaultBalance() {
        SatoLog.d(TAG,"fetchVaultBalance START")
        withContext(Dispatchers.IO) {
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    SatoLog.d(TAG,"fetchVaultBalance uninitialized vault")
                    return@map null
                }

                it.fetchBalance()
                SatoLog.d(TAG,"fetchVaultBalance updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
        }
    }

    private suspend fun fetchVaultPrice(){
        SatoLog.d(TAG,"fetchVaultPrice START")
        withContext(Dispatchers.IO) {
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    SatoLog.d(TAG,"fetchVaultPrice uninitialized vault")
                    return@map null
                }

                // update asset value/rate fields
                it.fetchAssetValue(it.nativeAsset)
                SatoLog.d(TAG,"fetchVaultPrice updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
        }
    }

    private suspend fun fetchVaultAssets() {
        SatoLog.d(TAG,"fetchVaultAssets START")
        withContext(Dispatchers.IO) {
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    SatoLog.d(TAG,"fetchVaultAssets uninitialized vault")
                    return@map null
                }

                it.fetchTokenList()
                it.fetchNftList()
                SatoLog.d(TAG,"fetchVaultAssets updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
        }
    }

    private suspend fun fetchVaultAssetPrices() {
        SatoLog.d(TAG,"fetchVaultAssetPrices START")
        withContext(Dispatchers.IO) {
            val updatedVaults = cardVaults.value?.map {
                if (it == null) {
                    SatoLog.d(TAG,"fetchVaultAssetPrices uninitialized vault")
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
                SatoLog.d(TAG,"fetchVaultAssetPrices updated vault ${it.index}")
                return@map it
            }
            cardVaults.postValue(updatedVaults)
        }
    }

}
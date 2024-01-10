package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.nfc.NfcAdapter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.satochip.android.NFCCardManager
import org.satochip.satodime.data.CardAuth
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.models.CardState
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeCardListener
import org.satochip.satodime.services.SatodimeStore

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

    var isCardConnected by mutableStateOf(false)
    var isAskingForCardOwnership by mutableStateOf(false)
    var isReadingFinished by mutableStateOf(true)
    var isOwner by mutableStateOf(false)
    var isAuthentic by mutableStateOf(false)

    // DEBUG
    var isCardDataAvailable by mutableStateOf(false)
    var cardSlots = mutableListOf<CardSlot>()
    //var cardVaults: List<CardVault?>? = null
    var cardVaults= mutableListOf<CardVault?>()
    var selectedVault by mutableIntStateOf(1)
    var showVaultsOnly by mutableStateOf(false) // TODO: put in vaultsView

    init {
        NFCCardService.isConnected.observeForever {
            isCardConnected = it
        }
        NFCCardService.waitForSetup.observeForever {
            isAskingForCardOwnership = it
            saveCardAuth()
        }
        NFCCardService.isReadingFinished.observeForever {
            isReadingFinished = it
            unlockCard()
        }
        NFCCardService.isAuthentic.observeForever {
            isAuthentic = it
        }
        NFCCardService.isOwner.observeForever {
            isOwner = it
        }

        // DEBUG TODO deprecate CardState, use NFCCardService instead
        CardState.isCardDataAvailable.observeForever{
            isCardDataAvailable = it
        }
        // update balances
        CardState.cardSlots.observeForever {
            viewModelScope.launch {
                println("DEBUG SharedViewModel cardSlots UPDATE START")
                updateVaults(it)
                println("DEBUG SharedViewModel cardSlots UPDATE FINISHED")
            }
            cardSlots = it
        }
        //
//        CardState.cardVaults.observeForever{
//            cardVaults = it
//        }

    }

    fun acceptCardOwnership() {
        NFCCardService.acceptOwnership()
        saveCardAuth()
        viewModelScope.launch {
            NFCCardService.readCard()
        }
    }

    fun dismissCardOwnership() {
        NFCCardService.dismissOwnership()
    }

    private fun saveCardAuth() {
        viewModelScope.launch {
            NFCCardService.authenticationKeyHex?.let { authKey ->
                NFCCardService.unlockSecret?.let { unlockSecret ->
                    val cardAuth = CardAuth(authKey, unlockSecret)
                    val updatedCardsAuth = satodimeStore.cardsAuthFromDataStore.first().filter {
                        it.authenticationKey != authKey
                    }.toMutableList()
                    updatedCardsAuth += cardAuth
                    satodimeStore.saveCardAuthToDataStore(updatedCardsAuth)
                }
            }
        }
    }

    private fun unlockCard() {
        viewModelScope.launch {
            satodimeStore.cardsAuthFromDataStore.collect {
                if (NFCCardService.waitForSetup.value == false) {
                    val unlockSecret = it.firstOrNull { cardAuth ->
                        cardAuth.authenticationKey == NFCCardService.authenticationKeyHex
                    }?.unlockSecret
                    NFCCardService.unlockCard(unlockSecret)
                }
            }
        }
    }

    // DEBUG
    fun scanCard(activity: Activity) {

        println("CardState.isCardDataAvailable beforeLaunch:" + CardState.isCardDataAvailable.value)
        println("sharedViewModel.isCardDataAvailable beforeLaunch:" + isCardDataAvailable)

        viewModelScope.launch {
            println("CardState.isCardDataAvailable beforeScan:" + CardState.isCardDataAvailable.value)
            println("sharedViewModel.isCardDataAvailable beforeScan:" + isCardDataAvailable)
            //val activity = app.mainActivity //this //LocalContext.current as Activity
            val cardManager = NFCCardManager()
            cardManager.setCardListener(SatodimeCardListener)
            cardManager.start()
            val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            nfcAdapter?.enableReaderMode(
                activity,
                cardManager,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
            println("CardState.isCardDataAvailable afterScan:" + CardState.isCardDataAvailable.value)
            println("sharedViewModel.isCardDataAvailable afterScan:" + isCardDataAvailable)
        }
        println("CardState.isCardDataAvailable afterLaunch:" + CardState.isCardDataAvailable.value)
        println("sharedViewModel.isCardDataAvailable afterLaunch:" + isCardDataAvailable)
    }

    private suspend fun updateVaults(cardSlots: List<CardSlot>) {
        println("DEBUG SharedViewModel updateVaults START")
        withContext(Dispatchers.IO) {
            println("DEBUG SharedViewModel updateVaults withContext")
            val updatedVaults = mapCardSlotsToVaults(cardSlots)
            cardVaults = updatedVaults.toMutableList()
            println("DEBUG SharedViewModel updateVaults postValue START")
            //CardState.cardVaults.postValue(updatedVaults.toMutableList())
            //CardState.cardVaults.postValue(updatedVaults)
            println("DEBUG SharedViewModel updateVaults after postValue")
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

}
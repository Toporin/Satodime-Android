package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.satochip.satodime.data.CardAuth
import org.satochip.satodime.services.NFCCardService
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
}
package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.Coin
import org.satochip.satodime.data.Currency
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.data.Vault
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.util.getCoinBalance
import org.satochip.satodime.util.getCurrencyAmount

class VaultsViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return VaultsViewModel(application) as T
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val satodimeStore = SatodimeStore(context)
    private var cardSlots = mutableListOf<CardSlot>()
    val vaults = satodimeStore.vaultsFromDataStore

    var showVaultsOnly by mutableStateOf(false)
    var selectedVault by mutableIntStateOf(1)

    init {
//        viewModelScope.launch {
//            satodimeStore.saveVaultsToDataStore(emptyList())
//        }
        NFCCardService.cardSlots.observeForever {
            viewModelScope.launch {
                updateVaults(it, satodimeStore.selectedCurrency.first())
            }
            cardSlots = it.toMutableList()
        }
        viewModelScope.launch {
            satodimeStore.selectedCurrency.collect {
                updateCurrencyAmountsOnVaults(it)
            }
        }
    }

    private suspend fun updateVaults(cardSlots: List<CardSlot>, currency: String) {
        withContext(Dispatchers.IO) {
            val selectedCurrency = Currency.valueOf(currency)
            satodimeStore.saveVaultsToDataStore(
                mapCardSlotsToVaults(
                    cardSlots, selectedCurrency
                )
            )
        }
    }

    private fun mapCardSlotsToVaults(cardSlots: List<CardSlot>, selectedCurrency: Currency): List<Vault?> {
        return cardSlots.map {
            if (it.slotState == SlotState.UNINITIALIZED) {
                return@map null
            }
            val coin = try {
                if (it.coinSymbol == "ROP") Coin.ETH else Coin.valueOf(it.coinSymbol.take(3))
            } catch (e: IllegalArgumentException) {
                Coin.UNKNOWN
            }
            val balance = it.address?.let { address ->
                getCoinBalance(coin, it.isTestnet, address)
            }
            Vault(
                coin,
                it.isTestnet,
                it.slotState == SlotState.SEALED,
                it.coinDisplayName,
                it.address ?: "N/A",
                it.privateKeyHex,
                it.privateKeyWif,
                it.entropyHex,
                balance,
                getCurrencyAmount(coin, it.isTestnet, balance ?: 0.0, selectedCurrency)
            )
        }
    }

    private suspend fun updateCurrencyAmountsOnVaults(currency: String) {
        withContext(Dispatchers.IO) {
            val selectedCurrency = Currency.valueOf(currency)
            val updatedVaults = satodimeStore.vaultsFromDataStore.first().map {
                if (it == null) {
                    return@map null
                }
                it.copy(
                    currencyAmount = getCurrencyAmount(
                        it.coin, it.isTesnet,
                        it.balance ?: 0.0,
                        selectedCurrency
                    )
                )
            }
            satodimeStore.saveVaultsToDataStore(updatedVaults)
        }
    }
}
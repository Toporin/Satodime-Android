package org.satochip.satodime.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.services.NFCCardService

//TODO: DEPRECATE
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
    //private val satodimeStore = SatodimeStore(context)
    private var cardSlots = mutableListOf<CardSlot>()
    //val vaults = satodimeStore.vaultsFromDataStore

    //var showVaultsOnly by mutableStateOf(false)
    var selectedVault by mutableIntStateOf(1)

    var cardVaults= mutableListOf<CardVault?>() // TODO: deprecate

    init {
//        viewModelScope.launch {
//            satodimeStore.saveVaultsToDataStore(emptyList())
//        }
        NFCCardService.cardSlots.observeForever {
            viewModelScope.launch {
                println("DEBUG VaultsViewModel cardSlots update START")
                //updateVaults(it, satodimeStore.selectedCurrency.first())
                updateCardVaults(it)
                println("DEBUG VaultsViewModel cardSlots update FINISHED")
            }
            cardSlots = it.toMutableList()
        }
//        viewModelScope.launch {
//            satodimeStore.selectedCurrency.collect {
//                updateCurrencyAmountsOnVaults(it)
//            }
//        }
    }

//    private suspend fun updateVaults(cardSlots: List<CardSlot>, currency: String) {
//        withContext(Dispatchers.IO) {
//            val selectedCurrency = Currency.valueOf(currency)
//            satodimeStore.saveVaultsToDataStore(
//                mapCardSlotsToVaults(
//                    cardSlots, selectedCurrency
//                )
//            )
//        }
//    }

//    private fun mapCardSlotsToVaults(cardSlots: List<CardSlot>, selectedCurrency: Currency): List<Vault?> {
//        return cardSlots.map {
//            if (it.slotState == SlotState.UNINITIALIZED) {
//                return@map null
//            }
//            val coin = try {
//                if (it.coinSymbol == "ROP") Coin.ETH else Coin.valueOf(it.coinSymbol.take(3))
//            } catch (e: IllegalArgumentException) {
//                Coin.UNKNOWN
//            }
//            val balance = it.address?.let { address ->
//                getCoinBalance(coin, it.isTestnet, address)
//            }
//            Vault(
//                coin,
//                it.isTestnet,
//                it.slotState == SlotState.SEALED,
//                it.coinDisplayName,
//                it.address ?: "N/A",
//                it.privateKeyHex,
//                it.privateKeyWif,
//                it.entropyHex,
//                balance,
//                getCurrencyAmount(coin, it.isTestnet, balance ?: 0.0, selectedCurrency)
//            )
//            //Vault(it, context)
//        }
//    }

//    private suspend fun updateCurrencyAmountsOnVaults(currency: String) {
//        withContext(Dispatchers.IO) {
//            val selectedCurrency = Currency.valueOf(currency)
//            val updatedVaults = satodimeStore.vaultsFromDataStore.first().map {
//                if (it == null) {
//                    return@map null
//                }
//                it.copy(
//                    currencyAmount = getCurrencyAmount(
//                        it.coin, it.isTestnet,
//                        it.balance ?: 0.0,
//                        selectedCurrency
//                    )
//                )
//            }
//            satodimeStore.saveVaultsToDataStore(updatedVaults)
//        }
//    }

    // New

    //
    private suspend fun updateCardVaults(cardSlots: List<CardSlot>) {
        println("DEBUG SharedViewModel updateVaults START")
        withContext(Dispatchers.IO) {
            println("DEBUG SharedViewModel updateVaults withContext")
            val updatedVaults = mapCardSlotsToCardVaults(cardSlots)
            cardVaults = updatedVaults.toMutableList()
            println("DEBUG SharedViewModel updateVaults postValue START")
            println("DEBUG SharedViewModel updateVaults after postValue")
        }
        println("DEBUG SharedViewModel updateVaults END")
    }

    private fun mapCardSlotsToCardVaults(cardSlots: List<CardSlot>): List<CardVault?> {
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
            //println("DEBUG SharedViewModel mapCardSlotsToVaults getBalance START")
            // get balance from api
            //val balance = cardVault.getBalanceDebug()
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance: $balance")
            //println("DEBUG SharedViewModel mapCardSlotsToVaults balance END")
            // TODO: get Tokens & NFTs

            return@map cardVault
        }
    }




}
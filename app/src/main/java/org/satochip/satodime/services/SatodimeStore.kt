package org.satochip.satodime.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.satochip.satodime.data.CardAuth
import org.satochip.satodime.data.CardsAuthSerializer
import org.satochip.satodime.data.Vault
import org.satochip.satodime.data.VaultsSerializer
import org.satochip.satodime.data.defaultCurrency

class SatodimeStore(private val context: Context) {

    companion object {
        private val Context.vaultsDataStore by dataStore("vaults.json", VaultsSerializer)
        private val Context.cardsAuthDataStore by dataStore("cards_auth.json", CardsAuthSerializer)
        private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        private val selectedCurrencyPrefsKey = stringPreferencesKey("selectedCurrency")
    }

    val vaultsFromDataStore: Flow<List<Vault?>> get() =  context.vaultsDataStore.data

    suspend fun saveVaultsToDataStore(vaults: List<Vault?>) {
        context.vaultsDataStore.updateData {
            vaults
        }
    }

    val cardsAuthFromDataStore: Flow<List<CardAuth>> get() =  context.cardsAuthDataStore.data

    suspend fun saveCardAuthToDataStore(cardsAuth: List<CardAuth>) {
        context.cardsAuthDataStore.updateData {
            cardsAuth
        }
    }

    val selectedCurrency: Flow<String>
        get() = context.settingsDataStore.data.map {
            it[selectedCurrencyPrefsKey] ?: defaultCurrency.name
        }

    suspend fun saveSelectedCurrency(value: String) {
        context.settingsDataStore.edit { it[selectedCurrencyPrefsKey] = value }
    }

}
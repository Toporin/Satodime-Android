package org.satochip.satodime.data

import kotlinx.serialization.Serializable

// TODO: remove?

@Serializable
data class Vault(
    val coin: Coin,
    val isTestnet: Boolean,
    val isSealed: Boolean,
    val displayName: String,
    val address: String,
    val privateKey: String?,
    val privateKeyWif: String?,
    val entropy: String?,
    val balance: Double?,
    val currencyAmount: String
)

package org.satochip.satodime.data

import kotlinx.serialization.Serializable

@Serializable
data class CardAuth(val authenticationKey: String, val unlockSecret: String?)
// TODO: authenticationKey => authentikey
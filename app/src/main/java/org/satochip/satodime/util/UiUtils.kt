package org.satochip.satodime.util

import org.satochip.satodime.data.Vault

fun getBalance(vault: Vault) : String {
    val amount = vault.balance?.toBigDecimal()?.toPlainString() ?: "N/A"
    val testnetSymbol = if (vault.isTesnet) "t" else ""
    return "$amount $testnetSymbol${vault.coin.name}"
}
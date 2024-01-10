package org.satochip.satodime.util

import org.satochip.satodime.data.CardVault

fun getBalance(vault: CardVault) : String {
    val amount = vault.balance?.toBigDecimal()?.toPlainString() ?: "N/A"
    val testnetSymbol = if (vault.isTestnet) "t" else ""
    return "$amount $testnetSymbol${vault.coin.name}"
}
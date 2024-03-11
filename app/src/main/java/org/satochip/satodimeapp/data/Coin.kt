package org.satochip.satodimeapp.data

import org.satochip.satodimeapp.R

enum class Coin(val label: String, val painterResourceId: Int) {
    BTC("Bitcoin", R.drawable.bitcoin),
    ETH("Ethereum", R.drawable.ethereum),
    LTC("Litecoin", R.drawable.litecoin),
    BCH("Bitcoin Cash", R.drawable.bitcoin_cash),
    XCP("Counterparty", R.drawable.counterparty),
    UNKNOWN("Unknown", R.drawable.top_left_logo)
}
package org.satochip.satodime.util

import org.satochip.javacryptotools.coins.BaseCoin
import org.satochip.javacryptotools.coins.Bitcoin
import org.satochip.javacryptotools.coins.BitcoinCash
import org.satochip.javacryptotools.coins.Counterparty
import org.satochip.javacryptotools.coins.Ethereum
import org.satochip.javacryptotools.coins.Litecoin
import org.satochip.javacryptotools.coins.UnsupportedCoin
import org.satochip.satodime.data.Coin

val btcCoinExplorer: BaseCoin = Bitcoin(false, apiKeys)
val btcTestnetCoinExplorer: BaseCoin = Bitcoin(true, apiKeys)
val ltcCoinExplorer: BaseCoin = Litecoin(false, apiKeys)
val ltcTestnetCoinExplorer: BaseCoin = Litecoin(true, apiKeys)
val bchCoinExplorer: BaseCoin = BitcoinCash(false, apiKeys)
val bchTestnetCoinExplorer: BaseCoin = BitcoinCash(true, apiKeys)
val ethCoinExplorer: BaseCoin = Ethereum(false, apiKeys)
val ethTestnetCoinExplorer: BaseCoin = Ethereum(true, apiKeys)
val xcpCoinExplorer: BaseCoin = Counterparty(false, apiKeys)
val xcpTestnetCoinExplorer: BaseCoin = Counterparty(true, apiKeys)
val unsupportedCoinExplorer: BaseCoin = UnsupportedCoin(false, apiKeys)
val unsupportedTestnetCoinExplorer: BaseCoin = UnsupportedCoin(true, apiKeys)

fun getCoinExplorer(coin: Coin, isTestnet: Boolean) : BaseCoin {
    return if(isTestnet) {
        when (coin) {
            Coin.BTC -> btcTestnetCoinExplorer
            Coin.LTC -> ltcTestnetCoinExplorer
            Coin.BCH -> bchTestnetCoinExplorer
            Coin.ETH -> ethTestnetCoinExplorer
            Coin.XCP -> xcpTestnetCoinExplorer
            else -> unsupportedTestnetCoinExplorer
        }
    } else {
        when (coin) {
            Coin.BTC -> btcCoinExplorer
            Coin.LTC -> ltcCoinExplorer
            Coin.BCH -> bchCoinExplorer
            Coin.ETH -> ethCoinExplorer
            Coin.XCP -> xcpCoinExplorer
            else -> unsupportedCoinExplorer
        }
    }
}
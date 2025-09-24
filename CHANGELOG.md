# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.3]:

* Intercept NDEF tag when app is in the foreground: this avoids the android system to manage the NDEF tag and disrupting usage by opening the web browser when the app is active.
* When app is closed or in the background, intercept NDEF link only if related to Satodime
* Add specific error message when scanning an unsupported NFC card (i.e. not a Satodime applet)
* (minor) Patch version display as unsigned integers
* Support for Satodime v0.2+ with fixed CVC:
  * add unlockCodeDialog to get cvc code from user
  * simplified handling of unclaimed/not owned cards: when CVC is fixed, ownership for unclaimed/not owned card can be taken seamlessly just before performing an action requiring ownership,
    either by taking ownership automatically or prompting user for the CVC code when needed. For non fixed cvc, behavior is unchanged.
  * Remove ownership dialogs when scanning a card for Satodime v0.2+ with fixed cvc code

## [0.3.2]:

* Update javacryptotools to v0.4.0 (simplified API + Blockscout explorer)

## [0.3.1]:

* feature: Paybis integration of crypto on-ramp

## [0.3.0]:

* Feature: submit button on settings page
* Feature: new nfc toast look, network failure toast implemented
* feature: added crashlytics and playstore in app review
* feature: added webview activity for url handling
* feature: redesigned vault screen buttons
* update: added customized spinner for the application
* feature: added support for polygon using jlib javacryptotools v0.3.0 (commit 468bfe510c2e8e4b70d80aa6b52dbaa6be9c52d2)
* Patch: updated satochip-android lib to v0.0.2: fix crash issue when removing card too early on android 12+ devices
* feature: added option to only use bitcoin blockchain
* feature: nft tab in vaults view is now only clickable for select coins

## [0.2.2]:
UI: onboarding screens quick fix for small display screen

## [0.2.1]:

Various UI improvements

## [2.0.0]:

New design, totally recoded from scratch with Jetpack Compose and Kotlin

## [0.1.2]:

Improved onboarding screens

## [0.1.1]:

Refactor code, clean & remove unused code, fix various bugs. 

## [0.1.0]: 

New design GUI

## [0.0.5]: 

Remove logs from release build

## [0.0.4]: 

Satodime-Android v0.0.4
    
Satodime companion app to use Satodime on a Android smartphone.
Seal-Unseal-Redeem Bitcoin and other cryptos with a tap on your phone!
    
v0.0.4 Beta version with support for BTC, BCH, LTC, ETH (including ERC20 & NFTs)

## [0.0.3]: 

upgrade to javacryptotools v0.0.3 - add PriceExplorer
Allows to get exchange rate of coins versus most common currencies using coingecko API

## [0.0.2]: 

Work in Progress
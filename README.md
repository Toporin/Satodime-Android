# Satodime-Android

Simple android app to use your [Satodime card](satodime.io) on a smartphone. 
Seal-Unseal-Redeem Bitcoin and other cryptos with a tap on your phone!

Satodime is an [open-source](https://github.com/Toporin/Satodime-Tool) bearer chip card that allows you to exchange crypto assets like any banknote. 
Safely pass it along multiple times thanks the secure chip, unseal anytime with ease. Trustless, easy to verify and completly secure.

## build and dependencies

Build apk with: ```./gradlew clean build```

Satodime-Android uses two external libraries: 
* [Satochip-Java](https://github.com/Toporin/Satochip-Java) 
* [Javacryptotools](https://github.com/Toporin/Javacryptotools)

You can build these jar files  using the instructions provided in their github repo and then put these files in ```./satodime-android/libs``` folder.

## APIs

The [Javacryptotools library](https://github.com/Toporin/Javacryptotools) uses API from the following explorer services:
* [Blockstream](https://blockstream.com/)
* Sochain](https://sochain.com/)
* Fullstack](https://fullstack.cash/)
* Etherscan](https://etherscan.io/)
* Rarible](https://rarible.com/)
* Opensea](https://opensea.io/)
* Coingecko](https://www.coingecko.com/)

These APIs are used to get info such as balances, NFT name, description and image preview.

Remark: some explorer APIs require an API key to work correctly. API keys are provided through ```./satodime-android/src/main/res/values/apikeys.xml ``` (see ```apikeys-sample.xml``` for sample).

package org.satochip.satodime.models

import androidx.lifecycle.MutableLiveData
import org.satochip.client.SatodimeStatus
import org.satochip.client.ApplicationStatus
import org.satochip.satodime.data.CardSlot
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.OwnershipStatus

// singleton
object CardState {

    var debugValue: String = "Test Test Test"

    //var isConnected = MutableLiveData(false)
    //var isReadingFinished = MutableLiveData(true)
    //var waitForSetup = MutableLiveData(false)
    //var isOwner = MutableLiveData(false)
    //var isAuthentic = MutableLiveData(false)
    //var cardSlots = MutableLiveData<List<CardSlot>>() // make mutable??
    var cardSlots = MutableLiveData<MutableList<CardSlot>>()
    var cardVaults = MutableLiveData<MutableList<CardVault?>>()
    //var unlockSecret: String? = null
    //var certificate: String? = null

    // added
    var ownershipStatus = MutableLiveData<OwnershipStatus>(OwnershipStatus.Unknown)
    var isCardDataAvailable = MutableLiveData(false)
    var cardStatus = MutableLiveData<ApplicationStatus>()
    var satodimeStatus = MutableLiveData<SatodimeStatus>()
    var authentikeyHex = MutableLiveData<String>()
    // ownership
    // certificate
    var certificateList = MutableLiveData<MutableList<String>>()
    // Vaults
    var vaultArray = MutableLiveData<MutableList<CardSlot>>()

}
package org.satochip.satodime.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener

private const val TAG = "SatodimeCardListener"

object SatodimeCardListener : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {
        NFCCardService.isReadingFinished.postValue(false)
        NFCCardService.isConnected.postValue(true)
        Log.i(TAG, "Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            NFCCardService.initialize(cmdSet)
        } catch (e: Exception) {
            Log.e(TAG, "An exception has been thrown during card init.")
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        NFCCardService.isReadingFinished.postValue(true)
        Log.i(TAG, "Card disconnected")
    }
}
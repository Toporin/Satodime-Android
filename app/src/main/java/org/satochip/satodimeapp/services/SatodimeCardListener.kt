package org.satochip.satodimeapp.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import org.satochip.satodimeapp.data.NfcActionType

private const val TAG = "SatodimeCardListener"

object SatodimeCardListenerForAction : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {

        NFCCardService.isConnected.postValue(true)
        SatoLog.d(TAG, "onConnected: Card is connected")
        try {
            SatoLog.configureLibraryLogging()
            val cmdSet = SatochipCommandSet(cardChannel)
            // start to interact with card
            NFCCardService.initialize(cmdSet)

            // NFCCardManager closes the connection and calls onDisconnected() after this method returns
            SatoLog.d(TAG, "onConnected: resultAfterConnection: ${NFCCardService.resultCodeLive.value}")

            Thread.sleep(100) // delay to let resultCodeLive update (avoid race condition?)
            SatoLog.d(TAG, "onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
        } catch (e: Exception) {
            SatoLog.e(TAG, "onConnected: an exception has been thrown during card init.")
            SatoLog.e(TAG, Log.getStackTraceString(e))
        } finally {
            // action done: ignore further taps until a new action is requested
            // (reader mode stays enabled so that other NFC apps are not launched)
            NFCCardService.actionType = NfcActionType.DoNothing
        }
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        SatoLog.d(TAG, "onDisconnected: Card disconnected!")
    }
}
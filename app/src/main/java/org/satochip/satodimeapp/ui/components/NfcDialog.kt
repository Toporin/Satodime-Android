package org.satochip.satodimeapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.vaults.VaultDrawerScreen
import org.satochip.satodimeapp.ui.components.vaults.VaultsBottomDrawer
import org.satochip.satodimeapp.ui.theme.SatoWarningOrange
import kotlin.time.Duration.Companion.seconds

private const val TAG = "NfcDialog"

@Composable
fun NfcDialog(
    openDialogCustom: MutableState<Boolean>,
    resultCodeLive: NfcResultCode,
    isConnected: Boolean
) {
    VaultsBottomDrawer(
        showSheet = openDialogCustom
    ) {
        LaunchedEffect(resultCodeLive) {
            SatoLog.d(TAG, "LaunchedEffect START ${resultCodeLive}")
            while (resultCodeLive == NfcResultCode.Busy || resultCodeLive == NfcResultCode.None) {
                SatoLog.d(TAG, "LaunchedEffect in while delay 2s ${resultCodeLive}")
                delay(2.seconds)
            }
            SatoLog.d(TAG, "LaunchedEffect after while delay ${resultCodeLive}")
        }

        if (resultCodeLive == NfcResultCode.Busy) {
            if (isConnected) {
                VaultDrawerScreen(
                    closeSheet = {
                        openDialogCustom.value = !openDialogCustom.value
                    },
                    message = R.string.scanning,
                    image = R.drawable.nfc_scanner,
                    //message = NfcResultCode.Busy.res, // show?
                )
            } else {
                VaultDrawerScreen(
                    closeSheet = {
                        openDialogCustom.value = !openDialogCustom.value
                    },
                    closeDrawerButton = true,
                    title = R.string.readyToScan,
                    image = R.drawable.phone_icon,
                    message = R.string.nfcHoldSatodime
                )
            }
        } else {
            VaultDrawerScreen(
                closeSheet = {
                    openDialogCustom.value = !openDialogCustom.value
                },
                title = resultCodeLive.resTitle,
                image = resultCodeLive.resImage,
                message = resultCodeLive.resMsg,
                colorFilter = if (resultCodeLive.resTitle == R.string.nfcTitleWarning) ColorFilter.tint(
                    SatoWarningOrange
                ) else null
            )
            LaunchedEffect(Unit) {
                delay(1.seconds)
                openDialogCustom.value = false
            }
        }
    }
}
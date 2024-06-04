package org.satochip.satodimeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.vaults.VaultDrawerScreen
import org.satochip.satodimeapp.ui.components.vaults.VaultsBottomDrawer
import org.satochip.satodimeapp.ui.theme.LightBlue
import org.satochip.satodimeapp.ui.theme.Orange
import org.satochip.satodimeapp.ui.theme.SatoWarningOrange
import kotlin.time.Duration.Companion.seconds

private const val TAG = "NfcDialog"

@Composable
fun NfcDialog(openDialogCustom: MutableState<Boolean>, resultCodeLive: NfcResultCode, isConnected: Boolean) {

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
                colorFilter = if (resultCodeLive.resTitle == R.string.nfcTitleWarning) ColorFilter.tint(SatoWarningOrange) else null
            )
            LaunchedEffect(Unit) {
                delay(1.seconds)
                openDialogCustom.value = false
            }
        }
    }
}

// TODO: remove!
@Composable
fun NfcDialogUI(modifier: Modifier = Modifier, openDialogCustom: MutableState<Boolean>, resultCodeLive: NfcResultCode, isConnected: Boolean) {
    Card(
        //shape = MaterialTheme.shapes.medium,
        shape = RoundedCornerShape(10.dp),
        // modifier = modifier.size(280.dp, 240.dp)
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = modifier
                .background(Color.LightGray),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            if (resultCodeLive == NfcResultCode.Busy) {
                if (isConnected) {
                    Icon(
                        painter = painterResource(R.drawable.contactless_24px),
                        contentDescription = "",
                        modifier = Modifier.size(128.dp),
                        tint = Color.Blue,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.contactless_off_24px),
                        contentDescription = "",
                        modifier = Modifier.size(128.dp),
                        tint = Orange,
                    )
                }
            } else {
                if (resultCodeLive == NfcResultCode.Ok) {
                    Icon(
                        painter = painterResource(R.drawable.task_alt_24px),
                        contentDescription = "",
                        modifier = Modifier.size(128.dp),
                        tint = Color.Green,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.error_24px),
                        contentDescription = "",
                        modifier = Modifier.size(128.dp),
                        tint = Color.Red,
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.readyToScan),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.body1,
                color = Color.Black, //MaterialTheme.colors.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(id = R.string.nfcHoldSatodime),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                color = Color.Black,
                modifier = Modifier
                    .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                    .fillMaxWidth(),
            )
            Text(
                text = "Status: ${getScanStatus(isConnected, resultCodeLive)}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                color = Color.Black,
                modifier = Modifier
                    .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                    .fillMaxWidth(),
            )

            // CLOSE BUTTON
            Button(
                onClick = {
                    openDialogCustom.value = false
                },
                modifier = Modifier
                    .padding(10.dp)
                    .height(40.dp)
                    .width(160.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LightBlue,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.close))
            }

        }
    }
}

fun getScanStatus(isConnected: Boolean, resultCode: NfcResultCode): String{
    if (resultCode == NfcResultCode.Busy){
        if (isConnected) {
            return "Connected, reading" // todo i18n
        } else {
            return "Not connected, place card closer!"
        }
    } else {
        if (resultCode == NfcResultCode.Ok){
            return "success!"
        } else {
            return "${resultCode}"
        }
    }
}

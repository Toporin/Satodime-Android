package org.satochip.satodime.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatoLog
import org.satochip.satodime.ui.components.InfoDialog
import org.satochip.satodime.ui.components.NfcDialog
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.theme.LightGray
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.viewmodels.SharedViewModel

private const val TAG = "TransferOwnershipView"

@Composable
fun TransferOwnershipView(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val showNoCardScannedDialog = remember { mutableStateOf(false) }
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopLeftBackButton(navController)
        Image(
            painter = painterResource(R.drawable.transfer_ownership_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = (-75).dp),
            contentScale = ContentScale.FillWidth
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 30.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.transfer_ownership)
        )
        Image(
            painter = painterResource(id = R.drawable.transfer_ownership),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp),
            contentScale = ContentScale.FillHeight
        )
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.please_note)
        )
        Spacer(Modifier.weight(1f))
//        val ownershipTransferredText = stringResource(R.string.ownership_transferred)
//        val ownershipTransferFailedtext = stringResource(R.string.ownership_transfer_failed)
//        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
//        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        Button(
            onClick = {
                SatoLog.d(TAG, "TransferOwnershipView: clicked on release button!")
                if (viewModel.isCardDataAvailable) {
                    // scan card
                    showNfcDialog.value = true // NfcDialog
                    viewModel.releaseOwnership(context as Activity)
                } else {
                    showNoCardScannedDialog.value = true
                }
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen,
                MaterialTheme.colors.secondary
            )
        ) {
            Text(stringResource(R.string.transfer))
        }
        // CANCEL BUTTON
        Button(
            onClick = {
                Toast.makeText(context, "Action cancelled", Toast.LENGTH_SHORT).show() // todo translate
                navController.navigateUp()
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(100.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGray,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.cancel))
        }
    }

    // no card scanned dialog
    if (showNoCardScannedDialog.value
        && !viewModel.isCardDataAvailable
        && !showNfcDialog.value){
        InfoDialog(
            openDialogCustom = showNoCardScannedDialog,
            title = "noCardScannedTitle",
            message = "noCardScannedText",
            isActionButtonVisible = false,
            buttonTitle = "",
            buttonAction = {},)
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = viewModel.resultCodeLive, isConnected = viewModel.isCardConnected)
    }

    // todo show result message/toast
}

@Preview(showBackground = true)
@Composable
fun TransferOwnershipViewPreview() {
    SatodimeTheme {
        TransferOwnershipView(rememberNavController(), viewModel(factory = SharedViewModel.Factory))
    }
}
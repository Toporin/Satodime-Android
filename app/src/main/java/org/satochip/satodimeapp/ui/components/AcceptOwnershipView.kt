package org.satochip.satodimeapp.ui.components

import android.app.Activity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import kotlin.time.Duration.Companion.seconds

private const val TAG = "AcceptOwnershipView"

@Composable
fun AcceptOwnershipView(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog
    val isReadyToNavigate = remember{ mutableStateOf(false) }// to show result

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopLeftBackButton(navController) {
            viewModel.dismissCardOwnership()
        }
        // TITLE
        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 40.dp),
                //.padding(top = 30.dp, bottom = 20.dp),
            //textAlign = TextAlign.Center,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.takeTheOwnershipTitle)
        )
        // BACKGROUND IMAGE
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
            .padding(top = 75.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)// start just below TopLeftButton
            //.padding(10.dp)
            .verticalScroll(state = scrollState)
    ) {

        Image(
            painter = painterResource(id = R.drawable.transfer_ownership),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp)
                .height(175.dp),
            contentScale = ContentScale.FillHeight
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.takeTheOwnershipDescription)
        )
        Spacer(Modifier.weight(1f))
        // ACCEPT
        Button(
            onClick = {
                SatoLog.d(TAG, "Clicked on accept button!")
                // scan card
                showNfcDialog.value = true // NfcDialog
                isReadyToNavigate.value = true
                viewModel.takeOwnership(context as Activity)
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
            Text(stringResource(R.string.accept))
        }
        // CANCEL BUTTON
        val toastMsg = stringResource(R.string.actionCancelled)
        Button(
            onClick = {
                viewModel.dismissCardOwnership()
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
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

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = viewModel.resultCodeLive, isConnected = viewModel.isCardConnected)
    }

    // auto-navigate when action is performed successfully
    LaunchedEffect(viewModel.resultCodeLive, showNfcDialog) {
        SatoLog.d(TAG, "LaunchedEffect START ${viewModel.resultCodeLive}")
        while (viewModel.resultCodeLive != NfcResultCode.TakeOwnershipSuccess
            || isReadyToNavigate.value == false
            || showNfcDialog.value) {
            SatoLog.d(TAG, "LaunchedEffect in while delay 1s ${viewModel.resultCodeLive}")
            delay(1.seconds)
        }
        // navigate
        SatoLog.d(TAG, "navigating back to Vaults view")
        navController.navigate(SatodimeScreen.Vaults.name) {
            popUpTo(0)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcceptOwnershipViewPreview() {
    SatodimeTheme {
        AcceptOwnershipView(rememberNavController(), viewModel(factory = SharedViewModel.Factory))
    }
}
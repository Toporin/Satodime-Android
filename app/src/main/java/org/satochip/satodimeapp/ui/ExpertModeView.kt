package org.satochip.satodimeapp.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.Coin
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.Network
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import java.security.MessageDigest
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ExpertModeView"

@Composable
fun ExpertModeView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int, selectedCoinName: String) {
    // todo merge with CreateVaultView
    val context = LocalContext.current
    var selectedNetwork by remember { mutableStateOf(Network.MainNet) }
    var entropy by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog
    val isReadyToNavigate = remember{ mutableStateOf(false) }// for auto navigation to next view

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primaryVariant)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 25.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(R.string.expertMode)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary)
                        .padding(20.dp),
                    //textAlign = TextAlign.Center,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.theExpertModeAllowsYou)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colors.secondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.network)
            )
            NetworkDivider()
            NetworkRadioButton(
                Network.MainNet.name,
                isSelected = { selectedNetwork == Network.MainNet }) {
                selectedNetwork = Network.MainNet
            }
            NetworkRadioButton(
                Network.TestNet.name,
                isSelected = { selectedNetwork == Network.TestNet }) {
                selectedNetwork = Network.TestNet
            }
            NetworkDivider()
            Text(
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colors.secondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.entropy)
            )
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(25.dp),
                value = entropy,
                textStyle = MaterialTheme.typography.body2,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.secondary,
                    backgroundColor = MaterialTheme.colors.primary
                ),
                onValueChange = { entropy = it },
            )
            LaunchedEffect(focusRequester) {
                awaitFrame()
                focusRequester.requestFocus()
            }
        }
        Spacer(Modifier.weight(1f))
//        val sealFailureText = stringResource(R.string.seal_failure)
//        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
//        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        BottomButton(
            onClick = {
                // select network
                var isTestnet = (selectedNetwork == Network.TestNet)

                // generate entropy based on current time
                //val random = SecureRandom()
                var entropyBytes = ByteArray(32)
                var entropyStringToBytes = entropy.toByteArray()
                if (entropyStringToBytes.size>32){
                    // compute the hash of it
                    val sha256 = MessageDigest.getInstance("SHA-256")
                    entropyStringToBytes = sha256.digest(entropyStringToBytes)
                }
                // copy to array
                entropyStringToBytes.copyInto(
                    destination= entropyBytes,
                    destinationOffset= 0,
                    startIndex= 0,
                    endIndex= minOf(entropyStringToBytes.size, 32)
                )

                // scan card
                SatoLog.d(TAG, "ExpertModeView: clicked on create button selectedVault: $selectedVault")
                showNfcDialog.value = true // NfcDialog
                isReadyToNavigate.value = true
                sharedViewModel.sealSlot(context as Activity, index = selectedVault - 1, coinSymbol = selectedCoinName, isTestnet= isTestnet, entropyBytes= entropyBytes)
            },
            text = stringResource(R.string.createAndSeal)
        )
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

    // auto-navigate when action is performed successfully
    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog) {
        SatoLog.d(TAG, "ExpertModeView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
        while (sharedViewModel.resultCodeLive != NfcResultCode.Ok
            || isReadyToNavigate.value == false
            || showNfcDialog.value) {
            SatoLog.d(TAG, "ExpertModeView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}")
            delay(1.seconds)
        }
        // navigate
        SatoLog.d(TAG, "ExpertModeView navigating to CongratsVaultCreated view")
        navController.navigate(SatodimeScreen.CongratsVaultCreated.name + "/$selectedCoinName") {
            popUpTo(0)
        }
    }

}

@Composable
fun NetworkRadioButton(text: String, isSelected: () -> Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(start = 20.dp)
            .width(125.dp)
            .height(50.dp)
    ) {
        RadioButton(
            selected = isSelected(),
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colors.secondary,
                unselectedColor = Color.DarkGray,
                disabledColor = MaterialTheme.colors.secondaryVariant,
            ),
        )
        Text(text, color = MaterialTheme.colors.secondary)
    }
}

@Composable
fun NetworkDivider() {
    Divider(
        modifier = Modifier
            .padding(10.dp)
            .height(2.dp)
            .width(500.dp),
        color = Color.DarkGray,
    )
}

@Preview(showBackground = true)
@Composable
fun ExpertModeViewPreview() {
    SatodimeTheme {
        ExpertModeView(rememberNavController(), viewModel(factory = SharedViewModel.Factory),1, Coin.BTC.name)
    }
}
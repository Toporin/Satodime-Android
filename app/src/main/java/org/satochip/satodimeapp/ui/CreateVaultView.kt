package org.satochip.satodimeapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
import org.satochip.satodimeapp.data.Coin
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import java.security.SecureRandom
import kotlin.time.Duration.Companion.seconds

private const val TAG = "CreateVaultView"

@Composable
fun CreateVaultView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int,
    selectedCoinName: String
) { // todo change order
    val context = LocalContext.current
    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
    val isReadyToNavigate = remember { mutableStateOf(false) }// for auto navigation to next view
    val selectedCoin = Coin.valueOf(selectedCoinName)
    // todo display vault index in view!

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primaryVariant)
            .padding(10.dp)
    ) {
        HeaderRow(
            onClick = {
                navController.navigateUp()
            },
            titleText = R.string.createYourVault,
            message = R.string.youAreAboutToCreateAndSeal
        )
        CoinDisplay(coin = selectedCoin)
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
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondary,
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.onceTheVaultHasBeengenerated) // todo support markdown
//                buildAnnotatedString {
//                    append(stringResource(R.string.once_the))
//                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                        append(stringResource(R.string.vault))
//                    }
//                    append(stringResource(R.string.has_been_generated_the_corresponding))
//                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                        append(stringResource(R.string.private_keys))
//                    }
//                    append(stringResource(R.string.is_hidden_in_the))
//                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                        append(stringResource(R.string.satodime_chip_s_memory))
//                    }
//                    append(".")
//                }
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(10.dp)
                .width(250.dp)
                .height(75.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate(
                        SatodimeScreen.ExpertMode.name +
                                "/$selectedCoinName/$selectedVault"
                    )
                },
                modifier = Modifier
                    .padding(10.dp)
                    .height(40.dp)
                    .width(250.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LightGray,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.activateTheExpertMode))
            }
        }
        Spacer(Modifier.weight(1f))
//        val sealFailureText = stringResource(R.string.seal_failure)
//        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
//        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        BottomButton(
            onClick = {
                // generate entropy based on current time
                val random = SecureRandom()
                var entropyBytes = ByteArray(32)
                random.nextBytes(entropyBytes)

                // scan card
                SatoLog.d(TAG, "CreateVaultView: clicked on create button!")
                SatoLog.d(TAG, "CreateVaultView: selectedVault: $selectedVault")
                SatoLog.d(TAG, "CreateVaultView: selectedCoinName: $selectedCoinName")
                showNfcDialog.value = true // NfcDialog
                isReadyToNavigate.value = true
                sharedViewModel.sealSlot(
                    context as Activity,
                    index = selectedVault - 1,
                    coinSymbol = selectedCoinName,
                    isTestnet = false,
                    entropyBytes = entropyBytes
                )
            },
            text = stringResource(R.string.createAndSeal)
        )

        // CANCEL BUTTON
        val toastMsg = stringResource(R.string.actionCancelled)
        Button(
            onClick = {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show() // todo translate
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
    if (showNfcDialog.value) {
        NfcDialog(
            openDialogCustom = showNfcDialog,
            resultCodeLive = sharedViewModel.resultCodeLive,
            isConnected = sharedViewModel.isCardConnected
        )
    }

    // auto-navigate when action is performed successfully
    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog) {
        SatoLog.d(TAG, "CreateVaultView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
        while (sharedViewModel.resultCodeLive != NfcResultCode.Ok
            || isReadyToNavigate.value == false
            || showNfcDialog.value
        ) {
            SatoLog.d(
                TAG,
                "CreateVaultView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}"
            )
            delay(1.seconds)
        }
        // navigate
        SatoLog.d(TAG, "CreateVaultView navigating to CongratsVaultCreated view")
        navController.navigate(SatodimeScreen.CongratsVaultCreated.name + "/$selectedCoinName") {
            popUpTo(0)
        }
    }

}

@Composable
fun CoinDisplay(coin: Coin) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            text = coin.name,
            modifier = Modifier.padding(10.dp)
        )
        Image(
            painter = painterResource(id = coin.painterResourceId),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateVaultViewPreview() {
    SatodimeTheme {
        CreateVaultView(
            rememberNavController(),
            viewModel(factory = SharedViewModel.Factory),
            3,
            Coin.BTC.name
        )
    }
}
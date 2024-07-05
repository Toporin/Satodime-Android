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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
    val scrollState = rememberScrollState()

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
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
    }

    // NfcDialog
    if (showNfcDialog.value) {
        NfcDialog(
            openDialogCustom = showNfcDialog,
            resultCodeLive = sharedViewModel.resultCodeLive,
            isConnected = sharedViewModel.isCardConnected
        )
    }

    if (isReadyToNavigate.value && sharedViewModel.resultCodeLive == NfcResultCode.SealVaultSuccess && !showNfcDialog.value) {
        SatoLog.d(TAG, "CreateVaultView navigating to CongratsVaultCreated view")
        navController.popBackStack()
        navController.navigate(SatodimeScreen.CongratsVaultCreated.name + "/$selectedCoinName")
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
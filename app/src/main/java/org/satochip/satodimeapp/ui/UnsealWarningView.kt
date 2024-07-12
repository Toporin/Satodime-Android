package org.satochip.satodimeapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.NfcResultCode
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.RedGradientBackground
import org.satochip.satodimeapp.ui.components.VaultCard
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "UnsealWarningView"

@Composable
fun UnsealWarningView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int
) {
    val context = LocalContext.current
    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
    val isReadyToNavigate = remember { mutableStateOf(false) }// for auto navigation to next view
    val scrollState = rememberScrollState()
    val satodimeUnclaimed = stringResource(R.string.satodimeUnclaimed)

    val vaults = sharedViewModel.cardVaults
    val vaultsSize = vaults?.size ?: 0
    if (selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!

    RedGradientBackground()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        HeaderRow(
            onClick = {
                navController.navigateUp()
            },
            titleText = R.string.warning
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(20.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondaryVariant,
                text = stringResource(R.string.youAreAboutToUnseal)
            )
            VaultCard(
                index = selectedVault,
                isSelected = true,
                vault = vault,
            )
            Text(
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.unsealingThisCryptoVaultWillReveal) // todo markdown
            )
            Text(
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.youCanThenTransferTheEntireBalance)
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
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.thisActionIsIrreversible)
                )
            }
            Spacer(Modifier.weight(1f))
            BottomButton(
                onClick = {
                    if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                        Toast.makeText(context, satodimeUnclaimed, Toast.LENGTH_SHORT).show()
                        return@BottomButton
                    }
                    // scan card
                    SatoLog.d(
                        TAG,
                        "UnsealWarningView: clicked on unseal button for selectedVault $selectedVault"
                    )
                    showNfcDialog.value = true // NfcDialog
                    isReadyToNavigate.value = true
                    sharedViewModel.unsealSlot(context as Activity, selectedVault - 1)
                },
                color = Color.Red,
                text = stringResource(R.string.unseal)
            )

            // CANCEL BUTTON
            val toastMsg = stringResource(R.string.actionCancelled)
            Button(
                onClick = {
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
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

    if (isReadyToNavigate.value && sharedViewModel.resultCodeLive == NfcResultCode.UnsealVaultSuccess && !showNfcDialog.value) {
        SatoLog.d(TAG, "UnsealWarningView navigating to UnsealCongrats view")
        navController.popBackStack()
        navController.navigate(SatodimeScreen.UnsealCongrats.name + "/$selectedVault")
    }
}

@Preview(showBackground = true)
@Composable
fun UnsealWarningViewPreview() {
    SatodimeTheme {
        UnsealWarningView(rememberNavController(), viewModel(factory = SharedViewModel.Factory), 1)
    }
}
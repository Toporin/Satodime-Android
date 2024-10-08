package org.satochip.satodimeapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Divider
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
import org.satochip.satodimeapp.ui.components.EmptyVaultCard
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.RedGradientBackground
import org.satochip.satodimeapp.ui.components.VaultCard
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "ResetWarningView"

@Composable
fun ResetWarningView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {
    val context = LocalContext.current
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog
    val isBackupConfirmed = remember { mutableStateOf(false) }
    val isReadyToNavigate = remember{ mutableStateOf(false) }// for auto navigation to next view
    val scrollState = rememberScrollState()
    val satodimeUnclaimed = stringResource(R.string.satodimeUnclaimed)

    val vaults = sharedViewModel.cardVaults

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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(R.string.youAreAboutToReset)
            )
            if (vaults?.get(selectedVault - 1) != null) {
                VaultCard(
                    index = selectedVault,
                    isSelected = true,
                    vault = vaults[selectedVault - 1]!!,
                )
            } else {
                EmptyVaultCard(index = selectedVault, isFirstEmptyVault = true) {
                    navController.navigate(SatodimeScreen.SelectBlockchain.name + "/$selectedVault")
                }
            }
            Text(
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.resettingThisCryptoVaultWill)
                // TODO add markdown support
            )
            Divider(
                modifier = Modifier
                    .padding(10.dp)
                    .height(2.dp)
                    .width(100.dp),
                color = Color.DarkGray,
            )
            Text(
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.afterThatYouWillBeAbleTo) // todo markdown support
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(10.dp)
                    .width(400.dp)
                    .height(75.dp)
                    .clickable {
                        isBackupConfirmed.value = !isBackupConfirmed.value
                    }
            ) {
                Checkbox(
                    colors = CheckboxDefaults.colors(Color.LightGray, Color.LightGray),
                    checked = isBackupConfirmed.value,
                    onCheckedChange = {
                        isBackupConfirmed.value = !isBackupConfirmed.value
                    }
                )
                Text(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.iConfirmThatBackup)
                )
            }
            Spacer(Modifier.weight(1f))
            val pleaseConfirmBackupText = stringResource(R.string.pleaseConfirmYouHaveMadeBackup)
            BottomButton(
                onClick = {
                    if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                        Toast.makeText(context, satodimeUnclaimed, Toast.LENGTH_SHORT).show()
                        return@BottomButton
                    }
                    // scan card
                    if (isBackupConfirmed.value) {
                        SatoLog.d(
                            TAG,
                            "ResetWarningView: clicked on reset button for selectedVault $selectedVault"
                        )
                        showNfcDialog.value = true // NfcDialog
                        isReadyToNavigate.value =
                            true // ready to navigate to next view once action is done
                        sharedViewModel.resetSlot(context as Activity, selectedVault - 1)
                    } else {
                        Toast.makeText(context, pleaseConfirmBackupText, Toast.LENGTH_SHORT).show()
                    }

                },
                //width = 240.dp,
                color = Color.Red,
                text = stringResource(R.string.resetBtn)
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
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

    if (isReadyToNavigate.value && sharedViewModel.resultCodeLive == NfcResultCode.ResetVaultSuccess && !showNfcDialog.value) {
        SatoLog.d(TAG, "ResetWarningView navigating to ResetCongratsView")
        navController.popBackStack()
        navController.navigate(SatodimeScreen.ResetCongratsView.name + "/$selectedVault")
    }
}

@Preview(showBackground = true)
@Composable
fun ResetWarningViewPreview() {
    SatodimeTheme {
        ResetWarningView(rememberNavController(), viewModel(factory = SharedViewModel.Factory),1)
    }
}
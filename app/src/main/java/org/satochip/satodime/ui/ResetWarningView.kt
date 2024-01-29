package org.satochip.satodime.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.satochip.satodime.R
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.EmptyVaultCard
import org.satochip.satodime.ui.components.NfcDialog
import org.satochip.satodime.ui.components.RedGradientBackground
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.components.VaultCard
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen
import org.satochip.satodime.viewmodels.SharedViewModel
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ResetWarningView"

@Composable
fun ResetWarningView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {
    val context = LocalContext.current
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog
    val isBackupConfirmed = remember { mutableStateOf(false) }
    val isReadyToNavigate = remember{ mutableStateOf(false) }// for auto navigation to next view

    val vaults = sharedViewModel.cardVaults.value
    val vaultsSize = vaults?.size ?: 0
    if(selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) {
        Log.e(TAG, "ResetWarningView VAULT IS NULL!!")
        //return
    }

    RedGradientBackground()
    TopLeftBackButton(navController)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.warning)
        )
        Text(
            modifier = Modifier
                .padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.you_are_about_to_reset_the_following_crypto_vault)
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
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.reset_cap))
                }
                append(stringResource(R.string.this_vault_will_completely_and_irrevocably))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.delete))
                }
                append(stringResource(R.string.the_corresponding))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.private_keys))
                }
                append(stringResource(R.string.from_your))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.satodime_device))
                }
                append(".")
            }
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
            text = buildAnnotatedString {
                append(stringResource(R.string.after_that_you_will_be_able_to))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.create_a_new_crypto_vault))
                }
                append(".")
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(10.dp)
                .width(400.dp)
                .height(75.dp)
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
                text = stringResource(R.string.i_confirm_that_i_have_made_a_backup_of_the_corresponding_private_key)
            )
        }
        Spacer(Modifier.weight(1f))
        val resetFailureText = stringResource(R.string.reset_failure)
        val cardLoadingText = stringResource(R.string.card_loading_please_try_again_in_few_seconds)
        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        val pleaseConfirmBackupText = stringResource(R.string.please_confirm_that_you_have_made_a_backup)
        BottomButton(
            onClick = {

                // scan card
                if (isBackupConfirmed.value) {
                    Log.d(TAG, "ResetWarningView: clicked on reset button!")
                    showNfcDialog.value = true // NfcDialog
                    isReadyToNavigate.value = true // ready to navigate to next view once action is done
                    sharedViewModel.resetSlot(context as Activity, selectedVault - 1)
//                    if (sharedViewModel.resultCodeLive == NfcResultCode.Ok) {
//                        Log.d(TAG, "ResetWarningView: successfully reset slot ${selectedVault - 1}")
//                        // wait until NfcDialog has closed
//                        if (showNfcDialog.value == false) {
//                            Log.d(TAG, "ResetWarningView navigating to ResetCongrats view")
//                            navController.navigate(
//                                SatodimeScreen.ResetCongratsView.name + "/$selectedVault"
//                            ) {
//                                popUpTo(0)
//                            }
//                        }
//                    }
                }

//                if (isBackupConfirmed.value) {
//                    if (NFCCardService.isConnected.value == true) {
//                        if (NFCCardService.isOwner()) {
//                            if (NFCCardService.isReadingFinished.value != true) {
//                                Toast.makeText(context, cardLoadingText, Toast.LENGTH_SHORT).show()
//                            } else if (NFCCardService.resetOld(selectedVault - 1)) {
//                                navController.navigate(
//                                    SatodimeScreen.ResetCongratsView.name + "/$selectedVault"
//                                ) {
//                                    popUpTo(0)
//                                }
//                            } else {
//                                Toast.makeText(context, resetFailureText, Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            Toast.makeText(context, youreNotTheOwnerText, Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(context, pleaseConnectTheCardText, Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(context, pleaseConfirmBackupText, Toast.LENGTH_SHORT).show()
//                }
            },
            color = Color.Red,
            text = stringResource(R.string.reset_the_vault)
        )
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

//    if (sharedViewModel.resultCodeLive == NfcResultCode.Ok
//        && isReadyToNavigate.value
//        && !showNfcDialog.value) {
//        // navigate
//        Log.d(TAG, "ResetWarningView navigating to ResetCongratsView!")
//        navController.navigate(SatodimeScreen.ResetCongratsView.name + "/$selectedVault")
////        {
////            popUpTo(0)
////        }
//    }


    // auto-navigate when action is performed successfully
    // todo improve?
    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog, isReadyToNavigate) {
        Log.d(TAG, "ResetWarningView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
        //delay(1.seconds)
        //Log.d(TAG, "ResetWarningView LaunchedEffect START after 1s ${sharedViewModel.resultCodeLive}")
        while (sharedViewModel.resultCodeLive != NfcResultCode.Ok
            || isReadyToNavigate.value == false
            || showNfcDialog.value) {
            Log.d(TAG, "ResetWarningView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}")
            delay(1.seconds)
        }
        //Log.d(TAG, "ResetWarningView LaunchedEffect after while delay 1s ${sharedViewModel.resultCodeLive}")
        //delay(1.seconds)
        // navigate
        Log.d(TAG, "ResetWarningView navigating to ResetCongratsView")
        navController.navigate(SatodimeScreen.ResetCongratsView.name + "/$selectedVault") {
            popUpTo(0)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ResetWarningViewPreview() {
    SatodimeTheme {
        ResetWarningView(rememberNavController(), viewModel(factory = SharedViewModel.Factory),1)
    }
}
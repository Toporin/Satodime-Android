package org.satochip.satodimeapp.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.RedGradientBackground
import org.satochip.satodimeapp.ui.components.VaultCard
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.NavigationParam
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.webviewActivityIntent
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ShowPrivateKeyView"

private enum class RequestedPrivkeyType {
    LEGACY, WIF, ENTROPY, NONE
}

@Composable
fun ShowPrivateKeyView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog

    val vaults = sharedViewModel.cardVaults
    val vaultsSize = vaults?.size ?: 0
    if (selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!
    val requestedPrivkeyType = remember{ mutableStateOf(RequestedPrivkeyType.NONE) }
    val satodimeUnclaimed = stringResource(R.string.satodimeUnclaimed)

    RedGradientBackground()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(state = scrollState)
    ) {
        HeaderRow(
            onClick = {
                navController.popBackStack()
                navController.navigateUp()
            },
            titleText = R.string.showPrivateKey,
        )
        VaultCard(index = selectedVault, true, vault = vault)

        // LEGACY FORMAT
        PrivateKeyItem(
            title = R.string.showPrivateKeyLegacy,
            icon = R.drawable.arrow_right_circle
        ) {
            if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                Toast.makeText(context, satodimeUnclaimed, Toast.LENGTH_SHORT).show()
                return@PrivateKeyItem
            }
            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]

            if (privkey == null) {
                // recover privkey
                SatoLog.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.LEGACY
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
            }

            if (privkey != null){
                SatoLog.d(TAG, "ShowPrivateKeyView privkey readily available")
                SatoLog.d(TAG, "ShowPrivateKeyView navigating to ShowPrivateKeyData view")
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/legacy"
                            + "?${NavigationParam.Data.name}=${privkey.privkeyHex}"
                )
            }
        }

        // WIF FORMAT
        PrivateKeyItem(
            title = R.string.showPrivateKeyWIF,
            icon = R.drawable.arrow_right_circle
        ) {
            if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                Toast.makeText(context, satodimeUnclaimed, Toast.LENGTH_SHORT).show()
                return@PrivateKeyItem
            }
            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]
            if (privkey == null) {
                // recover privkey
                SatoLog.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.WIF
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
                if (sharedViewModel.resultCodeLive == NfcResultCode.RecoverPrivkeySuccess) {
                    SatoLog.d(TAG, "ShowPrivateKeyView: successfully recovered privkey for slot ${selectedVault -1}")
                }
            }

            if (privkey != null) {
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/wif"
                            + "?${NavigationParam.Data.name}=${privkey.privkeyWif}"
                )
            }
        }

        // ENTROPY
        PrivateKeyItem(
            title = R.string.showEntropy,
            icon = R.drawable.arrow_right_circle
        ) {
            if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                Toast.makeText(context, satodimeUnclaimed, Toast.LENGTH_SHORT).show()
                return@PrivateKeyItem
            }
            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]
            if (privkey == null) {
                // recover privkey
                SatoLog.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.ENTROPY
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
                if (sharedViewModel.resultCodeLive == NfcResultCode.RecoverPrivkeySuccess) {
                    SatoLog.d(TAG, "ShowPrivateKeyView: successfully recovered privkey for slot ${selectedVault -1}")
                    // todo something?
                }
            }

            if (privkey != null) {
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/entropy"
                            + "?${NavigationParam.Data.name}=${privkey.entropyHex}"
                )
            }
        }

        // HELP LINK
        PrivateKeyItem(
            title = R.string.howDoiExportPrivateKey,
            icon = R.drawable.outlined_info
        ) {
            webviewActivityIntent(
                url = "https://satochip.io/satodime-how-to-export-private-key/",
                context = context
            )
        }
        Spacer(Modifier.weight(1f))
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

    // auto-navigate to privkey when action is performed successfully
    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog, requestedPrivkeyType) {
        SatoLog.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
//        SatoLog.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${showNfcDialog.value}")
//        SatoLog.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${requestedPrivkeyType.value}")
        while (sharedViewModel.resultCodeLive != NfcResultCode.RecoverPrivkeySuccess
            || requestedPrivkeyType.value == RequestedPrivkeyType.NONE
            || showNfcDialog.value) {
            SatoLog.d(TAG, "ShowPrivateKeyView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}")
            delay(1.seconds)
        }
        // navigate
        SatoLog.d(TAG, "ShowPrivateKeyView navigating to ShowPrivateKeyData view ${sharedViewModel.resultCodeLive}")
//        SatoLog.d(TAG, "ShowPrivateKeyView selectedVault: $selectedVault")
//        SatoLog.d(TAG, "ShowPrivateKeyView requestedPrivkeyType: ${requestedPrivkeyType.value}")
        when (requestedPrivkeyType.value){
            RequestedPrivkeyType.LEGACY -> {
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/legacy"
                            + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyHex}"
                )
            }
            RequestedPrivkeyType.WIF -> {
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/wif"
                            + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyWif}"
                )
            }
            RequestedPrivkeyType.ENTROPY -> {
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/entropy"
                            + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.entropyHex}"
                )
            }
            RequestedPrivkeyType.NONE -> {
                // do nothing...
            }
        }
    }

}

@Composable
fun PrivateKeyItem(
    title: Int,
    icon: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = 5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .clickable {
                    onClick()
                }
                .padding(10.dp)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                modifier = Modifier.padding(5.dp),
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(id = title)
            )
            Spacer(Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
//                painter = painterResource(id = R.drawable.arrow_right_circle),
                painter = painterResource(id = icon),
                tint = MaterialTheme.colors.secondary,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowPrivateKeyViewPreview() {
    SatodimeTheme {
        ShowPrivateKeyView(rememberNavController(), viewModel(factory = SharedViewModel.Factory),1)
    }
}
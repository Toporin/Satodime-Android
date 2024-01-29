package org.satochip.satodime.ui

import android.app.Activity
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import kotlinx.coroutines.runBlocking
import org.satochip.satodime.R
import org.satochip.satodime.data.NfcResultCode
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.NfcDialog
import org.satochip.satodime.ui.components.RedGradientBackground
import org.satochip.satodime.ui.components.VaultCard
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.NavigationParam
import org.satochip.satodime.util.SatodimeScreen
import org.satochip.satodime.viewmodels.SharedViewModel
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ShowPrivateKeyView"

private enum class RequestedPrivkeyType {
    LEGACY, WIF, ENTROPY, NONE
}

@Composable
fun ShowPrivateKeyView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {
    val context = LocalContext.current
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog

    val vaults = sharedViewModel.cardVaults.value
    val vaultsSize = vaults?.size ?: 0
    if(selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!
    val requestedPrivkeyType = remember{ mutableStateOf(RequestedPrivkeyType.NONE) }

    RedGradientBackground()
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
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.show_private_key)
        )
        VaultCard(index = selectedVault, true, vault = vault)

        // LEGACY FORMAT
        PrivateKeyItem(stringResource(R.string.show_private_key_legacy)) {

            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]

            if (privkey == null) {
                // recover privkey
                Log.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.LEGACY
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
                if (sharedViewModel.resultCodeLive == NfcResultCode.Ok) {
                    Log.d(TAG, "ShowPrivateKeyView: successfully recovered privkey for slot ${selectedVault -1}")
//                    navController.navigate(
//                        SatodimeScreen.ShowPrivateKeyData.name
//                                + "/$selectedVault"
//                                + "/legacy"
//                                + "?${NavigationParam.Data.name}=${privkey?.privkeyHex}"
//                    )
                }
            }

            // todo: the ShowPrivateKeyData screen is not shown automatically after privkey recovery...
            if (privkey != null){
                Log.d(TAG, "ShowPrivateKeyView privkey readily available")
                Log.d(TAG, "ShowPrivateKeyView navigating to ShowPrivateKeyData view")
                navController.navigate(
                    SatodimeScreen.ShowPrivateKeyData.name
                            + "/$selectedVault"
                            + "/legacy"
                            + "?${NavigationParam.Data.name}=${privkey.privkeyHex}"
                )
            }
        }

        // WIF FORMAT
        PrivateKeyItem(stringResource(R.string.show_private_key_wif)) {
            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]
            if (privkey == null) {
                // recover privkey
                Log.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.WIF
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
                if (sharedViewModel.resultCodeLive == NfcResultCode.Ok) {
                    Log.d(TAG, "ShowPrivateKeyView: successfully recovered privkey for slot ${selectedVault -1}")
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
        PrivateKeyItem(stringResource(R.string.show_entropy)) {
            var privkey = sharedViewModel.cardPrivkeys[selectedVault - 1]
            if (privkey == null) {
                // recover privkey
                Log.d(TAG, "ShowPrivateKeyView: privkey NOT available")
                requestedPrivkeyType.value = RequestedPrivkeyType.ENTROPY
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.recoverSlotPrivkey(context as Activity, selectedVault - 1)
                if (sharedViewModel.resultCodeLive == NfcResultCode.Ok) {
                    Log.d(TAG, "ShowPrivateKeyView: successfully recovered privkey for slot ${selectedVault -1}")
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
        PrivateKeyHelpItem()
        Spacer(Modifier.weight(1f))

        // BACK TO VAULTS
        BottomButton(
            onClick = {
                navController.navigate(SatodimeScreen.Vaults.name) {
                    popUpTo(0)
                }
            },
            width = 200.dp,
            text = stringResource(R.string.back_to_my_vaults)
        )
    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

    // auto-navigate to privkey when action is performed successfully
    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog, requestedPrivkeyType) {
    //LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog) {
        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${showNfcDialog.value}")
        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${requestedPrivkeyType.value}")
        //sharedViewModel.resultCodeLive = NfcResultCode.Busy // avoid to trigger launch effect directly
        //delay(1.seconds)
        while (sharedViewModel.resultCodeLive != NfcResultCode.Ok
            || requestedPrivkeyType.value == RequestedPrivkeyType.NONE
            || showNfcDialog.value) {
            Log.d(TAG, "ShowPrivateKeyView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}")
            delay(1.seconds)
        }
        //Log.d(TAG, "ShowPrivateKeyView LaunchedEffect after while delay 1s ${sharedViewModel.resultCodeLive}")
        //delay(1.seconds)
        // navigate
        Log.d(TAG, "ShowPrivateKeyView navigating to ShowPrivateKeyData view ${sharedViewModel.resultCodeLive}")
        Log.d(TAG, "ShowPrivateKeyView selectedVault: $selectedVault")
        Log.d(TAG, "ShowPrivateKeyView requestedPrivkeyType: ${requestedPrivkeyType.value}")
        Log.d(TAG, "ShowPrivateKeyView data: ${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyHex}")
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

//    LaunchedEffect(sharedViewModel.resultCodeLive, showNfcDialog, requestedPrivkeyType) {
//        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${sharedViewModel.resultCodeLive}")
//        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${showNfcDialog.value}")
//        Log.d(TAG, "ShowPrivateKeyView LaunchedEffect START ${requestedPrivkeyType.value}")
//        delay(1.seconds)
//        if (sharedViewModel.resultCodeLive == NfcResultCode.Ok
//            && requestedPrivkeyType.value != RequestedPrivkeyType.NONE
//            && showNfcDialog.value == false) {
//            Log.d(TAG,"ShowPrivateKeyView LaunchedEffect in while delay 1s ${sharedViewModel.resultCodeLive}")
//
//            delay(1.seconds)
//            // navigate
//            Log.d(TAG,"ShowPrivateKeyView navigating to ShowPrivateKeyData view ${sharedViewModel.resultCodeLive}")
//            Log.d(TAG, "ShowPrivateKeyView selectedVault: $selectedVault")
//            Log.d(TAG, "ShowPrivateKeyView requestedPrivkeyType: ${requestedPrivkeyType.value}")
//            Log.d(TAG, "ShowPrivateKeyView data: ${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyHex}")
//            when (requestedPrivkeyType.value) {
//                RequestedPrivkeyType.LEGACY -> {
//                    navController.navigate(
//                        SatodimeScreen.ShowPrivateKeyData.name
//                                + "/$selectedVault"
//                                + "/legacy"
//                                + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyHex}"
//                    )
//                }
//
//                RequestedPrivkeyType.WIF -> {
//                    navController.navigate(
//                        SatodimeScreen.ShowPrivateKeyData.name
//                                + "/$selectedVault"
//                                + "/wif"
//                                + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.privkeyWif}"
//                    )
//                }
//
//                RequestedPrivkeyType.ENTROPY -> {
//                    navController.navigate(
//                        SatodimeScreen.ShowPrivateKeyData.name
//                                + "/$selectedVault"
//                                + "/entropy"
//                                + "?${NavigationParam.Data.name}=${sharedViewModel.cardPrivkeys[selectedVault - 1]?.entropyHex}"
//                    )
//                }
//
//                RequestedPrivkeyType.NONE -> {
//                    // do nothing...
//                }
//            }
//        }
//    }
}

@Composable
fun PrivateKeyItem(title: String, onSelect: () -> Unit) {
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
                .padding(10.dp)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                modifier = Modifier.padding(5.dp),
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = title
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onSelect
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = Color.LightGray,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun PrivateKeyHelpItem() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = 5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .padding(10.dp)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                modifier = Modifier.padding(5.dp),
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(R.string.how_do_i_export_my_private_key)
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = {
                    uriHandler.openUri("https://satochip.io/satodime-how-to-export-private-key/")
                }
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Outlined.Info,
                    tint = Color.LightGray,
                    contentDescription = null
                )
            }
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
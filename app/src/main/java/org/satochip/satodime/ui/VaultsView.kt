package org.satochip.satodime.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.ui.components.DarkBlueGradientBackground
import org.satochip.satodime.ui.components.EmptyVaultCard
import org.satochip.satodime.ui.components.NfcDialog
import org.satochip.satodime.ui.components.RedGradientBackground
import org.satochip.satodime.ui.components.VaultCard
import org.satochip.satodime.ui.theme.DarkRed
import org.satochip.satodime.ui.theme.LightDarkBlue
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen
import org.satochip.satodime.util.getBalance
import org.satochip.satodime.viewmodels.SharedViewModel
import org.satochip.satodime.viewmodels.VaultsViewModel

private const val TAG = "VaultsView"

@Composable
fun VaultsView(navController: NavController, viewModel: VaultsViewModel, sharedViewModel: SharedViewModel) {
    // TODO: remove viewModel
    val activity = LocalContext.current as Activity
    var showVaultsOnly by remember{mutableStateOf(false) }
    val showNfcDialog = remember{ mutableStateOf(false) } // for NfcDialog

    //val vaults = viewModel.vaults.collectAsState(initial = listOf(null, null, null)) // TODO deprecate
    val vaults = sharedViewModel.cardVaults.value
    val vaultsSize = vaults?.size ?: 0
    val vaultsListState = rememberLazyListState()
    val visibleItems by remember { derivedStateOf { vaultsListState.layoutInfo.visibleItemsInfo } }
    val configuration = LocalConfiguration.current
    sharedViewModel.selectedVault = findVaultToSelect(visibleItems, configuration.screenWidthDp)

//    if (vaults.value.isEmpty() || vaults.value[viewModel.selectedVault - 1] == null || vaults.value[viewModel.selectedVault - 1]!!.isSealed) {
//        DarkBlueGradientBackground()
//    } else {
//        RedGradientBackground()
//    }
    if (sharedViewModel.selectedVault > vaultsSize || vaults?.get(sharedViewModel.selectedVault - 1) == null || vaults[sharedViewModel.selectedVault - 1]!!.isSealed) {
        DarkBlueGradientBackground()
    } else {
        RedGradientBackground()
    }

    // RED/GREEN DOT (TO REMOVE!)
    Canvas(modifier = Modifier
        .padding(15.dp)
        .size(5.dp), onDraw = {
        drawCircle(color = if (sharedViewModel.isCardConnected) Color.Green else Color.Red)
    })
    // LOGO
    // TODO: BUTTON to CardAuth + color
    Image(
        painter = painterResource(if (isSystemInDarkTheme()) R.drawable.top_left_logo else R.drawable.top_left_logo_light),
        contentDescription = null,
        modifier = Modifier
            .size(45.dp)
            .offset(x = 20.dp, y = 20.dp),
        contentScale = ContentScale.Crop
    )
    // RESCAN + SWITCH + MENU
    Row(modifier = Modifier.padding(top = 20.dp, end = 5.dp)) {
        Spacer(modifier = Modifier.weight(1f))
        // RESCAN
        IconButton(onClick = {
            showNfcDialog.value = true // NfcDialog
            sharedViewModel.scanCard(activity)
        }) {
            Icon(Icons.Default.Loop, "", tint = MaterialTheme.colors.secondary)
        }
        // SWITCH VIEW
        CustomSwitch(checked = showVaultsOnly) {
            showVaultsOnly = it
        }
        // MENU
        IconButton(onClick = { navController.navigate(SatodimeScreen.MenuView.name) }) {
            Icon(Icons.Default.MoreVert, "", tint = MaterialTheme.colors.secondary)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            // TITLE
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(R.string.vaults),
            )
        }
        val onAddFunds = {
            if(vaults?.get(sharedViewModel.selectedVault - 1) != null) {
                navController.navigate(
                    SatodimeScreen.AddFunds.name + "/${sharedViewModel.selectedVault}"
                )
            }
        }
        val onUnseal = {
            if(vaults?.get(sharedViewModel.selectedVault - 1) != null) {
                navController.navigate(
                    SatodimeScreen.UnsealWarning.name + "/${sharedViewModel.selectedVault}"
                )
            }
        }
        val onAddVault = { index: Int ->
            navController.navigate(
                SatodimeScreen.SelectBlockchain.name
                        + "/${index}"
            )
        }
        val onShowKey = {
            navController.navigate(
                SatodimeScreen.ShowPrivateKey.name + "/${sharedViewModel.selectedVault}"
            )
        }
        val onReset = {
            navController.navigate(
                SatodimeScreen.ResetWarningView.name + "/${sharedViewModel.selectedVault}"
            )
        }

//        Text("isVaultDataAvailable: ${sharedViewModel.isVaultDataAvailable.value}")
//        Text("isVaultDataAvailable2: ${sharedViewModel.isVaultDataAvailable2}")
//        Text("isVaultDataAvailable3: ${sharedViewModel.isVaultDataAvailable3}")

        if (sharedViewModel.isCardDataAvailable) {
        //if (NFCCardService.isCardDataAvailable.value == true) {
            //val vaultsWithDefaultsValuesIfEmpty = vaults.ifEmpty { listOf(null, null, null) }
            val cardVaultsWithDefaultsValuesIfEmpty = vaults?.ifEmpty { listOf(null, null, null) } ?: listOf(null, null, null)
            //val cardVaultsWithDefaultsValuesIfEmpty = sharedViewModel.cardVaults.value ?: listOf(null, null, null)
            //val cardVaultsWithDefaultsValuesIfEmpty = if (viewModel.cardVaults.isEmpty()) listOf(null, null, null) else {viewModel.cardVaults} //?: listOf(null, null, null)
            //val cardVaultsWithDefaultsValuesIfEmpty = viewModel.cardVaults.ifEmpty { listOf(null, null, null) }

            if (showVaultsOnly) {
                VaultsListView(cardVaultsWithDefaultsValuesIfEmpty, sharedViewModel.selectedVault, onAddVault)
            } else {
                DetailedVaultView(
                    vaultsListState,
                    cardVaultsWithDefaultsValuesIfEmpty,
                    sharedViewModel.selectedVault,
                    onAddFunds,
                    onUnseal,
                    onAddVault,
                    onShowKey,
                    onReset
                )
            }
        } else {
            // DEBUG
            //val activity = LocalContext.current as Activity
            Button(onClick = {
                Log.d("VaultsView", "Clicked on Scan button!")
                // scan card
                showNfcDialog.value = true // NfcDialog
                sharedViewModel.scanCard(activity)
            }) {
                Text("Click & Scan")
            }
        }

    }

    // NfcDialog
    if (showNfcDialog.value){
        NfcDialog(openDialogCustom = showNfcDialog, resultCodeLive = sharedViewModel.resultCodeLive, isConnected = sharedViewModel.isCardConnected)
    }

}

/// LIST VIEW

@Composable
fun VaultsListView(
    vaults: List<CardVault?>,
    selectedCard: Int,
    onAddVault: (Int) -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        renderVaults(vaults, selectedCard, onAddVault)
    }
}

/// DETAILED VIEW

@Composable
fun DetailedVaultView(
    vaultsListState: LazyListState,
    vaults: List<CardVault?>,
    selectedCard: Int,
    onAddFunds: () -> Unit, // todo: add Int parameter for vault index?
    onUnseal: () -> Unit, // todo: add Int parameter for vault index?
    onAddVault: (Int) -> Unit,
    onShowKey: () -> Unit, // todo: add Int parameter for vault index?
    onReset: () -> Unit, // todo: add Int parameter for vault index?
) {
    // VAULT CARD
    VaultCards(vaultsListState, vaults, selectedCard, onAddVault)

    // ACTIONS ROW
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(120.dp)
    ) {

        if (selectedCard<=vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state != SlotState.UNINITIALIZED) {

            // ADD FUNDS BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .aspectRatio(1f)
                        .background(LightGreen, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onAddFunds,
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White),
                        contentPadding = PaddingValues(0.dp),  //avoid the little icon
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            backgroundColor = LightGreen
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
                Text(
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = stringResource(R.string.add_funds)
                )
            }
            Divider(
                modifier = Modifier
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 40.dp)
                    .height(30.dp)
                    .width(2.dp),
                color = Color.LightGray,
            )
        }
        if (selectedCard<=vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state == SlotState.SEALED) {
            // UNSEAL BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedButton(
                    onClick = onUnseal,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.Gray),
                    contentPadding = PaddingValues(0.dp),  //avoid the little icon
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray,
                        backgroundColor = LightDarkBlue
                    )
                ) {
                    Image(
                        painter = painterResource(R.drawable.unlock),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        contentScale = ContentScale.FillHeight
                    )
                }
                Text(
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray,
                    text = stringResource(R.string.unseal_cap)
                )
            }
        }
        if (selectedCard<=vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state == SlotState.UNSEALED) {
            // SHOW PRIVKEY BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedButton(
                    onClick = onShowKey,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),  //avoid the little icon
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Image(
                        painter = painterResource(R.drawable.show_key),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                        contentScale = ContentScale.FillHeight
                    )
                }
                Text(
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = stringResource(R.string.show_key)
                )
            }
            Divider(modifier = Modifier
                .padding(10.dp)
                .size(0.dp))
            // RESET BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.Red),
                    contentPadding = PaddingValues(0.dp),  //avoid the little icon
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red,
                        backgroundColor = DarkRed
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Close, contentDescription = null
                    )
                }
                Text(
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = stringResource(R.string.reset_cap)
                )
            }
        }

    }

    if (selectedCard<=vaults.size
        && vaults[selectedCard - 1] != null
        && vaults[selectedCard - 1]!!.state != SlotState.UNINITIALIZED) {
        VaultsViewTabScreen(vaults[selectedCard - 1])
    }
}

@Composable
fun VaultCards(
    vaultsListState: LazyListState,
    vaults: List<CardVault?>,
    selectedCard: Int,
    onAddVault: (Int) -> Unit
) {
    LazyRow(state = vaultsListState) {
        renderVaults(vaults, selectedCard, onAddVault)
    }
}

@Composable
fun VaultsViewTabScreen(vault: CardVault?) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Token", "NFT")
//    val tabs = listOf("Token", "NFT", "History")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(20.dp, 20.dp)
                ),
            selectedTabIndex = tabIndex,
            contentColor = MaterialTheme.colors.secondary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.background(MaterialTheme.colors.primary),
                    text = { Text(title, color = MaterialTheme.colors.secondary) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                )
            }
        }
        when (tabIndex) {
            0 -> vault?.let { VaultsViewToken(it) }
            1 -> Log.d("VaultsView", "1")
//            2 -> VaultsViewHistory()
        }
    }
}

@Composable
fun VaultsViewToken(vault: CardVault) {
    LazyColumn {
        items(1) {//TODO add tokens
            VaultsViewTokenRow(vault)
            Divider(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(start = 20.dp, end = 20.dp),
                thickness = 1.dp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun VaultsViewTokenRow(vault: CardVault) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(MaterialTheme.colors.primary)
            .padding(10.dp)
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Image(
            painter = painterResource(id = vault.coin.painterResourceId),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .size(40.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = vault.displayName
            )
            Text(
                fontSize = 12.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = getBalance(vault)
            )
        }

        Spacer(Modifier.weight(1f))
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = vault.currencyAmount
            )
        }
    }
}

@Composable
fun CustomSwitch(modifier: Modifier = Modifier, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color.White,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.Gray,
        )
    )
}

fun LazyListScope.renderVaults(
    vaults: List<CardVault?>,
    selectedCard: Int,
    onAddVault: (Int) -> Unit
) {
    itemsIndexed(vaults) { index, vault ->
        if(vault != null) {
            VaultCard(index + 1, selectedCard == index + 1, vault)
        } else {
            val isFirstEmptyVault = index == vaults.indexOfFirst { it == null }
            EmptyVaultCard(index + 1, isFirstEmptyVault, onAddVault)
        }

    }
}

fun findVaultToSelect(visibleItems: List<LazyListItemInfo>, screenWidth: Int) : Int {
    if(visibleItems.isEmpty()) return 1

    return if ((visibleItems[0].index == 1 && visibleItems[0].offset < -(screenWidth / 2))
        || visibleItems[0].index == 2
    ) {
        3
    } else if ((visibleItems[0].index == 0 && visibleItems[0].offset < -(screenWidth / 2))
        || visibleItems[0].index == 1
    ) {
        2
    } else {
        1
    }
}

@Preview(showBackground = true)
@Composable
fun VaultsViewPreview() {
    SatodimeTheme {
        VaultsView(
            rememberNavController(),
            viewModel(factory = VaultsViewModel.Factory),
            viewModel(factory = SharedViewModel.Factory)
        )
    }
}
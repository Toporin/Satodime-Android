package org.satochip.satodimeapp.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.satochip.javacryptotools.coins.Asset
import org.satochip.javacryptotools.coins.AssetType
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.AuthenticityStatus
import org.satochip.satodimeapp.data.CardVault
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.data.SlotState
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.DarkBlueGradientBackground
import org.satochip.satodimeapp.ui.components.EmptyVaultCard
import org.satochip.satodimeapp.ui.components.InfoDialog
import org.satochip.satodimeapp.ui.components.NfcDialog
import org.satochip.satodimeapp.ui.components.NftDialog
import org.satochip.satodimeapp.ui.components.RedGradientBackground
import org.satochip.satodimeapp.ui.components.VaultCard
import org.satochip.satodimeapp.ui.components.shared.SatoGradientButton
import org.satochip.satodimeapp.ui.components.shared.SatoRoundButton
import org.satochip.satodimeapp.ui.theme.DarkRed
import org.satochip.satodimeapp.ui.theme.LightBlue
import org.satochip.satodimeapp.ui.theme.LightDarkBlue
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.formatBalance
import org.satochip.satodimeapp.util.sanitizeNftImageUrlString
import org.satochip.satodimeapp.util.webviewActivityIntent
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "VaultsView"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultsView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onClick: (Int) -> Unit
) {
    val activity = LocalContext.current as Activity
    val uriHandler = LocalUriHandler.current
    var showVaultsOnly by remember { mutableStateOf(false) }
    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
    val showNoCardScannedDialog = remember { mutableStateOf(false) }// for NoCardScannedDialog

    // NfcDialog
    if (showNfcDialog.value) {
        NfcDialog(
            openDialogCustom = showNfcDialog,
            resultCodeLive = sharedViewModel.resultCodeLive,
            isConnected = sharedViewModel.isCardConnected
        )
    }

//    val showOwnershipDialog = remember{ mutableStateOf(true) } // for OwnershipDialog
//    val showAuthenticityDialog = remember{ mutableStateOf(true) } // for AuthenticityDialog

    val vaults = sharedViewModel.cardVaults
    val vaultsSize = vaults.size
    val pagerState = rememberPagerState(pageCount = {
        vaults.size
    })
    sharedViewModel.selectedVault = findVaultToSelect(selectedVault = pagerState)

    if (sharedViewModel.selectedVault > vaultsSize
        || vaults?.get(sharedViewModel.selectedVault - 1) == null
        || vaults[sharedViewModel.selectedVault - 1]?.state == SlotState.SEALED
    ) {
        DarkBlueGradientBackground()
    } else {
        RedGradientBackground()
    }

    // HEADER ROW (LOGO - TITLE - ACTION BUTTONS)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 5.dp, start = 20.dp, end = 5.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LOGO
        if (sharedViewModel.authenticityStatus == AuthenticityStatus.Authentic) {
            IconButton(
                onClick = {
                    navController.navigate(
                        //SatodimeScreen.AuthenticCardView.name
                        SatodimeScreen.CardInfoView.name
                    )
                },
            )
            {
                Image(
                    painter = painterResource(R.drawable.ic_sato_small),
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(45.dp), //.size(45.dp)
                    //.offset(x = 20.dp, y = 20.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.Green)
                )
            }
        } else if (sharedViewModel.authenticityStatus == AuthenticityStatus.NotAuthentic) {
            IconButton(
                onClick = {
                    navController.navigate(
                        SatodimeScreen.CardInfoView.name
                    )
                },
            )
            {
                Image(
                    painter = painterResource(R.drawable.ic_sato_small),
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(45.dp), //.size(45.dp)
                    //.offset(x = 20.dp, y = 20.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.Red)
                )
            }
        } else { // no card scanned
            IconButton(
                onClick = {
                    showNoCardScannedDialog.value = true
                },
            )
            {
                Image(
                    painter = painterResource(R.drawable.ic_sato_small),
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(45.dp), //.size(45.dp)
                    //.offset(x = 20.dp, y = 20.dp),
                    contentScale = ContentScale.Crop,
                    //colorFilter = ColorFilter.tint(Color.Yellow)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // TITLE
        Text(
            //modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 38.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.vaults),
        )

        Spacer(modifier = Modifier.weight(1f))

        // ICONS
        Row(
            modifier = Modifier
            //.padding(top = 20.dp, end = 5.dp)
        ) {
            if (sharedViewModel.isCardDataAvailable) {
                // SWITCH VIEW
                CustomSwitch(checked = showVaultsOnly) {
                    showVaultsOnly = it
                }
                // RESCAN BUTTON
                IconButton(onClick = {
                    showNfcDialog.value = true // NfcDialog
                    sharedViewModel.scanCard(activity)
                }) {
                    Icon(Icons.Default.Loop, "", tint = MaterialTheme.colors.secondary)
                }
            }
            // MENU BUTTON
            IconButton(onClick = { navController.navigate(SatodimeScreen.MenuView.name) }) {
                Icon(Icons.Default.MoreVert, "", tint = MaterialTheme.colors.secondary)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .padding(top = 75.dp, bottom = 5.dp, start = 5.dp, end = 5.dp)
    ) {
        val onAddFunds = {
            if (vaults?.get(sharedViewModel.selectedVault - 1) != null) {
                navController.navigate(
                    SatodimeScreen.AddFunds.name + "/${sharedViewModel.selectedVault}"
                )
            }
        }
        val onUnseal = {
            if (vaults?.get(sharedViewModel.selectedVault - 1) != null) {
                navController.navigate(
                    SatodimeScreen.UnsealWarning.name + "/${sharedViewModel.selectedVault}"
                )
            }
        }
        val onAddVault = { index: Int ->
            onClick(index)
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
        val onExplore = {
            if (vaults != null && sharedViewModel.selectedVault <= vaultsSize && vaults[sharedViewModel.selectedVault - 1] != null) {
                val explorerLink =
                    vaults[sharedViewModel.selectedVault - 1]!!.nativeAsset.explorerLink
                uriHandler.openUri(explorerLink)
            }
        }

        if (sharedViewModel.isCardDataAvailable) {
            if (showVaultsOnly) {
                VaultsListView(
                    vaults,
                    sharedViewModel.selectedVault,
                    onAddVault
                )
            } else {
                DetailedVaultView(
                    vaults,
                    pagerState,
                    sharedViewModel.selectedVault,
                    onAddFunds,
                    onExplore,
                    onUnseal,
                    onAddVault,
                    onShowKey,
                    onReset
                )
            }
        } else {
            // SCAN BUTTON
            //Todo improve UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))
                SatoRoundButton(
                    modifier = Modifier,
                    text = R.string.clickAndScan,
                    onClick = {
                        SatoLog.d("VaultsView", "Clicked on Scan button!")
                        // scan card
                        showNfcDialog.value = true // NfcDialog
                        sharedViewModel.scanCard(activity)
                    }
                )
                Spacer(Modifier.weight(1f))
                SatoGradientButton(
                    modifier = Modifier,
                    text = R.string.dontHaveASatodime,
                    onClick = {
                        webviewActivityIntent(
                            url = "https://satochip.io/product/satodime/",
                            context = activity
                        )
                    }
                )
            }
        }

    }

    // no card scanned dialog
    if (showNoCardScannedDialog.value
        && !sharedViewModel.isCardDataAvailable
        && !showNfcDialog.value
    ) {
        InfoDialog(
            openDialogCustom = showNoCardScannedDialog,
            title = stringResource(R.string.cardNeedToBeScannedTitle),
            message = stringResource(R.string.cardNeedToBeScannedMessage),
            isActionButtonVisible = false,
            buttonTitle = "",
            buttonAction = {},
        )
    }

    // Authenticity dialog
    if (sharedViewModel.showAuthenticityDialog.value
        && sharedViewModel.authenticityStatus == AuthenticityStatus.NotAuthentic
        && !showNfcDialog.value
    ) {
        InfoDialog(
            openDialogCustom = sharedViewModel.showAuthenticityDialog,
            title = stringResource(R.string.warning),
            message = stringResource(R.string.notAuthenticText),
            isActionButtonVisible = true,
            buttonTitle = stringResource(R.string.moreInfo),
            buttonAction =
            {
                navController.navigate(SatodimeScreen.CardInfoView.name)
            },
        )
    }

    // Ownership dialog
    if (sharedViewModel.showOwnershipDialog.value
        && sharedViewModel.ownershipStatus == OwnershipStatus.NotOwner
        && !showNfcDialog.value
    ) {
        InfoDialog(
            openDialogCustom = sharedViewModel.showOwnershipDialog,
            title = stringResource(R.string.warning),
            message = stringResource(R.string.ownershipText),
            isActionButtonVisible = true,
            buttonTitle = stringResource(R.string.moreInfo),
            buttonAction =
            {
                webviewActivityIntent(
                    url = "https://satochip.io/satodime-ownership-explained/",
                    context = activity
                )
            },
        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailedVaultView(
    vaults: SnapshotStateList<CardVault?>,
    pagerState: PagerState,
    selectedCard: Int,
    onAddFunds: () -> Unit, // todo: add Int parameter for vault index?
    onExplore: () -> Unit, // todo: add Int parameter for vault index?
    onUnseal: () -> Unit, // todo: add Int parameter for vault index?
    onAddVault: (Int) -> Unit,
    onShowKey: () -> Unit, // todo: add Int parameter for vault index?
    onReset: () -> Unit, // todo: add Int parameter for vault index?
) {
    // VAULT CARD
    VaultCards(pagerState, vaults, selectedCard, onAddVault)

    // ACTIONS ROW
    Row(
        verticalAlignment = Alignment.CenterVertically,
        //horizontalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(120.dp)
    ) {

        if (selectedCard <= vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state != SlotState.UNINITIALIZED
        ) {

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
                    text = stringResource(R.string.addFunds)
                )
            }
//            Divider(
//                modifier = Modifier
//                    .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 40.dp)
//                    .height(30.dp)
//                    .width(2.dp),
//                color = Color.LightGray,
//            )

            // EXPLORE BUTTON
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
                        onClick = onExplore,
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        //border = BorderStroke(2.dp, Color.White),
                        contentPadding = PaddingValues(0.dp),  //avoid the little icon
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            backgroundColor = LightBlue
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.open_in_new_24px),
                            contentDescription = "link to explorer",
                            modifier = Modifier
                                .size(30.dp), //.size(45.dp)
                            //tint = Color.LightGray,
                        )
                    }
                }
                Text(
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = stringResource(R.string.exploreBlockchain)
                )
            }
        }
        if (selectedCard <= vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state == SlotState.SEALED
        ) {
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
                    text = stringResource(R.string.unseal)
                )
            }
        }
        if (selectedCard <= vaults.size
            && vaults[selectedCard - 1] != null
            && vaults[selectedCard - 1]!!.state == SlotState.UNSEALED
        ) {
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
                    text = stringResource(R.string.showKey)
                )
            }
//            Divider(modifier = Modifier
//                .padding(10.dp)
//                .size(0.dp))

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
                    text = stringResource(R.string.resetBtn)
                )
            }
        }

    }

    if (selectedCard <= vaults.size
        && vaults[selectedCard - 1] != null
        && vaults[selectedCard - 1]!!.state != SlotState.UNINITIALIZED
    ) {
        VaultsViewTabScreen(vaults[selectedCard - 1])
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultCards(
    pagerState: PagerState,
    vaults: SnapshotStateList<CardVault?>,
    selectedCard: Int,
    onAddVault: (Int) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(10.dp),
        pageSize = PageSize.Fixed(300.dp)
    ) { page ->
        val cardVault = vaults.getOrNull(page)
        cardVault?.let {
            VaultCard(page + 1, selectedCard == page + 1, cardVault)
        } ?: run {
            val isFirstEmptyVault = page == vaults.indexOfFirst { it == null }
            EmptyVaultCard(page + 1, isFirstEmptyVault, onAddVault)
        }
    }
}

@Composable
fun VaultsViewTabScreen(vault: CardVault?) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Token", "NFT")
    val isAvailable: Boolean = vault?.baseCoin?.nft_supported ?: run {
        true
    }

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
                    text = {
                        Text(
                            text = title,
                            color = if (index != 1 || isAvailable) MaterialTheme.colors.secondary else Color.Gray
                        )
                    },
                    selected = tabIndex == index,
                    onClick = {
                        if (index != 1 || isAvailable) {
                            tabIndex = index;
                        }
                    },
                )
            }
        }
        when (tabIndex) {
            0 -> vault?.let { VaultsViewToken(it) }
            1 -> vault?.let { VaultsViewNft(it) }
        }
    }
}

@Composable
fun VaultsViewToken(vault: CardVault) {
    LazyColumn {
        items(vault.tokenList) { asset ->//TODO add tokens
            // only show Token, not NFTs
            if (asset.type == AssetType.Token) {
                VaultsViewTokenRow(asset)
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
}

@Composable
fun VaultsViewNft(vault: CardVault) {
    LazyColumn {
        items(vault.nftList) { asset ->
            // todo only show NFTs?
            //if (asset.type == AssetType.NFT) {
            VaultsViewNftRow(asset)
            Divider(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(start = 20.dp, end = 20.dp),
                thickness = 1.dp,
                color = Color.DarkGray
            )
            //}
        }
    }
}

@Composable
fun VaultsViewTokenRow(asset: Asset) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, //Arrangement.SpaceBetween,
        modifier = Modifier
            .background(MaterialTheme.colors.primary)
            .clickable {
                // LINK TO EXPLORER
                uriHandler.openUri(asset.explorerLink ?: "")
            }
            .padding(5.dp)
            .fillMaxWidth()
            .height(80.dp),
    ) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(asset.iconUrl ?: "")
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_sato_small),
                error = painterResource(R.drawable.ic_sato_small),
                contentDescription = (asset.name ?: asset.contract ?: ""),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(10.dp)
                    .size(60.dp)
                    .clip(
                        RoundedCornerShape(50)
                    ),
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = "${asset.name}",// vault.displayName
                )
                // TOKEN BALANCE
                Text(
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = formatBalance(
                        balanceString = asset.balance,
                        decimalsString = asset.decimals,
                        symbol = asset.symbol
                    )
                )
            }
        }
        Text(
            fontSize = 10.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = formatBalance(
                balanceString = asset.valueInSecondCurrency,
                decimalsString = "0",
                symbol = asset.secondCurrency
            )
        )

    }
}

@Composable
fun VaultsViewNftRow(asset: Asset) {
    val showNftDialog = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        //horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(MaterialTheme.colors.primary)
            .clickable {
                // LINK TO EXPLORER
                //uriHandler.openUri(asset.nftImageLink ?: "")
                uriHandler.openUri(
                    sanitizeNftImageUrlString(
                        asset.nftExplorerLink ?: asset.explorerLink ?: ""
                    )
                )
            }
            .padding(5.dp)
            .fillMaxWidth()
            .height(80.dp)
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    sanitizeNftImageUrlString(
                        asset.nftImageLink ?: ""
                    )
                ) //.data(asset.nftImageLink ?: "")
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_sato_small),
            error = painterResource(R.drawable.ic_sato_small),
            contentDescription = (asset.nftName ?: asset.contract ?: "NFT"),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clickable(
                    onClick = {
                        // show bigger image in dialog
                        showNftDialog.value = true
                    },
                    onClickLabel = "open image in dialog"
                ),
        )
        Column(
            modifier = Modifier
                .padding(10.dp)
                .weight(1f)
        ) {
            // NAME
            Text(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = "${asset.nftName ?: asset.contract ?: "NFT"}",
            )
            // NFT BALANCE
            Text(
                fontSize = 12.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = formatBalance(
                    balanceString = asset.balance,
                    decimalsString = asset.decimals,
                    symbol = asset.symbol
                )
            )
            // VALUE IN SECOND CURRENCY
            Text(
                fontSize = 10.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = formatBalance(
                    balanceString = asset.valueInSecondCurrency,
                    decimalsString = "0",
                    symbol = asset.secondCurrency
                )
            )

        }

        // LINK TO EXPLORER
//        Icon(
//            painter = painterResource(R.drawable.open_in_new_24px),
//            contentDescription = "link to NFT explorer",
//            modifier = Modifier
//                .width(30.dp)
//                .clickable {
//                    uriHandler.openUri(asset.nftExplorerLink ?: asset.explorerLink ?: "")
//                },
//            //.requiredWidth(30.dp)
//            //.size(30.dp), //.size(45.dp)
//            tint = MaterialTheme.colors.secondary, //Color.LightGray,
//        )
    }

    // show bigger image in dialog
    if (showNftDialog.value) {
        NftDialog(showNftDialog, asset)
    }
}

@Composable
fun CustomSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colors.secondary, //Color.White,
            checkedTrackColor = Color.White,
            uncheckedThumbColor = MaterialTheme.colors.secondary, //Color.White,
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
        if (vault != null) {
            VaultCard(index + 1, selectedCard == index + 1, vault)
        } else {
            val isFirstEmptyVault = index == vaults.indexOfFirst { it == null }
            EmptyVaultCard(index + 1, isFirstEmptyVault, onAddVault)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun findVaultToSelect(selectedVault: PagerState): Int {
    return selectedVault.currentPage + 1
}

@Preview(showBackground = true)
@Composable
fun VaultsViewPreview() {
    SatodimeTheme {
        VaultsView(
            rememberNavController(),
            viewModel(factory = SharedViewModel.Factory),
            onClick = {}
        )
    }
}
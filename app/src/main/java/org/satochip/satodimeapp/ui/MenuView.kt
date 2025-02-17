package org.satochip.satodimeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.ui.components.InfoDialog
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.DarkBlue
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.webviewActivityIntent
import org.satochip.satodimeapp.viewmodels.SharedViewModel

@Composable
fun MenuView(navController: NavController, sharedViewModel: SharedViewModel) {
    val scrollState = rememberScrollState()
    val showNoCardScannedDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) DarkBlue else Color.LightGray)
    ) {
        HeaderRow(
            onClick = {
                navController.navigateUp()
            }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .verticalScroll(state = scrollState)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_settings),
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .width(250.dp)
                    .height(70.dp),
                contentScale = ContentScale.FillHeight
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // CARD INFO
                MenuCard(
                    stringResource(R.string.cardInfo),
                    TextAlign.Left,
                    180,
                    110, Color(0xFF67889B),
                    R.drawable.cards_info
                ) {
                    if (sharedViewModel.isCardDataAvailable) {
                        navController.navigate(SatodimeScreen.CardInfoView.name)
                    } else {
                        showNoCardScannedDialog.value = true
                    }
                }
                // RELEASE OWNERSHIP
                MenuCard(
                    stringResource(R.string.transferOwner),
                    TextAlign.Left,
                    180,
                    110,
                    LightGray,
                    R.drawable.users
                ) {
                    if (sharedViewModel.isCardDataAvailable && sharedViewModel.ownershipStatus == OwnershipStatus.Owner) {
                        navController.navigate(SatodimeScreen.TransferOwnershipView.name)
                    } else if (sharedViewModel.ownershipStatus == OwnershipStatus.Unclaimed) {
                        sharedViewModel.isAskingForCardOwnership = true
                    } else {
                        showNoCardScannedDialog.value = true
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                MenuCard(
                    stringResource(R.string.howToUse),
                    TextAlign.Left,
                    220, 110,
                    Color(0xFF64B3B3),
                    R.drawable.how_to
                ) {
                    webviewActivityIntent(
                        url = "https://satochip.io/setup-use-satodime-on-mobile/",
                        context = context
                    )
                }
                MenuCard(
                    stringResource(R.string.settings),
                    TextAlign.Left,
                    150,
                    110,
                    LightGray,
                    R.drawable.settings
                ) {
                    navController.navigate(SatodimeScreen.SettingsView.name)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                MenuCard(
                    stringResource(R.string.termsOfService),
                    TextAlign.Center,
                    190,
                    100,
                    Color(0xFF2D2F45)
                ) {
                    webviewActivityIntent(
                        url = "https://satochip.io/terms-of-service/",
                        context = context
                    )
                }
                MenuCard(
                    stringResource(R.string.privacyPolicy),
                    TextAlign.Center,
                    190,
                    100,
                    Color(0xFF2D2F45)
                ) {
                    webviewActivityIntent(
                        url = "https://satochip.io/privacy-policy/",
                        context = context
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(10.dp)
                    .padding(bottom = 50.dp)
                    .clickable {
                        webviewActivityIntent(
                            url = "https://satochip.io/shop/",
                            context = context
                        )
                    },
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    modifier = Modifier
                        .background(Color(0xFF2D2F45))
                        .padding(top = 20.dp, start = 15.dp, bottom = 15.dp, end = 225.dp),
                    textAlign = TextAlign.Left,
                    color = Color.White,
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.allOurProducts)
                )
                Image(
                    painter = painterResource(id = R.drawable.all_our_products),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }

    if (showNoCardScannedDialog.value) {
        var title = stringResource(id = R.string.cardNeedToBeScannedTitle)
        var message = stringResource(id = R.string.cardNeedToBeScannedMessage)
        if (sharedViewModel.ownershipStatus == OwnershipStatus.NotOwner) {
            title = stringResource(id = R.string.youAreNotTheCardOwner)
            message = stringResource(id = R.string.nfcUnlockSecretNotFound)
        }
        InfoDialog(
            openDialogCustom = showNoCardScannedDialog,
            title = title,
            message = message,
            isActionButtonVisible = false,
            buttonTitle = "",
            buttonAction = {},
        )
    }
}

@Composable
fun MenuCard(
    text: String,
    textAlign: TextAlign,
    width: Int,
    height: Int,
    color: Color,
    drawableId: Int? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .padding(5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp)
    ) {
        val endTextPadding = if (drawableId == null) 15.dp else 30.dp
        Text(
            modifier = Modifier
                .background(color)
                .padding(top = 20.dp, start = 15.dp, bottom = 15.dp, end = endTextPadding),
            textAlign = textAlign,
            color = Color.White,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            text = text
        )
        if (drawableId != null) {
            Row {
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 55.dp, end = 10.dp)
                        .size(30.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuViewPreview() {
    SatodimeTheme {
        MenuView(rememberNavController(), viewModel(factory = SharedViewModel.Factory))
    }
}
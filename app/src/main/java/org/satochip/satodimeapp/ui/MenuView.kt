package org.satochip.satodimeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
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
import org.satochip.satodimeapp.ui.components.InfoDialog
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.DarkBlue
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

@Composable
fun MenuView(navController: NavController, sharedViewModel: SharedViewModel) {
    val showNoCardScannedDialog = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) DarkBlue else Color.LightGray)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp)
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
                .fillMaxWidth()
                .height(110.dp)
        ) {
            // CARD INFO
            MenuCard(
                stringResource(R.string.cardInfo),
                TextAlign.Left,
                180,
                110, Color(0xFF67889B),
                R.drawable.cards_info
            ) {
                if (sharedViewModel.isCardDataAvailable){
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
                navController.navigate(SatodimeScreen.TransferOwnershipView.name)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .height(120.dp)
        ) {
            MenuCard(
                stringResource(R.string.howToUse),
                TextAlign.Left,
                220, 110,
                Color(0xFF64B3B3),
                R.drawable.how_to
            ) { uriHandler.openUri("https://satochip.io/setup-use-satodime-on-mobile/") }
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
                .padding(5.dp)
                .fillMaxWidth()
                .height(75.dp)
        ) {
            MenuCard(stringResource(R.string.termsOfService), TextAlign.Center, 190, 75, Color(0xFF2D2F45)) {
                uriHandler.openUri("https://satochip.io/terms-of-service/")
            }
            MenuCard(stringResource(R.string.privacyPolicy), TextAlign.Center, 190, 75, Color(0xFF2D2F45)) {
                uriHandler.openUri("https://satochip.io/privacy-policy/")
            }
        }
        Divider(
            modifier = Modifier
                .padding(20.dp)
                .height(2.dp)
                .width(150.dp),
            color = Color.DarkGray,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(10.dp)
                .padding(bottom = 50.dp)
                .clickable {
                    uriHandler.openUri("https://satochip.io/shop/")
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

    if (showNoCardScannedDialog.value
        && !sharedViewModel.isCardDataAvailable){
        InfoDialog(
            openDialogCustom = showNoCardScannedDialog,
            title = stringResource(R.string.cardNeedToBeScannedTitle),
            message = stringResource(R.string.cardNeedToBeScannedMessage),
            isActionButtonVisible = false,
            buttonTitle = "",
            buttonAction = {},)
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
        val endTextPadding = if(drawableId == null) 15.dp else 30.dp
        Text(
            modifier = Modifier
                .background(color)
                .padding(top = 20.dp, start = 15.dp, bottom = 15.dp, end = endTextPadding),
            textAlign = textAlign,
            color = Color.White,
            fontSize = 18.sp,
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
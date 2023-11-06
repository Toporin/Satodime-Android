package org.satochip.satodime.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.data.Coin
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.Title
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen

@Composable
fun CongratsVaultCreatedView(navController: NavController, selectedCoinName: String) {
    val selectedCoin = Coin.valueOf(selectedCoinName)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Title(
                stringResource(R.string.congrats_cap),
                stringResource(R.string.your_vault_has_been_successfully_created_and_sealed)
            )
            VaultDisplay(coin = selectedCoin)
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(20.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondary,
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                text = buildAnnotatedString {
                    append(stringResource(R.string.remember_that_your))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.private_keys))
                    }
                    append(stringResource(R.string.will_be_accessible_once_you_ve))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.unsealed))
                    }
                    append(stringResource(R.string.your))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.vault))
                    }
                    append(".")
                }
            )
            Spacer(Modifier.weight(1f))
            BottomButton(
                onClick = {
                    navController.navigate(SatodimeScreen.Vaults.name) {
                        popUpTo(0)
                    }
                },
                text = stringResource(R.string.show_my_vault)
            )
        }
    }

}

@Composable
fun VaultDisplay(coin: Coin) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.vault),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = coin.painterResourceId),
            contentDescription = null,
            modifier = Modifier
                .offset((-75).dp, (-50).dp)
                .size(70.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CongratsVaultCreatedViewPreview() {
    SatodimeTheme {
        CongratsVaultCreatedView(rememberNavController(), Coin.BTC.name)
    }
}
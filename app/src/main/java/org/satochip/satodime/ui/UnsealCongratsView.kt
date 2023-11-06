package org.satochip.satodime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.RedGradientBackground
import org.satochip.satodime.ui.components.VaultCard
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen

@Composable
fun UnsealCongratsView(navController: NavController, selectedVault: Int) {
    val satodimeStore = SatodimeStore(LocalContext.current)
    val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value

    if(vaults.isEmpty() || vaults[selectedVault - 1] == null) return

    val vault = vaults[selectedVault - 1]!!

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
            text = stringResource(R.string.congrats)
        )
        Text(
            modifier = Modifier
                .padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondaryVariant,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.vault_cap))
                }
                append(stringResource(R.string.successfully))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.unseal))
                }
            }
        )
        VaultCard(index = selectedVault, isSelected = true, vault = vault)
        Text(
            modifier = Modifier
                .padding(40.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondaryVariant,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            text = stringResource(R.string.you_can_now_view_the_private_key)
        )
        Spacer(Modifier.weight(1f))
        BottomButton(
            onClick = {
                navController.navigate(SatodimeScreen.ShowPrivateKey.name  + "/$selectedVault") {
                    popUpTo(0)
                }
            },
            width = 200.dp,
            text = stringResource(R.string.show_the_private_key)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnsealCongratsViewPreview() {
    SatodimeTheme {
        UnsealCongratsView(rememberNavController(), 1)
    }
}
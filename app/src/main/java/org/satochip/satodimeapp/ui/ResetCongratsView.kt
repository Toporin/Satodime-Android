package org.satochip.satodimeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.DarkBlueGradientBackground
import org.satochip.satodimeapp.ui.components.EmptyVaultCard
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen

@Composable
fun ResetCongratsView(navController: NavController, selectedVault: Int) {
    val scrollState = rememberScrollState()

    DarkBlueGradientBackground()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(state = scrollState)
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
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.vaultSuccessfullyReset)
        )
        EmptyVaultCard(index = selectedVault, isFirstEmptyVault = true) {
            navController.navigate(SatodimeScreen.SelectBlockchain.name + "/$selectedVault")
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(20.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 14.sp,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.youCanNowCreateAndSeal)
            )
        }
        Spacer(Modifier.weight(1f))
        BottomButton(
            onClick = {
                navController.navigateUp()
            },
            width = 240.dp,
            text = stringResource(R.string.backToMyVaults)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetCongratsViewPreview() {
    SatodimeTheme {
        ResetCongratsView(rememberNavController(), 2)
    }
}
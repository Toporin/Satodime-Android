package org.satochip.satodimeapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.RedGradientBackground
import org.satochip.satodimeapp.ui.components.VaultCard
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

@Composable
fun UnsealCongratsView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {

    val vaults = sharedViewModel.cardVaults
    val vaultsSize = vaults?.size ?: 0
    if (selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!
    val scrollState = rememberScrollState()

    RedGradientBackground()
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
            color = MaterialTheme.colors.secondaryVariant,
            text = stringResource(R.string.vaultSuccessfullyUnseal)
        )
        Text(
            modifier = Modifier
                .padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondaryVariant,
            text = stringResource(R.string.youCanNowCreateAndSeal)
        )

        VaultCard(index = selectedVault, isSelected = true, vault = vault)
        Text(
            modifier = Modifier
                .padding(40.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondaryVariant,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            text = stringResource(R.string.youCanNowViewThePrivateKey)
        )

        //Spacer(Modifier.weight(1f))

        BottomButton(
            onClick = {
                navController.navigate(SatodimeScreen.ShowPrivateKey.name  + "/$selectedVault") {
                    popUpTo(0)
                }
            },
            width = 200.dp,
            text = stringResource(R.string.showThePrivateKey)
        )

        // BACK TO VAULT BUTTON
        BottomButton(
            onClick = {
                navController.navigate(SatodimeScreen.Vaults.name) {
                    popUpTo(0)
                }
            },
            width = 240.dp,
            text = stringResource(R.string.backToMyVaults)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnsealCongratsViewPreview() {
    SatodimeTheme {
        UnsealCongratsView(rememberNavController(), viewModel(factory = SharedViewModel.Factory),1)
    }
}
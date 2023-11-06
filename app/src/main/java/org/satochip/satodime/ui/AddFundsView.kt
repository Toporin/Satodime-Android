package org.satochip.satodime.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.DisplayDataView
import org.satochip.satodime.ui.theme.SatodimeTheme

@Composable
fun AddFundsView(navController: NavController, selectedVault: Int) {
    val satodimeStore = SatodimeStore(LocalContext.current)
    val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value

    if(vaults.isEmpty() || vaults[selectedVault - 1] == null) return
    val vault = vaults[selectedVault - 1]!!

    DisplayDataView(
        navController = navController,
        vault = vault,
        index = selectedVault,
        title = stringResource(R.string.add_funds),
        label = stringResource(R.string.deposit_address),
        data = vault.address
    )
}

@Preview(showBackground = true)
@Composable
fun AddFundsViewPreview() {
    SatodimeTheme {
        AddFundsView(rememberNavController(), 1)
    }
}
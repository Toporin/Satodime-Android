package org.satochip.satodime.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.ui.components.DisplayDataView
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.viewmodels.SharedViewModel

@Composable
fun AddFundsView(navController: NavController, sharedViewModel: SharedViewModel, selectedVault: Int) {
//    val satodimeStore = SatodimeStore(LocalContext.current)
//    val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value
//
//    if(vaults.isEmpty() || vaults[selectedVault - 1] == null) return
//    val vault = vaults[selectedVault - 1]!!

    val cardVaults = sharedViewModel.cardVaults.value
    val cardVaultsSize = cardVaults?.size ?: 0
    if(selectedVault>cardVaultsSize || cardVaults?.get(selectedVault - 1) == null) return
    val cardVault = cardVaults[selectedVault - 1]!!

    DisplayDataView(
        navController = navController,
        vault = cardVault,
        index = selectedVault,
        title = stringResource(R.string.add_funds),
        label = stringResource(R.string.deposit_address),
        data = cardVault.nativeAsset.address
    )
}

@Preview(showBackground = true)
@Composable
fun AddFundsViewPreview() {
    SatodimeTheme {
        AddFundsView(rememberNavController(), viewModel(factory = SharedViewModel.Factory), 1)
    }
}
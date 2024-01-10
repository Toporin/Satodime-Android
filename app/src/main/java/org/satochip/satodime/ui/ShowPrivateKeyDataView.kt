package org.satochip.satodime.ui

import androidx.compose.runtime.Composable
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
fun ShowPrivateKeyDataView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int,
    label: String,
    data: String,
    subLabel: String
) {
    //val satodimeStore = SatodimeStore(LocalContext.current)
    //val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value

    val vaults = sharedViewModel.cardVaults
    if (selectedVault > vaults.size || vaults[selectedVault - 1] == null) return
    val vault = vaults[selectedVault - 1]!!

    DisplayDataView(
        navController = navController,
        vault = vault,
        index = selectedVault,
        title = stringResource(R.string.show_private_key),
        label = label,
        subLabel = subLabel,
        data = data
    )
}

@Preview(showBackground = true)
@Composable
fun ShowPrivateKeyDataViewPreview() {
    SatodimeTheme {
        ShowPrivateKeyDataView(
            rememberNavController(),
            viewModel(factory = SharedViewModel.Factory),
            1,
            "Private key",
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            "(Legacy)"
        )
    }
}
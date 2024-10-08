package org.satochip.satodimeapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.DisplayDataView
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "ShowPrivateKeyDataView"

@Composable
fun ShowPrivateKeyDataView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int,
    label: String,
    data: String,
) {
    val vaults = sharedViewModel.cardVaults
    val vaultsSize = vaults?.size ?: 0
    if (selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!

    // labels
    val labelString = when(label){
        "legacy" -> stringResource(R.string.privateKey)
        "wif" -> stringResource(R.string.privateKey)
        "entropy" -> stringResource(R.string.entropy)
        else -> label
    }
    val subLabelString = when(label){
        "legacy" -> "Legacy"
        "wif" -> "Wallet Import Format"
        "entropy" -> ""
        else -> ""
    }

    DisplayDataView(
        navController = navController,
        vault = vault,
        index = selectedVault,
        title = R.string.showPrivateKey,
        label = labelString,
        subLabel = subLabelString,
        data = data,
        onClick = {}
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
        )
    }
}
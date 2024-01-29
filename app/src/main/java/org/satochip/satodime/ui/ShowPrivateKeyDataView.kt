package org.satochip.satodime.ui

import android.util.Log
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

private const val TAG = "ShowPrivateKeyDataView"

@Composable
fun ShowPrivateKeyDataView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int,
    label: String,
    data: String,
) {
//    Log.d(TAG, "ShowPrivateKeyDataView START")
//    Log.d(TAG, "ShowPrivateKeyDataView selectedVault: $selectedVault")
//    Log.d(TAG, "ShowPrivateKeyDataView label: $label")
//    Log.d(TAG, "ShowPrivateKeyDataView data: $data")
    val vaults = sharedViewModel.cardVaults.value
    val vaultsSize = vaults?.size ?: 0
    if (selectedVault > vaultsSize || vaults?.get(selectedVault - 1) == null) return
    val vault = vaults[selectedVault - 1]!!

    // labels
    val labelString = when(label){
        "legacy" -> stringResource(R.string.private_key)
        "wif" -> stringResource(R.string.private_key)
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
        title = stringResource(R.string.show_private_key),
        label = labelString,
        subLabel = subLabelString,
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
        )
    }
}
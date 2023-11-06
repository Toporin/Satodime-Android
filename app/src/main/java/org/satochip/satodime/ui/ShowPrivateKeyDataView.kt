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
fun ShowPrivateKeyDataView(
    navController: NavController,
    label: String,
    data: String,
    subLabel: String,
    selectedVault: Int,
) {
    val satodimeStore = SatodimeStore(LocalContext.current)
    val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value

    if (vaults.isEmpty() || vaults[selectedVault - 1] == null) return

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
            "Private key",
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            "(Legacy)",
            1
        )
    }
}
package org.satochip.satodimeapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.CardVault
import org.satochip.satodimeapp.ui.components.DisplayDataView
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.apiKeys
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


fun getAddressForPaybis(cardVault: CardVault): String {

    // for BCH, remove prefix
    if (cardVault.nativeAsset.symbol=="BCH"){
        val address = cardVault.nativeAsset.address.removePrefix("bitcoincash:")
        return address
    }

    return cardVault.nativeAsset.address
}

fun getCurrencyCodeForPaybis(cardVault: CardVault): String? {

    // testnet not supported
    if (cardVault.isTestnet){return null}
    // xcp not supported
    if (cardVault.nativeAsset.symbol=="XCP"){return null}

    return cardVault.nativeAsset.symbol
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddFundsView(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    selectedVault: Int,
) {
    val cardVaults = sharedViewModel.cardVaults
    val cardVaultsSize = cardVaults.size
    if(selectedVault>cardVaultsSize || cardVaults.get(selectedVault - 1) == null) return
    val cardVault = cardVaults[selectedVault - 1]!!

    // for crypto purchase using paybis
    val depositAddress = getAddressForPaybis(cardVault= cardVault) //cardVault.nativeAsset.address
    val currencyCodeTo = getCurrencyCodeForPaybis(cardVault= cardVault)
    var paybisUrl: String? = null
    if (currencyCodeTo != null) {
        val apiKey = apiKeys["API_KEY_PAYBIS_ID"]
        val hmacKey = apiKeys["API_KEY_PAYBIS_HMAC"]
        val uri = "https://widget.paybis.com/"
        val query = "?partnerId=$apiKey" +
                "&cryptoAddress=$depositAddress" +
                "&currencyCodeFrom=EUR" +
                "&currencyCodeTo=$currencyCodeTo" //${cardVault.nativeAsset.symbol}"
        val decodedKey = Base64.getDecoder().decode(hmacKey)
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(decodedKey, "HmacSHA256")
        mac.init(secretKeySpec)
        val signatureBytes = mac.doFinal(query.toByteArray(Charsets.UTF_8))
        val signature = Base64.getEncoder().encodeToString(signatureBytes)
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")

        paybisUrl = "$uri$query&signature=$encodedSignature"
    }

    DisplayDataView(
        navController = navController,
        vault = cardVault,
        index = selectedVault,
        title = R.string.addFunds,
        label = stringResource(R.string.depositAddress),
        data = cardVault.nativeAsset.address,
        url = paybisUrl,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddFundsViewPreview() {
    SatodimeTheme {
        AddFundsView(rememberNavController(), viewModel(factory = SharedViewModel.Factory), 1)
    }
}
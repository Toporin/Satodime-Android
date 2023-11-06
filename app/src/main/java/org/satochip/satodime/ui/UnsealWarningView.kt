package org.satochip.satodime.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.RedGradientBackground
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.components.VaultCard
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen

@Composable
fun UnsealWarningView(navController: NavController, selectedVault: Int) {
    val context = LocalContext.current
    val satodimeStore = SatodimeStore(context)
    val vaults = satodimeStore.vaultsFromDataStore.collectAsState(initial = emptyList()).value

    if (vaults.isEmpty()) return

    RedGradientBackground()
    TopLeftBackButton(navController)
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
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.warning)
        )
        Text(
            modifier = Modifier
                .padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondaryVariant,
            text = stringResource(R.string.you_are_about_to_unseal_the_following_crypto_vault)
        )
        VaultCard(
            index = selectedVault,
            isSelected = true,
            vault = vaults[selectedVault - 1]!!,
        )
        Text(
            modifier = Modifier
                .padding(10.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondaryVariant,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.unsealing))
                }
                append(stringResource(R.string.this_crypto_vault_will_reveal_the_corresponding_private_key))
            }
        )
        Divider(
            modifier = Modifier
                .padding(10.dp)
                .height(2.dp)
                .width(100.dp),
            color = Color.DarkGray,
        )
        Text(
            modifier = Modifier
                .padding(10.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondaryVariant,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body1,
            text = stringResource(R.string.you_can_then_transfer_the_entire_balance)
        )
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
                text = stringResource(R.string.this_action_is_irreversible)
            )
        }
        Spacer(Modifier.weight(1f))
        val unsealFailureText = stringResource(R.string.unseal_failure)
        val cardLoadingText = stringResource(R.string.card_loading_please_try_again_in_few_seconds)
        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        BottomButton(
            onClick = {
                if (NFCCardService.isConnected.value == true) {
                    if (NFCCardService.isOwner()) {
                        if (NFCCardService.isReadingFinished.value != true) {
                            Toast.makeText(
                                context, cardLoadingText, Toast.LENGTH_SHORT).show()
                        } else if (NFCCardService.unseal(selectedVault - 1)) {
                            navController.navigate(SatodimeScreen.UnsealCongrats.name + "/$selectedVault") {
                                popUpTo(0)
                            }
                        } else {
                            Toast.makeText(context, unsealFailureText, Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(context, youreNotTheOwnerText, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, pleaseConnectTheCardText, Toast.LENGTH_SHORT).show()
                }
            },
            color = Color.Red,
            text = stringResource(R.string.unseal)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnsealWarningViewPreview() {
    SatodimeTheme {
        UnsealWarningView(rememberNavController(), 1)
    }
}
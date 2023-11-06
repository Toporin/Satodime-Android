package org.satochip.satodime.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.theme.LightGray
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme

@Composable
fun TransferOwnershipView(navController: NavController) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopLeftBackButton(navController)
        Image(
            painter = painterResource(R.drawable.transfer_ownership_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = (-75).dp),
            contentScale = ContentScale.FillWidth
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 30.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.transfer_ownership)
        )
        Image(
            painter = painterResource(id = R.drawable.transfer_ownership),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp),
            contentScale = ContentScale.FillHeight
        )
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.please_note)
        )
        Spacer(Modifier.weight(1f))
        val ownershipTransferredText = stringResource(R.string.ownership_transferred)
        val ownershipTransferFailedtext = stringResource(R.string.ownership_transfer_failed)
        val cardLoadingText = stringResource(R.string.card_loading_please_try_again_in_few_seconds)
        val youreNotTheOwnerText = stringResource(R.string.you_re_not_the_owner)
        val pleaseConnectTheCardText = stringResource(R.string.please_connect_the_card)
        Button(
            onClick = {
                if (NFCCardService.isConnected.value == true) {
                    if (NFCCardService.isOwner()) {
                        if (NFCCardService.isReadingFinished.value != true) {
                            Toast.makeText(context, cardLoadingText, Toast.LENGTH_SHORT).show()
                        } else if (NFCCardService.transferOwnership()) {
                            Toast.makeText(context, ownershipTransferredText, Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        } else {
                            Toast.makeText(context, ownershipTransferFailedtext, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, youreNotTheOwnerText, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, pleaseConnectTheCardText, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen,
                MaterialTheme.colors.secondary
            )
        ) {
            Text(stringResource(R.string.transfer))
        }
        Button(
            onClick = {
                navController.navigateUp()
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(100.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGray,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransferOwnershipViewPreview() {
    SatodimeTheme {
        TransferOwnershipView(rememberNavController())
    }
}
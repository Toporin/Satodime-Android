package org.satochip.satodimeapp.ui

import CardInfoCard
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.AuthenticityStatus
import org.satochip.satodimeapp.services.NFCCardService
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.DarkBlue
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme

@Composable
fun AuthenticCardView(navController: NavController) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showCardCert by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()


    val shortStatusText = if (NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic){
        stringResource(R.string.authenticationSuccessTitle)
    } else {
        stringResource(R.string.authenticationFailedTitle)
    }
    val longStatusText = if (NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic){
        stringResource(R.string.authenticationSuccessText)
    } else {
        stringResource(R.string.authenticationFailedText)
    }
    val authenticityColor = if (NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic){
        LightGreen
    } else {
        Color.Red
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) DarkBlue else Color.LightGray)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp)
            .verticalScroll(state = scrollState)
    ) {
        // LOGO
        Image(
            painter = painterResource(id = R.drawable.logo_settings),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .width(250.dp)
                .height(70.dp),
            contentScale = ContentScale.FillHeight
        )
        Spacer(Modifier.height(50.dp))

        // LOGO SUCCESS/FAILURE
        Image(
            painter = painterResource(id = R.drawable.ic_sato_small),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .height(150.dp),
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(authenticityColor)
        )
        // SHORT STATUS
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = shortStatusText
        )
        // DETAILED STATUS
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = longStatusText
        )

        val showCardCertText =  stringResource(R.string.showDeviceCert)
        val hideCardCertText =  stringResource(R.string.hideDeviceCert)
        if (!showCardCert){
            CardInfoCard(showCardCertText, 275, authenticityColor) {
                showCardCert = showCardCert.not() // toggle display
            }
        }
        // SHOW CERTIFICATE DETAILS
        if (showCardCert && (NFCCardService.certificateList.value?.size ?: 0) > 0){
            CardInfoCard(hideCardCertText, 275, authenticityColor) {
                showCardCert = showCardCert.not() // toggle display
            }
            Column(modifier = Modifier
                .border(width = 4.dp, color = authenticityColor, shape = RoundedCornerShape(15.dp))
                .padding(10.dp),
            ){
                // COPY TO CLIPBOARD
                Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.secondary,
                        text = stringResource(R.string.copyToClipboard)
                    )
                    val toastText = stringResource(R.string.copied_to_clipboard)
                    Icon(
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                var txt = """
                                    RootCA: \n 
                                    ${NFCCardService.certificateList.value?.get(1) ?: "(None)"} \n\n 
                                    SubCA: \n 
                                    ${NFCCardService.certificateList.value?.get(2) ?: "(None)"} \n\n 
                                    card: \n 
                                    ${NFCCardService.certificateList.value?.get(3) ?: "(None)"}
                                    """
                                clipboardManager.setText(AnnotatedString(txt))
                                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                            },
                        imageVector = Icons.Outlined.ContentCopy,
                        tint = Color.LightGray,
                        contentDescription = "Copy cert to clipboard"
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                // ROOT CA CERT
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = stringResource(R.string.rootCaInfo),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.body1,
                    text = NFCCardService.certificateList.value?.get(1) ?: "" //todo
                )
                // SUBCA CERT
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = stringResource(R.string.subcaInfo),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.body1,
                    text = NFCCardService.certificateList.value?.get(2) ?: "" // todo
                )
                // CARD CERT
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = stringResource(R.string.deviceInfo),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.body1,
                    text = NFCCardService.certificateList.value?.get(3) ?: "" // todo
                )

            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticCardViewPreview() {
    SatodimeTheme {
        AuthenticCardView(rememberNavController())
    }
}
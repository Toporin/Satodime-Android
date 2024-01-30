import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.data.AuthenticityStatus
import org.satochip.satodime.data.OwnershipStatus
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.theme.LightGray
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen
import org.satochip.satodime.viewmodels.SharedViewModel

@Composable
fun CardInfoView(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showCertificate by remember { mutableStateOf(false) }

    if (showCertificate && (NFCCardService.certificateList.value?.size ?: 0) > 0) {
        Dialog(
            content = {
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .padding(10.dp)
                ) {
                    item {
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier.padding(10.dp),
                                text = stringResource(R.string.certificate_details),
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.secondary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            modifier = Modifier.padding(5.dp),
                            color = MaterialTheme.colors.secondaryVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body1,
                            text = NFCCardService.certificateList.value?.get(1) ?: "" //todo
                        )
                        Text(
                            modifier = Modifier.padding(5.dp),
                            color = MaterialTheme.colors.secondaryVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body1,
                            text = NFCCardService.certificateList.value?.get(2) ?: "" // todo
                        )
                        Text(
                            modifier = Modifier.padding(5.dp),
                            color = MaterialTheme.colors.secondaryVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body1,
                            text = NFCCardService.certificateList.value?.get(3) ?: "" // todo
                        )
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier.padding(5.dp),
                                color = MaterialTheme.colors.secondaryVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.body1,
                                text = stringResource(R.string.copy_to_clipboard)
                            )
                            val toastText = stringResource(R.string.copied_to_clipboard)
                            Icon(
                                modifier = Modifier
                                    .size(25.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(NFCCardService.certificateList.value?.get(3) ?: "")) //TODO!
                                        Toast
                                            .makeText(
                                                context,
                                                toastText,
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    },
                                imageVector = Icons.Outlined.ContentCopy,
                                tint = Color.LightGray,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        TextButton(
                            onClick = {
                                showCertificate = false
                            }
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.secondary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            },
            onDismissRequest = { showCertificate = false },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 38.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_info)
        )
        Spacer(Modifier.weight(1f))

        // CARD OWNERSHIP STATUS
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_ownership_status)
        )

        var ownershipStatusString = "Unknown"
        var colorOwner = Color.Yellow
        if (NFCCardService.ownershipStatus.value == OwnershipStatus.Owner) {
            ownershipStatusString = "You are the card owner"
            colorOwner = LightGreen
        } else if (NFCCardService.ownershipStatus.value == OwnershipStatus.NotOwner) {
            ownershipStatusString = "You are NOT the card owner"
            colorOwner = Color.Red
        } else if (NFCCardService.ownershipStatus.value == OwnershipStatus.Unclaimed) {
            ownershipStatusString = "The card has no owner!"
            colorOwner = Color.Blue
        }
        CardInfoCard(ownershipStatusString, 300, colorOwner)
        Spacer(Modifier.weight(1f))

        // CARD VERSION
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_version)
        )
        CardInfoCard(NFCCardService.cardAppletVersion, 225)
        Spacer(Modifier.weight(1f))

        Divider(
            modifier = Modifier
                .padding(20.dp)
                .height(2.dp)
                .width(150.dp),
            color = Color.DarkGray,
        )
        Spacer(Modifier.weight(1f))

        // CARD AUTHENTICITY
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_authenticity)
        )

        var authenticityStatusString = "Unknown"
        var authenticityColor = Color.Yellow
        //if(NFCCardService.isAuthentic.value == true) {
        if(NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic) {
            authenticityStatusString =  stringResource(R.string.cardAuthenticStatus)
            authenticityColor = LightGreen
        } else {
            authenticityStatusString = stringResource(R.string.cardNotAuthenticStatus)
            authenticityColor = Color.Red
        }
        CardInfoCard(authenticityStatusString, 275, authenticityColor) {
            val authenticScreen = if (NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic) {
                SatodimeScreen.AuthenticCardView
            } else {
                SatodimeScreen.FakeCardView
            }
            navController.navigate(authenticScreen.name)
        }
        CardInfoCard("Certificate details", 250, LightGray) {
            showCertificate = true
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun CardInfoCard(text: String, width: Int, color: Color = LightGreen, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .width(width.dp)
            .height(65.dp)
            .padding(5.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(
            modifier = Modifier
                .background(color)
                .padding(15.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            text = text
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardInfoViewPreview() {
    SatodimeTheme {
        CardInfoView(rememberNavController(), viewModel(factory = SharedViewModel.Factory))
    }
}

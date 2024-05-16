import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.AuthenticityStatus
import org.satochip.satodimeapp.data.OwnershipStatus
import org.satochip.satodimeapp.services.NFCCardService
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.webviewActivityIntent
import org.satochip.satodimeapp.viewmodels.SharedViewModel

@Composable
fun CardInfoView(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current

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
        //TITLE
        Text(
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 30.sp, // todo uniform
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.cardInfo)
        )
        Spacer(Modifier.weight(1f))

        // CARD OWNERSHIP STATUS
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.cardOwnershipStatus)
        )

        var ownershipStatusString = "Unknown"
        var colorOwner = Color.Yellow
        if (NFCCardService.ownershipStatus.value == OwnershipStatus.Owner) {
            ownershipStatusString = stringResource(R.string.youAreTheCardOwner)
            colorOwner = LightGreen
        } else if (NFCCardService.ownershipStatus.value == OwnershipStatus.NotOwner) {
            ownershipStatusString = stringResource(R.string.youAreNotTheCardOwner)
            colorOwner = Color.Red
        } else if (NFCCardService.ownershipStatus.value == OwnershipStatus.Unclaimed) {
            ownershipStatusString = stringResource(R.string.cardHasNoOwner)
            colorOwner = Color.Blue
        }
        CardInfoCard(ownershipStatusString, 300, colorOwner) {
            webviewActivityIntent(
                url = "https://satochip.io/satodime-ownership-explained/",
                context = context
            )
        }

        Spacer(Modifier.weight(1f))

        // CARD VERSION
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.cardVersion)
        )
        CardInfoCard(NFCCardService.cardAppletVersion, 225)

        Spacer(Modifier.weight(1f))

        // CARD AUTHENTICITY
        Text(
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.cardAuthenticity)
        )

        var authenticityStatusString = "Unknown"
        var authenticityColor = Color.Yellow
        //if(NFCCardService.isAuthentic.value == true) {
        if (NFCCardService.authenticityStatus.value == AuthenticityStatus.Authentic) {
            authenticityStatusString = stringResource(R.string.thisCardIsGenuine)
            authenticityColor = LightGreen
        } else {
            authenticityStatusString = stringResource(R.string.thisCardIsNotGenuine)
            authenticityColor = Color.Red
        }
        CardInfoCard(authenticityStatusString, 275, authenticityColor) {
            navController.navigate(SatodimeScreen.AuthenticCardView.name)
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun CardInfoCard(text: String, width: Int, color: Color = LightGreen, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp),
//            .width(width.dp)
//            .height(height.dp)
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

package org.satochip.satodimeapp.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.CardVault
import org.satochip.satodimeapp.data.SlotState

private val topBoxHeight = 225.dp

@Composable
fun DisplayDataView(
    navController: NavController,
    vault: CardVault,
    index: Int,
    title: String,
    label: String,
    subLabel: String = "",
    data: String
) {
    //TODO: in entropy, show utf8 if possible, and sha256 of entropy should match privkey?

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBoxHeight)
            .paint(
                painterResource(if (vault.state == SlotState.SEALED) R.drawable.background_for_card1 else R.drawable.unsealed_card),
                contentScale = ContentScale.FillBounds
            )
    ) {
        TopLeftBackButton(navController)
        Text(
            modifier = Modifier
                .padding(top = 40.dp)
                .width(150.dp)
                .align(Alignment.TopCenter),
            color = Color.LightGray,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            text = title
        )
        Column(modifier = Modifier.padding(start = 40.dp, top = 100.dp)) {
            Text(
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                text = "0$index"
            )
            SealedIndicator(
                modifier = Modifier.width(if (vault.state == SlotState.SEALED) 60.dp else 80.dp),
                isSealed = (vault.state == SlotState.SEALED)
            )
        }
        Image(
            painter = painterResource(id = vault.coin.painterResourceId),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.BottomEnd)
                .offset((-40).dp, (-75).dp),
            contentScale = ContentScale.Crop
        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-32).dp, y = (-60).dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = Color.LightGray,
            text = if (vault.isTestnet) "Testnet" else ""
        )
    }
    Card(
        modifier = Modifier
            .padding(top = topBoxHeight - 50.dp)
            .fillMaxSize(),
        shape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colors.primaryVariant)
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 18.dp),
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                text = label
            )
            Text(
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 16.sp,
                style = MaterialTheme.typography.body1,
                text = subLabel
            )
            Text(
                modifier = Modifier
                    .padding(20.dp),
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 18.sp,
                style = MaterialTheme.typography.body1,
                text = data
            )
            DataAsQrCode(data)
            if(vault.state == SlotState.SEALED) {
                Text(
                    modifier = Modifier
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondaryVariant,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.youOrAnybodyCanDepositFunds)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
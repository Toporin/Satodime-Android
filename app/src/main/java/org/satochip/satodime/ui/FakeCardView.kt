package org.satochip.satodime.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.theme.DarkRed
import org.satochip.satodime.ui.theme.LightRed
import org.satochip.satodime.ui.theme.SatodimeTheme

@Composable
fun FakeCardView(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) DarkRed else LightRed)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp)
    ) {
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
        Image(
            painter = painterResource(id = R.drawable.fake_card),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .height(100.dp),
            contentScale = ContentScale.FillHeight
        )
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_authentication_failed)
        )
        Text(
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.card_authentication_failed_description)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FakeCardViewPreview() {
    SatodimeTheme {
        FakeCardView(rememberNavController())
    }
}
package org.satochip.satodime.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodime.R
import org.satochip.satodime.ui.components.BottomButton
import org.satochip.satodime.ui.components.WelcomeViewTitle
import org.satochip.satodime.ui.theme.LightGray
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimeScreen

@Composable
fun ThirdWelcomeView(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Image(
            painter = painterResource(R.drawable.third_welcome_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillBounds
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier =
            Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            WelcomeViewTitle()
            Spacer(modifier = Modifier.height(50.dp))
            ThirdWelcomeViewContent()
            Spacer(modifier = Modifier.weight(1f))
            BottomButton(
                onClick = {
                    navController.navigate(SatodimeScreen.Vaults.name) {
                        popUpTo(0)
                    }
                },
                text = stringResource(R.string.start)
            )
        }
    }
}

@Composable
fun ThirdWelcomeViewContent() {
    val uriHandler = LocalUriHandler.current
    Text(
        fontSize = 36.sp,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.using_nfc)
    )
    Text(
        modifier = Modifier.padding(PaddingValues(start = 28.dp, end = 28.dp, top = 20.dp)),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.to_use_it_put_the_card_on_top_of_your_phone)
    )
    Button(
        onClick = {
            uriHandler.openUri("https://satochip.io/product/satodime/")
        },
        modifier = Modifier
            .padding(10.dp)
            .height(40.dp)
            .width(150.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = LightGray,
            contentColor = Color.White
        )
    ) {
        Text(stringResource(R.string.more_info))
    }
}

@Preview(showBackground = true)
@Composable
fun ThirdWelcomePreview() {
    SatodimeTheme {
        ThirdWelcomeView(rememberNavController())
    }
}
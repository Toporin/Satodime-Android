package org.satochip.satodimeapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.BottomButton
import org.satochip.satodimeapp.ui.components.WelcomeViewTitle
import org.satochip.satodimeapp.ui.components.shared.SatoButton
import org.satochip.satodimeapp.ui.theme.LightGray
import org.satochip.satodimeapp.ui.theme.SatoGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.webviewActivityIntent

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
                .clickable {
                    navController.navigate(SatodimeScreen.Vaults.name) {
                        popUpTo(0)
                    }
                }
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
    val context = LocalContext.current
    val urlString = stringResource(id = R.string.onboardingVideo)

    Text(
        fontSize = 36.sp,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.usingNFC)
    )
    Text(
        modifier = Modifier.padding(PaddingValues(start = 28.dp, end = 28.dp, top = 20.dp)),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.toUseItText)
    )
    SatoButton(
        modifier = Modifier.width(150.dp),
        onClick = {
            webviewActivityIntent(
                url = "https://satochip.io/product/satodime/",
                context = context
            )
        },
        text = R.string.moreInfo,
        textColor = Color.White,
        buttonColor = LightGray
    )

    SatoButton(
        modifier = Modifier,
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlString)
            )
            val packageManager = context.packageManager
            val chooserIntent = Intent.createChooser(intent, "Open with")

            if (chooserIntent.resolveActivity(packageManager) != null) {
                context.startActivity(chooserIntent)
            } else {
                webviewActivityIntent(
                    url = urlString,
                    context = context
                )
            }
        },
        text = R.string.videoInfo,
        textColor = Color.White,
        buttonColor = SatoGreen
    )
}

@Preview(showBackground = true)
@Composable
fun ThirdWelcomePreview() {
    SatodimeTheme {
        ThirdWelcomeView(rememberNavController())
    }
}
package org.satochip.satodimeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.NextButton
import org.satochip.satodimeapp.ui.components.StepCircles
import org.satochip.satodimeapp.ui.components.WelcomeViewTitle
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen

@Composable
fun SecondWelcomeView(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Image(
            painter = painterResource(R.drawable.second_welcome_background),
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
            Spacer(modifier = Modifier.height(25.dp))
            SecondWelcomeViewContent()
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.transfer_ownership),
                contentDescription = null,
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.FillHeight
            )
            Spacer(modifier = Modifier.weight(1f))
            StepCircles(listOf(Color.Gray, Color.White, Color.Gray))
            NextButton {
                navController.navigate(SatodimeScreen.ThirdWelcome.name)
            }
        }
    }
}

@Composable
fun SecondWelcomeViewContent() {
    Text(
        fontSize = 32.sp,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.create_seal_gift_it)
    )
    Text(
        modifier = Modifier.padding(PaddingValues(start = 28.dp, end = 28.dp, top = 20.dp)),
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body1,
        text = stringResource(R.string.create_up_to_3_different_vaults)
    )
}

@Preview(showBackground = true)
@Composable
fun SecondWelcomePreview() {
    SatodimeTheme {
        SecondWelcomeView(rememberNavController())
    }
}
package org.satochip.satodime.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.satochip.satodime.R

@Composable
fun WelcomeViewTitle() {
    Image(
        painter = painterResource(R.drawable.top_welcome_logo),
        contentDescription = null,
        modifier = Modifier
            .padding(20.dp)
            .width(200.dp)
            .height(50.dp),
        contentScale = ContentScale.FillHeight
    )
}
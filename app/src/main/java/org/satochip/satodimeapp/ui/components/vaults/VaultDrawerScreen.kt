package org.satochip.satodimeapp.ui.components.vaults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.components.shared.GifImage
import org.satochip.satodimeapp.ui.components.shared.SatoButton
import org.satochip.satodimeapp.ui.theme.SatoLightGrey

@Composable
fun VaultDrawerScreen(
    closeSheet: () -> Unit,
    closeDrawerButton: Boolean = false,
    title: Int? = null,
    message: Int? = null,
    image: Int? = null,
    colorFilter: ColorFilter? = null,
    ) {
    Column(
        modifier = Modifier
            .height(350.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        title?.let {
            Text(
                text = stringResource(it),
                style = TextStyle(
                    color = SatoLightGrey,
                    fontSize = 26.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        image?.let {
            GifImage(
                modifier = Modifier.size(125.dp),
                image = image,
                colorFilter = colorFilter
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        message?.let {
            Text(
                text = stringResource(message),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (closeDrawerButton) {
            SatoButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = closeSheet,
                text = R.string.cancel,
                buttonColor = SatoLightGrey,
                textColor = Color.Black,
                shape = RoundedCornerShape(20)
            )
        }
    }
}
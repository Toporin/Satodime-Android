package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.theme.SatoToastGrey
import org.satochip.satodimeapp.ui.theme.SatoWarningOrange
import kotlin.time.Duration.Companion.seconds

@Composable
fun SatoToast(
    title: Int,
    text: Int,
    icon: Int,
    iconColor: Color = SatoWarningOrange
) {
    var showToast by remember {
        mutableStateOf(true)
    }
    if (showToast) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(
                    color = SatoToastGrey,
                    shape = RoundedCornerShape(50)
                ),
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GifImage(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(36.dp),
                    colorFilter = ColorFilter.tint(iconColor),
                    image = icon
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = title),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(id = text),
                        style = TextStyle(
                            fontSize = 16.sp,
                        )
                    )
                }
            }
        }
        LaunchedEffect(showToast) {
            delay(5.seconds)
            showToast = !showToast
        }
    }
}

@Preview
@Composable
private fun SatoToastPreview() {
    SatoToast(
        title = R.string.networkError,
        text = R.string.networkErrorMessage,
        icon = R.drawable.error_cross
    )
}
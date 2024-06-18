package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.satodimeapp.ui.theme.SatoGradientBlue
import org.satochip.satodimeapp.ui.theme.SatoGreen

@Composable
fun SatoRoundButton(
    modifier: Modifier = Modifier,
    text: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Pulsating() {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        color = SatoGradientBlue.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
            )
        }
        Box(
            modifier = modifier
                .size(80.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SatoGradientBlue, SatoGreen)
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(text),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            )
        }
    }
}

@Composable
fun Pulsating(pulseFraction: Float = 1f, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = ""    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}
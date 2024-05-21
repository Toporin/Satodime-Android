package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.theme.InfoDialogBackgroundColor

@Composable
fun Spinner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
                .background(
                    color = InfoDialogBackgroundColor
                ),
        )
        SpinnerComponent()
    }
}

@Composable
fun SpinnerComponent(
    image: Int = R.drawable.ic_sato_small,
) {
    var rotationAngle by remember { mutableStateOf(0f) }
    val rotationSpeedFactor = 2f
    val maxRotationAngle = 360f

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            rotationAngle += rotationSpeedFactor
            if (rotationAngle >= maxRotationAngle) {
                rotationAngle -= maxRotationAngle
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            rotationAngle += dragAmount * rotationSpeedFactor
                            rotationAngle %= maxRotationAngle
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationY = rotationAngle
                    }
            ) {
                GifImage(
                    modifier = Modifier.size(120.dp),
                    image = image,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}
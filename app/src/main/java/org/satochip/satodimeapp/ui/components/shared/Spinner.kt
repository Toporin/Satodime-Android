package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import org.satochip.satodimeapp.ui.theme.InfoDialogBackgroundColor

@Composable
fun Spinner(
    hasBackground: Boolean = false
) {
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
        CircularProgressIndicator(
            color = Color.White
        )
    }
}
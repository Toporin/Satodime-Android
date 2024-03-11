package org.satochip.satodimeapp.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.satochip.satodimeapp.ui.theme.LightGreen

@Composable
fun BottomButton(onClick: () -> Unit, text: String, color: Color = LightGreen, width: Dp = 160.dp) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .padding(40.dp)
            .height(40.dp)
            .width(width),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color,
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}
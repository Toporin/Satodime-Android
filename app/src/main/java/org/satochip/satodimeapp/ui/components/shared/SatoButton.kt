package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SatoButton(
    onClick: () -> Unit,
    text: Int,
    buttonColor: Color = MaterialTheme.colors.primary,
    textColor: Color = MaterialTheme.colors.secondary
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(10.dp)
            .height(40.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonColor,
        )
    ) {
        Text(
            text = stringResource(text),
            color = textColor
        )
    }
}

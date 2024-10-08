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
import org.satochip.satodimeapp.R

@Composable
fun SatoButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: Int,
    assetSymbol: String = "",
    buttonColor: Color = MaterialTheme.colors.primary,
    textColor: Color = MaterialTheme.colors.secondary,
    shape: RoundedCornerShape = RoundedCornerShape(50)
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = modifier
            .padding(10.dp)
            .height(40.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonColor,
        )
    ) {
        if(text == R.string.start) {
            Text(
                text = assetSymbol,
                color = textColor
            )
        } else {
            Text(
                text = stringResource(text) + assetSymbol,
                color = textColor
            )
        }
    }
}

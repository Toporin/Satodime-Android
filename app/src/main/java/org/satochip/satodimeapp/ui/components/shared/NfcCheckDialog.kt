package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.theme.SatoGreen

@Composable
fun NfcCheckDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    // explicit colors: the app typography uses white text, which is unreadable on the dialog background
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = Color.White,
        contentColor = Color.Black,
        title = { 
            Text(text = stringResource(R.string.nfc_disabled_title), color = Color.Black)
        },
        text = { 
            Text(text = stringResource(R.string.nfc_disabled_message), color = Color.DarkGray)
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(backgroundColor = SatoGreen, contentColor = Color.White)
            ) {
                Text(text = stringResource(R.string.open_settings), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel), color = Color.DarkGray)
            }
        }
    )
}

package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.satochip.satodimeapp.R

@Composable
fun NfcCheckDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(text = stringResource(R.string.nfc_disabled_title)) 
        },
        text = { 
            Text(text = stringResource(R.string.nfc_disabled_message)) 
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

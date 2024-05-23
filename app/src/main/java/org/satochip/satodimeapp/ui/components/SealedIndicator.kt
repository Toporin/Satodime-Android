package org.satochip.satodimeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.theme.SatoOrange
import org.satochip.satodimeapp.ui.theme.Teal200

@Composable
fun SealedIndicator(modifier: Modifier, isSealed: Boolean) {
    val color = if(isSealed) Teal200 else SatoOrange
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = if (isSealed) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
            tint = color,
            contentDescription = ""
        )
        Text(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            color = color,
            text = if (isSealed) stringResource(R.string.sealed) else stringResource(R.string.unsealed)
        )
    }
}
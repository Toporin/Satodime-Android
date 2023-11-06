package org.satochip.satodime.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(title: String, description: String) {
    Text(
        textAlign = TextAlign.Center,
        fontSize = 38.sp,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.secondary,
        text = title,
        modifier = Modifier.padding(start = 50.dp, end = 50.dp)
    )
    Text(
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.secondaryVariant,
        text = description,
        modifier = Modifier.padding(20.dp)
    )
}
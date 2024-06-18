package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.util.satoClickEffect

@Composable
fun HeaderRow(
    onClick: () -> Unit,
    titleText: Int? = null,
    message: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .satoClickEffect(
                    onClick = {
                        onClick()
                    }
                ),
            painter = painterResource(id = R.drawable.top_left_back),
            tint = Color.White,
            contentDescription = ""
        )
        titleText?.let {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = stringResource(titleText),
                modifier = Modifier.padding(start = 50.dp, end = 50.dp)
            )
        }
        Spacer(modifier = Modifier.width(32.dp))
    }
    message?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondaryVariant,
            text = stringResource(message),
            modifier = Modifier.padding(20.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
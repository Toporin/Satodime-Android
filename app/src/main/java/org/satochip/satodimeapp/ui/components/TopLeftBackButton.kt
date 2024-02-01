package org.satochip.satodimeapp.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TopLeftBackButton(navController: NavController, onClick: () -> Unit = {}) {
    IconButton(
        modifier = Modifier
            .offset(x = 20.dp, y = 40.dp),
        onClick = {
            onClick()
            navController.navigateUp()
        }
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            imageVector = Icons.Outlined.ArrowBack,
            tint = MaterialTheme.colors.secondary,
            contentDescription = ""
        )
    }
}
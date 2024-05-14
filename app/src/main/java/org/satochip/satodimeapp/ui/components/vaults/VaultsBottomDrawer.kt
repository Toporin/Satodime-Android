package org.satochip.satodimeapp.ui.components.vaults

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.satochip.satodimeapp.ui.components.shared.BottomSheet

@Composable
fun VaultsBottomDrawer(
    showSheet: MutableState<Boolean>,
    content: @Composable () -> Unit,
    ) {
    BottomSheet(showSheet = showSheet, modifier = Modifier) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
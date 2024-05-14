package org.satochip.satodimeapp.ui.components.shared

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showSheet: MutableState<Boolean>,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    if (!showSheet.value) {
        return
    } else {
        BottomSheet(
            modifier = modifier,
            showSheet = showSheet,
        ) {
            content()
        }
        ModalBottomSheet(
            modifier = modifier,
            containerColor = Color.White,
            sheetState = sheetState,
            onDismissRequest = {
                showSheet.value = !showSheet.value
            },
            shape = RoundedCornerShape(10.dp)
        ) {
            content()
        }
    }
}
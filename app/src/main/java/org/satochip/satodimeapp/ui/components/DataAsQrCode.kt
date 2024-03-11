package org.satochip.satodimeapp.ui.components

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.g0dkar.qrcode.QRCode
import org.satochip.satodimeapp.R

@Composable
fun DataAsQrCode(data: String) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .width(160.dp)
                .height(50.dp)
        ) {
            Text(
                color = MaterialTheme.colors.secondaryVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                text = stringResource(R.string.copyToClipboard)
            )
            val copiedToClipboardText = stringResource(R.string.copied_to_clipboard)
            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(data))
                        Toast.makeText(context, copiedToClipboardText, Toast.LENGTH_SHORT).show()
                    },
                imageVector = Icons.Outlined.ContentCopy,
                tint = Color.LightGray,
                contentDescription = null
            )
        }
        val qrCode = QRCode(data).render().getBytes()
        val bitmapQrCode = BitmapFactory.decodeByteArray(qrCode, 0, qrCode.size)
        Card(
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Image(
                modifier = Modifier.background(Color.White).padding(5.dp),
                bitmap = bitmapQrCode.asImageBitmap(),
                contentDescription = null,
            )
        }
    }
}
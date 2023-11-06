package org.satochip.satodime.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.satodime.R
import org.satochip.satodime.data.Vault
import org.satochip.satodime.util.getBalance

@Composable
fun VaultCard(
    index: Int,
    isSelected: Boolean,
    vault: Vault,
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
            .padding(10.dp)
            .selectable(
                selected = isSelected,
                onClick = { }
            ),
        shape = RoundedCornerShape(15.dp),
        border = if (isSelected) BorderStroke(2.dp, Color.DarkGray) else null
    ) {
        val background = if (!vault.isSealed) R.drawable.unsealed_card else when (index) {
            1 -> R.drawable.card1
            2 -> R.drawable.card2
            3 -> R.drawable.card3
            else -> {
                R.drawable.card1
            }
        }
        Box(
            modifier = Modifier
                .paint(
                    painterResource(id = background),
                    contentScale = ContentScale.FillBounds
                )
                .padding(20.dp)
        ) {
            Text(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                text = "0$index"
            )
            SealedIndicator(modifier = Modifier
                .padding(top = 45.dp)
                .width(if (vault.isSealed) 60.dp else 80.dp), isSealed = vault.isSealed)
            Text(
                modifier = Modifier.offset(120.dp, 5.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.body1,
                color = Color.LightGray,
                text = vault.address.substring(0, minOf(vault.address.length, 12)) + "..."
            )
            val copiedToClipboardText = stringResource(R.string.copied_to_clipboard)
            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .offset(220.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(vault.address))
                        Toast.makeText(context, copiedToClipboardText, Toast.LENGTH_SHORT).show()
                    },
                imageVector = Icons.Outlined.ContentCopy,
                tint = Color.LightGray,
                contentDescription = ""
            )
            Image(
                painter = painterResource(id = vault.coin.painterResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .offset(y = 110.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier.offset(x= (-8).dp, y = 135.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.body1,
                color = Color.LightGray,
                text = if (vault.isTesnet) "Testnet" else ""
            )
            Balance(modifier = Modifier.offset(130.dp, 70.dp), vault)
        }
    }
}

@Composable
fun Balance(modifier: Modifier, vault: Vault) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = Color.LightGray,
            text = stringResource(R.string.total_balance)
        )
        Text(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            color = Color.White,
            text = vault.currencyAmount
        )
        Text(
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = Color.LightGray,
            text = getBalance(vault)
        )
    }
}

@Composable
fun EmptyVaultCard(index: Int, isFirstEmptyVault: Boolean, onAddVault: () -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(2.dp, Color.Gray),
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.primaryVariant)
                .padding(20.dp)
        ) {
            Text(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                text = "0$index"
            )
            val addButtonColor = if(isFirstEmptyVault) Color.White else Color.Gray
            OutlinedButton(
                onClick = {
                    if (isFirstEmptyVault) {
                        onAddVault()
                    }
                },
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .size(40.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, addButtonColor),
                contentPadding = PaddingValues(0.dp),  //avoid the little icon
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = addButtonColor,
                    backgroundColor = MaterialTheme.colors.primaryVariant
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}
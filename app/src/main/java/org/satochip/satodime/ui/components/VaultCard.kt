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
import org.satochip.satodime.data.CardVault
import org.satochip.satodime.data.SlotState
import org.satochip.satodime.util.formatBalance

@Composable
fun VaultCard(
    index: Int,
    isSelected: Boolean,
    vault: CardVault,
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
        val background = if (vault.state == SlotState.UNSEALED) R.drawable.unsealed_card else when (index) {
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
            SealedIndicator(
                modifier = Modifier
                    .padding(top = 45.dp)
                    .width(if (vault.state == SlotState.SEALED) 60.dp else 80.dp),
                isSealed = (vault.state == SlotState.SEALED)
            )
            Text(
                modifier = Modifier.offset(120.dp, 5.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.body1,
                color = Color.LightGray,
                text = vault.nativeAsset.address.substring(0, minOf(vault.nativeAsset.address.length, 12)) + "..."
            )
            val copiedToClipboardText = stringResource(R.string.copied_to_clipboard)
            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .offset(220.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(vault.nativeAsset.address))
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
                text = if (vault.isTestnet) "Testnet" else ""
            )
            Balance(
                modifier = Modifier
                    .align(Alignment.BottomEnd), //.padding(10.dp),
                    //.offset(130.dp, 70.dp),
//                    .padding(top = 45.dp)
//                    .padding(end = 20.dp),
                vault
            )
        }
    }
}

@Composable
fun Balance(modifier: Modifier, vault: CardVault) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = Color.LightGray,
            text = stringResource(R.string.total_balance) // todo change
        )
        Text(
            fontSize = 18.sp, //24.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            color = Color.White,
            text = formatBalance(
                balanceString = vault.nativeAsset.balance,
                decimalsString = vault.nativeAsset.decimals,
                symbol = vault.nativeAsset.symbol)//vault.currencyAmount
        )
        Text(
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.body1,
            color = Color.LightGray,
            text = formatBalance(
                balanceString = vault.nativeAsset.valueInSecondCurrency,
                decimalsString = "0",
                symbol = vault.nativeAsset.secondCurrency)
        )
    }
}

@Composable
fun EmptyVaultCard(index: Int, isFirstEmptyVault: Boolean, onAddVault: (Int) -> Unit) {
    //todo: remove isFirstEmptyVault
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
                    onAddVault(index)
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
package org.satochip.satodimeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.Coin
import org.satochip.satodimeapp.ui.components.Title
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen

private const val TAG = "SelectBlockchainView"

@Composable
fun SelectBlockchainView(navController: NavController, selectedVault: Int) {
    // todo display vault index in view!
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primaryVariant)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Title(stringResource(R.string.selectTheBlockchain), stringResource(R.string.selectTheCrypto))
        SelectBlockchainList(navController, selectedVault)
    }
}

@Composable
fun SelectBlockchainList(navController: NavController, selectedVault: Int) {
    Coin.values().filter { it != Coin.UNKNOWN }.forEach {
        BlockchainCard(
            painter = painterResource(it.painterResourceId),
            title = "${it.label} (${it.name})",
            onSelect = {
                navController.navigate(SatodimeScreen.CreateVault.name
                        + "/${it.name}/$selectedVault")
            }
        )
    }

}

@Composable
fun BlockchainCard(painter: Painter, title: String, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = 5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .padding(10.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier.padding(15.dp),
                fontSize = 20.sp,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondaryVariant,
                text = title
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onSelect
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = ""
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectBlockchainPreview() {
    SatodimeTheme {
        SelectBlockchainView(rememberNavController(), 1)
    }
}
package org.satochip.satodimeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.Coin
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimeScreen

private const val TAG = "SelectBlockchainView"

@Composable
fun SelectBlockchainView(navController: NavController, selectedVault: Int) {
    // todo display vault index in view!
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primaryVariant)
            .padding(10.dp)
    ) {
        HeaderRow(
            onClick = {
                navController.navigateUp()
            },
            titleText = R.string.selectTheBlockchain,
            message = R.string.selectTheCrypto
        )
        SelectBlockchainList(navController, selectedVault)
    }
}

@Composable
fun SelectBlockchainList(navController: NavController, selectedVault: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        Coin.values().filter { it != Coin.UNKNOWN }.forEach {
            item {
                BlockchainCard(
                    painter = painterResource(it.painterResourceId),
                    title = "${it.label} (${it.name})",
                    onSelect = {
                        navController.popBackStack()
                        navController.navigate(
                            SatodimeScreen.CreateVault.name
                                    + "/${it.name}/$selectedVault"
                        )
                    }
                )
            }
        }
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
                .clickable {
                    onSelect()
                }
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
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
                painter = painterResource(id = R.drawable.arrow_right_circle),
                tint = MaterialTheme.colors.secondary,
                contentDescription = ""
            )
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
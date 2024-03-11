package org.satochip.satodimeapp.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.satochip.javacryptotools.coins.Asset
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.ui.MenuView
import org.satochip.satodimeapp.ui.theme.InfoDialogBackgroundColor
import org.satochip.satodimeapp.ui.theme.LightBlue
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.sanitizeNftImageUrlString
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "InfoDialog"

@Composable
fun NftDialog(openDialogCustom: MutableState<Boolean>,
              asset: Asset,
) {
    Dialog(onDismissRequest = {
        openDialogCustom.value = false
    }) {
        NftDialogUI(openDialogCustom = openDialogCustom,
            asset = asset
        )
    }
}

@Composable
fun NftDialogUI(modifier: Modifier = Modifier,
                 openDialogCustom: MutableState<Boolean>,
                 asset: Asset
) {
    val uriHandler = LocalUriHandler.current
    Card(
        //shape = MaterialTheme.shapes.medium,
        shape = RoundedCornerShape(20.dp),
        // modifier = modifier.size(280.dp, 240.dp)
        modifier = Modifier
            .padding(10.dp, 5.dp, 10.dp, 10.dp),
        //.background(InfoDialogBackgroundColor),
        elevation = 8.dp,

        ) {
        Column(
            modifier = modifier
                .background(InfoDialogBackgroundColor)
                .padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // TITLE
            Text(
                text = asset.nftName ?: asset.contract ?: "NFT",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(),
                fontSize = 30.sp,
                style = MaterialTheme.typography.body1,
                color = Color.White, //MaterialTheme.colors.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // IMAGE
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(sanitizeNftImageUrlString(asset.nftImageLink ?: "")) //.data(asset.nftImageLink ?: "")
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_sato_small),
                error = painterResource(R.drawable.ic_sato_small),
                contentDescription = (asset.nftName ?: asset.contract ?: "NFT"),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(250.dp)
                    .clickable(
                        onClick = {
                            uriHandler.openUri(asset.nftExplorerLink ?: asset.explorerLink ?: "")
                        },
                        onClickLabel = "open asset in exlorer"
                    ),
            )
            // DESCRIPTION
            Text(
                text = asset.nftDescription ?: asset.tokenid ?: "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                color = Color.LightGray, //MaterialTheme.colors.primary, //Color.Black,
                modifier = Modifier
                    .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                    .fillMaxWidth(),
            )

            // CLOSE BUTTON
            Button(
                onClick = {
                    openDialogCustom.value = false
                },
                modifier = Modifier
                    .padding(20.dp)
                    .height(40.dp)
                    .width(160.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LightBlue,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.close))
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun NftDialogUIPreview() {
    SatodimeTheme {
        NftDialogUI(modifier= Modifier,
            openDialogCustom= remember {mutableStateOf(false)},
            asset = Asset()
        )
    }
}
package org.satochip.satodime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.satochip.satodime.R
import org.satochip.satodime.ui.MenuView
import org.satochip.satodime.ui.theme.InfoDialogBackgroundColor
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.viewmodels.SharedViewModel

private const val TAG = "InfoDialog"

@Composable
fun InfoDialog(openDialogCustom: MutableState<Boolean>,
               title: String,
               message: String,
               buttonTitle: String,
               buttonAction: () -> Unit,
               isActionButtonVisible: Boolean = true
) {
    Dialog(onDismissRequest = {
        openDialogCustom.value = false
    }) {
        InfoDialogUI(openDialogCustom = openDialogCustom,
            title = title,
            message = message,
            buttonTitle = buttonTitle,
            buttonAction = buttonAction,
            isActionButtonVisible = isActionButtonVisible
        )
    }
}

@Composable
fun InfoDialogUI(modifier: Modifier = Modifier,
                 openDialogCustom: MutableState<Boolean>,
                 title: String,
                 message: String,
                 buttonTitle: String,
                 buttonAction: () -> Unit,
                 isActionButtonVisible: Boolean
) {
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
                .background(InfoDialogBackgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                // TITLE
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(),
                    fontSize = 30.sp,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.primary, //Color.White, //MaterialTheme.colors.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // MESSAGE
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.primary, //Color.Black,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                        .fillMaxWidth(),
                )
            }

            //BUTTONS
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(Color.Gray), // todo
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                // CLOSE BUTTON
                TextButton(onClick = {
                    openDialogCustom.value = false
                }) {
                    Text(
                        stringResource(R.string.close),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.secondary,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }

                // ACTION BUTTON (optional)
                if (isActionButtonVisible) {
                    TextButton(onClick = {
                        openDialogCustom.value = false
                        buttonAction()
                    }) {
                        Text(
                            buttonTitle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                        )
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoDialogUIPreview() {
    SatodimeTheme {
        InfoDialogUI(modifier= Modifier,
            openDialogCustom= remember {mutableStateOf(false)},
            title = "Title",
            message= "Message",
            buttonTitle= "Button",
            buttonAction= {},
            isActionButtonVisible= true)
    }
}
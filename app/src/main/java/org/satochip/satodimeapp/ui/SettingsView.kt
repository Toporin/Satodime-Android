package org.satochip.satodimeapp.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.data.Currency
import org.satochip.satodimeapp.ui.components.shared.HeaderRow
import org.satochip.satodimeapp.ui.components.shared.SatoButton
import org.satochip.satodimeapp.ui.theme.LightGreen
import org.satochip.satodimeapp.ui.theme.SatoGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.SatodimePreferences
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

@Composable
fun SettingsView(navController: NavController, viewModel: SharedViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current as Activity
    val settings = context.getSharedPreferences("satodime", Context.MODE_PRIVATE)
    var showCurrenciesMenu by remember { mutableStateOf(false) }
    var starterIntro by remember {
        mutableStateOf(settings.getBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, true))
    }
    var debugMode by remember {
        mutableStateOf(settings.getBoolean(SatodimePreferences.VERBOSE_MODE.name, false))
    }
    val savedCurrency by remember {
        mutableStateOf(settings.getString(SatodimePreferences.SELECTED_CURRENCY.name, "USD"))
    }
    var selectedCurrency = savedCurrency //savedCurrency.value

    // todo show list of ownership card

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        HeaderRow(
            onClick = {
                navController.navigateUp()
            },
            titleText = R.string.settings,
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 75.dp,
                bottom = 20.dp,
                start = 20.dp,
                end = 20.dp
            )// start just below TopLeftButton
            //.padding(75.dp)//.padding(20.dp)
            .verticalScroll(state = scrollState)
    ) {
        // IMAGE
        Image(
            painter = painterResource(id = R.drawable.tools),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp)
                .height(200.dp),
            contentScale = ContentScale.FillHeight
        )

        // CURRENCY SELECTOR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(Modifier.background(MaterialTheme.colors.primary)) {
                Text(
                    modifier = Modifier
                        .padding(15.dp),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.currency)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .padding(top = 15.dp),
                    textAlign = TextAlign.Start,
                    color = LightGreen,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.body1,
                    text = Currency.valueOf(selectedCurrency ?: "USD").name
                )
                Box {
                    IconButton(
                        modifier = Modifier.padding(10.dp),
                        onClick = { showCurrenciesMenu = !showCurrenciesMenu }) {
                        Icon(Icons.Default.List, tint = LightGreen, contentDescription = "")
                    }
                    DropdownMenu(
                        expanded = showCurrenciesMenu,
                        onDismissRequest = { showCurrenciesMenu = false }) {
                        Currency.values().forEach {
                            DropdownMenuItem(onClick = {
                                selectedCurrency = it.name
                                showCurrenciesMenu = false
                            }) {
                                Text(text = it.name, color = MaterialTheme.colors.secondary)
                            }
                        }
                    }
                }
            }
        }

        // STARTER INTRO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(Modifier.background(MaterialTheme.colors.primary)) {
                Text(
                    modifier = Modifier
                        .padding(15.dp),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.starterIntro)
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    modifier = Modifier.padding(10.dp),
                    checked = starterIntro,
                    onCheckedChange = { starterIntro = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.Gray,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        // DEBUG MODE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(Modifier.background(MaterialTheme.colors.primary)) {
                Text(
                    modifier = Modifier
                        .padding(15.dp),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.body1,
                    text = "Debug mode"// todo i18n
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    modifier = Modifier.padding(10.dp),
                    checked = debugMode,
                    onCheckedChange = { debugMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White,
                        uncheckedThumbColor = Color.LightGray,// todo more visible
                        uncheckedTrackColor = Color.Gray,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        // SHOW LOGS BUTTON
        SatoButton(
            onClick = {
                navController.navigate(
                    SatodimeScreen.ShowLogsView.name
                )
            },
            text = R.string.showLogs,
        )
        Spacer(modifier = Modifier.weight(1f))

        // SEND FEEDBACK
        SatoButton(
            onClick = {
                viewModel.sendMail(
                    activity = context,
                    subject = "Satodime-Android - Feedback",
                    senderEmail = "support@satochip.io"
                )
            },
            text = R.string.sendFeedback,
            buttonColor = SatoGreen
        )
        Spacer(modifier = Modifier.weight(1f))

        // APPLY BUTTON
        Button(
            onClick = {
                settings.edit()
                    .putBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, starterIntro)
                    .apply()
                settings.edit()
                    .putBoolean(SatodimePreferences.VERBOSE_MODE.name, debugMode).apply()
                settings.edit()
                    .putString(
                        SatodimePreferences.SELECTED_CURRENCY.name,
                        selectedCurrency ?: "USD"
                    ).apply()

                navController.navigateUp()
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(modifier = Modifier.weight(1f))

    }
}

@Preview(showBackground = true)
@Composable
fun SettingsViewPreview() {
    SatodimeTheme {
        SettingsView(
            rememberNavController(),
            viewModel(factory = SharedViewModel.Factory)
        )
    }
}
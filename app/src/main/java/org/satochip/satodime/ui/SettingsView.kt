package org.satochip.satodime.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.satochip.satodime.R
import org.satochip.satodime.data.Currency
import org.satochip.satodime.data.defaultCurrency
import org.satochip.satodime.services.NFCCardService
import org.satochip.satodime.services.SatodimeStore
import org.satochip.satodime.ui.components.TopLeftBackButton
import org.satochip.satodime.ui.theme.LightGreen
import org.satochip.satodime.ui.theme.SatodimeTheme
import org.satochip.satodime.util.SatodimePreferences

@Composable
fun SettingsView(navController: NavController) {
    val couroutineScope = rememberCoroutineScope() // todo remove
    val context = LocalContext.current as Activity
    //val settings = context.getSharedPreferences(SatodimePreferences::class.simpleName, 0)
    val settings = context.getSharedPreferences("satodime", Context.MODE_PRIVATE)
    val satodimeStore = SatodimeStore(context) // todo remove
    var showCurrenciesMenu by remember { mutableStateOf(false) }
    var starterIntro by remember {
        mutableStateOf(settings.getBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name,true))
    }
    var debugMode by remember {
        mutableStateOf(settings.getBoolean(SatodimePreferences.DEBUG_MODE.name,false))
    }
    //val savedCurrency = satodimeStore.selectedCurrency.collectAsState(initial = defaultCurrency.name)
    val savedCurrency by remember {
        mutableStateOf(settings.getString(SatodimePreferences.SELECTED_CURRENCY.name,"USD"))
    }
    var selectedCurrency = savedCurrency //savedCurrency.value


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopLeftBackButton(navController)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // TITLE
        Text(
            modifier = Modifier.padding(top = 30.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary,
            text = stringResource(R.string.settings)
        )
        Image(
            painter = painterResource(id = R.drawable.tools),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
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
                    text = stringResource(R.string.starter_intro)
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
        Button(
            onClick = { },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(125.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
            )
        ) {
            Text(stringResource(R.string.show_logs), color = MaterialTheme.colors.secondary)
        }
        Spacer(modifier = Modifier.weight(1f))
        // APPLY BUTTON
        Button(
            onClick = {
//                couroutineScope.launch {
//                    satodimeStore.saveSelectedCurrency(selectedCurrency)
//                }
                settings.edit()
                    .putBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, starterIntro).apply()
                settings.edit()
                    .putBoolean(SatodimePreferences.DEBUG_MODE.name, debugMode).apply()
                settings.edit()
                    .putString(SatodimePreferences.SELECTED_CURRENCY.name, selectedCurrency ?: "USD").apply()

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
        SettingsView(rememberNavController())
    }
}
package org.satochip.satodimeapp.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.satochip.satodimeapp.R
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.components.TopLeftBackButton
import org.satochip.satodimeapp.ui.theme.LightBlue
import org.satochip.satodimeapp.util.SatodimePreferences
import java.util.logging.Level

private const val TAG = "ShowLogsView"

@Composable
fun ShowLogsView(
    navController: NavController,
) {
    val context = LocalContext.current as Activity
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val settings = context.getSharedPreferences("satodime", Context.MODE_PRIVATE)
    var verboseMode by remember {
        mutableStateOf(settings.getBoolean(SatodimePreferences.VERBOSE_MODE.name,false))
    }

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
            text = stringResource(R.string.LogsTitle)
        )
        // VERBOSE MODE
        Row {
            Text(
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.secondary,
                text = "LogLevel: ${
                    if (verboseMode) {
                        "verbose "
                    } else {
                        "warning only "
                    }
                }"
            )
            val toastText = stringResource(R.string.copied_to_clipboard)
            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        var txt = ""
                        for (log in SatoLog.logList) {
                            val logString =
                                "${log.date}; ${log.level.name}; ${log.tag}; ${log.msg} \n"
                            txt = txt + logString
                        }
                        clipboardManager.setText(AnnotatedString(txt))
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                    },
                imageVector = Icons.Outlined.ContentCopy,
                tint = Color.LightGray,
                contentDescription = "Copy logs to clipboard"
            )
        }

        LazyColumn {
            items(SatoLog.logList) { log ->

                // DATE
                Text(
                    modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = "${log.date.toString()}"
                )

                // LEVEl
                Text(
                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    //text = "\uD83D\uDD34 ${log.level.name} - ${log.tag}"
                    text = "${getEmojiFromLevel(log.level)} ${log.level.name} - ${log.tag}"
                )

                // TAG
//                Text(
//                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
//                    textAlign = TextAlign.Start,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    style = MaterialTheme.typography.body1,
//                    color = MaterialTheme.colors.secondary,
//                    text = log.tag
//                )

                // MSG
                Text(
                    modifier = Modifier.padding(top = 5.dp, bottom = 10.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    text = log.msg
                )

                Divider(color = Color.LightGray)
            }
        }

        // CLOSE BUTTON
        Button(
            onClick = {
                navController.navigateUp()
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
                .width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightBlue, //LightGreen,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.close))
        }
    }
}

private fun getEmojiFromLevel(level: Level): String {
    return when(level){
        Level.SEVERE -> "\uD83D\uDD34"
        Level.WARNING -> "\uD83D\uDFE1"
        Level.INFO -> "\uD83D\uDD35"
        Level.CONFIG -> "\uD83D\uDFE2"
        else -> "\uD83D\uDD34" // should not happen
    }
}

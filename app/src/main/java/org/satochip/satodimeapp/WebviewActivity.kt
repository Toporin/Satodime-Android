package org.satochip.satodimeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.satochip.satodimeapp.data.IntentConstants
import org.satochip.satodimeapp.ui.components.shared.Spinner
import org.satochip.satodimeapp.ui.components.shared.WebViewComponent
import org.satochip.satodimeapp.ui.theme.InfoDialogBackgroundColor
import org.satochip.satodimeapp.ui.theme.MoreInfoButtonColor
import org.satochip.satodimeapp.ui.theme.SatodimeTheme

class WebviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val url = remember {
                mutableStateOf("")
            }
            val isSpinnerActive = remember {
                mutableStateOf(true)
            }

            intent.getStringExtra(IntentConstants.URL_STRING.name)?.let {
                url.value = it
            }
            intent.removeExtra(IntentConstants.URL_STRING.name)

            SatodimeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MoreInfoButtonColor
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                color = InfoDialogBackgroundColor
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            modifier = Modifier.background(
                                color = Color.Transparent
                            ),
                            onClick = { finish() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.top_left_back),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(24.dp),
                                tint = Color.White,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        WebViewComponent(
                            url = url,
                            isSpinnerActive = isSpinnerActive
                        )

                        if (isSpinnerActive.value) {
                            Spinner()
                        }
                    }
                }
            }
        }
    }
}
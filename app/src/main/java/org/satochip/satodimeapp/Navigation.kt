package org.satochip.satodimeapp

import CardInfoView
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.AddFundsView
import org.satochip.satodimeapp.ui.AuthenticCardView
import org.satochip.satodimeapp.ui.CongratsVaultCreatedView
import org.satochip.satodimeapp.ui.CreateVaultView
import org.satochip.satodimeapp.ui.ExpertModeView
import org.satochip.satodimeapp.ui.FirstWelcomeView
import org.satochip.satodimeapp.ui.MenuView
import org.satochip.satodimeapp.ui.ResetCongratsView
import org.satochip.satodimeapp.ui.ResetWarningView
import org.satochip.satodimeapp.ui.SecondWelcomeView
import org.satochip.satodimeapp.ui.SelectBlockchainView
import org.satochip.satodimeapp.ui.SettingsView
import org.satochip.satodimeapp.ui.ShowLogsView
import org.satochip.satodimeapp.ui.ShowPrivateKeyDataView
import org.satochip.satodimeapp.ui.ShowPrivateKeyView
import org.satochip.satodimeapp.ui.ThirdWelcomeView
import org.satochip.satodimeapp.ui.TransferOwnershipView
import org.satochip.satodimeapp.ui.UnsealCongratsView
import org.satochip.satodimeapp.ui.UnsealWarningView
import org.satochip.satodimeapp.ui.VaultsView
import org.satochip.satodimeapp.ui.components.AcceptOwnershipView
import org.satochip.satodimeapp.util.NavigationParam
import org.satochip.satodimeapp.util.SatodimePreferences
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.util.apiKeys
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val TAG = "Navigation"

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Navigation() {
    val context = LocalContext.current as Activity
    //Lock screen orientation to portrait
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val settings = context.getSharedPreferences("satodime", Context.MODE_PRIVATE)
    SatoLog.setVerboseMode(
        settings.getBoolean(
            SatodimePreferences.VERBOSE_MODE.name,
            false
        )
    ) // use verbose logs?
    val startDestination =
        if (settings.getBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, true)) {
            SatoLog.d(TAG, "Navigation: Start onboarding screens!")
            settings.edit().putBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, false).apply()
            SatodimeScreen.FirstWelcome.name
        } else {
            SatoLog.d(TAG, "Navigation: Skip onboarding screens!")
            SatodimeScreen.Vaults.name
        }

    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModel.Factory)

    if (sharedViewModel.isAskingForCardOwnership) {
        SatoLog.d(TAG, "Navigation: Card needs ownership!")
        navController.navigate(SatodimeScreen.AcceptOwnershipView.name)

    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ONBOARDING
        composable(route = SatodimeScreen.FirstWelcome.name) {
            FirstWelcomeView(navController)
        }
        composable(route = SatodimeScreen.SecondWelcome.name) {
            SecondWelcomeView(navController)
        }
        composable(route = SatodimeScreen.ThirdWelcome.name) {
            ThirdWelcomeView(navController)
        }
        composable(route = SatodimeScreen.AcceptOwnershipView.name) {
            AcceptOwnershipView(navController, sharedViewModel)
        }

        // SEAL
        composable(route = SatodimeScreen.SelectBlockchain.name + "/{${NavigationParam.SelectedVault.name}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            SelectBlockchainView(navController, selectedVault)
        }
        composable(route = SatodimeScreen.CreateVault.name + "/{${NavigationParam.SelectedCoin.name}}"
                + "/{${NavigationParam.SelectedVault.name}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedCoin.name) {
                    type = NavType.StringType
                },
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedCoin = it.arguments?.getString(NavigationParam.SelectedCoin.name)!!
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            CreateVaultView(navController, sharedViewModel, selectedVault, selectedCoin)
        }
        composable(route = SatodimeScreen.CongratsVaultCreated.name + "/{${NavigationParam.SelectedCoin}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedCoin.name) {
                    type = NavType.StringType
                }
            )
        ) {
            val selectedCoin = it.arguments?.getString(NavigationParam.SelectedCoin.name)!!
            CongratsVaultCreatedView(navController, selectedCoin)
        }

        // SHOW VAULTS
        composable(route = SatodimeScreen.Vaults.name) {
            VaultsView(
                navController,
                sharedViewModel,
                onClick = { selectedVault ->
                    val bitcoinOnly =
                        settings.getBoolean(SatodimePreferences.BITCOIN_ONLY.name, false)
                    if (bitcoinOnly) {
                        navController.navigate(
                            SatodimeScreen.CreateVault.name
                                    + "/BTC/$selectedVault"
                        )
                    } else {
                        navController.navigate(
                            SatodimeScreen.SelectBlockchain.name
                                    + "/${selectedVault}"
                        )
                    }
                }
            )
        }
        composable(route = SatodimeScreen.AddFunds.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            AddFundsView(
                navController,
                sharedViewModel,
                selectedVault,
            )
        }

        // UNSEAL
        composable(route = SatodimeScreen.UnsealWarning.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            UnsealWarningView(navController, sharedViewModel, selectedVault)
        }
        composable(route = SatodimeScreen.UnsealCongrats.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            UnsealCongratsView(navController, sharedViewModel, selectedVault)
        }

        // SHOW PRIVKEY
        composable(route = SatodimeScreen.ShowPrivateKey.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ShowPrivateKeyView(navController, sharedViewModel, selectedVault)
        }

        // SHOW PRIVKEY DETAILS
        composable(
            route = SatodimeScreen.ShowPrivateKeyData.name
                    + "/{${NavigationParam.SelectedVault.name}}"
                    + "/{${NavigationParam.Label.name}}" // specify format legacy/wif/entropy
                    + "?${NavigationParam.Data.name}={${NavigationParam.Data.name}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                },
                navArgument(NavigationParam.Label.name) {
                    type = NavType.StringType
                },
                navArgument(NavigationParam.Data.name) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
            )
        ) {
            val label = it.arguments?.getString(NavigationParam.Label.name) ?: ""
            val data = it.arguments?.getString(NavigationParam.Data.name) ?: ""
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ShowPrivateKeyDataView(navController, sharedViewModel, selectedVault, label, data)
        }

        // EXPERT MODE 
        // TODO merge with create Vault
        composable(route = SatodimeScreen.ExpertMode.name + "/{${NavigationParam.SelectedCoin.name}}"
                + "/{${NavigationParam.SelectedVault.name}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedCoin.name) {
                    type = NavType.StringType
                },
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedCoin = it.arguments?.getString(NavigationParam.SelectedCoin.name)!!
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ExpertModeView(navController, sharedViewModel, selectedVault, selectedCoin)
        }

        // RESET
        composable(route = SatodimeScreen.ResetWarningView.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ResetWarningView(navController, sharedViewModel, selectedVault)
        }
        composable(route = SatodimeScreen.ResetCongratsView.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ResetCongratsView(navController, selectedVault)
        }

        // MENU
        composable(route = SatodimeScreen.MenuView.name) {
            MenuView(navController, sharedViewModel)
        }
        // CARDINFO
        composable(route = SatodimeScreen.CardInfoView.name) {
            CardInfoView(navController, sharedViewModel)
        }
        // TRANSFERT
        composable(route = SatodimeScreen.TransferOwnershipView.name) {
            TransferOwnershipView(navController, sharedViewModel)
        }
        // SETTINGS
        composable(route = SatodimeScreen.SettingsView.name) {
            SettingsView(navController, sharedViewModel)
        }
        // LOGS
        composable(route = SatodimeScreen.ShowLogsView.name) {
            ShowLogsView(navController)
        }
        // AUTHENTICITY
        composable(route = SatodimeScreen.AuthenticCardView.name) {
            AuthenticCardView(navController)
        }
    }
}
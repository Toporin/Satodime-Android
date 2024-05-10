package org.satochip.satodimeapp

import CardInfoView
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.satochip.satodimeapp.services.SatoLog
import org.satochip.satodimeapp.ui.*
import org.satochip.satodimeapp.ui.components.AcceptOwnershipView
import org.satochip.satodimeapp.util.NavigationParam
import org.satochip.satodimeapp.util.SatodimePreferences
import org.satochip.satodimeapp.util.SatodimeScreen
import org.satochip.satodimeapp.viewmodels.SharedViewModel

private const val TAG = "Navigation"

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Navigation() {
    val context = LocalContext.current as Activity
    //Lock screen orientation to portrait
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val settings = context.getSharedPreferences("satodime", Context.MODE_PRIVATE)
    SatoLog.setVerboseMode(settings.getBoolean(SatodimePreferences.VERBOSE_MODE.name,false)) // use verbose logs?
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

    if(sharedViewModel.isAskingForCardOwnership) {
        SatoLog.d(TAG, "Navigation: Card needs ownership!")
        AcceptOwnershipView(navController, sharedViewModel)
        return
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

        // SEAL
        composable(route = SatodimeScreen.SelectBlockchain.name+ "/{${NavigationParam.SelectedVault.name}}",
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
            VaultsView(navController, sharedViewModel)
        }
        composable(route = SatodimeScreen.AddFunds.name + "/{${NavigationParam.SelectedVault}}",
            arguments = listOf(
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                }
            )
        ) {
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            AddFundsView(navController, sharedViewModel, selectedVault)
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
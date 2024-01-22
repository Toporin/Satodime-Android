package org.satochip.satodime

import CardInfoView
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.satochip.satodime.ui.*
import org.satochip.satodime.ui.components.AcceptOwnershipView
import org.satochip.satodime.util.NavigationParam
import org.satochip.satodime.util.SatodimePreferences
import org.satochip.satodime.util.SatodimeScreen
import org.satochip.satodime.viewmodels.SharedViewModel
import org.satochip.satodime.viewmodels.VaultsViewModel

private const val TAG = "Navigation"

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Navigation() {
    val context = LocalContext.current as Activity
    //Lock screen orientation to portrait
    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val settings = context.getSharedPreferences(SatodimePreferences::class.simpleName, 0)
    val startDestination =
        if (settings.getBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, true)) {
            settings.edit().putBoolean(SatodimePreferences.FIRST_TIME_LAUNCH.name, false).apply()
            SatodimeScreen.FirstWelcome.name
        } else {
            SatodimeScreen.Vaults.name
        }

    val navController = rememberNavController()
    val vaultsViewModel: VaultsViewModel = viewModel(factory = VaultsViewModel.Factory)
    val sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModel.Factory)

    //if(sharedViewModel.isAskingForCardOwnership && sharedViewModel.isCardConnected && sharedViewModel.isReadingFinished) {
    if(sharedViewModel.isAskingForCardOwnership) {
        Log.d(TAG, "Navigation: Card needs ownership!")
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
            VaultsView(navController, vaultsViewModel, sharedViewModel)
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
        //todo simplify navigation, only provide vaultIndex & privdata type (legacy, WIF, entropy)
        composable(
            route = SatodimeScreen.ShowPrivateKeyData.name + "/{${NavigationParam.Label.name}}"
                    + "/{${NavigationParam.SelectedVault}}"
                    + "?${NavigationParam.SubLabel.name}={${NavigationParam.SubLabel.name}}"
                    + "&${NavigationParam.Data.name}={${NavigationParam.Data.name}}",
            arguments = listOf(
                navArgument(NavigationParam.Label.name) {
                    type = NavType.StringType
                },
                navArgument(NavigationParam.SelectedVault.name) {
                    type = NavType.IntType
                },
                navArgument(NavigationParam.SubLabel.name) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument(NavigationParam.Data.name) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
            )
        ) {
            val label = it.arguments?.getString(NavigationParam.Label.name) ?: ""
            val subLabel = it.arguments?.getString(NavigationParam.SubLabel.name) ?: ""
            val data = it.arguments?.getString(NavigationParam.Data.name) ?: ""
            val selectedVault = it.arguments?.getInt(NavigationParam.SelectedVault.name)!!
            ShowPrivateKeyDataView(navController, sharedViewModel, selectedVault, label, data, subLabel)
        }
        // EXPERT MODE 
        // TODO: merge with create Vault
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
            MenuView(navController)
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
            SettingsView(navController)
        }
        // AUTHENTICITY
        composable(route = SatodimeScreen.AuthenticCardView.name) {
            AuthenticCardView(navController)
        }
        composable(route = SatodimeScreen.FakeCardView.name) {
            FakeCardView(navController)
        }
    }
}
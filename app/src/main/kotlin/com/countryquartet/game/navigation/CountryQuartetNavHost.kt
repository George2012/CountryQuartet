package com.countryquartet.game.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.countryquartet.game.ui.screens.CountriesScreen
import com.countryquartet.game.ui.screens.GameScreen
import com.countryquartet.game.ui.screens.HowToPlayScreen
import com.countryquartet.game.ui.screens.MainMenuScreen
import com.countryquartet.game.ui.screens.SettingsScreen
import com.countryquartet.game.ui.screens.SplashScreen

/** Root composable: owns the navigation graph for the whole app. */
@Composable
fun CountryQuartetNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onReady = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                onPlay = { navController.navigate(Routes.GAME) },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                onCountries = { navController.navigate(Routes.COUNTRIES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.GAME) {
            GameScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.COUNTRIES) {
            CountriesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

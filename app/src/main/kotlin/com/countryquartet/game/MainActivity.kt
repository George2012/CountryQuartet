package com.countryquartet.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.countryquartet.game.navigation.CountryQuartetNavHost
import com.countryquartet.game.ui.theme.CountryQuartetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CountryQuartetTheme {
                CountryQuartetNavHost()
            }
        }
    }
}

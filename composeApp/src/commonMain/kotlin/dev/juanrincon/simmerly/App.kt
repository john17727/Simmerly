package dev.juanrincon.simmerly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.juanrincon.simmerly.navigation.auth.App
import dev.juanrincon.simmerly.navigation.auth.AuthNavigationViewModel
import dev.juanrincon.simmerly.navigation.auth.Login
import dev.juanrincon.simmerly.navigation.auth.Splash
import dev.juanrincon.simmerly.splash.presentation.SplashScreen
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import dev.juanrincon.simmerly.welcome.presentation.WelcomeContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    SimmerlyTheme {
        SimmerlyApp(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun SimmerlyApp(
    viewModel: AuthNavigationViewModel = koinViewModel(),
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate(App)
        } else {
            navController.navigate(Login)
        }
    }
    NavHost(
        navController = navController,
        startDestination = Splash,
        modifier = modifier
    ) {
        composable<Splash> {
            SplashScreen()
        }
        composable<Login> {
            WelcomeContent()
        }
        composable<App> {
            Text("app")
        }
    }
}

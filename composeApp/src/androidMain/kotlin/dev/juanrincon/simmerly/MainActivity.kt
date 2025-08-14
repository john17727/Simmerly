package dev.juanrincon.simmerly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.juanrincon.simmerly.navigation.auth.DefaultRootComponent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            storeFactory = DefaultStoreFactory()
        )

        setContent {
            App(root)
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}
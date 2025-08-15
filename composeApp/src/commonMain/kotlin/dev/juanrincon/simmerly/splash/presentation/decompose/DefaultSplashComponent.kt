package dev.juanrincon.simmerly.splash.presentation.decompose

import com.arkivanov.decompose.ComponentContext

class DefaultSplashComponent(
    componentContext: ComponentContext
): SplashComponent, ComponentContext by componentContext {
}
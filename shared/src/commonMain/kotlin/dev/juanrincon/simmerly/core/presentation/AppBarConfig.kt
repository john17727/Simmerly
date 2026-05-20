package dev.juanrincon.simmerly.core.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class AppBarAction(
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
)

@Immutable
data class AppBarConfig(
    val title: String,
    val showNavigationIcon: Boolean = false,
    val navigationIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    val onNavigationClick: (() -> Unit)? = null,
    val actions: List<AppBarAction> = emptyList(),
)
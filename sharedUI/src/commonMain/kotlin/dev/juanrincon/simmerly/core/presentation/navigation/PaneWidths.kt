package dev.juanrincon.simmerly.core.presentation.navigation

import androidx.compose.ui.unit.dp

/** Widths of the side panels the recipe screens open beside (or over) the detail pane. */
object PaneWidths {
    /** Comments extra pane, from the "Recipe detail, comments open" design. */
    val Comments = 400.dp

    /** Docked (standard) settings side sheet on foldable and desktop widths. */
    val SettingsDocked = 360.dp

    /** Cap on the modal settings side sheet on phones, which otherwise takes 85% of the screen. */
    val SettingsModalMax = 320.dp
}

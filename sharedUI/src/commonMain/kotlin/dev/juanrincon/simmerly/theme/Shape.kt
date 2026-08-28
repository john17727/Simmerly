package dev.juanrincon.simmerly.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material's defaults, except extraSmall. Text fields resolve their shape from
// extraSmall (4.dp out of the box); the design calls for 12.dp, so overriding it
// here gives every OutlinedTextField the right corner without a per-field shape
// argument. Nothing else in the app references extraSmall directly — only M3
// internals (text fields, snackbars, tooltips) pick it up.
val SimmerlyShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

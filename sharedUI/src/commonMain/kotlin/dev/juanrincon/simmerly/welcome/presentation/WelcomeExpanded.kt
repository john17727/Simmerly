package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState

// Design artboard is 1440x900 with a fixed 780px collage panel. Kept as a ratio
// so the split still works at the 1200x800 default desktop window, where a fixed
// 780dp would leave too little room for the 436dp form plus its 72dp gutters.
private const val COLLAGE_WEIGHT = 780f
private const val FORM_WEIGHT = 660f

@Composable
internal fun ExpandedWelcome(
    state: WelcomeState,
    onEvent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(COLLAGE_WEIGHT)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                WelcomeCollageExpanded(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(FORM_WEIGHT)
                    .padding(vertical = 48.dp, horizontal = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(modifier = Modifier.widthIn(max = 436.dp).fillMaxWidth()) {
                    Header(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(40.dp))
                    Login(state, onEvent)
                }
            }
        }
    }
}

package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.core.presentation.KeepScreenOn
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.toTimerPresets
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeSideEffect
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookPhase
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Entry point for the full-screen cooking flow, presented as a bottom sheet by
 * [dev.juanrincon.simmerly.core.presentation.navigation.BottomSheetSceneStrategy] rather than
 * pushed like a normal destination — see that file for why.
 */
@Composable
fun CookModeScreen(
    recipeId: String,
    onExit: () -> Unit,
    viewModel: CookModeViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            CookModeSideEffect.Exit -> onExit()
            is CookModeSideEffect.TimerFinished -> Unit // the rail/sheet already reflect it live
        }
    }

    Content(state = state, onEvent = viewModel::onEvent, onExit = onExit, modifier = modifier)
}

@Composable
private fun Content(
    state: CookModeState,
    onEvent: (CookModeIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeepScreenOn()

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AnimatedContent(targetState = state.phase) { phase ->
                when (phase) {
                    CookPhase.MISE_EN_PLACE -> MiseEnPlaceView(state = state, onEvent = onEvent, onExit = onExit)
                    CookPhase.STEPS -> CookStepView(state = state, onEvent = onEvent, onExit = onExit)
                    CookPhase.DONE -> CookDoneView(state = state, onEvent = onEvent, onExit = onExit)
                }
            }
        }
    }

    state.newTimerDraft?.let { draft ->
        val presets = remember(state.recipe) { state.steps.toTimerPresets() }
        NewTimerSheet(
            draft = draft,
            presets = presets,
            onDraftChange = { onEvent(CookModeIntent.UpdateTimerDraft(it)) },
            onDismiss = { onEvent(CookModeIntent.DismissNewTimerSheet) },
            onConfirm = { onEvent(CookModeIntent.ConfirmNewTimer) }
        )
    }

    if (state.showTimerList) {
        TimerListSheet(
            timers = state.timers,
            nowMillis = state.nowMillis,
            onDismiss = { onEvent(CookModeIntent.DismissTimerList) },
            onAddTimer = { onEvent(CookModeIntent.ShowNewTimerSheet) },
            onPauseTimer = { onEvent(CookModeIntent.PauseTimer(it)) },
            onResumeTimer = { onEvent(CookModeIntent.ResumeTimer(it)) },
            onCancelTimer = { onEvent(CookModeIntent.CancelTimer(it)) }
        )
    }
}

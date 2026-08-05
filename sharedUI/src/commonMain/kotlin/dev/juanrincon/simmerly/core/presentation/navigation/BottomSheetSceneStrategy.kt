@file:OptIn(ExperimentalMaterial3Api::class)

package dev.juanrincon.simmerly.core.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * A [SceneStrategy] that renders an entry marked with [BottomSheetSceneStrategy.bottomSheet] as a
 * full-height [ModalBottomSheet] over the entries beneath it, instead of pushing them off screen.
 *
 * The entry stays a normal nav3 back-stack entry — it gets its own scoped `ViewModelStore`,
 * predictive back, and process-death restore for free from the existing `entryDecorators` — this
 * strategy only changes *how it is presented*. That distinction matters for Cook Mode: hosting it
 * from a raw `ModalBottomSheet` in the recipe detail screen would resolve its ViewModel against
 * the *Detail* entry's store, so a running timer would be torn down the moment the sheet closed.
 *
 * Ported from AndroidX's own `AnimatedBottomSheetSample` (navigation3-ui 1.1.1 samples), with
 * four deliberate changes: `overlaidEntries` keeps every entry beneath rather than just the
 * last one, so [androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy] can still
 * compute a real two-pane scene underneath on expanded widths; the sheet skips the partially
 * expanded state and hides its drag handle and insets, since Cook Mode draws its own full-height
 * chrome; swipe-to-dismiss is disabled, since without a drag handle the whole surface would
 * otherwise be a dismiss target and a cook's hand brushing the screen shouldn't cancel the flow —
 * only each screen's own close button should; and [onRemove] awaits [SheetState.hide] so back
 * animates the sheet down instead of cutting it.
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val entry = entries.lastOrNull() ?: return null
        val properties = entry.metadata[BottomSheetKey] ?: return null

        return object : OverlayScene<T> {
            override val key: Any = entry.contentKey
            override val entries: List<NavEntry<T>> = listOf(entry)
            override val previousEntries: List<NavEntry<T>> = entries.dropLast(1)

            // Not `.takeLast(1)`, unlike the sample this is ported from: on expanded widths the
            // entries below Cook Mode are List *and* Detail, and ListDetailSceneStrategy needs both
            // to keep rendering the real two-pane scene underneath the sheet.
            override val overlaidEntries: List<NavEntry<T>> = previousEntries

            private lateinit var sheetState: SheetState

            override val content: @Composable () -> Unit = {
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = onBack,
                    sheetState = sheetState,
                    dragHandle = null,
                    // With no drag handle, the whole sheet surface is otherwise a drag target —
                    // a swipe down anywhere would dismiss Cook Mode. Only the screen's own X
                    // button (wired to onExit, which calls the same onBack) should be able to.
                    sheetGesturesEnabled = false,
                    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                    properties = properties,
                ) {
                    entry.Content()
                }
            }

            override suspend fun onRemove() {
                sheetState.hide()
            }
        }
    }

    companion object {
        private object BottomSheetKey : NavMetadataKey<ModalBottomSheetProperties>

        /** Marks a [NavEntry] to be rendered as a full-height bottom sheet by
         * [BottomSheetSceneStrategy]. Put this strategy first in `sceneStrategies` — like
         * `DialogSceneStrategy`, an overlay strategy must run before any non-overlay one, or the
         * non-overlay strategy claims the entry first. */
        fun bottomSheet(
            properties: ModalBottomSheetProperties = ModalBottomSheetProperties()
        ): Map<String, Any> = metadata { put(BottomSheetKey, properties) }
    }
}

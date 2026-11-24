package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.timeline.domain.PickedImage

sealed interface TimelineEditIntent {
    data class SubjectChanged(val text: String) : TimelineEditIntent
    data class MessageChanged(val text: String) : TimelineEditIntent
    data class PhotoPicked(val image: PickedImage) : TimelineEditIntent
    data object RemovePhoto : TimelineEditIntent
    data object Save : TimelineEditIntent
    data object DeleteClicked : TimelineEditIntent
    data object DeleteConfirmed : TimelineEditIntent
    data object DeleteDismissed : TimelineEditIntent
}

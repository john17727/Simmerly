package dev.juanrincon.simmerly.timeline.presentation.orbit

import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType

data class TimelineEditState(
    val isLoaded: Boolean = false,
    val type: TimelineEventType = TimelineEventType.Comment,
    val date: String = "",
    val recipeName: String = "",
    val subject: String = "",
    val message: String = "",
    /** URL of the photo the server has, until removed or replaced. */
    val existingImage: String? = null,
    val pickedImage: PickedImage? = null,
    val removeImage: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirm: Boolean = false,
) {
    val hasPhoto: Boolean get() = pickedImage != null || (existingImage != null && !removeImage)
    val canSave: Boolean get() = isLoaded && subject.isNotBlank() && !isSaving && !isDeleting
}

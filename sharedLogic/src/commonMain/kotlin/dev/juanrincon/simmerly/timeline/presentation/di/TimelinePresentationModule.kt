package dev.juanrincon.simmerly.timeline.presentation.di

import dev.juanrincon.simmerly.timeline.presentation.TimelineEditViewModel
import dev.juanrincon.simmerly.timeline.presentation.TimelineViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val timelinePresentationModule = module {
    viewModel { (recipeId: String) -> TimelineViewModel(recipeId, get(), get()) }
    viewModel { (recipeId: String, eventId: String) ->
        TimelineEditViewModel(recipeId, eventId, get(), get())
    }
}

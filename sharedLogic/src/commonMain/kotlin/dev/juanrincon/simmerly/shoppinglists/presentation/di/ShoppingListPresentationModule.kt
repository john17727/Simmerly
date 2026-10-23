package dev.juanrincon.simmerly.shoppinglists.presentation.di

import dev.juanrincon.simmerly.shoppinglists.presentation.detail.ShoppingListDetailViewModel
import dev.juanrincon.simmerly.shoppinglists.presentation.list.ShoppingListsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shoppingListPresentationModule = module {
    viewModelOf(::ShoppingListsViewModel)
    viewModel { (listId: String) -> ShoppingListDetailViewModel(listId, get()) }
}

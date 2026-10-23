package dev.juanrincon.simmerly.shoppinglists.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.shoppinglists.data.DefaultShoppingListRepository
import dev.juanrincon.simmerly.shoppinglists.data.remote.ShoppingListNetworkClient
import dev.juanrincon.simmerly.shoppinglists.domain.ShoppingListRepository
import org.koin.dsl.module

val shoppingListDataModule = module {
    single<ShoppingListNetworkClient> { ShoppingListNetworkClient(get(qualifier = AuthClient)) }
    single<ShoppingListRepository> { DefaultShoppingListRepository(get(), get()) }
}

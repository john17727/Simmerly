package dev.juanrincon.simmerly.initialload.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.initialload.data.DefaultAuxiliaryRepository
import dev.juanrincon.simmerly.initialload.data.DefaultUserRepository
import dev.juanrincon.simmerly.initialload.data.remote.AuxiliaryNetworkClient
import dev.juanrincon.simmerly.initialload.data.remote.UserNetworkClient
import dev.juanrincon.simmerly.initialload.domain.AuxiliaryRepository
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import org.koin.dsl.module

val initialLoadDataModule = module {
    single<UserNetworkClient> { UserNetworkClient(get(qualifier = AuthClient)) }
    single<AuxiliaryNetworkClient> { AuxiliaryNetworkClient(get(qualifier = AuthClient)) }
    single<UserRepository> { DefaultUserRepository(get(), get(), get()) }
    single<AuxiliaryRepository> {
        DefaultAuxiliaryRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

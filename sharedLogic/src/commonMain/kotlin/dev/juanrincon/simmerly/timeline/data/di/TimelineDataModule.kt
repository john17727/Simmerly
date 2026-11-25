package dev.juanrincon.simmerly.timeline.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.domain.ResettableCache
import dev.juanrincon.simmerly.timeline.data.DefaultTimelineRepository
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventDao
import dev.juanrincon.simmerly.timeline.data.remote.TimelineNetworkClient
import dev.juanrincon.simmerly.timeline.domain.TimelineRepository
import org.koin.dsl.binds
import org.koin.dsl.module

val timelineDataModule = module {
    single<TimelineNetworkClient> { TimelineNetworkClient(get(qualifier = AuthClient)) }
    single<TimelineEventDao> { get<SimmerlyDatabase>().timelineEventDao() }
    single { DefaultTimelineRepository(get(), get(), get(), get(), get(), get(), get()) } binds arrayOf(
        TimelineRepository::class,
        ResettableCache::class,
    )
}

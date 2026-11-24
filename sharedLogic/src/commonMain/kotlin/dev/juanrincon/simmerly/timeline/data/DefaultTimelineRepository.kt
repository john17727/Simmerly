package dev.juanrincon.simmerly.timeline.data

import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.domain.ResettableCache
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventDao
import dev.juanrincon.simmerly.timeline.data.remote.TimelineNetworkClient
import dev.juanrincon.simmerly.timeline.data.remote.dto.TimelineEventUpdateDto
import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.domain.TimelineError
import dev.juanrincon.simmerly.timeline.domain.TimelineEvent
import dev.juanrincon.simmerly.timeline.domain.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PAGE_SIZE = 32

/** Offline-first timeline: Room is the source of truth, the network fills it (see PR #29). */
class DefaultTimelineRepository(
    private val networkClient: TimelineNetworkClient,
    private val timelineDao: TimelineEventDao,
    private val recipeDao: RecipeDao,
    private val userDao: UserDao,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val freshness: Freshness,
) : TimelineRepository, ResettableCache {

    /** Pagination cursor per recipe: the next page to load, or null when the end was reached. */
    private val nextPage = mutableMapOf<String, Int?>()
    private val pageLock = Mutex()

    override fun reset() {
        nextPage.clear()
    }

    override fun timeline(
        recipeId: String
    ): Flow<Either<TimelineError, LoadingResult<List<TimelineEvent>>>> = channelFlow {
        val key = CacheKeys.recipeTimeline(recipeId)
        val stale = freshness.isStale(key, CacheTtl.RECIPE_TIMELINE)
        if (stale && timelineDao.count(recipeId) == 0) {
            send(Either.Right(LoadingResult.Loading))
            val result = refresh(recipeId)
            if (result.isLeft()) send(Either.Left(TimelineError.FetchError))
        } else if (stale) {
            launch {
                send(Either.Right(LoadingResult.Refreshing))
                refresh(recipeId).onLeft { send(Either.Left(it)) }
                send(Either.Right(LoadingResult.RefreshComplete))
            }
        }

        val currentUserId = userRepository.currentUserId().getOrNull()
        val isAdmin = currentUserId?.let { userDao.getById(it)?.admin } ?: false
        combine(
            sessionDataStore.observeServerAddress(),
            timelineDao.observe(recipeId),
        ) { host, rows -> rows.map { it.toDomain(host, currentUserId, isAdmin) } }
            .distinctUntilChanged()
            .collect { send(Either.Right(LoadingResult.Loaded(it))) }
    }

    override suspend fun refresh(recipeId: String): Either<TimelineError, Unit> = either {
        val slug = recipeDao.getSlug(recipeId) ?: recipeId
        val page = networkClient.getEvents(slug, page = 1, perPage = PAGE_SIZE)
            .mapLeft { TimelineError.FetchError }.bind()
        syncMembers()
        val entities = page.items.map { it.toEntity() }
        timelineDao.upsertAll(entities)
        if (entities.isEmpty()) {
            timelineDao.deleteByRecipeId(recipeId)
        } else {
            timelineDao.deleteMissing(recipeId, entities.map { it.id })
        }
        pageLock.withLock { nextPage[recipeId] = if (page.next != null) 2 else null }
        freshness.markSynced(CacheKeys.recipeTimeline(recipeId))
    }

    override suspend fun loadNextPage(recipeId: String): Either<TimelineError, Boolean> = either {
        val pageNumber = pageLock.withLock {
            if (nextPage.containsKey(recipeId)) nextPage[recipeId] else 2
        } ?: return@either false
        val slug = recipeDao.getSlug(recipeId) ?: recipeId
        val page = networkClient.getEvents(slug, pageNumber, PAGE_SIZE)
            .mapLeft { TimelineError.FetchError }.bind()
        syncMembers()
        timelineDao.upsertAll(page.items.map { it.toEntity() })
        val hasMore = page.next != null
        pageLock.withLock { nextPage[recipeId] = if (hasMore) pageNumber + 1 else null }
        hasMore
    }

    override suspend fun updateEvent(
        recipeId: String,
        eventId: String,
        subject: String,
        message: String?,
        newImage: PickedImage?,
        removeImage: Boolean,
    ): Either<TimelineError, Unit> = either {
        val updated = networkClient.updateEvent(
            eventId,
            TimelineEventUpdateDto(
                subject = subject,
                eventMessage = message,
                image = if (removeImage && newImage == null) IMAGE_ABSENT else null,
            )
        ).mapLeft { TimelineError.UpdateError }.bind()
        var entity = updated.toEntity()
        if (newImage != null) {
            networkClient.uploadImage(eventId, newImage.bytes, newImage.extension)
                .mapLeft { TimelineError.UpdateError }.bind()
            entity = entity.copy(hasImage = true)
        } else if (removeImage) {
            entity = entity.copy(hasImage = false)
        }
        timelineDao.upsertAll(listOf(entity))
    }

    override suspend fun deleteEvent(recipeId: String, eventId: String): Either<TimelineError, Unit> =
        either {
            networkClient.deleteEvent(eventId).mapLeft { TimelineError.DeleteError }.bind()
            timelineDao.deleteById(eventId)
        }

    /** Best effort: the timeline only carries user ids, so names come from the household members.
     * An existing row's `admin` flag is kept (the members endpoint doesn't report it). */
    private suspend fun syncMembers() {
        networkClient.getMembers().onRight { members ->
            userDao.upsertAll(members.items.map {
                UserEntity(
                    id = it.id,
                    username = it.username,
                    admin = userDao.getById(it.id)?.admin ?: false,
                    fullName = it.fullName,
                )
            })
        }
    }
}

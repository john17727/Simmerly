package dev.juanrincon.simmerly.core.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class OptimisticTest {

    private val events = mutableListOf<String>()

    private suspend fun run(
        remote: Either<String, Int>,
        onSuccess: suspend (Int) -> Unit = { events += "success:$it" },
    ): Either<Throwable, Int> = optimistic(
        apply = { events += "apply" },
        remote = { events += "remote"; remote },
        rollback = { events += "rollback" },
        onSuccess = onSuccess,
        mapError = { IllegalStateException(it) },
    )

    @Test
    fun successAppliesThenCallsRemoteThenReportsTheResult() = runTest {
        val result = run(remote = 42.right())

        assertThat(events).containsExactly("apply", "remote", "success:42")
        assertThat(result).isEqualTo(42.right())
    }

    @Test
    fun successDoesNotRollBack() = runTest {
        run(remote = 1.right())
        assertThat(events.contains("rollback")).isEqualTo(false)
    }

    @Test
    fun failureRollsBackAfterTheRemoteCall() = runTest {
        run(remote = "boom".left())
        assertThat(events).containsExactly("apply", "remote", "rollback")
    }

    @Test
    fun failureMapsTheError() = runTest {
        val result = run(remote = "boom".left())
        val error = (result as Either.Left).value
        assertThat(error).isInstanceOf<IllegalStateException>()
        assertThat(error.message).isEqualTo("boom")
    }

    @Test
    fun failureDoesNotCallOnSuccess() = runTest {
        run(remote = "boom".left())
        assertThat(events.contains("success:0")).isEqualTo(false)
        assertThat(events.none { it.startsWith("success") }).isEqualTo(true)
    }

    @Test
    fun anExceptionFromTheRemoteCallPropagatesWithoutRollingBack() = runTest {
        assertFailsWith<IllegalArgumentException> {
            optimistic<String, Int, String>(
                apply = { events += "apply" },
                remote = { throw IllegalArgumentException("crash") },
                rollback = { events += "rollback" },
                mapError = { it },
            )
        }
        assertThat(events).containsExactly("apply")
    }

    @Test
    fun onSuccessIsOptional() = runTest {
        val result = optimistic(
            apply = {},
            remote = { Unit.right() },
            rollback = {},
            mapError = { e: String -> e },
        )
        assertThat(result).isEqualTo(Unit.right())
    }
}

package dev.juanrincon.simmerly.recipes.presentation.comments

import arrow.core.left
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aComment
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class RecipeCommentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRecipeRepository
    private lateinit var viewModel: RecipeCommentsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeRecipeRepository()
        viewModel = RecipeCommentsViewModel(recipeId = "test-recipe", repository = repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateHasEmptyCommentsAndEmptyCommentText() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.comments).isEmpty()
        assertThat(state.commentText).isEqualTo("")
    }

    // endregion

    // region Init flow

    @Test
    fun observeCommentsEmitsCommentsMappedToCommentUi() = runTest(testDispatcher) {
        viewModel.test(this) {
            runOnCreate()
            repo.commentsFlow.emit(listOf(aComment(text = "Hello", authorName = "John Doe")))
            val state = awaitState()
            assertThat(state.comments).hasSize(1)
            assertThat(state.comments[0].text).isEqualTo("Hello")
            assertThat(state.comments[0].author).isEqualTo("John Doe")
            cancelAndIgnoreRemainingItems()
        }
    }

    // endregion

    // region Intents

    @Test
    fun onCommentTextChangedUpdatesCommentText() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeCommentsIntent.OnCommentTextChanged("Great recipe!"))
            assertThat(awaitState().commentText).isEqualTo("Great recipe!")
        }
    }

    @Test
    fun onSendCommentClickedCallsAddCommentWithRecipeIdAndText() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeCommentsIntent.OnCommentTextChanged("Hello"))
            awaitState() // commentText = "Hello"
            viewModel.onEvent(RecipeCommentsIntent.OnSendCommentClicked)
            awaitState() // commentText = ""
        }
        assertThat(repo.lastAddCommentCall?.first).isEqualTo("test-recipe")
        assertThat(repo.lastAddCommentCall?.second).isEqualTo("Hello")
    }

    @Test
    fun onSendCommentClickedClearsCommentTextOnSuccess() = runTest(testDispatcher) {
        viewModel.test(this) {
            viewModel.onEvent(RecipeCommentsIntent.OnCommentTextChanged("Hello"))
            awaitState()
            viewModel.onEvent(RecipeCommentsIntent.OnSendCommentClicked)
            assertThat(awaitState().commentText).isEqualTo("")
        }
    }

    @Test
    fun onSendCommentClickedDoesNotClearCommentTextOnFailure() = runTest(testDispatcher) {
        repo.addCommentResult = RecipesError.FetchError.left()
        viewModel.test(this) {
            viewModel.onEvent(RecipeCommentsIntent.OnCommentTextChanged("Hello"))
            awaitState() // commentText = "Hello"
            viewModel.onEvent(RecipeCommentsIntent.OnSendCommentClicked)
            // ifLeft block is empty (TODO) → no reduce → no state emission
        }
        assertThat(viewModel.container.stateFlow.value.commentText).isEqualTo("Hello")
    }

    // endregion
}

package dev.juanrincon.simmerly.recipes.domain

import androidx.paging.PagingData
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun getRecipes(pageSize: Int): Flow<PagingData<RecipeSummary>>

    suspend fun getRecipe(id: String): Result<RecipeDetail, LoginError.NetworkError>
}
package com.pemmob.gamedex.data.repository

import com.pemmob.gamedex.data.model.GameDetailDto
import com.pemmob.gamedex.data.model.GameListResponse
import com.pemmob.gamedex.data.network.RawgApiService
import kotlin.coroutines.cancellation.CancellationException

class GameRepository(private val apiService: RawgApiService) {

    suspend fun getGames(query: String? = null, page: Int = 1): Result<GameListResponse> {
        return try {
            val response = apiService.getGames(
                search = query?.takeIf { it.isNotBlank() },
                page = page
            )
            Result.success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameDetail(gameId: Int): Result<GameDetailDto> {
        return try {
            val detail = apiService.getGameDetail(gameId = gameId)
            Result.success(detail)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

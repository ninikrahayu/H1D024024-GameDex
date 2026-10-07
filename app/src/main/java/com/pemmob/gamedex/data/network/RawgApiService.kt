package com.pemmob.gamedex.data.network

import com.pemmob.gamedex.BuildConfig
import com.pemmob.gamedex.data.model.GameDetailDto
import com.pemmob.gamedex.data.model.GameListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {

    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("search") search: String? = null,
        @Query("page_size") pageSize: Int = 20
    ): GameListResponse

    @GET("games/{id}")
    suspend fun getGameDetail(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY
    ): GameDetailDto
}

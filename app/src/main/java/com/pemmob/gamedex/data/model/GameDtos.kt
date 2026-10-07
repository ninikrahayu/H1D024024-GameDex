package com.pemmob.gamedex.data.model

import androidx.core.text.HtmlCompat
import com.google.gson.annotations.SerializedName
import com.pemmob.gamedex.model.Game

data class GameListResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("results") val results: List<GameItemDto>
)

data class GenreDto(
    @SerializedName("name") val name: String
)

data class GameItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("released") val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("genres") val genres: List<GenreDto>?
)

data class GameDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("released") val released: String?,
    @SerializedName("description_raw") val descriptionRaw: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("genres") val genres: List<GenreDto>?
)

fun GameItemDto.toGame() = Game(
    id = id,
    name = name,
    rating = rating,
    released = released,
    backgroundImage = backgroundImage,
    genres = genres.orEmpty().map { it.name }
)

fun GameDetailDto.toGame() = Game(
    id = id,
    name = name,
    rating = rating,
    released = released,
    description = descriptionRaw?.takeIf { it.isNotBlank() }
        ?: description?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim() },
    backgroundImage = backgroundImage,
    genres = genres.orEmpty().map { it.name }
)

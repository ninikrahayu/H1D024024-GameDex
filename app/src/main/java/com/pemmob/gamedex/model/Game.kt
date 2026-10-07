package com.pemmob.gamedex.model

data class Game(
    val id: Int,
    val name: String,
    val rating: Double,
    val released: String?,
    val description: String? = null,
    val backgroundImage: String? = null,
    val genres: List<String> = emptyList()
)

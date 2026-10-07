package com.pemmob.gamedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.gamedex.ui.theme.ChipBeige
import com.pemmob.gamedex.ui.theme.ChipBlue
import com.pemmob.gamedex.ui.theme.ChipGreen
import com.pemmob.gamedex.ui.theme.ChipIndigo
import com.pemmob.gamedex.ui.theme.ChipPurple
import com.pemmob.gamedex.ui.theme.ChipSky

private val fallbackChipColors = listOf(ChipBlue, ChipSky, ChipPurple, ChipIndigo, ChipGreen, ChipBeige)

private fun chipColors(genre: String): Pair<Color, Color> = when (genre.lowercase()) {
    "rpg" -> ChipBlue
    "adventure" -> ChipSky
    "action" -> ChipPurple
    "sci-fi" -> ChipIndigo
    "fantasy" -> ChipGreen
    "survival" -> ChipBeige
    else -> fallbackChipColors[(genre.hashCode() and Int.MAX_VALUE) % fallbackChipColors.size]
}

@Composable
fun GenreChip(
    genre: String,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val (container, content) = chipColors(genre)
    Text(
        text = genre,
        color = content,
        fontSize = if (large) 14.sp else 11.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 16.dp else 10.dp))
            .background(container)
            .padding(
                horizontal = if (large) 16.dp else 8.dp,
                vertical = if (large) 8.dp else 4.dp
            )
    )
}

@Composable
fun GenreChips(
    genres: List<String>,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    maxCount: Int = Int.MAX_VALUE,
    spacing: Dp = if (large) 10.dp else 6.dp
) {
    if (genres.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        genres.take(maxCount).forEach { GenreChip(genre = it, large = large) }
    }
}

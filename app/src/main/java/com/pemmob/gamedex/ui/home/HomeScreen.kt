package com.pemmob.gamedex.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pemmob.gamedex.model.Game
import com.pemmob.gamedex.ui.components.GenreChips
import com.pemmob.gamedex.ui.theme.RatingGold
import java.util.Locale

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val games: List<Game>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onGameClick: (Game) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val backgroundBrush = Brush.verticalGradient(
        listOf(colors.background, lerp(colors.background, colors.surface, 0.7f))
    )

    Scaffold(
        modifier = modifier.background(backgroundBrush),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HomeHeader(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp))
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    HomeUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is HomeUiState.Error -> ErrorContent(
                        message = uiState.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is HomeUiState.Success -> {
                        if (uiState.games.isEmpty()) {
                            Text(
                                text = "Game tidak ditemukan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            GameList(games = uiState.games, onGameClick = onGameClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Game")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Dex") }
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Discover. Explore. Play.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("Search games...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun GameList(
    games: List<Game>,
    onGameClick: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = games.chunked(2)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Popular Games",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(rows, key = { it.first().id }) { rowGames ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowGames.forEach { game ->
                    GameItem(
                        game = game,
                        onClick = { onGameClick(game) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (rowGames.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GameItem(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            GameImage(
                url = game.backgroundImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(MaterialTheme.shapes.small)
            )
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, top = 10.dp, end = 4.dp)
            )
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 6.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingBadge(rating = game.rating)
                VerticalDivider(modifier = Modifier.height(14.dp))
                Text(
                    text = game.released ?: "TBA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            GenreChips(
                genres = game.genres,
                maxCount = 2,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
fun GameImage(url: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    starSize: Dp = 16.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = RatingGold,
            modifier = Modifier.size(starSize)
        )
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text("Coba lagi") }
    }
}

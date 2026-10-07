package com.pemmob.gamedex.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pemmob.gamedex.model.Game
import com.pemmob.gamedex.ui.components.GenreChips
import com.pemmob.gamedex.ui.home.GameImage
import com.pemmob.gamedex.ui.home.RatingBadge

private val HeaderHeight = 360.dp
private val SheetOverlap = 28.dp

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val game: Game) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuccess = uiState is DetailUiState.Success
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when (uiState) {
            DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is DetailUiState.Error -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) { Text("Coba lagi") }
            }
            is DetailUiState.Success -> DetailBody(game = uiState.game)
        }

        CircleIconButton(
            onClick = onBack,
            overImage = isSuccess,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
        }
    }
}

@Composable
private fun DetailBody(game: Game) {
    Box(modifier = Modifier.fillMaxSize()) {
        GameImage(
            url = game.backgroundImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(HeaderHeight - SheetOverlap))
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                ),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    DetailInfo(
                        game = game,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(start = 24.dp, top = 14.dp, end = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "About this game",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = game.description?.takeIf { it.isNotBlank() } ?: "Tidak ada deskripsi.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    overImage: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        colors = if (overImage) {
            IconButtonDefaults.iconButtonColors(
                containerColor = Color.White.copy(alpha = 0.7f),
                contentColor = Color(0xFF14213D)
            )
        } else {
            IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        },
        content = content
    )
}

@Composable
private fun DetailInfo(game: Game, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = game.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RatingBadge(
                rating = game.rating,
                textStyle = MaterialTheme.typography.titleMedium,
                starSize = 24.dp
            )
            VerticalDivider(modifier = Modifier.height(20.dp))
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = game.released ?: "TBA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GenreChips(genres = game.genres, large = true)
    }
}

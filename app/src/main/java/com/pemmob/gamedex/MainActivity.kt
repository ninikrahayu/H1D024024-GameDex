package com.pemmob.gamedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pemmob.gamedex.ui.GameViewModel
import com.pemmob.gamedex.ui.detail.DetailScreen
import com.pemmob.gamedex.ui.home.HomeScreen
import com.pemmob.gamedex.ui.theme.GameDexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameDexTheme {
                GameDexApp()
            }
        }
    }
}

@Composable
fun GameDexApp(viewModel: GameViewModel = viewModel()) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedGameId by viewModel.selectedGameId.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()

    if (selectedGameId != null) {
        BackHandler { viewModel.closeGame() }
        DetailScreen(
            uiState = detailState,
            onBack = viewModel::closeGame,
            onRetry = viewModel::loadDetail,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        HomeScreen(
            uiState = homeState,
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onGameClick = { viewModel.openGame(it.id) },
            onRetry = viewModel::loadGames,
            modifier = Modifier.fillMaxSize()
        )
    }
}

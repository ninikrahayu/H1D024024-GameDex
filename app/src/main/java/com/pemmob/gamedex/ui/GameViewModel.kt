package com.pemmob.gamedex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.gamedex.data.model.toGame
import com.pemmob.gamedex.data.network.RetrofitClient
import com.pemmob.gamedex.data.repository.GameRepository
import com.pemmob.gamedex.ui.detail.DetailUiState
import com.pemmob.gamedex.ui.home.HomeUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _selectedGameId = MutableStateFlow<Int?>(null)
    val selectedGameId: StateFlow<Int?> = _selectedGameId.asStateFlow()

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private var gamesJob: Job? = null
    private var detailJob: Job? = null

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        _query
            .drop(1)
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { loadGames() }
            .launchIn(viewModelScope)
    }

    init {
        loadGames()
        observeSearch()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun loadGames() {
        gamesJob?.cancel()
        gamesJob = viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            repository.getGames(_query.value.trim())
                .onSuccess { items -> _homeState.value = HomeUiState.Success(items.map { it.toGame() }) }
                .onFailure { _homeState.value = HomeUiState.Error(ERROR_MESSAGE) }
        }
    }

    fun openGame(id: Int) {
        _selectedGameId.value = id
        loadDetail()
    }

    fun closeGame() {
        detailJob?.cancel()
        _selectedGameId.value = null
    }

    fun loadDetail() {
        val id = _selectedGameId.value ?: return
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            repository.getGameDetail(id)
                .onSuccess { _detailState.value = DetailUiState.Success(it.toGame()) }
                .onFailure { _detailState.value = DetailUiState.Error(ERROR_MESSAGE) }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val ERROR_MESSAGE = "Gagal memuat data. Periksa koneksi internet Anda."
    }
}

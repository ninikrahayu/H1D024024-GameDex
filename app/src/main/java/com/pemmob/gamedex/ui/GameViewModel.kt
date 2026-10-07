package com.pemmob.gamedex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.gamedex.data.model.toGame
import com.pemmob.gamedex.data.network.RetrofitClient
import com.pemmob.gamedex.data.repository.GameRepository
import com.pemmob.gamedex.ui.all.AllGamesUiState
import com.pemmob.gamedex.ui.detail.DetailUiState
import com.pemmob.gamedex.ui.home.HomeUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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

    private val _showAllGames = MutableStateFlow(false)
    val showAllGames: StateFlow<Boolean> = _showAllGames.asStateFlow()

    private val _allQuery = MutableStateFlow("")
    val allQuery: StateFlow<String> = _allQuery.asStateFlow()

    private val _allState = MutableStateFlow(AllGamesUiState())
    val allState: StateFlow<AllGamesUiState> = _allState.asStateFlow()

    private val _selectedGameId = MutableStateFlow<Int?>(null)
    val selectedGameId: StateFlow<Int?> = _selectedGameId.asStateFlow()

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private var gamesJob: Job? = null
    private var allJob: Job? = null
    private var detailJob: Job? = null
    private var allPage = 1

    init {
        loadGames()
        onSearchSettled(_query) { loadGames() }
        onSearchSettled(_allQuery) { if (_showAllGames.value) loadAllGames() }
    }

    @OptIn(FlowPreview::class)
    private fun onSearchSettled(source: Flow<String>, action: () -> Unit) {
        source
            .drop(1)
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { action() }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun loadGames() {
        gamesJob?.cancel()
        gamesJob = viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            repository.getGames(_query.value.trim())
                .onSuccess { response ->
                    _homeState.value = HomeUiState.Success(response.results.map { it.toGame() })
                }
                .onFailure { _homeState.value = HomeUiState.Error(ERROR_MESSAGE) }
        }
    }

    fun openAllGames() {
        _showAllGames.value = true
        loadAllGames()
    }

    fun closeAllGames() {
        allJob?.cancel()
        _showAllGames.value = false
        _allQuery.value = ""
        _allState.value = AllGamesUiState()
    }

    fun onAllQueryChange(newQuery: String) {
        _allQuery.value = newQuery
    }

    fun loadAllGames() {
        allJob?.cancel()
        allPage = 1
        allJob = viewModelScope.launch {
            _allState.value = AllGamesUiState(isLoading = true)
            repository.getGames(_allQuery.value.trim(), page = 1)
                .onSuccess { response ->
                    _allState.value = AllGamesUiState(
                        games = response.results.map { it.toGame() },
                        isLoading = false,
                        canLoadMore = response.next != null
                    )
                }
                .onFailure {
                    _allState.value = AllGamesUiState(isLoading = false, errorMessage = ERROR_MESSAGE)
                }
        }
    }

    fun loadMoreAllGames() {
        val current = _allState.value
        if (current.isLoading || current.isLoadingMore || !current.canLoadMore) return
        allJob = viewModelScope.launch {
            _allState.value = current.copy(isLoadingMore = true)
            repository.getGames(_allQuery.value.trim(), page = allPage + 1)
                .onSuccess { response ->
                    allPage++
                    val merged = (current.games + response.results.map { it.toGame() }).distinctBy { it.id }
                    _allState.value = current.copy(
                        games = merged,
                        isLoadingMore = false,
                        canLoadMore = response.next != null
                    )
                }
                .onFailure { _allState.value = current.copy(isLoadingMore = false) }
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

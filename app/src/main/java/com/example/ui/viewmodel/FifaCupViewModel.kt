package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FifaCupRepository
import com.example.data.TeamEntity
import com.example.data.TournamentEntity
import com.example.model.ChampionWithDetails
import com.example.model.CompetitionStats
import com.example.model.CompetitionType
import com.example.model.GlobalStats
import com.example.model.MatchWithTeams
import com.example.model.TeamPerformanceStats
import com.example.model.TournamentDetailBundle
import com.example.model.TournamentFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FifaCupViewModel(
    private val repository: FifaCupRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureTeamsSeeded()
        }
    }

    val allTeams: StateFlow<List<TeamEntity>> = repository.allTeamsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.allTournamentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchWithTeams>> = repository.observeAllMatchesWithTeams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChampions: StateFlow<List<ChampionWithDetails>> = repository.observeAllChampionsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalStats: StateFlow<GlobalStats> = repository.observeGlobalStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalStats())

    val teamLeaderboard: StateFlow<List<TeamPerformanceStats>> = repository.observeTeamPerformanceLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCompetition = MutableStateFlow(CompetitionType.MUNDIAL)
    val selectedCompetition: StateFlow<CompetitionType> = _selectedCompetition.asStateFlow()

    private val _activeTournamentId = MutableStateFlow<Long?>(null)
    val activeTournamentId: StateFlow<Long?> = _activeTournamentId.asStateFlow()

    val competitionTournaments: StateFlow<List<TournamentEntity>> = _selectedCompetition
        .flatMapLatest { comp -> repository.observeTournamentsForCompetition(comp) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val competitionStats: StateFlow<CompetitionStats> = _selectedCompetition
        .flatMapLatest { comp -> repository.observeCompetitionStats(comp) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompetitionStats())

    val activeTournamentBundle: StateFlow<TournamentDetailBundle?> = _activeTournamentId
        .flatMapLatest { tid ->
            if (tid == null) flowOf(null) else repository.observeTournamentDetail(tid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private val _newlyCrownedChampion = MutableStateFlow<Pair<TeamEntity, String>?>(null)
    val newlyCrownedChampion: StateFlow<Pair<TeamEntity, String>?> = _newlyCrownedChampion.asStateFlow()

    fun openCompetition(competitionType: CompetitionType, preferredTournamentId: Long? = null) {
        _selectedCompetition.value = competitionType
        viewModelScope.launch {
            val targetId = preferredTournamentId
                ?: repository.ensureOrCreateTournamentForCompetition(competitionType)
            _activeTournamentId.value = targetId
        }
    }

    fun selectTournament(tournamentId: Long) {
        _activeTournamentId.value = tournamentId
    }

    fun createNewTournamentInCompetition(competitionType: CompetitionType) {
        viewModelScope.launch {
            val existingCount = competitionTournaments.value.size + 1
            val newId = repository.createTournament(
                competitionType = competitionType,
                name = "${competitionType.title} #$existingCount",
                startDate = com.example.data.TournamentEngine.todayString(),
                prize = competitionType.defaultPrize,
                participantCount = competitionType.defaultParticipantCount,
                format = competitionType.defaultFormat
            )
            _activeTournamentId.value = newId
            _uiMessage.tryEmit("Nuevo torneo creado en ${competitionType.title}")
        }
    }

    fun saveTournamentConfig(
        tournamentId: Long,
        name: String,
        startDate: String,
        prize: String,
        participantCount: Int,
        format: TournamentFormat
    ) {
        viewModelScope.launch {
            repository.updateTournamentConfig(
                tournamentId = tournamentId,
                name = name,
                startDate = startDate,
                prize = prize,
                participantCount = participantCount,
                format = format
            )
            _uiMessage.tryEmit("Configuración guardada correctamente")
        }
    }

    fun toggleTeam(tournamentId: Long, teamId: String) {
        viewModelScope.launch {
            val res = repository.toggleTeamSelection(tournamentId, teamId)
            res.onFailure { err ->
                _uiMessage.tryEmit(err.message ?: "No se pudo actualizar el equipo")
            }
        }
    }

    fun autoFillTeams(tournamentId: Long) {
        viewModelScope.launch {
            val res = repository.autoFillRemainingTeams(tournamentId)
            res.onSuccess { added ->
                if (added > 0) {
                    _uiMessage.tryEmit("Se completaron $added equipos automáticamente")
                } else {
                    _uiMessage.tryEmit("La lista de participantes ya está completa")
                }
            }.onFailure { err ->
                _uiMessage.tryEmit(err.message ?: "No se pudo completar")
            }
        }
    }

    fun clearTeams(tournamentId: Long) {
        viewModelScope.launch {
            repository.clearSelectedTeams(tournamentId)
            _uiMessage.tryEmit("Equipos limpiados")
        }
    }

    fun startDraw(tournamentId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.executeDrawAndStartTournament(tournamentId)
            res.onSuccess {
                _uiMessage.tryEmit("¡Sorteo realizado! Llaves y calendario generados")
                onSuccess()
            }.onFailure { err ->
                _uiMessage.tryEmit(err.message ?: "Error al realizar el sorteo")
            }
        }
    }

    fun resetDraw(tournamentId: Long) {
        viewModelScope.launch {
            repository.resetTournamentDraw(tournamentId)
            _uiMessage.tryEmit("Torneo reiniciado a configuración")
        }
    }

    fun deleteTournament(tournamentId: Long, competitionType: CompetitionType) {
        viewModelScope.launch {
            repository.deleteTournament(tournamentId)
            val nextId = repository.ensureOrCreateTournamentForCompetition(competitionType)
            _activeTournamentId.value = nextId
            _uiMessage.tryEmit("Torneo eliminado")
        }
    }

    fun recordMatchScore(
        matchId: Long,
        homeGoals: Int,
        awayGoals: Int,
        penaltyWinnerTeamId: String?,
        homePenalties: Int?,
        awayPenalties: Int?,
        tournamentTitle: String
    ) {
        viewModelScope.launch {
            val res = repository.recordMatchResult(
                matchId = matchId,
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                penaltyWinnerTeamId = penaltyWinnerTeamId,
                homePenalties = homePenalties,
                awayPenalties = awayPenalties
            )
            res.onSuccess { championId ->
                if (championId != null) {
                    val champTeam = allTeams.value.firstOrNull { it.id == championId }
                    if (champTeam != null) {
                        _newlyCrownedChampion.value = champTeam to tournamentTitle
                    }
                } else {
                    _uiMessage.tryEmit("Marcador registrado y guardado en SQLite")
                }
            }.onFailure { err ->
                _uiMessage.tryEmit(err.message ?: "No se pudo guardar el marcador")
            }
        }
    }

    fun dismissChampionCelebration() {
        _newlyCrownedChampion.value = null
    }
}

class FifaCupViewModelFactory(
    private val repository: FifaCupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FifaCupViewModel::class.java)) {
            return FifaCupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

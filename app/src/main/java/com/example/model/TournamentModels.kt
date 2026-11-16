package com.example.model

import com.example.data.ChampionEntity
import com.example.data.MatchEntity
import com.example.data.TeamEntity
import com.example.data.TournamentEntity

data class MatchWithTeams(
    val match: MatchEntity,
    val homeTeam: TeamEntity?,
    val awayTeam: TeamEntity?,
    val winnerTeam: TeamEntity?,
    val competitionTitle: String,
    val tournamentName: String
)

data class StandingRow(
    val position: Int,
    val team: TeamEntity,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

data class TeamPerformanceStats(
    val team: TeamEntity,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val titles: Int
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
    val winRatePercent: Int get() = if (played == 0) 0 else (wins * 100) / played
}

data class ChampionWithDetails(
    val champion: ChampionEntity,
    val team: TeamEntity?,
    val runnerUp: TeamEntity?
)

data class GlobalStats(
    val tournamentsCreated: Int = 0,
    val tournamentsFinished: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesPending: Int = 0,
    val totalWinsResolved: Int = 0,
    val totalDraws: Int = 0,
    val totalGoals: Int = 0,
    val championsCount: Int = 0
)

data class CompetitionStats(
    val tournamentsCount: Int = 0,
    val finishedCount: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesPending: Int = 0,
    val totalGoals: Int = 0,
    val champions: List<ChampionWithDetails> = emptyList(),
    val topTeams: List<TeamPerformanceStats> = emptyList()
)

data class TournamentDetailBundle(
    val tournament: TournamentEntity,
    val selectedTeams: List<TeamEntity>,
    val matches: List<MatchWithTeams>,
    val groupStandings: Map<String, List<StandingRow>>,
    val leagueStandings: List<StandingRow>,
    val championTeam: TeamEntity?
)

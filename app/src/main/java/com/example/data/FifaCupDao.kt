package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FifaCupDao {

    // ===== TEAMS =====
    @Query("SELECT * FROM teams ORDER BY rating DESC, name ASC")
    fun observeAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY rating DESC, name ASC")
    suspend fun getAllTeamsOnce(): List<TeamEntity>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun countTeams(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    // ===== TOURNAMENTS =====
    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC, id DESC")
    fun observeAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE competitionType = :competitionType ORDER BY createdAt DESC, id DESC")
    fun observeTournamentsByCompetition(competitionType: String): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun observeTournamentById(tournamentId: Long): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: Long): TournamentEntity?

    @Query("SELECT * FROM tournaments WHERE competitionType = :competitionType ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun getLatestTournamentForCompetition(competitionType: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :tournamentId")
    suspend fun deleteTournament(tournamentId: Long)

    // ===== TOURNAMENT PARTICIPANTS =====
    @Query(
        """
        SELECT t.* FROM teams t
        INNER JOIN tournament_teams tt ON t.id = tt.teamId
        WHERE tt.tournamentId = :tournamentId
        ORDER BY tt.seedOrder ASC, t.name ASC
        """
    )
    fun observeTournamentTeams(tournamentId: Long): Flow<List<TeamEntity>>

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tournamentId ORDER BY seedOrder ASC")
    fun observeTournamentTeamLinks(tournamentId: Long): Flow<List<TournamentTeamEntity>>

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tournamentId ORDER BY seedOrder ASC")
    suspend fun getTournamentTeamLinksOnce(tournamentId: Long): List<TournamentTeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeams(links: List<TournamentTeamEntity>)

    @Query("DELETE FROM tournament_teams WHERE tournamentId = :tournamentId")
    suspend fun clearTournamentTeams(tournamentId: Long)

    @Query("DELETE FROM tournament_teams WHERE tournamentId = :tournamentId AND teamId = :teamId")
    suspend fun removeTeamFromTournament(tournamentId: Long, teamId: String)

    // ===== MATCHES =====
    @Query("SELECT * FROM matches ORDER BY scheduledDate ASC, roundNumber ASC, matchIndex ASC, id ASC")
    fun observeAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC, matchIndex ASC, id ASC")
    fun observeMatchesForTournament(tournamentId: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC, matchIndex ASC, id ASC")
    suspend fun getMatchesForTournamentOnce(tournamentId: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Query(
        """
        SELECT * FROM matches
        WHERE tournamentId = :tournamentId
          AND stageType = :stageType
          AND roundNumber = :roundNumber
          AND matchIndex = :matchIndex
        LIMIT 1
        """
    )
    suspend fun findBracketMatch(
        tournamentId: Long,
        stageType: String,
        roundNumber: Int,
        matchIndex: Int
    ): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE tournamentId = :tournamentId")
    suspend fun deleteMatchesForTournament(tournamentId: Long)

    // ===== CHAMPIONS =====
    @Query("SELECT * FROM champions ORDER BY crownedAt DESC, id DESC")
    fun observeAllChampions(): Flow<List<ChampionEntity>>

    @Query("SELECT * FROM champions WHERE competitionType = :competitionType ORDER BY crownedAt DESC, id DESC")
    fun observeChampionsByCompetition(competitionType: String): Flow<List<ChampionEntity>>

    @Query("SELECT * FROM champions WHERE tournamentId = :tournamentId LIMIT 1")
    suspend fun getChampionByTournament(tournamentId: Long): ChampionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChampion(champion: ChampionEntity)

    @Query("DELETE FROM champions WHERE tournamentId = :tournamentId")
    suspend fun deleteChampionByTournament(tournamentId: Long)

    // ===== TRANSACTIONAL HELPERS =====
    @Transaction
    suspend fun replaceTournamentTeams(tournamentId: Long, teamIds: List<String>) {
        clearTournamentTeams(tournamentId)
        val links = teamIds.distinct().mapIndexed { index, teamId ->
            TournamentTeamEntity(
                tournamentId = tournamentId,
                teamId = teamId,
                seedOrder = index
            )
        }
        if (links.isNotEmpty()) {
            insertTournamentTeams(links)
        }
    }

    @Transaction
    suspend fun replaceDrawMatchesAndTeams(
        tournament: TournamentEntity,
        links: List<TournamentTeamEntity>,
        matches: List<MatchEntity>
    ) {
        deleteChampionByTournament(tournament.id)
        deleteMatchesForTournament(tournament.id)
        clearTournamentTeams(tournament.id)
        insertTournamentTeams(links)
        insertMatches(matches)
        updateTournament(tournament)
    }
}

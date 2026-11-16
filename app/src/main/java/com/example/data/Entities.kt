package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.BadgePattern
import com.example.model.Confederation
import com.example.model.MatchStage
import com.example.model.TeamCategory
import com.example.model.TournamentFormat
import com.example.model.TournamentStatus

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortCode: String,
    val category: String, // TeamCategory.name (SELECCION or CLUB)
    val confederation: String, // Confederation.name
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val pattern: String, // BadgePattern.name
    val rating: Int
) {
    fun categoryEnum(): TeamCategory =
        runCatching { TeamCategory.valueOf(category) }.getOrDefault(TeamCategory.CLUB)

    fun confederationEnum(): Confederation =
        runCatching { Confederation.valueOf(confederation) }.getOrDefault(Confederation.EUROPA)

    fun patternEnum(): BadgePattern =
        runCatching { BadgePattern.valueOf(pattern) }.getOrDefault(BadgePattern.SHIELD_CENTER)
}

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val competitionType: String, // CompetitionType.id
    val name: String,
    val startDate: String,
    val prize: String,
    val participantCount: Int,
    val format: String, // TournamentFormat.name
    val status: String, // TournamentStatus.name
    val championTeamId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formatEnum(): TournamentFormat =
        runCatching { TournamentFormat.valueOf(format) }.getOrDefault(TournamentFormat.ELIMINATORIA)

    fun statusEnum(): TournamentStatus =
        runCatching { TournamentStatus.valueOf(status) }.getOrDefault(TournamentStatus.CONFIGURANDO)
}

@Entity(
    tableName = "tournament_teams",
    primaryKeys = ["tournamentId", "teamId"],
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId"), Index("teamId")]
)
data class TournamentTeamEntity(
    val tournamentId: Long,
    val teamId: String,
    val seedOrder: Int,
    val groupName: String? = null
)

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId"), Index("homeTeamId"), Index("awayTeamId")]
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tournamentId: Long,
    val stageType: String, // MatchStage.name
    val roundNumber: Int,
    val roundName: String,
    val matchIndex: Int,
    val groupName: String? = null,
    val homeTeamId: String? = null,
    val awayTeamId: String? = null,
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
    val penaltyWinnerTeamId: String? = null,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val winnerTeamId: String? = null,
    val isPlayed: Boolean = false,
    val scheduledDate: String,
    val nextMatchRound: Int? = null,
    val nextMatchIndex: Int? = null,
    val nextMatchIsHome: Boolean? = null
) {
    fun stageEnum(): MatchStage =
        runCatching { MatchStage.valueOf(stageType) }.getOrDefault(MatchStage.KNOCKOUT)
}

@Entity(
    tableName = "champions",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId", unique = true), Index("teamId")]
)
data class ChampionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tournamentId: Long,
    val competitionType: String,
    val tournamentName: String,
    val teamId: String,
    val runnerUpTeamId: String? = null,
    val finalScoreSummary: String,
    val prize: String,
    val crownedDate: String,
    val crownedAt: Long = System.currentTimeMillis()
)

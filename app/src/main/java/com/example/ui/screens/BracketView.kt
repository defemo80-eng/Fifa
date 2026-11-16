package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TeamEntity
import com.example.model.MatchStage
import com.example.model.MatchWithTeams
import com.example.model.StandingRow
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TeamBadge
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.StadiumNavyCard
import com.example.ui.theme.StadiumNavyElevated
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.StatusDraw
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KnockoutBracketView(
    knockoutMatches: List<MatchWithTeams>,
    onMatchClick: (MatchWithTeams) -> Unit
) {
    if (knockoutMatches.isEmpty()) {
        Text(
            text = "Realiza el sorteo para generar las llaves del torneo.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val rounds = knockoutMatches
        .groupBy { it.match.roundNumber }
        .toSortedMap()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "LLAVES DE ELIMINATORIA (DESLIZA HORIZONTALMENTE)",
            style = MaterialTheme.typography.labelMedium,
            color = FifaGold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            rounds.forEach { (roundNum, roundMatches) ->
                val roundTitle = roundMatches.firstOrNull()?.match?.roundName ?: "Ronda $roundNum"
                val isFinalRound = roundTitle.equals("Final", ignoreCase = true)

                Column(
                    modifier = Modifier.width(248.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = if (isFinalRound) FifaGold.copy(alpha = 0.22f) else StadiumNavyElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isFinalRound) FifaGold else StadiumBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isFinalRound) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = FifaGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = roundTitle.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFinalRound) FifaGoldBright else TextPrimary
                            )
                        }
                    }

                    roundMatches.sortedBy { it.match.matchIndex }.forEach { mwt ->
                        BracketMatchCard(
                            matchWithTeams = mwt,
                            isFinal = isFinalRound,
                            onClick = { onMatchClick(mwt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BracketMatchCard(
    matchWithTeams: MatchWithTeams,
    isFinal: Boolean = false,
    onClick: () -> Unit
) {
    val m = matchWithTeams.match
    val ready = m.homeTeamId != null && m.awayTeamId != null
    val borderColor = when {
        isFinal && m.isPlayed -> FifaGold
        m.isPlayed -> FifaEmerald.copy(alpha = 0.7f)
        ready -> StadiumBorder
        else -> StadiumBorder.copy(alpha = 0.4f)
    }

    SportsPanelCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${m.id}"),
        borderColor = borderColor,
        containerColor = if (ready) StadiumNavyCard else StadiumNavyCard.copy(alpha = 0.55f),
        onClick = if (ready) onClick else null
    ) {
        Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (m.groupName != null) "Grupo ${m.groupName}" else m.scheduledDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (!ready) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Esperando ganador",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Por definir", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                } else if (m.isPlayed) {
                    val penTxt = if (m.penaltyWinnerTeamId != null && m.homePenalties != null && m.awayPenalties != null) {
                        "PEN ${m.homePenalties}-${m.awayPenalties}"
                    } else if (m.penaltyWinnerTeamId != null) {
                        "PENALES"
                    } else {
                        "FINALIZADO"
                    }
                    Text(
                        text = penTxt,
                        style = MaterialTheme.typography.labelMedium,
                        color = FifaEmerald
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Registrar marcador",
                            tint = FifaGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Jugar", style = MaterialTheme.typography.labelMedium, color = FifaGold)
                    }
                }
            }

            BracketTeamRow(
                team = matchWithTeams.homeTeam,
                goals = m.homeGoals,
                isWinner = m.isPlayed && m.winnerTeamId == m.homeTeamId && m.homeTeamId != null
            )

            HorizontalDivider(color = StadiumBorder.copy(alpha = 0.5f))

            BracketTeamRow(
                team = matchWithTeams.awayTeam,
                goals = m.awayGoals,
                isWinner = m.isPlayed && m.winnerTeamId == m.awayTeamId && m.awayTeamId != null
            )
        }
    }
}

@Composable
private fun BracketTeamRow(
    team: TeamEntity?,
    goals: Int?,
    isWinner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            TeamBadge(team = team, size = 26.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = team?.name ?: "Por definir",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    team == null -> TextMuted
                    isWinner -> FifaGoldBright
                    else -> TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            color = if (isWinner) FifaEmerald.copy(alpha = 0.25f) else StadiumNavyElevated,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = goals?.toString() ?: "-",
                style = MaterialTheme.typography.labelLarge,
                color = if (isWinner) FifaEmerald else TextSecondary,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun StandingsTableCard(
    title: String,
    standings: List<StandingRow>,
    highlightQualifiersCount: Int = 2
) {
    SportsPanelCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = StadiumBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = FifaGoldBright
            )
            Spacer(Modifier.height(8.dp))

            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StadiumNavyElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(22.dp))
                Text("EQUIPO", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1f))
                Text("PJ", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("G", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("E", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("P", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("DG", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text("PTS", style = MaterialTheme.typography.labelSmall, color = FifaGold, modifier = Modifier.width(34.dp), textAlign = TextAlign.End)
            }

            Spacer(Modifier.height(4.dp))

            standings.forEach { row ->
                val isQualifying = row.position <= highlightQualifiersCount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.width(22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "${row.position}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isQualifying) FifaEmerald else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamBadge(team = row.team, size = 22.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = row.team.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text("${row.played}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text("${row.wins}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("${row.draws}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("${row.losses}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    val dgPrefix = if (row.goalDifference > 0) "+" else ""
                    Text("$dgPrefix${row.goalDifference}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text(
                        text = "${row.points}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (row.position == 1) FifaGoldBright else TextPrimary,
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.End
                    )
                }
                HorizontalDivider(color = StadiumBorder.copy(alpha = 0.28f))
            }
        }
    }
}

@Composable
fun MatchListItemCard(
    matchWithTeams: MatchWithTeams,
    onClick: () -> Unit
) {
    val m = matchWithTeams.match
    val ready = m.homeTeamId != null && m.awayTeamId != null
    val isDraw = m.isPlayed && m.homeGoals == m.awayGoals && m.stageEnum() != MatchStage.KNOCKOUT

    SportsPanelCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("list_match_${m.id}"),
        borderColor = when {
            m.isPlayed -> FifaEmerald.copy(alpha = 0.55f)
            ready -> StadiumBorder
            else -> StadiumBorder.copy(alpha = 0.35f)
        },
        onClick = if (ready) onClick else null
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${matchWithTeams.competitionTitle} • ${m.roundName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = FifaGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = when {
                        m.isPlayed -> FifaEmerald.copy(alpha = 0.16f)
                        ready -> StatusDraw.copy(alpha = 0.16f)
                        else -> StadiumNavyElevated
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when {
                            m.isPlayed -> "JUGADO"
                            ready -> "REGISTRAR"
                            else -> "POR DEFINIR"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            m.isPlayed -> FifaEmerald
                            ready -> FifaGoldBright
                            else -> TextMuted
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = matchWithTeams.homeTeam?.name ?: "Por definir",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (m.winnerTeamId == m.homeTeamId && m.homeTeamId != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (m.winnerTeamId == m.homeTeamId && m.homeTeamId != null) FifaGoldBright else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(Modifier.width(8.dp))
                    TeamBadge(team = matchWithTeams.homeTeam, size = 30.dp)
                }

                // Score Center Box
                Surface(
                    color = StadiumNavyElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDraw) StatusDraw.copy(alpha = 0.5f) else StadiumBorder),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = if (m.isPlayed) "${m.homeGoals} - ${m.awayGoals}" else "VS",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (m.isPlayed) Color.White else FifaGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Away
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TeamBadge(team = matchWithTeams.awayTeam, size = 30.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = matchWithTeams.awayTeam?.name ?: "Por definir",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (m.winnerTeamId == m.awayTeamId && m.awayTeamId != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (m.winnerTeamId == m.awayTeamId && m.awayTeamId != null) FifaGoldBright else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${matchWithTeams.tournamentName} • ${m.scheduledDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (m.penaltyWinnerTeamId != null) {
                    val penTeamName = if (m.penaltyWinnerTeamId == m.homeTeamId) {
                        matchWithTeams.homeTeam?.shortCode
                    } else {
                        matchWithTeams.awayTeam?.shortCode
                    }
                    Text(
                        text = "Penales: $penTeamName (${m.homePenalties ?: ""}-${m.awayPenalties ?: ""})",
                        style = MaterialTheme.typography.labelSmall,
                        color = FifaGoldBright
                    )
                }
            }
        }
    }
}

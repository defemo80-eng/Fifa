package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.GlobalStats
import com.example.model.TeamPerformanceStats
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TeamBadge
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatsScreen(
    globalStats: GlobalStats,
    teamLeaderboard: List<TeamPerformanceStats>
) {
    val activeTeams = remember(teamLeaderboard) {
        teamLeaderboard.filter { it.played > 0 || it.titles > 0 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = FifaEmerald,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ESTADÍSTICAS GLOBALES",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Métricas reales guardadas en SQLite",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            SportsPanelCard(borderColor = FifaGold.copy(alpha = 0.55f)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Torneos Creados", "${globalStats.tournamentsCreated}", Modifier.weight(1f), highlight = true)
                        StatTile("Campeones", "${globalStats.championsCount}", Modifier.weight(1f), highlight = true)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Partidos Jugados", "${globalStats.matchesPlayed}", Modifier.weight(1f))
                        StatTile("Pendientes", "${globalStats.matchesPending}", Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Victorias / Derrotas", "${globalStats.totalWinsResolved}", Modifier.weight(1f))
                        StatTile("Empates", "${globalStats.totalDraws}", Modifier.weight(1f))
                        StatTile("Goles Anotados", "${globalStats.totalGoals}", Modifier.weight(1f), highlight = true)
                    }
                }
            }
        }

        item {
            Text(
                text = "RENDIMIENTO DE LOS EQUIPOS",
                style = MaterialTheme.typography.headlineMedium,
                color = FifaGoldBright
            )
        }

        if (activeTeams.isEmpty()) {
            item {
                SportsPanelCard {
                    Text(
                        text = "Todavía no se han disputado partidos. Registra marcadores en cualquier competición para ver el ranking de rendimiento de selecciones y clubes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(activeTeams, key = { it.team.id }) { perf ->
                SportsPanelCard(borderColor = if (perf.titles > 0) FifaGold.copy(alpha = 0.65f) else StadiumBorder) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamBadge(team = perf.team, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = perf.team.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "PJ ${perf.played} • G ${perf.wins} • E ${perf.draws} • P ${perf.losses} • GF ${perf.goalsFor} • GC ${perf.goalsAgainst}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${perf.winRatePercent}% VIC",
                                style = MaterialTheme.typography.labelLarge,
                                color = FifaEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            if (perf.titles > 0) {
                                Text(
                                    text = "${perf.titles} Tít.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = FifaGoldBright
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

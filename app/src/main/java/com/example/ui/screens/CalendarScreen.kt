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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.CompetitionType
import com.example.model.MatchWithTeams
import com.example.ui.components.MatchScoreDialog
import com.example.ui.components.SportsPanelCard
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumNavyCard
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CalendarScreen(
    allMatches: List<MatchWithTeams>,
    onRecordScore: (Long, Int, Int, String?, Int?, Int?, String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PLAYED
    var compFilter by remember { mutableStateOf("ALL") }
    var matchToScore by remember { mutableStateOf<MatchWithTeams?>(null) }

    val filtered = remember(allMatches, statusFilter, compFilter) {
        allMatches.filter { item ->
            val matchesStatus = when (statusFilter) {
                "PENDING" -> !item.match.isPlayed
                "PLAYED" -> item.match.isPlayed
                else -> true
            }
            val matchesComp = compFilter == "ALL" || item.competitionTitle == compFilter
            matchesStatus && matchesComp
        }
    }

    val groupedByDate = remember(filtered) {
        filtered.groupBy { it.match.scheduledDate }.toSortedMap()
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
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = FifaGoldBright,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CALENDARIO DE PARTIDOS",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${allMatches.count { !it.match.isPlayed }} pendientes • ${allMatches.count { it.match.isPlayed }} jugados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "Todos", "PENDING" to "Pendientes", "PLAYED" to "Jugados").forEach { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { statusFilter = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaEmerald,
                            selectedLabelColor = StadiumObsidian,
                            containerColor = StadiumNavyCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = compFilter == "ALL",
                        onClick = { compFilter = "ALL" },
                        label = { Text("Todas las competiciones") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaGold,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
                items(CompetitionType.entries, key = { it.id }) { comp ->
                    FilterChip(
                        selected = compFilter == comp.title,
                        onClick = { compFilter = comp.title },
                        label = { Text(comp.shortTitle) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaGold,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
            }
        }

        if (groupedByDate.isEmpty()) {
            item {
                SportsPanelCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = FifaGold, modifier = Modifier.size(42.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No hay partidos en el calendario",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Entra a cualquiera de las 11 competiciones en INICIO, elige equipos y realiza el sorteo para programar los encuentros.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            groupedByDate.forEach { (date, dayMatches) ->
                item(key = "date_hdr_$date") {
                    Text(
                        text = "FECHA: $date",
                        style = MaterialTheme.typography.labelLarge,
                        color = FifaGoldBright,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(dayMatches, key = { it.match.id }) { mwt ->
                    MatchListItemCard(
                        matchWithTeams = mwt,
                        onClick = { matchToScore = mwt }
                    )
                }
            }
        }
    }

    matchToScore?.let { mwt ->
        MatchScoreDialog(
            matchWithTeams = mwt,
            onDismiss = { matchToScore = null },
            onConfirm = { hg, ag, penWinner, hp, ap ->
                onRecordScore(
                    mwt.match.id,
                    hg,
                    ag,
                    penWinner,
                    hp,
                    ap,
                    mwt.tournamentName
                )
                matchToScore = null
            }
        )
    }
}

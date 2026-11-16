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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Confederation
import com.example.model.TeamPerformanceStats
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TeamBadge
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumNavyElevated
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TeamsScreen(
    teamLeaderboard: List<TeamPerformanceStats>
) {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ALL") } // ALL, SELECCION, CLUB
    var confederation by remember { mutableStateOf("ALL") }
    var selectedTeamStats by remember { mutableStateOf<TeamPerformanceStats?>(null) }

    val filtered = remember(teamLeaderboard, search, category, confederation) {
        teamLeaderboard.filter { item ->
            val t = item.team
            val matchSearch = search.isBlank() ||
                t.name.contains(search, ignoreCase = true) ||
                t.shortCode.contains(search, ignoreCase = true)
            val matchCat = category == "ALL" || t.category == category
            val matchConf = confederation == "ALL" || t.confederation == confederation
            matchSearch && matchCat && matchConf
        }.sortedByDescending { it.team.rating }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = FifaGoldBright,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CATÁLOGO DE EQUIPOS",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${teamLeaderboard.size} selecciones nacionales y clubes oficiales",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Buscar equipo, país o código...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FifaGold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_input"),
                colors = fifaTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "Todos", "SELECCION" to "Selecciones", "CLUB" to "Clubes").forEach { (key, label) ->
                    FilterChip(
                        selected = category == key,
                        onClick = { category = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaEmerald,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = confederation == "ALL",
                        onClick = { confederation = "ALL" },
                        label = { Text("Todas las regiones") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaGold,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
                items(Confederation.entries) { conf ->
                    FilterChip(
                        selected = confederation == conf.name,
                        onClick = { confederation = conf.name },
                        label = { Text(conf.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaGold,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
            }
        }

        items(filtered, key = { it.team.id }) { perf ->
            val team = perf.team
            SportsPanelCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_team_${team.id}"),
                borderColor = if (perf.titles > 0) FifaGold.copy(alpha = 0.65f) else StadiumBorder,
                onClick = { selectedTeamStats = perf }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamBadge(team = team, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${team.categoryEnum().label} • ${team.confederationEnum().label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (perf.played > 0) {
                            Text(
                                text = "PJ ${perf.played} • G ${perf.wins} • E ${perf.draws} • P ${perf.losses}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FifaEmerald
                            )
                        }
                    }
                    Surface(
                        color = StadiumNavyElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FifaGold.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${team.rating}", style = MaterialTheme.typography.titleMedium, color = FifaGoldBright)
                            Text("OVR", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    selectedTeamStats?.let { perf ->
        val t = perf.team
        AlertDialog(
            onDismissRequest = { selectedTeamStats = null },
            containerColor = StadiumNavyDeep,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamBadge(team = t, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(t.name, style = MaterialTheme.typography.headlineMedium, color = FifaGoldBright)
                        Text("${t.categoryEnum().label} • ${t.confederationEnum().label}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Nivel OVR", "${t.rating}", Modifier.weight(1f), highlight = true)
                        StatTile("Títulos", "${perf.titles}", Modifier.weight(1f), highlight = true)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Jugados", "${perf.played}", Modifier.weight(1f))
                        StatTile("Victorias", "${perf.wins}", Modifier.weight(1f))
                        StatTile("Empates", "${perf.draws}", Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Derrotas", "${perf.losses}", Modifier.weight(1f))
                        StatTile("Goles Favor", "${perf.goalsFor}", Modifier.weight(1f))
                        StatTile("Goles Contra", "${perf.goalsAgainst}", Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTeamStats = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FifaEmerald, contentColor = StadiumObsidian)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

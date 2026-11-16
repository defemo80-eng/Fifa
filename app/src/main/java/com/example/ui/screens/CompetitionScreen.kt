package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TeamEntity
import com.example.data.TournamentEntity
import com.example.model.CompetitionStats
import com.example.model.CompetitionType
import com.example.model.MatchStage
import com.example.model.MatchWithTeams
import com.example.model.TeamCategory
import com.example.model.TournamentDetailBundle
import com.example.model.TournamentFormat
import com.example.model.TournamentStatus
import com.example.ui.components.MatchScoreDialog
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TeamBadge
import com.example.ui.components.TrophyShowcaseBadge
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.StadiumNavyCard
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumNavyElevated
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.StatusLoss
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CompetitionScreen(
    competition: CompetitionType,
    tournaments: List<TournamentEntity>,
    activeBundle: TournamentDetailBundle?,
    allTeams: List<TeamEntity>,
    competitionStats: CompetitionStats,
    onBack: () -> Unit,
    onSelectTournament: (Long) -> Unit,
    onCreateNewTournament: () -> Unit,
    onSaveConfig: (Long, String, String, String, Int, TournamentFormat) -> Unit,
    onToggleTeam: (Long, String) -> Unit,
    onAutoFillTeams: (Long) -> Unit,
    onClearTeams: (Long) -> Unit,
    onStartDraw: (Long, () -> Unit) -> Unit,
    onResetDraw: (Long) -> Unit,
    onDeleteTournament: (Long) -> Unit,
    onRecordScore: (Long, Int, Int, String?, Int?, Int?, String) -> Unit
) {
    BackHandler { onBack() }

    val accent = Color(competition.accentColorHex)
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Config, 1: Equipos, 2: Estadísticas, 3: Modo de Juego
    var showDetailsDialog by remember { mutableStateOf(false) }
    var matchToScore by remember { mutableStateOf<MatchWithTeams?>(null) }

    // Default to Modo Juego when resuming an already-started tournament
    val tournament = activeBundle?.tournament
    LaunchedEffect(tournament?.id) {
        if (tournament != null && tournament.statusEnum() != TournamentStatus.CONFIGURANDO) {
            selectedSection = 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Competition Top Bar
        Surface(
            color = StadiumNavyDeep.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("competition_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    TrophyShowcaseBadge(accentColor = accent, size = 38.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = competition.title.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = competition.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.testTag("competition_details_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Consultar detalles",
                            tint = FifaGoldBright
                        )
                    }
                    IconButton(
                        onClick = onCreateNewTournament,
                        modifier = Modifier.testTag("competition_new_tournament_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Nuevo torneo",
                            tint = FifaEmerald
                        )
                    }
                }

                // Independent tournament selector chips within this competition
                if (tournaments.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tournaments, key = { it.id }) { tour ->
                            val isSelected = tour.id == tournament?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectTournament(tour.id) },
                                label = {
                                    Text(
                                        text = "${tour.name} (${tour.statusEnum().label})",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent.copy(alpha = 0.25f),
                                    selectedLabelColor = FifaGoldBright,
                                    containerColor = StadiumNavyCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                // 4 Mandatory Sections TabRow
                val tabs = listOf("Config.", "Equipos", "Estadísticas", "Modo Juego")
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color.Transparent,
                    contentColor = accent,
                    indicator = { tabPositions ->
                        if (selectedSection < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                                color = accent
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedSection == idx,
                            onClick = { selectedSection = idx },
                            modifier = Modifier.testTag("comp_tab_$idx"),
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selectedSection == idx) accent else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        }

        if (activeBundle == null || tournament == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando competición...", color = TextSecondary)
            }
        } else {
            when (selectedSection) {
                0 -> ConfigurationSection(
                    competition = competition,
                    bundle = activeBundle,
                    onSaveConfig = onSaveConfig,
                    onGoToTeams = { selectedSection = 1 },
                    onStartDraw = {
                        onStartDraw(tournament.id) { selectedSection = 3 }
                    },
                    onDeleteTournament = { onDeleteTournament(tournament.id) }
                )
                1 -> ParticipantsSection(
                    competition = competition,
                    bundle = activeBundle,
                    allTeams = allTeams,
                    onToggleTeam = { teamId -> onToggleTeam(tournament.id, teamId) },
                    onAutoFill = { onAutoFillTeams(tournament.id) },
                    onClear = { onClearTeams(tournament.id) },
                    onStartDraw = {
                        onStartDraw(tournament.id) { selectedSection = 3 }
                    }
                )
                2 -> CompetitionStatsSection(
                    competition = competition,
                    stats = competitionStats
                )
                3 -> GameModeSection(
                    competition = competition,
                    bundle = activeBundle,
                    onStartDraw = { onStartDraw(tournament.id) {} },
                    onResetDraw = { onResetDraw(tournament.id) },
                    onGoToTeams = { selectedSection = 1 },
                    onMatchClick = { matchToScore = it }
                )
            }
        }
    }

    if (showDetailsDialog && tournament != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = StadiumNavyDeep,
            title = {
                Text(
                    text = "DETALLES • ${competition.title.uppercase()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FifaGoldBright
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Torneo", tournament.name)
                    DetailRow("Categoría", competition.teamPoolRule.label)
                    DetailRow("Formato", tournament.formatEnum().label)
                    DetailRow("Participantes", "${activeBundle.selectedTeams.size} / ${tournament.participantCount}")
                    DetailRow("Fecha", tournament.startDate)
                    DetailRow("Trofeo", competition.trophyTitle)
                    DetailRow("Premio", tournament.prize)
                    DetailRow("Estado", tournament.statusEnum().label)
                    if (activeBundle.championTeam != null) {
                        DetailRow("Campeón", activeBundle.championTeam.name)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FifaEmerald, contentColor = StadiumObsidian)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
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

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConfigurationSection(
    competition: CompetitionType,
    bundle: TournamentDetailBundle,
    onSaveConfig: (Long, String, String, String, Int, TournamentFormat) -> Unit,
    onGoToTeams: () -> Unit,
    onStartDraw: () -> Unit,
    onDeleteTournament: () -> Unit
) {
    val t = bundle.tournament
    var name by remember(t.id, t.name) { mutableStateOf(t.name) }
    var date by remember(t.id, t.startDate) { mutableStateOf(t.startDate) }
    var prize by remember(t.id, t.prize) { mutableStateOf(t.prize) }
    var participantCount by remember(t.id, t.participantCount) { mutableIntStateOf(t.participantCount) }
    var format by remember(t.id, t.format) { mutableStateOf(t.formatEnum()) }

    val isEditable = t.statusEnum() == TournamentStatus.CONFIGURANDO

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SportsPanelCard(borderColor = FifaGold.copy(alpha = 0.45f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CONFIGURACIÓN DEL TORNEO",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FifaGoldBright
                    )
                    if (!isEditable) {
                        Text(
                            text = "Este torneo ya está en estado '${t.statusEnum().label}'. Puedes ver su configuración o crear/reiniciar en Modo de Juego.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FifaGold
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (isEditable) name = it },
                        label = { Text("Nombre del torneo") },
                        enabled = isEditable,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_name_input"),
                        colors = fifaTextFieldColors()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { if (isEditable) date = it },
                        label = { Text("Fecha de inicio (AAAA-MM-DD)") },
                        enabled = isEditable,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_date_input"),
                        colors = fifaTextFieldColors()
                    )

                    OutlinedTextField(
                        value = prize,
                        onValueChange = { if (isEditable) prize = it },
                        label = { Text("Premio o recompensa") },
                        enabled = isEditable,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_prize_input"),
                        colors = fifaTextFieldColors()
                    )

                    Text(
                        text = "Número de participantes",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(competition.allowedParticipantCounts) { count ->
                            FilterChip(
                                selected = participantCount == count,
                                onClick = { if (isEditable) participantCount = count },
                                enabled = isEditable,
                                modifier = Modifier.testTag("config_count_$count"),
                                label = { Text("$count equipos") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FifaEmerald,
                                    selectedLabelColor = StadiumObsidian
                                )
                            )
                        }
                    }

                    Text(
                        text = "Formato de competición",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        competition.supportedFormats.forEach { fmt ->
                            val selected = format == fmt
                            SportsPanelCard(
                                borderColor = if (selected) FifaEmerald else StadiumBorder,
                                containerColor = if (selected) FifaEmerald.copy(alpha = 0.14f) else StadiumNavyElevated,
                                onClick = if (isEditable) {
                                    { format = fmt }
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fmt.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (selected) FifaEmerald else TextPrimary
                                        )
                                        Text(
                                            text = fmt.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = FifaEmerald
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isEditable) {
                        Button(
                            onClick = {
                                onSaveConfig(t.id, name, date, prize, participantCount, format)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_config_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FifaGold,
                                contentColor = StadiumObsidian
                            )
                        ) {
                            Text("GUARDAR CONFIGURACIÓN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onGoToTeams,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("config_goto_teams_btn"),
                    border = BorderStroke(1.dp, FifaGold)
                ) {
                    Text(
                        text = "EQUIPOS (${bundle.selectedTeams.size}/$participantCount)",
                        color = FifaGoldBright,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        if (isEditable) {
                            onSaveConfig(t.id, name, date, prize, participantCount, format)
                        }
                        onStartDraw()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("config_start_comp_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FifaEmerald,
                        contentColor = StadiumObsidian
                    )
                ) {
                    Icon(Icons.Filled.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("INICIAR", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onDeleteTournament,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, StatusLoss.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = StatusLoss, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar este torneo", color = StatusLoss)
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    competition: CompetitionType,
    bundle: TournamentDetailBundle,
    allTeams: List<TeamEntity>,
    onToggleTeam: (String) -> Unit,
    onAutoFill: () -> Unit,
    onClear: () -> Unit,
    onStartDraw: () -> Unit
) {
    val t = bundle.tournament
    val selectedIds = bundle.selectedTeams.map { it.id }.toSet()
    val isEditable = t.statusEnum() == TournamentStatus.CONFIGURANDO

    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember(competition.id) {
        mutableStateOf(
            when (competition.teamPoolRule) {
                TeamCategory.SELECCION -> "SELECCION"
                TeamCategory.CLUB -> "CLUB"
                TeamCategory.MIXTO -> "ALL"
            }
        )
    }

    val filteredTeams = remember(allTeams, searchQuery, categoryFilter) {
        allTeams.filter { team ->
            val matchesCat = categoryFilter == "ALL" || team.category == categoryFilter
            val matchesQuery = searchQuery.isBlank() ||
                team.name.contains(searchQuery, ignoreCase = true) ||
                team.shortCode.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SportsPanelCard(borderColor = FifaEmerald.copy(alpha = 0.55f)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EQUIPOS PARTICIPANTES",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedIds.size} de ${t.participantCount} seleccionados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedIds.size == t.participantCount) FifaEmerald else FifaGold
                            )
                        }
                        if (selectedIds.size == t.participantCount && isEditable) {
                            Button(
                                onClick = onStartDraw,
                                modifier = Modifier.testTag("participants_draw_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FifaEmerald,
                                    contentColor = StadiumObsidian
                                )
                            ) {
                                Text("SORTEAR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isEditable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAutoFill,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("autofill_teams_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StadiumNavyElevated,
                                    contentColor = FifaGoldBright
                                )
                            ) {
                                Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Completar al azar", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = onClear,
                                modifier = Modifier.testTag("clear_teams_btn")
                            ) {
                                Text("Limpiar", color = TextSecondary)
                            }
                        }
                    }

                    if (bundle.selectedTeams.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bundle.selectedTeams, key = { it.id }) { team ->
                                Surface(
                                    color = FifaEmerald.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, FifaEmerald.copy(alpha = 0.7f)),
                                    onClick = { if (isEditable) onToggleTeam(team.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TeamBadge(team = team, size = 22.dp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = team.shortCode,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar selección o club...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FifaGold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("participants_search_input"),
                colors = fifaTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "Todos", "SELECCION" to "Selecciones", "CLUB" to "Clubes").forEach { (key, label) ->
                    FilterChip(
                        selected = categoryFilter == key,
                        onClick = { categoryFilter = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FifaGold,
                            selectedLabelColor = StadiumObsidian
                        )
                    )
                }
            }
        }

        items(filteredTeams, key = { it.id }) { team ->
            val isSelected = team.id in selectedIds
            SportsPanelCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pick_team_${team.id}"),
                borderColor = if (isSelected) FifaEmerald else StadiumBorder,
                containerColor = if (isSelected) FifaEmerald.copy(alpha = 0.14f) else StadiumNavyCard,
                onClick = if (isEditable) {
                    { onToggleTeam(team.id) }
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamBadge(team = team, size = 38.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${team.categoryEnum().label} • ${team.confederationEnum().label} • OVR ${team.rating}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = if (isSelected) FifaEmerald else StadiumNavyElevated,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isSelected) "SELECCIONADO" else "ELEGIR",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) StadiumObsidian else FifaGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompetitionStatsSection(
    competition: CompetitionType,
    stats: CompetitionStats
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SportsPanelCard(borderColor = Color(competition.accentColorHex).copy(alpha = 0.55f)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ESTADÍSTICAS DE ${competition.title.uppercase()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FifaGoldBright
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Torneos", "${stats.tournamentsCount}", Modifier.weight(1f))
                        StatTile("Finalizados", "${stats.finishedCount}", Modifier.weight(1f))
                        StatTile("Goles", "${stats.totalGoals}", Modifier.weight(1f), highlight = true)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Partidos Jugados", "${stats.matchesPlayed}", Modifier.weight(1f))
                        StatTile("Pendientes", "${stats.matchesPending}", Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text(
                text = "CAMPEONES HISTÓRICOS",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        if (stats.champions.isEmpty()) {
            item {
                SportsPanelCard {
                    Text(
                        text = "Aún no hay campeones coronados en ${competition.title}. Completa una final o liga para registrar al campeón.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(stats.champions, key = { it.champion.id }) { item ->
                SportsPanelCard(borderColor = FifaGold.copy(alpha = 0.65f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamBadge(team = item.team, size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.team?.name ?: "Campeón",
                                style = MaterialTheme.typography.titleMedium,
                                color = FifaGoldBright
                            )
                            Text(
                                text = "${item.champion.tournamentName} • ${item.champion.finalScoreSummary}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = FifaGold)
                    }
                }
            }
        }

        if (stats.topTeams.isNotEmpty()) {
            item {
                Text(
                    text = "RENDIMIENTO EN ESTA COMPETICIÓN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
            items(stats.topTeams.take(12), key = { it.team.id }) { perf ->
                SportsPanelCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamBadge(team = perf.team, size = 34.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(perf.team.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "PJ ${perf.played} • G ${perf.wins} • E ${perf.draws} • P ${perf.losses} • GF ${perf.goalsFor}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        if (perf.titles > 0) {
                            Text("${perf.titles} 🏆", style = MaterialTheme.typography.labelLarge, color = FifaGoldBright)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeSection(
    competition: CompetitionType,
    bundle: TournamentDetailBundle,
    onStartDraw: () -> Unit,
    onResetDraw: () -> Unit,
    onGoToTeams: () -> Unit,
    onMatchClick: (MatchWithTeams) -> Unit
) {
    val t = bundle.tournament
    val isConfiguring = t.statusEnum() == TournamentStatus.CONFIGURANDO
    val koMatches = bundle.matches.filter { it.match.stageEnum() == MatchStage.KNOCKOUT }
    val groupMatches = bundle.matches.filter { it.match.stageEnum() == MatchStage.GROUP }
    val leagueMatches = bundle.matches.filter { it.match.stageEnum() == MatchStage.LEAGUE }

    var subView by remember(t.id, t.format) {
        mutableIntStateOf(
            when {
                koMatches.isNotEmpty() && groupMatches.isEmpty() -> 0 // Llaves
                groupMatches.isNotEmpty() -> 1 // Grupos y Tablas
                else -> 2 // Jornadas / Tabla Liga
            }
        )
    }

    if (isConfiguring || bundle.matches.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrophyShowcaseBadge(accentColor = Color(competition.accentColorHex), size = 72.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "LISTO PARA EL SORTEO",
                style = MaterialTheme.typography.headlineLarge,
                color = FifaGoldBright
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Equipos seleccionados: ${bundle.selectedTeams.size} / ${t.participantCount}\nFormato: ${t.formatEnum().label}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(20.dp))
            if (bundle.selectedTeams.size < t.participantCount) {
                Button(
                    onClick = onGoToTeams,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FifaGold, contentColor = StadiumObsidian)
                ) {
                    Text("SELECCIONAR EQUIPOS (${bundle.selectedTeams.size}/${t.participantCount})", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
            }
            Button(
                onClick = onStartDraw,
                enabled = bundle.selectedTeams.size == t.participantCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("gamemode_draw_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = FifaEmerald, contentColor = StadiumObsidian)
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("REALIZAR SORTEO Y CREAR LLAVES", fontWeight = FontWeight.ExtraBold)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Champion Banner if finished
        if (bundle.championTeam != null) {
            item {
                SportsPanelCard(
                    borderColor = FifaGold,
                    containerColor = FifaGold.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamBadge(team = bundle.championTeam, size = 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CAMPEÓN DEL TORNEO", style = MaterialTheme.typography.labelMedium, color = FifaGold)
                            Text(bundle.championTeam.name.uppercase(), style = MaterialTheme.typography.headlineMedium, color = FifaGoldBright)
                            Text(t.prize, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = FifaGoldBright, modifier = Modifier.size(34.dp))
                    }
                }
            }
        }

        // Sub-view switches & Reset Draw action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (koMatches.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = subView == 0,
                                onClick = { subView = 0 },
                                label = { Text("Llaves") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FifaEmerald,
                                    selectedLabelColor = StadiumObsidian
                                )
                            )
                        }
                    }
                    if (groupMatches.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = subView == 1,
                                onClick = { subView = 1 },
                                label = { Text("Grupos y Tablas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FifaEmerald,
                                    selectedLabelColor = StadiumObsidian
                                )
                            )
                        }
                    }
                    if (leagueMatches.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = subView == 2,
                                onClick = { subView = 2 },
                                label = { Text("Tabla y Jornadas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FifaEmerald,
                                    selectedLabelColor = StadiumObsidian
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onResetDraw,
                    modifier = Modifier.testTag("reset_draw_btn")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar sorteo", tint = FifaGold)
                }
            }
        }

        if (subView == 0 && koMatches.isNotEmpty()) {
            item {
                KnockoutBracketView(
                    knockoutMatches = koMatches,
                    onMatchClick = onMatchClick
                )
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "TODOS LOS PARTIDOS DE ELIMINATORIA",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            items(koMatches, key = { it.match.id }) { mwt ->
                MatchListItemCard(matchWithTeams = mwt, onClick = { onMatchClick(mwt) })
            }
        } else if (subView == 1 && groupMatches.isNotEmpty()) {
            val allGroupsDone = groupMatches.all { it.match.isPlayed }
            if (!allGroupsDone) {
                item {
                    SportsPanelCard(borderColor = FifaGold.copy(alpha = 0.5f)) {
                        Text(
                            text = "Completa todos los partidos de la Fase de Grupos para clasificar automáticamente a las llaves finales.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FifaGoldBright,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            bundle.groupStandings.toSortedMap().forEach { (groupName, rows) ->
                item(key = "group_table_$groupName") {
                    StandingsTableCard(
                        title = "Grupo $groupName",
                        standings = rows,
                        highlightQualifiersCount = 2
                    )
                }
            }
            item {
                Text(
                    text = "PARTIDOS DE FASE DE GRUPOS",
                    style = MaterialTheme.typography.titleMedium,
                    color = FifaGoldBright
                )
            }
            items(groupMatches, key = { it.match.id }) { mwt ->
                MatchListItemCard(matchWithTeams = mwt, onClick = { onMatchClick(mwt) })
            }
        } else if (leagueMatches.isNotEmpty()) {
            item {
                StandingsTableCard(
                    title = "Tabla de Posiciones • ${t.name}",
                    standings = bundle.leagueStandings,
                    highlightQualifiersCount = 1
                )
            }
            item {
                Text(
                    text = "CALENDARIO DE JORNADAS",
                    style = MaterialTheme.typography.titleMedium,
                    color = FifaGoldBright
                )
            }
            items(leagueMatches, key = { it.match.id }) { mwt ->
                MatchListItemCard(matchWithTeams = mwt, onClick = { onMatchClick(mwt) })
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = StadiumNavyElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (highlight) FifaGold.copy(alpha = 0.65f) else StadiumBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = if (highlight) FifaGoldBright else TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun fifaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FifaGold,
    unfocusedBorderColor = StadiumBorder,
    focusedLabelColor = FifaGoldBright,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = FifaEmerald
)

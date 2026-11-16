package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TournamentEntity
import com.example.model.CompetitionType
import com.example.model.GlobalStats
import com.example.model.TournamentStatus
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TrophyShowcaseBadge
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.StadiumNavyCard
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumNavyElevated
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    tournaments: List<TournamentEntity>,
    globalStats: GlobalStats,
    onOpenCompetition: (CompetitionType, Long?) -> Unit
) {
    val tournamentsByComp = tournaments.groupBy { it.competitionType }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Panoramic Stadium Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_menu_principal),
                    contentDescription = "Estadio Menú Principal FIFA CUP",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to StadiumObsidian.copy(alpha = 0.45f),
                                0.55f to StadiumNavyDeep.copy(alpha = 0.65f),
                                1.0f to StadiumObsidian
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Surface(
                        color = StadiumNavyDeep.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, FifaGold.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = FifaGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "PRESIDENCIA: DEIVER MANUEL FERNÁNDEZ MORALES",
                                style = MaterialTheme.typography.labelMedium,
                                color = FifaGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "FIFA CUP • MENÚ PRINCIPAL",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniStatChip("${globalStats.tournamentsCreated} Torneos")
                        MiniStatChip("${globalStats.matchesPlayed} Jugados")
                        MiniStatChip("${globalStats.championsCount} Campeones", gold = true)
                    }
                }
            }
        }

        // Active / Created Tournaments Quick Resume
        if (tournaments.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "TORNEOS CREADOS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FifaGoldBright
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tournaments, key = { it.id }) { tour ->
                            val comp = CompetitionType.fromId(tour.competitionType)
                            val accent = Color(comp.accentColorHex)
                            val statusColor = when (tour.statusEnum()) {
                                TournamentStatus.CONFIGURANDO -> FifaGold
                                TournamentStatus.EN_CURSO -> FifaEmerald
                                TournamentStatus.FINALIZADO -> accent
                            }
                            SportsPanelCard(
                                modifier = Modifier
                                    .width(235.dp)
                                    .testTag("resume_tournament_${tour.id}"),
                                borderColor = statusColor.copy(alpha = 0.65f),
                                onClick = { onOpenCompetition(comp, tour.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = comp.shortTitle.uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accent
                                        )
                                        Surface(
                                            color = statusColor.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tour.statusEnum().label,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = tour.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tour.participantCount} equipos • ${tour.formatEnum().label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title for all 11 Competitions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "11 COMPETICIONES OFICIALES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SportsSoccer,
                        contentDescription = null,
                        tint = FifaEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Independientes",
                        style = MaterialTheme.typography.labelMedium,
                        color = FifaEmerald
                    )
                }
            }
        }

        // The 11 Official Competitions
        items(CompetitionType.entries, key = { it.id }) { comp ->
            val accent = Color(comp.accentColorHex)
            val compTours = tournamentsByComp[comp.id].orEmpty()
            val latestTour = compTours.firstOrNull()

            SportsPanelCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("competition_card_${comp.id}"),
                borderColor = accent.copy(alpha = 0.55f),
                containerColor = StadiumNavyCard.copy(alpha = 0.94f),
                onClick = { onOpenCompetition(comp, latestTour?.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrophyShowcaseBadge(accentColor = accent, size = 50.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${comp.order}. ${comp.title}",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = comp.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = StadiumNavyElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, StadiumBorder)
                            ) {
                                Text(
                                    text = "${comp.defaultParticipantCount} eq. • ${comp.defaultFormat.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (compTours.isNotEmpty()) {
                                Surface(
                                    color = FifaEmerald.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${compTours.size} activo${if (compTours.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FifaEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        color = FifaEmerald,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Abrir ${comp.title}",
                                tint = StadiumObsidian
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatChip(text: String, gold: Boolean = false) {
    Surface(
        color = if (gold) FifaGold.copy(alpha = 0.2f) else StadiumNavyElevated.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (gold) FifaGold.copy(alpha = 0.7f) else StadiumBorder)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (gold) FifaGoldBright else TextPrimary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

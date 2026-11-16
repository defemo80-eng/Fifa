package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChampionWithDetails
import com.example.model.CompetitionType
import com.example.model.MatchWithTeams
import com.example.ui.components.MatchScoreDialog
import com.example.ui.components.SportsPanelCard
import com.example.ui.components.TeamBadge
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HistoryScreen(
    champions: List<ChampionWithDetails>,
    allMatches: List<MatchWithTeams>,
    onRecordScore: (Long, Int, Int, String?, Int?, Int?, String) -> Unit
) {
    val playedMatches = remember(allMatches) {
        allMatches.filter { it.match.isPlayed }.reversed()
    }
    var matchToEdit by remember { mutableStateOf<MatchWithTeams?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_trophy_banner),
                    contentDescription = "Salón de Trofeos FIFA CUP",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to StadiumObsidian.copy(alpha = 0.35f),
                                1.0f to StadiumObsidian
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "HISTORIAL Y SALÓN DE CAMPEONES",
                        style = MaterialTheme.typography.headlineLarge,
                        color = FifaGoldBright
                    )
                    Text(
                        text = "${champions.size} campeones coronados • ${playedMatches.size} partidos disputados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "CAMPEONES OFICIALES",
                style = MaterialTheme.typography.headlineMedium,
                color = FifaGold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (champions.isEmpty()) {
            item {
                SportsPanelCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = FifaGold, modifier = Modifier.size(38.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Aún no hay campeones registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Juega una competición hasta la Gran Final o completa una liga para inmortalizar al campeón aquí.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(champions, key = { it.champion.id }) { item ->
                val comp = CompetitionType.fromId(item.champion.competitionType)
                val accent = Color(comp.accentColorHex)
                SportsPanelCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    borderColor = FifaGold.copy(alpha = 0.75f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = accent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = comp.title.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = item.champion.crownedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeamBadge(team = item.team, size = 48.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.team?.name?.uppercase() ?: "CAMPEÓN",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = FifaGoldBright,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${item.champion.tournamentName} • ${item.champion.finalScoreSummary}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                if (item.runnerUp != null) {
                                    Text(
                                        text = "Subcampeón: ${item.runnerUp.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Trofeo",
                                tint = FifaGoldBright,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = "Premio: ${item.champion.prize}",
                            style = MaterialTheme.typography.labelMedium,
                            color = FifaGold
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "PARTIDOS Y FINALES REALIZADAS",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (playedMatches.isEmpty()) {
            item {
                SportsPanelCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.History, null, tint = TextSecondary)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Los resultados que registres aparecerán guardados permanentemente aquí.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(playedMatches, key = { it.match.id }) { mwt ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MatchListItemCard(
                        matchWithTeams = mwt,
                        onClick = { matchToEdit = mwt }
                    )
                }
            }
        }
    }

    matchToEdit?.let { mwt ->
        MatchScoreDialog(
            matchWithTeams = mwt,
            onDismiss = { matchToEdit = null },
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
                matchToEdit = null
            }
        )
    }
}

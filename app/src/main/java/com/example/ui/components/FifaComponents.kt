package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TeamEntity
import com.example.model.BadgePattern
import com.example.model.MatchStage
import com.example.model.MatchWithTeams
import com.example.model.TeamCategory
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaEmeraldDark
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.FifaGoldDeep
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.StadiumBorder
import com.example.ui.theme.StadiumNavyCard
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumNavyElevated
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.StatusDraw
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurfGreen

@Composable
fun StadiumBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        StadiumNavyDeep,
                        StadiumObsidian,
                        Color(0xFF061310)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle top stadium floodlight glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FifaGold.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.05f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.05f),
                radius = size.width * 0.65f
            )
            // Subtle bottom pitch emerald aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FifaEmerald.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.92f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.92f),
                radius = size.width * 0.7f
            )
        }
        content()
    }
}

@Composable
fun SportsPanelCard(
    modifier: Modifier = Modifier,
    borderColor: Color = StadiumBorder,
    containerColor: Color = StadiumNavyCard.copy(alpha = 0.92f),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
fun TeamBadge(
    team: TeamEntity?,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    if (team == null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(StadiumNavyElevated)
                .border(1.dp, StadiumBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted
            )
        }
        return
    }

    val c1 = Color(team.primaryColorHex)
    val c2 = Color(team.secondaryColorHex)
    val c3 = Color(team.accentColorHex)
    val pattern = team.patternEnum()
    val isNation = team.categoryEnum() == TeamCategory.SELECCION

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val clipShape = Path().apply {
                if (isNation) {
                    // Rounded circular medallion flag
                    addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
                } else {
                    // Classic football club shield
                    moveTo(w * 0.10f, h * 0.08f)
                    lineTo(w * 0.90f, h * 0.08f)
                    lineTo(w * 0.90f, h * 0.56f)
                    cubicTo(
                        w * 0.90f, h * 0.78f,
                        w * 0.65f, h * 0.90f,
                        w * 0.50f, h * 0.96f
                    )
                    cubicTo(
                        w * 0.35f, h * 0.90f,
                        w * 0.10f, h * 0.78f,
                        w * 0.10f, h * 0.56f
                    )
                    close()
                }
            }

            clipPath(clipShape) {
                drawRect(color = c1)
                when (pattern) {
                    BadgePattern.HORIZONTAL_3 -> {
                        drawRect(c1, Offset(0f, 0f), Size(w, h / 3f))
                        drawRect(c2, Offset(0f, h / 3f), Size(w, h / 3f))
                        drawRect(c3, Offset(0f, 2f * h / 3f), Size(w, h / 3f + 2f))
                    }
                    BadgePattern.VERTICAL_3 -> {
                        drawRect(c1, Offset(0f, 0f), Size(w / 3f, h))
                        drawRect(c2, Offset(w / 3f, 0f), Size(w / 3f, h))
                        drawRect(c3, Offset(2f * w / 3f, 0f), Size(w / 3f + 2f, h))
                    }
                    BadgePattern.HORIZONTAL_2 -> {
                        drawRect(c1, Offset(0f, 0f), Size(w, h / 2f))
                        drawRect(c2, Offset(0f, h / 2f), Size(w, h / 2f + 2f))
                    }
                    BadgePattern.VERTICAL_2 -> {
                        drawRect(c1, Offset(0f, 0f), Size(w / 2f, h))
                        drawRect(c2, Offset(w / 2f, 0f), Size(w / 2f + 2f, h))
                    }
                    BadgePattern.CROSS -> {
                        drawRect(c1)
                        drawRect(c2, Offset(w * 0.38f, 0f), Size(w * 0.24f, h))
                        drawRect(c2, Offset(0f, h * 0.38f), Size(w, h * 0.24f))
                    }
                    BadgePattern.DIAGONAL_SASH -> {
                        drawRect(c1)
                        val sash = Path().apply {
                            moveTo(0f, h * 0.72f)
                            lineTo(w * 0.72f, 0f)
                            lineTo(w, 0f)
                            lineTo(w, h * 0.28f)
                            lineTo(w * 0.28f, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(sash, color = c2)
                    }
                    BadgePattern.HOOPS -> {
                        val stripeH = h / 5f
                        for (i in 0 until 5) {
                            drawRect(
                                color = if (i % 2 == 0) c1 else c2,
                                topLeft = Offset(0f, i * stripeH),
                                size = Size(w, stripeH + 1f)
                            )
                        }
                    }
                    BadgePattern.SHIELD_CENTER -> {
                        drawRect(c1)
                        drawCircle(
                            color = c2,
                            radius = w * 0.30f,
                            center = Offset(w * 0.5f, h * 0.48f)
                        )
                        drawCircle(
                            color = c3,
                            radius = w * 0.16f,
                            center = Offset(w * 0.5f, h * 0.48f)
                        )
                    }
                    BadgePattern.STAR_CREST -> {
                        drawRect(c1)
                        drawCircle(
                            color = c2,
                            radius = w * 0.26f,
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                        drawCircle(
                            color = c3,
                            radius = w * 0.12f,
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                    }
                }
            }

            // Metallic gold/white border
            drawPath(
                path = clipShape,
                color = if (isNation) Color.White.copy(alpha = 0.75f) else FifaGold.copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.06f)
            )
        }

        if (!isNation) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StadiumObsidian.copy(alpha = 0.78f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = team.shortCode.take(3),
                    color = FifaGoldBright,
                    fontSize = (size.value * 0.22f).coerceAtLeast(8f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TrophyShowcaseBadge(
    accentColor: Color = FifaGold,
    size: Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.25f),
                        StadiumNavyElevated
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Trofeo",
            tint = accentColor,
            modifier = Modifier.size(size * 0.56f)
        )
    }
}

@Composable
fun MatchScoreDialog(
    matchWithTeams: MatchWithTeams,
    onDismiss: () -> Unit,
    onConfirm: (
        homeGoals: Int,
        awayGoals: Int,
        penaltyWinnerId: String?,
        homePen: Int?,
        awayPen: Int?
    ) -> Unit
) {
    val match = matchWithTeams.match
    val home = matchWithTeams.homeTeam
    val away = matchWithTeams.awayTeam
    val isKnockout = match.stageEnum() == MatchStage.KNOCKOUT

    var homeGoals by remember { mutableIntStateOf(match.homeGoals ?: 0) }
    var awayGoals by remember { mutableIntStateOf(match.awayGoals ?: 0) }
    var penaltyWinnerId by remember { mutableStateOf(match.penaltyWinnerTeamId) }
    var homePen by remember { mutableIntStateOf(match.homePenalties ?: 4) }
    var awayPen by remember { mutableIntStateOf(match.awayPenalties ?: 3) }

    val isTieKnockout = isKnockout && homeGoals == awayGoals
    val canSave = home != null && away != null &&
        (!isTieKnockout || (penaltyWinnerId == home.id || penaltyWinnerId == away.id))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StadiumNavyDeep,
        titleContentColor = FifaGoldBright,
        textContentColor = TextPrimary,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = match.roundName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = FifaGold
                )
                Text(
                    text = "${matchWithTeams.competitionTitle} • ${match.scheduledDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home team column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamBadge(team = home, size = 48.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = home?.name ?: "Por definir",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        ScoreStepper(
                            score = homeGoals,
                            onDecrease = { if (homeGoals > 0) homeGoals-- },
                            onIncrease = { if (homeGoals < 25) homeGoals++ },
                            minusTag = "home_score_minus",
                            plusTag = "home_score_plus"
                        )
                    }

                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.headlineLarge,
                        color = FifaGold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    // Away team column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamBadge(team = away, size = 48.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = away?.name ?: "Por definir",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        ScoreStepper(
                            score = awayGoals,
                            onDecrease = { if (awayGoals > 0) awayGoals-- },
                            onIncrease = { if (awayGoals < 25) awayGoals++ },
                            minusTag = "away_score_minus",
                            plusTag = "away_score_plus"
                        )
                    }
                }

                if (isTieKnockout && home != null && away != null) {
                    Surface(
                        color = StadiumNavyCard,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StatusDraw.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "EMPATE EN ELIMINATORIA — TANDA DE PENALES",
                                style = MaterialTheme.typography.labelMedium,
                                color = FifaGoldBright
                            )
                            Text(
                                text = "Selecciona quién avanzó por penales:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val homeSelected = penaltyWinnerId == home.id
                                val awaySelected = penaltyWinnerId == away.id
                                OutlinedButton(
                                    onClick = {
                                        penaltyWinnerId = home.id
                                        if (homePen <= awayPen) homePen = awayPen + 1
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("penalty_win_home"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (homeSelected) FifaEmerald.copy(alpha = 0.25f) else Color.Transparent,
                                        contentColor = if (homeSelected) FifaEmerald else TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, if (homeSelected) FifaEmerald else StadiumBorder)
                                ) {
                                    Text(home.shortCode, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        penaltyWinnerId = away.id
                                        if (awayPen <= homePen) awayPen = homePen + 1
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("penalty_win_away"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (awaySelected) FifaEmerald.copy(alpha = 0.25f) else Color.Transparent,
                                        contentColor = if (awaySelected) FifaEmerald else TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, if (awaySelected) FifaEmerald else StadiumBorder)
                                ) {
                                    Text(away.shortCode, maxLines = 1)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pen. ${home.shortCode}: $homePen", style = MaterialTheme.typography.labelMedium)
                                Row {
                                    IconButton(onClick = { if (homePen > 0) homePen-- }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Remove, "Menos penal local", tint = TextSecondary)
                                    }
                                    IconButton(onClick = { if (homePen < 20) homePen++ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Add, "Más penal local", tint = FifaGold)
                                    }
                                }
                                Text("Pen. ${away.shortCode}: $awayPen", style = MaterialTheme.typography.labelMedium)
                                Row {
                                    IconButton(onClick = { if (awayPen > 0) awayPen-- }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Remove, "Menos penal visitante", tint = TextSecondary)
                                    }
                                    IconButton(onClick = { if (awayPen < 20) awayPen++ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Add, "Más penal visitante", tint = FifaGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        homeGoals,
                        awayGoals,
                        if (isTieKnockout) penaltyWinnerId else null,
                        if (isTieKnockout) homePen else null,
                        if (isTieKnockout) awayPen else null
                    )
                },
                enabled = canSave,
                modifier = Modifier.testTag("save_match_score_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FifaEmerald,
                    contentColor = StadiumObsidian
                )
            ) {
                Text("GUARDAR MARCADOR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ScoreStepper(
    score: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    minusTag: String,
    plusTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            color = StadiumNavyElevated,
            shape = CircleShape,
            border = BorderStroke(1.dp, StadiumBorder),
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onDecrease)
                .testTag(minusTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Remove, contentDescription = "Restar gol", tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = FifaGoldBright,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.Center
        )

        Surface(
            color = TurfGreen,
            shape = CircleShape,
            border = BorderStroke(1.dp, FifaEmerald),
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onIncrease)
                .testTag(plusTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Add, contentDescription = "Sumar gol", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ChampionCelebrationDialog(
    championTeam: TeamEntity,
    tournamentTitle: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophyPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StadiumNavyDeep,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Trofeo de Campeón",
                    tint = FifaGoldBright,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(pulse)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "¡CAMPEÓN OFICIAL!",
                    style = MaterialTheme.typography.displayMedium,
                    color = FifaGoldBright,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeamBadge(team = championTeam, size = 76.dp)
                Text(
                    text = championTeam.name.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Se consagra campeón de $tournamentTitle y queda inmortalizado en el Salón de Campeones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = StadiumNavyCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FifaGold.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PRESIDENTE DE LA FIFA", style = MaterialTheme.typography.labelMedium, color = FifaGold)
                        Text(
                            "DEIVER MANUEL FERNÁNDEZ MORALES",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_champion_dialog"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FifaEmerald,
                    contentColor = StadiumObsidian
                )
            ) {
                Text("CONTINUAR", fontWeight = FontWeight.Bold)
            }
        }
    )
}

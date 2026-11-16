package com.example.data

import com.example.model.MatchStage
import com.example.model.StandingRow
import com.example.model.TournamentFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

object TournamentEngine {

    data class DrawResult(
        val teamLinks: List<TournamentTeamEntity>,
        val matches: List<MatchEntity>
    )

    fun generateDraw(
        tournament: TournamentEntity,
        selectedTeams: List<TeamEntity>,
        random: Random = Random.Default
    ): DrawResult {
        val shuffled = selectedTeams.shuffled(random)
        val format = tournament.formatEnum()
        return when (format) {
            TournamentFormat.PARTIDO_UNICO,
            TournamentFormat.ELIMINATORIA -> generateKnockoutDraw(tournament, shuffled)
            TournamentFormat.GRUPOS_Y_LLAVES -> {
                if (shuffled.size >= 8 && shuffled.size % 4 == 0) {
                    generateGroupsAndKnockoutDraw(tournament, shuffled)
                } else {
                    generateKnockoutDraw(tournament, shuffled)
                }
            }
            TournamentFormat.LIGA -> generateLeagueDraw(tournament, shuffled, doubleRoundRobin = false)
            TournamentFormat.LIGA_IDA_VUELTA -> generateLeagueDraw(tournament, shuffled, doubleRoundRobin = true)
        }
    }

    private fun generateKnockoutDraw(
        tournament: TournamentEntity,
        teams: List<TeamEntity>,
        roundOffset: Int = 0
    ): DrawResult {
        val n = teams.size
        require(n >= 2) { "Se requieren al menos 2 equipos para generar llaves." }

        val links = teams.mapIndexed { idx, team ->
            TournamentTeamEntity(
                tournamentId = tournament.id,
                teamId = team.id,
                seedOrder = idx,
                groupName = null
            )
        }

        val matches = mutableListOf<MatchEntity>()
        val p = highestPowerOfTwoAtMost(n) // e.g., 16 for 24, 32 for 48, 16 for 16
        val prelimCount = n - p
        val totalMainRounds = Integer.numberOfTrailingZeros(p) // e.g., 16 -> 4 rounds

        // Pre-build main bracket rounds 1..totalMainRounds
        val mainRound1SlotTeams = arrayOfNulls<String>(p)
        if (prelimCount == 0) {
            for (i in 0 until p) {
                mainRound1SlotTeams[i] = teams[i].id
            }
        } else {
            val byeCount = p - prelimCount
            // First `byeCount` teams go straight to Round 1 slots
            for (i in 0 until byeCount) {
                mainRound1SlotTeams[i] = teams[i].id
            }
            // Remaining `prelimCount * 2` teams play Ronda Clasificatoria (roundOffset + 0)
            val prelimTeams = teams.subList(byeCount, n)
            for (k in 0 until prelimCount) {
                val targetSlot = byeCount + k
                val nextMatchIndex = targetSlot / 2
                val nextMatchIsHome = (targetSlot % 2 == 0)
                matches.add(
                    MatchEntity(
                        tournamentId = tournament.id,
                        stageType = MatchStage.KNOCKOUT.name,
                        roundNumber = roundOffset,
                        roundName = "Ronda Clasificatoria",
                        matchIndex = k,
                        groupName = null,
                        homeTeamId = prelimTeams[k * 2].id,
                        awayTeamId = prelimTeams[k * 2 + 1].id,
                        scheduledDate = addDays(tournament.startDate, 0),
                        nextMatchRound = roundOffset + 1,
                        nextMatchIndex = nextMatchIndex,
                        nextMatchIsHome = nextMatchIsHome
                    )
                )
            }
        }

        val dayShiftStart = if (prelimCount > 0) 3 else 0
        for (r in 1..totalMainRounds) {
            val matchCountInRound = p shr r
            val roundName = knockoutRoundName(matchCountInRound)
            val roundNum = roundOffset + r
            val dateForRound = addDays(tournament.startDate, dayShiftStart + (r - 1) * 4)

            for (m in 0 until matchCountInRound) {
                val homeId = if (r == 1) mainRound1SlotTeams[m * 2] else null
                val awayId = if (r == 1) mainRound1SlotTeams[m * 2 + 1] else null
                val hasNext = r < totalMainRounds
                matches.add(
                    MatchEntity(
                        tournamentId = tournament.id,
                        stageType = MatchStage.KNOCKOUT.name,
                        roundNumber = roundNum,
                        roundName = roundName,
                        matchIndex = m,
                        groupName = null,
                        homeTeamId = homeId,
                        awayTeamId = awayId,
                        scheduledDate = dateForRound,
                        nextMatchRound = if (hasNext) roundNum + 1 else null,
                        nextMatchIndex = if (hasNext) m / 2 else null,
                        nextMatchIsHome = if (hasNext) (m % 2 == 0) else null
                    )
                )
            }
        }

        return DrawResult(teamLinks = links, matches = matches)
    }

    private fun generateGroupsAndKnockoutDraw(
        tournament: TournamentEntity,
        teams: List<TeamEntity>
    ): DrawResult {
        val groupCount = teams.size / 4
        val groupNames = (0 until groupCount).map { ('A' + it).toString() }
        val links = mutableListOf<TournamentTeamEntity>()
        val matches = mutableListOf<MatchEntity>()

        teams.forEachIndexed { idx, team ->
            val groupName = groupNames[idx / 4]
            links.add(
                TournamentTeamEntity(
                    tournamentId = tournament.id,
                    teamId = team.id,
                    seedOrder = idx,
                    groupName = groupName
                )
            )
        }

        // Create 3 matchdays per 4-team group
        val teamsByGroup = links.groupBy { it.groupName!! }
        groupNames.forEach { gName ->
            val gTeams = teamsByGroup[gName].orEmpty().map { it.teamId }
            if (gTeams.size == 4) {
                val pairingsByMatchday = listOf(
                    listOf(0 to 1, 2 to 3),
                    listOf(0 to 2, 1 to 3),
                    listOf(0 to 3, 1 to 2)
                )
                pairingsByMatchday.forEachIndexed { dayIdx, pairs ->
                    val jornada = dayIdx + 1
                    val date = addDays(tournament.startDate, dayIdx * 3)
                    pairs.forEachIndexed { pIdx, (hIdx, aIdx) ->
                        matches.add(
                            MatchEntity(
                                tournamentId = tournament.id,
                                stageType = MatchStage.GROUP.name,
                                roundNumber = jornada,
                                roundName = "Grupo $gName - Jornada $jornada",
                                matchIndex = (groupNames.indexOf(gName) * 2) + pIdx,
                                groupName = gName,
                                homeTeamId = gTeams[hIdx],
                                awayTeamId = gTeams[aIdx],
                                scheduledDate = date
                            )
                        )
                    }
                }
            }
        }

        // Determine knockout bracket size after groups:
        // 8 teams (2 groups) -> 4 qualifiers
        // 12 teams (3 groups) -> 8 qualifiers
        // 16 teams (4 groups) -> 8 qualifiers
        // 24 teams (6 groups) -> 16 qualifiers
        // 32 teams (8 groups) -> 16 qualifiers
        // 48 teams (12 groups) -> 32 qualifiers
        val knockoutSize = when (teams.size) {
            8 -> 4
            12, 16 -> 8
            24, 32 -> 16
            48 -> 32
            else -> highestPowerOfTwoAtMost(teams.size / 2).coerceAtLeast(2)
        }

        val totalKoRounds = Integer.numberOfTrailingZeros(knockoutSize)
        val koRoundOffset = 100
        for (r in 1..totalKoRounds) {
            val matchCountInRound = knockoutSize shr r
            val roundName = knockoutRoundName(matchCountInRound)
            val roundNum = koRoundOffset + r
            val date = addDays(tournament.startDate, 10 + (r - 1) * 4)
            for (m in 0 until matchCountInRound) {
                val hasNext = r < totalKoRounds
                matches.add(
                    MatchEntity(
                        tournamentId = tournament.id,
                        stageType = MatchStage.KNOCKOUT.name,
                        roundNumber = roundNum,
                        roundName = roundName,
                        matchIndex = m,
                        groupName = null,
                        homeTeamId = null,
                        awayTeamId = null,
                        scheduledDate = date,
                        nextMatchRound = if (hasNext) roundNum + 1 else null,
                        nextMatchIndex = if (hasNext) m / 2 else null,
                        nextMatchIsHome = if (hasNext) (m % 2 == 0) else null
                    )
                )
            }
        }

        return DrawResult(teamLinks = links, matches = matches)
    }

    private fun generateLeagueDraw(
        tournament: TournamentEntity,
        teams: List<TeamEntity>,
        doubleRoundRobin: Boolean
    ): DrawResult {
        val n = teams.size
        require(n >= 2) { "Se requieren al menos 2 equipos para generar una liga." }

        val links = teams.mapIndexed { idx, team ->
            TournamentTeamEntity(
                tournamentId = tournament.id,
                teamId = team.id,
                seedOrder = idx,
                groupName = null
            )
        }

        val paddedIds: MutableList<String?> = teams.map { it.id }.toMutableList()
        if (paddedIds.size % 2 != 0) {
            paddedIds.add(null) // Bye slot
        }
        val m = paddedIds.size
        val roundsPerLeg = m - 1
        val matchesPerRound = m / 2
        val matches = mutableListOf<MatchEntity>()

        val legs = if (doubleRoundRobin) 2 else 1
        for (leg in 0 until legs) {
            val rotation = paddedIds.toMutableList()
            for (round in 1..roundsPerLeg) {
                val globalRound = leg * roundsPerLeg + round
                val date = addDays(tournament.startDate, (globalRound - 1) * 4)
                var matchIdx = 0
                for (i in 0 until matchesPerRound) {
                    val t1 = rotation[i]
                    val t2 = rotation[m - 1 - i]
                    if (t1 != null && t2 != null) {
                        val swap = (round % 2 == 0 && i == 0) || (leg == 1)
                        val home = if (swap) t2 else t1
                        val away = if (swap) t1 else t2
                        matches.add(
                            MatchEntity(
                                tournamentId = tournament.id,
                                stageType = MatchStage.LEAGUE.name,
                                roundNumber = globalRound,
                                roundName = "Jornada $globalRound",
                                matchIndex = matchIdx++,
                                groupName = null,
                                homeTeamId = home,
                                awayTeamId = away,
                                scheduledDate = date
                            )
                        )
                    }
                }
                // Rotate all except index 0
                val last = rotation.removeAt(m - 1)
                rotation.add(1, last)
            }
        }

        return DrawResult(teamLinks = links, matches = matches)
    }

    fun computeStandings(
        teams: List<TeamEntity>,
        matches: List<MatchEntity>
    ): List<StandingRow> {
        val statsMap = teams.associate { team ->
            team.id to MutableStanding(team)
        }.toMutableMap()

        matches.filter { it.isPlayed && it.homeTeamId != null && it.awayTeamId != null }
            .forEach { match ->
                val hg = match.homeGoals ?: return@forEach
                val ag = match.awayGoals ?: return@forEach
                val home = statsMap[match.homeTeamId]
                val away = statsMap[match.awayTeamId]
                if (home != null && away != null) {
                    home.played++
                    away.played++
                    home.goalsFor += hg
                    home.goalsAgainst += ag
                    away.goalsFor += ag
                    away.goalsAgainst += hg
                    when {
                        hg > ag -> {
                            home.wins++
                            home.points += 3
                            away.losses++
                        }
                        ag > hg -> {
                            away.wins++
                            away.points += 3
                            home.losses++
                        }
                        else -> {
                            home.draws++
                            away.draws++
                            home.points += 1
                            away.points += 1
                        }
                    }
                }
            }

        return statsMap.values
            .sortedWith(
                compareByDescending<MutableStanding> { it.points }
                    .thenByDescending { it.goalsFor - it.goalsAgainst }
                    .thenByDescending { it.goalsFor }
                    .thenBy { it.team.name }
            )
            .mapIndexed { idx, s ->
                StandingRow(
                    position = idx + 1,
                    team = s.team,
                    played = s.played,
                    wins = s.wins,
                    draws = s.draws,
                    losses = s.losses,
                    goalsFor = s.goalsFor,
                    goalsAgainst = s.goalsAgainst,
                    points = s.points
                )
            }
    }

    fun selectGroupQualifiersSeeded(
        groupStandings: Map<String, List<StandingRow>>,
        knockoutSize: Int
    ): List<Pair<TeamEntity, TeamEntity>> {
        val sortedGroups = groupStandings.keys.sorted()
        val firsts = sortedGroups.mapNotNull { g -> groupStandings[g]?.getOrNull(0) }
        val seconds = sortedGroups.mapNotNull { g -> groupStandings[g]?.getOrNull(1) }
        val thirds = sortedGroups.mapNotNull { g -> groupStandings[g]?.getOrNull(2) }
            .sortedWith(
                compareByDescending<StandingRow> { it.points }
                    .thenByDescending { it.goalDifference }
                    .thenByDescending { it.goalsFor }
            )

        val directCount = firsts.size + seconds.size
        val extraNeeded = (knockoutSize - directCount).coerceAtLeast(0)
        val qualifiedPool = (firsts + seconds + thirds.take(extraNeeded)).take(knockoutSize)

        // Cross-seed top half vs reversed bottom half to keep 1st vs 2nd/3rd balance
        val half = qualifiedPool.size / 2
        val topSeeds = qualifiedPool.take(half).map { it.team }
        val bottomSeeds = qualifiedPool.drop(half).reversed().map { it.team }
        return topSeeds.zip(bottomSeeds)
    }

    fun knockoutRoundName(matchCountInRound: Int): String = when (matchCountInRound) {
        1 -> "Final"
        2 -> "Semifinales"
        4 -> "Cuartos de Final"
        8 -> "Octavos de Final"
        16 -> "Dieciseisavos de Final"
        else -> "Ronda de ${matchCountInRound * 2}"
    }

    private fun highestPowerOfTwoAtMost(n: Int): Int {
        var p = 1
        while (p * 2 <= n) {
            p *= 2
        }
        return p
    }

    fun addDays(baseDateStr: String, daysToAdd: Int): String {
        val formats = listOf("yyyy-MM-dd", "dd/MM/yyyy")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(baseDateStr)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                }
            } catch (_: Exception) {
                // try next
            }
        }
        return baseDateStr
    }

    fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private class MutableStanding(val team: TeamEntity) {
        var played = 0
        var wins = 0
        var draws = 0
        var losses = 0
        var goalsFor = 0
        var goalsAgainst = 0
        var points = 0
    }
}

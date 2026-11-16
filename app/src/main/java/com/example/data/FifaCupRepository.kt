package com.example.data

import com.example.model.ChampionWithDetails
import com.example.model.CompetitionStats
import com.example.model.CompetitionType
import com.example.model.GlobalStats
import com.example.model.MatchStage
import com.example.model.MatchWithTeams
import com.example.model.StandingRow
import com.example.model.TeamCategory
import com.example.model.TeamPerformanceStats
import com.example.model.TournamentDetailBundle
import com.example.model.TournamentFormat
import com.example.model.TournamentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FifaCupRepository(
    private val dao: FifaCupDao
) {
    val allTeamsFlow: Flow<List<TeamEntity>> = dao.observeAllTeams()
    val allTournamentsFlow: Flow<List<TournamentEntity>> = dao.observeAllTournaments()
    val allMatchesFlow: Flow<List<MatchEntity>> = dao.observeAllMatches()
    val allChampionsFlow: Flow<List<ChampionEntity>> = dao.observeAllChampions()

    suspend fun ensureTeamsSeeded() {
        if (dao.countTeams() == 0) {
            dao.insertTeams(TeamSeedData.allTeams)
        }
    }

    fun observeTournamentsForCompetition(competitionType: CompetitionType): Flow<List<TournamentEntity>> {
        return dao.observeTournamentsByCompetition(competitionType.id)
    }

    fun observeAllMatchesWithTeams(): Flow<List<MatchWithTeams>> {
        return combine(
            allTeamsFlow,
            allTournamentsFlow,
            allMatchesFlow
        ) { teams, tournaments, matches ->
            val teamMap = teams.associateBy { it.id }
            val tourMap = tournaments.associateBy { it.id }
            matches.map { match ->
                val tour = tourMap[match.tournamentId]
                val comp = tour?.let { CompetitionType.fromId(it.competitionType) }
                MatchWithTeams(
                    match = match,
                    homeTeam = match.homeTeamId?.let { teamMap[it] },
                    awayTeam = match.awayTeamId?.let { teamMap[it] },
                    winnerTeam = match.winnerTeamId?.let { teamMap[it] },
                    competitionTitle = comp?.title ?: "FIFA CUP",
                    tournamentName = tour?.name ?: "Torneo"
                )
            }
        }
    }

    fun observeAllChampionsWithDetails(): Flow<List<ChampionWithDetails>> {
        return combine(allChampionsFlow, allTeamsFlow) { champions, teams ->
            val teamMap = teams.associateBy { it.id }
            champions.map { champ ->
                ChampionWithDetails(
                    champion = champ,
                    team = teamMap[champ.teamId],
                    runnerUp = champ.runnerUpTeamId?.let { teamMap[it] }
                )
            }
        }
    }

    fun observeGlobalStats(): Flow<GlobalStats> {
        return combine(
            allTournamentsFlow,
            allMatchesFlow,
            allChampionsFlow
        ) { tournaments, matches, champions ->
            val playedMatches = matches.filter { it.isPlayed }
            val pendingMatches = matches.count { !it.isPlayed }
            var totalGoals = 0
            var draws = 0
            var winsResolved = 0
            playedMatches.forEach { m ->
                val hg = m.homeGoals ?: 0
                val ag = m.awayGoals ?: 0
                totalGoals += hg + ag
                if (hg == ag) {
                    draws++
                } else {
                    winsResolved++
                }
            }
            GlobalStats(
                tournamentsCreated = tournaments.size,
                tournamentsFinished = tournaments.count { it.status == TournamentStatus.FINALIZADO.name },
                matchesPlayed = playedMatches.size,
                matchesPending = pendingMatches,
                totalWinsResolved = winsResolved,
                totalDraws = draws,
                totalGoals = totalGoals,
                championsCount = champions.size
            )
        }
    }

    fun observeTeamPerformanceLeaderboard(): Flow<List<TeamPerformanceStats>> {
        return combine(
            allTeamsFlow,
            allMatchesFlow,
            allChampionsFlow
        ) { teams, matches, champions ->
            val titleCounts = champions.groupingBy { it.teamId }.eachCount()
            val playedMap = mutableMapOf<String, Int>()
            val winsMap = mutableMapOf<String, Int>()
            val drawsMap = mutableMapOf<String, Int>()
            val lossesMap = mutableMapOf<String, Int>()
            val gfMap = mutableMapOf<String, Int>()
            val gaMap = mutableMapOf<String, Int>()

            matches.filter { it.isPlayed && it.homeTeamId != null && it.awayTeamId != null }
                .forEach { m ->
                    val h = m.homeTeamId!!
                    val a = m.awayTeamId!!
                    val hg = m.homeGoals ?: 0
                    val ag = m.awayGoals ?: 0

                    playedMap[h] = (playedMap[h] ?: 0) + 1
                    playedMap[a] = (playedMap[a] ?: 0) + 1
                    gfMap[h] = (gfMap[h] ?: 0) + hg
                    gaMap[h] = (gaMap[h] ?: 0) + ag
                    gfMap[a] = (gfMap[a] ?: 0) + ag
                    gaMap[a] = (gaMap[a] ?: 0) + hg

                    when {
                        hg > ag -> {
                            winsMap[h] = (winsMap[h] ?: 0) + 1
                            lossesMap[a] = (lossesMap[a] ?: 0) + 1
                        }
                        ag > hg -> {
                            winsMap[a] = (winsMap[a] ?: 0) + 1
                            lossesMap[h] = (lossesMap[h] ?: 0) + 1
                        }
                        else -> {
                            drawsMap[h] = (drawsMap[h] ?: 0) + 1
                            drawsMap[a] = (drawsMap[a] ?: 0) + 1
                        }
                    }
                }

            teams.map { team ->
                TeamPerformanceStats(
                    team = team,
                    played = playedMap[team.id] ?: 0,
                    wins = winsMap[team.id] ?: 0,
                    draws = drawsMap[team.id] ?: 0,
                    losses = lossesMap[team.id] ?: 0,
                    goalsFor = gfMap[team.id] ?: 0,
                    goalsAgainst = gaMap[team.id] ?: 0,
                    titles = titleCounts[team.id] ?: 0
                )
            }.sortedWith(
                compareByDescending<TeamPerformanceStats> { it.titles }
                    .thenByDescending { it.wins }
                    .thenByDescending { it.played }
                    .thenByDescending { it.goalDifference }
                    .thenByDescending { it.team.rating }
            )
        }
    }

    fun observeCompetitionStats(competitionType: CompetitionType): Flow<CompetitionStats> {
        return combine(
            dao.observeTournamentsByCompetition(competitionType.id),
            allMatchesFlow,
            dao.observeChampionsByCompetition(competitionType.id),
            allTeamsFlow
        ) { compTournaments, allMatches, compChampions, allTeams ->
            val tourIds = compTournaments.map { it.id }.toSet()
            val compMatches = allMatches.filter { it.tournamentId in tourIds }
            val played = compMatches.filter { it.isPlayed }
            val totalGoals = played.sumOf { (it.homeGoals ?: 0) + (it.awayGoals ?: 0) }
            val teamMap = allTeams.associateBy { it.id }

            val titleCounts = compChampions.groupingBy { it.teamId }.eachCount()
            val winCounts = mutableMapOf<String, Int>()
            val playCounts = mutableMapOf<String, Int>()
            val drawCounts = mutableMapOf<String, Int>()
            val lossCounts = mutableMapOf<String, Int>()
            val gfCounts = mutableMapOf<String, Int>()
            val gaCounts = mutableMapOf<String, Int>()

            played.forEach { m ->
                val h = m.homeTeamId ?: return@forEach
                val a = m.awayTeamId ?: return@forEach
                val hg = m.homeGoals ?: 0
                val ag = m.awayGoals ?: 0
                playCounts[h] = (playCounts[h] ?: 0) + 1
                playCounts[a] = (playCounts[a] ?: 0) + 1
                gfCounts[h] = (gfCounts[h] ?: 0) + hg
                gaCounts[h] = (gaCounts[h] ?: 0) + ag
                gfCounts[a] = (gfCounts[a] ?: 0) + ag
                gaCounts[a] = (gaCounts[a] ?: 0) + hg
                when {
                    hg > ag -> {
                        winCounts[h] = (winCounts[h] ?: 0) + 1
                        lossCounts[a] = (lossCounts[a] ?: 0) + 1
                    }
                    ag > hg -> {
                        winCounts[a] = (winCounts[a] ?: 0) + 1
                        lossCounts[h] = (lossCounts[h] ?: 0) + 1
                    }
                    else -> {
                        drawCounts[h] = (drawCounts[h] ?: 0) + 1
                        drawCounts[a] = (drawCounts[a] ?: 0) + 1
                    }
                }
            }

            val topTeams = playCounts.keys.mapNotNull { tid ->
                teamMap[tid]?.let { team ->
                    TeamPerformanceStats(
                        team = team,
                        played = playCounts[tid] ?: 0,
                        wins = winCounts[tid] ?: 0,
                        draws = drawCounts[tid] ?: 0,
                        losses = lossCounts[tid] ?: 0,
                        goalsFor = gfCounts[tid] ?: 0,
                        goalsAgainst = gaCounts[tid] ?: 0,
                        titles = titleCounts[tid] ?: 0
                    )
                }
            }.sortedWith(
                compareByDescending<TeamPerformanceStats> { it.titles }
                    .thenByDescending { it.wins }
                    .thenByDescending { it.goalDifference }
            )

            CompetitionStats(
                tournamentsCount = compTournaments.size,
                finishedCount = compTournaments.count { it.status == TournamentStatus.FINALIZADO.name },
                matchesPlayed = played.size,
                matchesPending = compMatches.count { !it.isPlayed },
                totalGoals = totalGoals,
                champions = compChampions.map { c ->
                    ChampionWithDetails(
                        champion = c,
                        team = teamMap[c.teamId],
                        runnerUp = c.runnerUpTeamId?.let { teamMap[it] }
                    )
                },
                topTeams = topTeams
            )
        }
    }

    fun observeTournamentDetail(tournamentId: Long): Flow<TournamentDetailBundle?> {
        return combine(
            dao.observeTournamentById(tournamentId),
            dao.observeTournamentTeams(tournamentId),
            dao.observeTournamentTeamLinks(tournamentId),
            dao.observeMatchesForTournament(tournamentId),
            allTeamsFlow
        ) { tournament, selectedTeams, links, matches, allTeams ->
            if (tournament == null) return@combine null
            val teamMap = allTeams.associateBy { it.id }
            val compTitle = CompetitionType.fromId(tournament.competitionType).title

            val matchesWithTeams = matches.map { m ->
                MatchWithTeams(
                    match = m,
                    homeTeam = m.homeTeamId?.let { teamMap[it] },
                    awayTeam = m.awayTeamId?.let { teamMap[it] },
                    winnerTeam = m.winnerTeamId?.let { teamMap[it] },
                    competitionTitle = compTitle,
                    tournamentName = tournament.name
                )
            }

            val groupStandings = mutableMapOf<String, List<StandingRow>>()
            val groups = links.mapNotNull { it.groupName }.distinct().sorted()
            if (groups.isNotEmpty()) {
                groups.forEach { g ->
                    val gTeams = links.filter { it.groupName == g }.mapNotNull { teamMap[it.teamId] }
                    val gMatches = matches.filter { it.stageType == MatchStage.GROUP.name && it.groupName == g }
                    groupStandings[g] = TournamentEngine.computeStandings(gTeams, gMatches)
                }
            }

            val leagueMatches = matches.filter { it.stageType == MatchStage.LEAGUE.name }
            val leagueStandings = if (leagueMatches.isNotEmpty() ||
                tournament.formatEnum() == TournamentFormat.LIGA ||
                tournament.formatEnum() == TournamentFormat.LIGA_IDA_VUELTA
            ) {
                TournamentEngine.computeStandings(selectedTeams, leagueMatches)
            } else {
                emptyList()
            }

            TournamentDetailBundle(
                tournament = tournament,
                selectedTeams = selectedTeams,
                matches = matchesWithTeams,
                groupStandings = groupStandings,
                leagueStandings = leagueStandings,
                championTeam = tournament.championTeamId?.let { teamMap[it] }
            )
        }
    }

    suspend fun ensureOrCreateTournamentForCompetition(competitionType: CompetitionType): Long {
        ensureTeamsSeeded()
        val existing = dao.getLatestTournamentForCompetition(competitionType.id)
        if (existing != null) return existing.id
        return createTournament(
            competitionType = competitionType,
            name = "${competitionType.title} 2026",
            startDate = TournamentEngine.todayString(),
            prize = competitionType.defaultPrize,
            participantCount = competitionType.defaultParticipantCount,
            format = competitionType.defaultFormat
        )
    }

    suspend fun createTournament(
        competitionType: CompetitionType,
        name: String,
        startDate: String,
        prize: String,
        participantCount: Int,
        format: TournamentFormat
    ): Long {
        ensureTeamsSeeded()
        val entity = TournamentEntity(
            competitionType = competitionType.id,
            name = name.ifBlank { "${competitionType.title} 2026" },
            startDate = startDate.ifBlank { TournamentEngine.todayString() },
            prize = prize.ifBlank { competitionType.defaultPrize },
            participantCount = participantCount,
            format = format.name,
            status = TournamentStatus.CONFIGURANDO.name
        )
        return dao.insertTournament(entity)
    }

    suspend fun updateTournamentConfig(
        tournamentId: Long,
        name: String,
        startDate: String,
        prize: String,
        participantCount: Int,
        format: TournamentFormat
    ) {
        val current = dao.getTournamentById(tournamentId) ?: return
        val existingLinks = dao.getTournamentTeamLinksOnce(tournamentId)
        val trimmedLinks = existingLinks.take(participantCount)
        if (trimmedLinks.size != existingLinks.size) {
            dao.replaceTournamentTeams(tournamentId, trimmedLinks.map { it.teamId })
        }
        dao.updateTournament(
            current.copy(
                name = name.trim().ifBlank { current.name },
                startDate = startDate.trim().ifBlank { current.startDate },
                prize = prize.trim().ifBlank { current.prize },
                participantCount = participantCount,
                format = format.name
            )
        )
    }

    suspend fun toggleTeamSelection(tournamentId: Long, teamId: String): Result<Unit> {
        val tournament = dao.getTournamentById(tournamentId)
            ?: return Result.failure(IllegalStateException("Torneo no encontrado"))
        if (tournament.statusEnum() != TournamentStatus.CONFIGURANDO) {
            return Result.failure(IllegalStateException("El torneo ya inició; reinicia el sorteo para cambiar equipos."))
        }
        val currentIds = dao.getTournamentTeamLinksOnce(tournamentId).map { it.teamId }.toMutableList()
        if (currentIds.contains(teamId)) {
            currentIds.remove(teamId)
            dao.replaceTournamentTeams(tournamentId, currentIds)
            return Result.success(Unit)
        }
        if (currentIds.size >= tournament.participantCount) {
            return Result.failure(
                IllegalStateException("Ya alcanzaste el límite de ${tournament.participantCount} equipos.")
            )
        }
        currentIds.add(teamId)
        dao.replaceTournamentTeams(tournamentId, currentIds)
        return Result.success(Unit)
    }

    suspend fun autoFillRemainingTeams(tournamentId: Long): Result<Int> {
        val tournament = dao.getTournamentById(tournamentId)
            ?: return Result.failure(IllegalStateException("Torneo no encontrado"))
        if (tournament.statusEnum() != TournamentStatus.CONFIGURANDO) {
            return Result.failure(IllegalStateException("No se pueden modificar equipos con el torneo en curso."))
        }
        val comp = CompetitionType.fromId(tournament.competitionType)
        val allTeams = dao.getAllTeamsOnce()
        val currentIds = dao.getTournamentTeamLinksOnce(tournamentId).map { it.teamId }.toMutableSet()
        val needed = (tournament.participantCount - currentIds.size).coerceAtLeast(0)
        if (needed == 0) return Result.success(0)

        val preferredPool = when (comp.teamPoolRule) {
            TeamCategory.SELECCION -> allTeams.filter { it.category == TeamCategory.SELECCION.name }
            TeamCategory.CLUB -> allTeams.filter { it.category == TeamCategory.CLUB.name }
            TeamCategory.MIXTO -> allTeams
        }
        val fallbackPool = allTeams
        val candidates = (preferredPool.filter { it.id !in currentIds }.shuffled() +
            fallbackPool.filter { it.id !in currentIds && it !in preferredPool }.shuffled())
            .distinctBy { it.id }
            .take(needed)

        val updatedList = currentIds.toList() + candidates.map { it.id }
        dao.replaceTournamentTeams(tournamentId, updatedList)
        return Result.success(candidates.size)
    }

    suspend fun clearSelectedTeams(tournamentId: Long) {
        val tournament = dao.getTournamentById(tournamentId) ?: return
        if (tournament.statusEnum() == TournamentStatus.CONFIGURANDO) {
            dao.clearTournamentTeams(tournamentId)
        }
    }

    suspend fun executeDrawAndStartTournament(tournamentId: Long): Result<Unit> {
        val tournament = dao.getTournamentById(tournamentId)
            ?: return Result.failure(IllegalStateException("Torneo no encontrado"))
        val links = dao.getTournamentTeamLinksOnce(tournamentId)
        val teamMap = dao.getAllTeamsOnce().associateBy { it.id }
        val selectedTeams = links.mapNotNull { teamMap[it.teamId] }

        if (selectedTeams.size != tournament.participantCount) {
            return Result.failure(
                IllegalStateException(
                    "Selecciona ${tournament.participantCount} equipos (${selectedTeams.size}/${tournament.participantCount} seleccionados)."
                )
            )
        }

        val drawResult = TournamentEngine.generateDraw(tournament, selectedTeams)
        val startedTournament = tournament.copy(
            status = TournamentStatus.EN_CURSO.name,
            championTeamId = null
        )
        dao.replaceDrawMatchesAndTeams(
            tournament = startedTournament,
            links = drawResult.teamLinks,
            matches = drawResult.matches
        )
        return Result.success(Unit)
    }

    suspend fun resetTournamentDraw(tournamentId: Long) {
        val tournament = dao.getTournamentById(tournamentId) ?: return
        dao.deleteChampionByTournament(tournamentId)
        dao.deleteMatchesForTournament(tournamentId)
        val existingLinks = dao.getTournamentTeamLinksOnce(tournamentId).map {
            it.copy(groupName = null)
        }
        dao.clearTournamentTeams(tournamentId)
        if (existingLinks.isNotEmpty()) {
            dao.insertTournamentTeams(existingLinks)
        }
        dao.updateTournament(
            tournament.copy(
                status = TournamentStatus.CONFIGURANDO.name,
                championTeamId = null
            )
        )
    }

    suspend fun deleteTournament(tournamentId: Long) {
        dao.deleteTournament(tournamentId)
    }

    suspend fun recordMatchResult(
        matchId: Long,
        homeGoals: Int,
        awayGoals: Int,
        penaltyWinnerTeamId: String? = null,
        homePenalties: Int? = null,
        awayPenalties: Int? = null
    ): Result<String?> {
        val match = dao.getMatchById(matchId)
            ?: return Result.failure(IllegalStateException("Partido no encontrado."))
        val homeId = match.homeTeamId
        val awayId = match.awayTeamId
        if (homeId == null || awayId == null) {
            return Result.failure(IllegalStateException("Este partido aún no tiene ambos equipos definidos."))
        }
        if (homeGoals < 0 || awayGoals < 0) {
            return Result.failure(IllegalStateException("Los goles no pueden ser negativos."))
        }

        val stage = match.stageEnum()
        if (stage == MatchStage.KNOCKOUT) {
            val nextRound = match.nextMatchRound
            val nextIndex = match.nextMatchIndex
            if (match.isPlayed && nextRound != null && nextIndex != null) {
                val nextMatch = dao.findBracketMatch(
                    tournamentId = match.tournamentId,
                    stageType = MatchStage.KNOCKOUT.name,
                    roundNumber = nextRound,
                    matchIndex = nextIndex
                )
                if (nextMatch != null && nextMatch.isPlayed) {
                    return Result.failure(
                        IllegalStateException("La siguiente ronda ya se jugó; no se puede cambiar este marcador.")
                    )
                }
            }
            if (homeGoals == awayGoals && penaltyWinnerTeamId != homeId && penaltyWinnerTeamId != awayId) {
                return Result.failure(
                    IllegalStateException("Hay empate en eliminatoria. Selecciona al ganador en penales.")
                )
            }
        }

        val winnerId: String? = when {
            homeGoals > awayGoals -> homeId
            awayGoals > homeGoals -> awayId
            stage == MatchStage.KNOCKOUT -> penaltyWinnerTeamId
            else -> null
        }

        val updatedMatch = match.copy(
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            penaltyWinnerTeamId = if (homeGoals == awayGoals && stage == MatchStage.KNOCKOUT) penaltyWinnerTeamId else null,
            homePenalties = if (homeGoals == awayGoals && stage == MatchStage.KNOCKOUT) homePenalties else null,
            awayPenalties = if (homeGoals == awayGoals && stage == MatchStage.KNOCKOUT) awayPenalties else null,
            winnerTeamId = winnerId,
            isPlayed = true
        )
        dao.updateMatch(updatedMatch)

        val tournament = dao.getTournamentById(match.tournamentId)
            ?: return Result.success(null)

        var newlyCrownedChampionId: String? = null

        when (stage) {
            MatchStage.KNOCKOUT -> {
                val nextRound = match.nextMatchRound
                val nextIndex = match.nextMatchIndex
                val nextIsHome = match.nextMatchIsHome
                if (nextRound != null && nextIndex != null && nextIsHome != null && winnerId != null) {
                    val nextMatch = dao.findBracketMatch(
                        tournamentId = match.tournamentId,
                        stageType = MatchStage.KNOCKOUT.name,
                        roundNumber = nextRound,
                        matchIndex = nextIndex
                    )
                    if (nextMatch != null) {
                        val patchedNext = if (nextIsHome) {
                            nextMatch.copy(homeTeamId = winnerId)
                        } else {
                            nextMatch.copy(awayTeamId = winnerId)
                        }
                        dao.updateMatch(patchedNext)
                    }
                } else if (nextRound == null && winnerId != null) {
                    // This was the Grand Final!
                    val runnerUpId = if (winnerId == homeId) awayId else homeId
                    val penSuffix = if (homeGoals == awayGoals && homePenalties != null && awayPenalties != null) {
                        " (Pen. $homePenalties-$awayPenalties)"
                    } else if (homeGoals == awayGoals) {
                        " (Penales)"
                    } else ""
                    crownChampion(
                        tournament = tournament,
                        championTeamId = winnerId,
                        runnerUpTeamId = runnerUpId,
                        finalScoreSummary = "$homeGoals - $awayGoals$penSuffix"
                    )
                    newlyCrownedChampionId = winnerId
                }
            }

            MatchStage.GROUP -> {
                val allMatches = dao.getMatchesForTournamentOnce(match.tournamentId)
                val groupMatches = allMatches.filter { it.stageType == MatchStage.GROUP.name }
                val koMatches = allMatches.filter { it.stageType == MatchStage.KNOCKOUT.name }
                val allGroupsDone = groupMatches.isNotEmpty() && groupMatches.all { it.isPlayed }
                if (allGroupsDone && koMatches.isNotEmpty()) {
                    val firstKoRound = koMatches.minOf { it.roundNumber }
                    val round1Ko = koMatches.filter { it.roundNumber == firstKoRound }.sortedBy { it.matchIndex }
                    val needsPopulation = round1Ko.any { it.homeTeamId == null && it.awayTeamId == null }
                    if (needsPopulation) {
                        val links = dao.getTournamentTeamLinksOnce(match.tournamentId)
                        val teamMap = dao.getAllTeamsOnce().associateBy { it.id }
                        val groupStandings = links.mapNotNull { it.groupName }.distinct().sorted().associateWith { g ->
                            val gTeams = links.filter { it.groupName == g }.mapNotNull { teamMap[it.teamId] }
                            val gMatches = groupMatches.filter { it.groupName == g }
                            TournamentEngine.computeStandings(gTeams, gMatches)
                        }
                        val pairs = TournamentEngine.selectGroupQualifiersSeeded(
                            groupStandings = groupStandings,
                            knockoutSize = round1Ko.size * 2
                        )
                        round1Ko.forEachIndexed { idx, koMatch ->
                            val pair = pairs.getOrNull(idx)
                            if (pair != null) {
                                dao.updateMatch(
                                    koMatch.copy(
                                        homeTeamId = pair.first.id,
                                        awayTeamId = pair.second.id
                                    )
                                )
                            }
                        }
                    }
                }
            }

            MatchStage.LEAGUE -> {
                val allMatches = dao.getMatchesForTournamentOnce(match.tournamentId)
                val leagueMatches = allMatches.filter { it.stageType == MatchStage.LEAGUE.name }
                if (leagueMatches.isNotEmpty() && leagueMatches.all { it.isPlayed }) {
                    val links = dao.getTournamentTeamLinksOnce(match.tournamentId)
                    val teamMap = dao.getAllTeamsOnce().associateBy { it.id }
                    val teams = links.mapNotNull { teamMap[it.teamId] }
                    val standings = TournamentEngine.computeStandings(teams, leagueMatches)
                    val leader = standings.firstOrNull()
                    val runnerUp = standings.getOrNull(1)
                    if (leader != null) {
                        crownChampion(
                            tournament = tournament,
                            championTeamId = leader.team.id,
                            runnerUpTeamId = runnerUp?.team?.id,
                            finalScoreSummary = "${leader.points} PTS (DG ${if (leader.goalDifference >= 0) "+" else ""}${leader.goalDifference})"
                        )
                        newlyCrownedChampionId = leader.team.id
                    }
                }
            }
        }

        return Result.success(newlyCrownedChampionId)
    }

    private suspend fun crownChampion(
        tournament: TournamentEntity,
        championTeamId: String,
        runnerUpTeamId: String?,
        finalScoreSummary: String
    ) {
        dao.updateTournament(
            tournament.copy(
                status = TournamentStatus.FINALIZADO.name,
                championTeamId = championTeamId
            )
        )
        dao.insertChampion(
            ChampionEntity(
                tournamentId = tournament.id,
                competitionType = tournament.competitionType,
                tournamentName = tournament.name,
                teamId = championTeamId,
                runnerUpTeamId = runnerUpTeamId,
                finalScoreSummary = finalScoreSummary,
                prize = tournament.prize,
                crownedDate = TournamentEngine.todayString()
            )
        )
    }
}

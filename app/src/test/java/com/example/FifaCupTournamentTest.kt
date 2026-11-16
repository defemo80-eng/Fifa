package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FifaCupDatabase
import com.example.data.FifaCupRepository
import com.example.data.TeamSeedData
import com.example.model.CompetitionType
import com.example.model.MatchStage
import com.example.model.TournamentFormat
import com.example.model.TournamentStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FifaCupTournamentTest {

    private lateinit var db: FifaCupDatabase
    private lateinit var repo: FifaCupRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FifaCupDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = FifaCupRepository(db.fifaCupDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `all 11 competitions create independent tournaments and preserve data`() = runTest {
        repo.ensureTeamsSeeded()
        val teams = db.fifaCupDao().getAllTeamsOnce()
        assertTrue(teams.size >= 70)
        // Verify requested clubs exist
        val names = teams.map { it.name }.toSet()
        listOf(
            "Real Madrid", "Barcelona", "Manchester City", "Manchester United",
            "Liverpool", "Bayern Múnich", "PSG", "Juventus", "Flamengo",
            "Boca Juniors", "River Plate", "Junior de Barranquilla", "Atlético Nacional"
        ).forEach { required ->
            assertTrue("Missing club $required", names.contains(required))
        }

        val ids = CompetitionType.entries.map { comp ->
            repo.ensureOrCreateTournamentForCompetition(comp)
        }
        assertEquals(11, ids.distinct().size)

        // Modifying Mundial does not affect Champions League
        val mundialId = ids[0]
        repo.updateTournamentConfig(
            tournamentId = mundialId,
            name = "Mundial 2026 Deiver",
            startDate = "2026-06-11",
            prize = "Copa de Oro",
            participantCount = 32,
            format = TournamentFormat.GRUPOS_Y_LLAVES
        )
        val championsTour = db.fifaCupDao().getTournamentById(ids[1])!!
        assertEquals(CompetitionType.CHAMPIONS_LEAGUE.defaultParticipantCount, championsTour.participantCount)
        assertEquals("Mundial 2026 Deiver", db.fifaCupDao().getTournamentById(mundialId)!!.name)
    }

    @Test
    fun `mundial supports 16 24 32 and 48 teams knockout and group stages`() = runTest {
        repo.ensureTeamsSeeded()
        val counts = listOf(16, 24, 32, 48)
        for (count in counts) {
            val tid = repo.createTournament(
                competitionType = CompetitionType.MUNDIAL,
                name = "Mundial $count",
                startDate = "2026-06-11",
                prize = "Trofeo Mundial",
                participantCount = count,
                format = TournamentFormat.ELIMINATORIA
            )
            repo.autoFillRemainingTeams(tid)
            val drawRes = repo.executeDrawAndStartTournament(tid)
            assertTrue("Draw failed for $count", drawRes.isSuccess)
            val matches = db.fifaCupDao().getMatchesForTournamentOnce(tid)
            // Single-elimination always has N - 1 total matches
            assertEquals("Expected ${count - 1} matches for $count teams", count - 1, matches.size)
        }
    }

    @Test
    fun `knockout advances winners with penalties and crowns champion`() = runTest {
        repo.ensureTeamsSeeded()
        val tid = repo.createTournament(
            competitionType = CompetitionType.SUPERCOPA,
            name = "Supercopa 4",
            startDate = "2026-08-01",
            prize = "Supercopa Dorada",
            participantCount = 4,
            format = TournamentFormat.ELIMINATORIA
        )
        repo.autoFillRemainingTeams(tid)
        assertTrue(repo.executeDrawAndStartTournament(tid).isSuccess)

        val initialMatches = db.fifaCupDao().getMatchesForTournamentOnce(tid)
        assertEquals(3, initialMatches.size) // 2 semis + 1 empty final
        val semis = initialMatches.filter { it.roundNumber == 1 }.sortedBy { it.matchIndex }
        val finalBefore = initialMatches.first { it.roundNumber == 2 }
        assertEquals(null, finalBefore.homeTeamId)
        assertEquals(null, finalBefore.awayTeamId)

        // Tied semifinal without penalty winner must fail!
        val invalidTie = repo.recordMatchResult(semis[0].id, 1, 1, penaltyWinnerTeamId = null)
        assertTrue(invalidTie.isFailure)

        // Tied semifinal WITH penalty winner succeeds & advances winner to Final home slot
        val semi1PenWinner = semis[0].homeTeamId!!
        val okTie = repo.recordMatchResult(
            matchId = semis[0].id,
            homeGoals = 1,
            awayGoals = 1,
            penaltyWinnerTeamId = semi1PenWinner,
            homePenalties = 5,
            awayPenalties = 4
        )
        assertTrue(okTie.isSuccess)

        // Semifinal 2 normal win
        val semi2Winner = semis[1].awayTeamId!!
        assertTrue(repo.recordMatchResult(semis[1].id, 0, 2).isSuccess)

        val finalReady = db.fifaCupDao().getMatchById(finalBefore.id)!!
        assertEquals(semi1PenWinner, finalReady.homeTeamId)
        assertEquals(semi2Winner, finalReady.awayTeamId)

        // Play the Final
        val finalRes = repo.recordMatchResult(finalReady.id, 3, 1)
        assertTrue(finalRes.isSuccess)
        assertEquals(semi1PenWinner, finalRes.getOrNull())

        val finishedTour = db.fifaCupDao().getTournamentById(tid)!!
        assertEquals(TournamentStatus.FINALIZADO.name, finishedTour.status)
        assertEquals(semi1PenWinner, finishedTour.championTeamId)

        val championRow = db.fifaCupDao().getChampionByTournament(tid)
        assertNotNull(championRow)
        assertEquals(semi1PenWinner, championRow!!.teamId)
    }

    @Test
    fun `groups plus knockout auto qualifies top teams after group stage`() = runTest {
        repo.ensureTeamsSeeded()
        val tid = repo.createTournament(
            competitionType = CompetitionType.MUNDIAL,
            name = "Mundial Grupos 16",
            startDate = "2026-06-11",
            prize = "Copa del Mundo",
            participantCount = 16,
            format = TournamentFormat.GRUPOS_Y_LLAVES
        )
        repo.autoFillRemainingTeams(tid)
        assertTrue(repo.executeDrawAndStartTournament(tid).isSuccess)

        val matches = db.fifaCupDao().getMatchesForTournamentOnce(tid)
        val groupMatches = matches.filter { it.stageType == MatchStage.GROUP.name }
        assertEquals(24, groupMatches.size) // 4 groups * 6 matches

        // Play all 24 group matches
        groupMatches.forEachIndexed { idx, m ->
            assertTrue(repo.recordMatchResult(m.id, (idx % 3) + 1, idx % 2).isSuccess)
        }

        val updatedMatches = db.fifaCupDao().getMatchesForTournamentOnce(tid)
        val cuartos = updatedMatches.filter { it.roundName == "Cuartos de Final" }
        assertEquals(4, cuartos.size)
        assertTrue(cuartos.all { it.homeTeamId != null && it.awayTeamId != null })
    }

    @Test
    fun `league computes standings and crowns champion on final matchday`() = runTest {
        repo.ensureTeamsSeeded()
        val tid = repo.createTournament(
            competitionType = CompetitionType.LIGA_PERSONALIZADA,
            name = "Liga Élite",
            startDate = "2026-09-01",
            prize = "Trofeo Liga",
            participantCount = 4,
            format = TournamentFormat.LIGA
        )
        repo.autoFillRemainingTeams(tid)
        assertTrue(repo.executeDrawAndStartTournament(tid).isSuccess)

        val matches = db.fifaCupDao().getMatchesForTournamentOnce(tid)
        assertEquals(6, matches.size) // 4 teams round robin = 6 matches
        matches.forEach { m ->
            assertTrue(repo.recordMatchResult(m.id, 2, 0).isSuccess)
        }

        val finished = db.fifaCupDao().getTournamentById(tid)!!
        assertEquals(TournamentStatus.FINALIZADO.name, finished.status)
        assertNotNull(finished.championTeamId)

        val globalStats = repo.observeGlobalStats().first()
        assertEquals(1, globalStats.championsCount)
        assertEquals(6, globalStats.matchesPlayed)
        assertEquals(12, globalStats.totalGoals)
    }

    @Test
    fun `duplicate team selection is blocked and limit is enforced`() = runTest {
        repo.ensureTeamsSeeded()
        val tid = repo.createTournament(
            competitionType = CompetitionType.AMISTOSOS,
            name = "Amistoso",
            startDate = "2026-07-01",
            prize = "Copa",
            participantCount = 2,
            format = TournamentFormat.PARTIDO_UNICO
        )
        val teams = TeamSeedData.allTeams.take(3)
        assertTrue(repo.toggleTeamSelection(tid, teams[0].id).isSuccess)
        assertTrue(repo.toggleTeamSelection(tid, teams[1].id).isSuccess)
        val thirdTry = repo.toggleTeamSelection(tid, teams[2].id)
        assertTrue(thirdTry.isFailure)
    }
}

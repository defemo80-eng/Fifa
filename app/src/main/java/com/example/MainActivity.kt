package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.FifaCupDatabase
import com.example.data.FifaCupRepository
import com.example.model.CompetitionType
import com.example.ui.components.ChampionCelebrationDialog
import com.example.ui.components.StadiumBackdrop
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CompetitionScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TeamsScreen
import com.example.ui.theme.FifaCupTheme
import com.example.ui.theme.FifaEmerald
import com.example.ui.theme.FifaGold
import com.example.ui.theme.FifaGoldBright
import com.example.ui.theme.StadiumNavyDeep
import com.example.ui.theme.StadiumObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FifaCupViewModel
import com.example.ui.viewmodel.FifaCupViewModelFactory

object Routes {
    const val SPLASH = "splash"
    const val HOME = "inicio"
    const val CALENDAR = "calendario"
    const val HISTORY = "historial"
    const val STATS = "estadisticas"
    const val TEAMS = "equipos"
    const val COMPETITION = "competition/{competitionId}"

    fun competitionRoute(competitionId: String) = "competition/$competitionId"
}

private data class BottomNavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = FifaCupDatabase.getInstance(applicationContext)
        val repository = FifaCupRepository(db.fifaCupDao())
        val factory = FifaCupViewModelFactory(repository)

        setContent {
            FifaCupTheme {
                val viewModel: FifaCupViewModel = viewModel(factory = factory)
                FifaCupApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FifaCupApp(viewModel: FifaCupViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val allTeams by viewModel.allTeams.collectAsStateWithLifecycle()
    val allTournaments by viewModel.allTournaments.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    val allChampions by viewModel.allChampions.collectAsStateWithLifecycle()
    val globalStats by viewModel.globalStats.collectAsStateWithLifecycle()
    val teamLeaderboard by viewModel.teamLeaderboard.collectAsStateWithLifecycle()
    val selectedCompetition by viewModel.selectedCompetition.collectAsStateWithLifecycle()
    val competitionTournaments by viewModel.competitionTournaments.collectAsStateWithLifecycle()
    val activeBundle by viewModel.activeTournamentBundle.collectAsStateWithLifecycle()
    val competitionStats by viewModel.competitionStats.collectAsStateWithLifecycle()
    val newlyCrownedChampion by viewModel.newlyCrownedChampion.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute != null &&
        currentRoute != Routes.SPLASH &&
        !currentRoute.startsWith("competition/")

    val bottomTabs = remember {
        listOf(
            BottomNavTab(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_inicio"),
            BottomNavTab(Routes.CALENDAR, R.string.nav_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_calendario"),
            BottomNavTab(Routes.HISTORY, R.string.nav_history, Icons.Filled.History, Icons.Outlined.History, "nav_historial"),
            BottomNavTab(Routes.STATS, R.string.nav_stats, Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_estadisticas"),
            BottomNavTab(Routes.TEAMS, R.string.nav_teams, Icons.Filled.Groups, Icons.Outlined.Groups, "nav_equipos")
        )
    }

    StadiumBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = StadiumNavyDeep.copy(alpha = 0.96f),
                        contentColor = TextPrimary
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.testTag(tab.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(id = tab.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = tab.labelRes),
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StadiumObsidian,
                                    selectedTextColor = FifaGoldBright,
                                    indicatorColor = FifaEmerald,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.SPLASH
                ) {
                    composable(Routes.SPLASH) {
                        SplashScreen(
                            onEnterMainMenu = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            tournaments = allTournaments,
                            globalStats = globalStats,
                            onOpenCompetition = { comp, preferredTournamentId ->
                                viewModel.openCompetition(comp, preferredTournamentId)
                                navController.navigate(Routes.competitionRoute(comp.id))
                            }
                        )
                    }

                    composable(Routes.CALENDAR) {
                        CalendarScreen(
                            allMatches = allMatches,
                            onRecordScore = { matchId, hg, ag, penId, hp, ap, tourTitle ->
                                viewModel.recordMatchScore(matchId, hg, ag, penId, hp, ap, tourTitle)
                            }
                        )
                    }

                    composable(Routes.HISTORY) {
                        HistoryScreen(
                            champions = allChampions,
                            allMatches = allMatches,
                            onRecordScore = { matchId, hg, ag, penId, hp, ap, tourTitle ->
                                viewModel.recordMatchScore(matchId, hg, ag, penId, hp, ap, tourTitle)
                            }
                        )
                    }

                    composable(Routes.STATS) {
                        StatsScreen(
                            globalStats = globalStats,
                            teamLeaderboard = teamLeaderboard
                        )
                    }

                    composable(Routes.TEAMS) {
                        TeamsScreen(teamLeaderboard = teamLeaderboard)
                    }

                    composable(
                        route = Routes.COMPETITION,
                        arguments = listOf(navArgument("competitionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val compId = backStackEntry.arguments?.getString("competitionId") ?: CompetitionType.MUNDIAL.id
                        val comp = CompetitionType.fromId(compId)
                        LaunchedEffect(compId) {
                            if (selectedCompetition != comp) {
                                viewModel.openCompetition(comp)
                            }
                        }
                        CompetitionScreen(
                            competition = comp,
                            tournaments = competitionTournaments,
                            activeBundle = activeBundle,
                            allTeams = allTeams,
                            competitionStats = competitionStats,
                            onBack = { navController.popBackStack() },
                            onSelectTournament = { tid -> viewModel.selectTournament(tid) },
                            onCreateNewTournament = { viewModel.createNewTournamentInCompetition(comp) },
                            onSaveConfig = { tid, name, date, prize, count, fmt ->
                                viewModel.saveTournamentConfig(tid, name, date, prize, count, fmt)
                            },
                            onToggleTeam = { tid, teamId -> viewModel.toggleTeam(tid, teamId) },
                            onAutoFillTeams = { tid -> viewModel.autoFillTeams(tid) },
                            onClearTeams = { tid -> viewModel.clearTeams(tid) },
                            onStartDraw = { tid, onOk -> viewModel.startDraw(tid, onOk) },
                            onResetDraw = { tid -> viewModel.resetDraw(tid) },
                            onDeleteTournament = { tid -> viewModel.deleteTournament(tid, comp) },
                            onRecordScore = { matchId, hg, ag, penId, hp, ap, tourTitle ->
                                viewModel.recordMatchScore(matchId, hg, ag, penId, hp, ap, tourTitle)
                            }
                        )
                    }
                }
            }
        }
    }

    newlyCrownedChampion?.let { (champTeam, tourTitle) ->
        ChampionCelebrationDialog(
            championTeam = champTeam,
            tournamentTitle = tourTitle,
            onDismiss = { viewModel.dismissChampionCelebration() }
        )
    }
}

package com.example.model

enum class TeamCategory(val label: String) {
    SELECCION("Selección Nacional"),
    CLUB("Club"),
    MIXTO("Selecciones y Clubes")
}

enum class Confederation(val label: String) {
    AMERICA("América"),
    EUROPA("Europa"),
    AFRICA("África"),
    ASIA("Asia"),
    OCEANIA("Oceanía")
}

enum class TournamentFormat(val label: String, val description: String) {
    ELIMINATORIA("Eliminatoria Directa", "Llaves de eliminación directa hasta la gran final"),
    GRUPOS_Y_LLAVES("Grupos + Eliminatoria", "Fase de grupos todos contra todos y llaves finales"),
    LIGA("Liga (Todos contra todos)", "Tabla general por puntos a una vuelta"),
    LIGA_IDA_VUELTA("Liga (Ida y Vuelta)", "Tabla general con partidos de ida y vuelta"),
    PARTIDO_UNICO("Final Directa", "Partido único por el título")
}

enum class TournamentStatus(val label: String) {
    CONFIGURANDO("Configurando"),
    EN_CURSO("En Curso"),
    FINALIZADO("Finalizado")
}

enum class MatchStage(val label: String) {
    GROUP("Fase de Grupos"),
    KNOCKOUT("Eliminatoria"),
    LEAGUE("Liga")
}

enum class BadgePattern {
    HORIZONTAL_3,
    VERTICAL_3,
    HORIZONTAL_2,
    VERTICAL_2,
    CROSS,
    DIAGONAL_SASH,
    HOOPS,
    SHIELD_CENTER,
    STAR_CREST
}

enum class CompetitionType(
    val id: String,
    val order: Int,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val defaultPrize: String,
    val defaultParticipantCount: Int,
    val allowedParticipantCounts: List<Int>,
    val defaultFormat: TournamentFormat,
    val supportedFormats: List<TournamentFormat>,
    val teamPoolRule: TeamCategory,
    val trophyTitle: String,
    val accentColorHex: Long
) {
    MUNDIAL(
        id = "mundial",
        order = 1,
        title = "Mundial",
        shortTitle = "Mundial",
        subtitle = "Copa Mundial de Selecciones FIFA",
        defaultPrize = "Trofeo Copa del Mundo + $42,000,000 USD",
        defaultParticipantCount = 16,
        allowedParticipantCounts = listOf(16, 24, 32, 48),
        defaultFormat = TournamentFormat.GRUPOS_Y_LLAVES,
        supportedFormats = listOf(TournamentFormat.GRUPOS_Y_LLAVES, TournamentFormat.ELIMINATORIA),
        teamPoolRule = TeamCategory.SELECCION,
        trophyTitle = "Copa Mundial FIFA",
        accentColorHex = 0xFFF5C451
    ),
    CHAMPIONS_LEAGUE(
        id = "champions_league",
        order = 2,
        title = "Champions League",
        shortTitle = "Champions",
        subtitle = "Liga de Campeones de Clubes",
        defaultPrize = "Orejona de Campeones + €20,000,000",
        defaultParticipantCount = 16,
        allowedParticipantCounts = listOf(8, 16, 32),
        defaultFormat = TournamentFormat.ELIMINATORIA,
        supportedFormats = listOf(TournamentFormat.ELIMINATORIA, TournamentFormat.GRUPOS_Y_LLAVES),
        teamPoolRule = TeamCategory.CLUB,
        trophyTitle = "Trofeo Champions League",
        accentColorHex = 0xFF38BDF8
    ),
    AMISTOSOS(
        id = "amistosos",
        order = 3,
        title = "Amistosos",
        shortTitle = "Amistosos",
        subtitle = "Duelos y series internacionales",
        defaultPrize = "Trofeo Fecha FIFA + Prestigio Mundial",
        defaultParticipantCount = 2,
        allowedParticipantCounts = listOf(2, 4, 8),
        defaultFormat = TournamentFormat.PARTIDO_UNICO,
        supportedFormats = listOf(
            TournamentFormat.PARTIDO_UNICO,
            TournamentFormat.ELIMINATORIA,
            TournamentFormat.LIGA
        ),
        teamPoolRule = TeamCategory.MIXTO,
        trophyTitle = "Copa Amistosa FIFA",
        accentColorHex = 0xFF00C853
    ),
    COPA_AMERICA(
        id = "copa_america",
        order = 4,
        title = "Copa América",
        shortTitle = "Copa América",
        subtitle = "Campeonato Continental de América",
        defaultPrize = "Trofeo Copa América + $16,000,000 USD",
        defaultParticipantCount = 8,
        allowedParticipantCounts = listOf(8, 12, 16),
        defaultFormat = TournamentFormat.GRUPOS_Y_LLAVES,
        supportedFormats = listOf(TournamentFormat.GRUPOS_Y_LLAVES, TournamentFormat.ELIMINATORIA),
        teamPoolRule = TeamCategory.SELECCION,
        trophyTitle = "Copa América",
        accentColorHex = 0xFF60A5FA
    ),
    COPA_EUROPA(
        id = "copa_europa",
        order = 5,
        title = "Copa Europea",
        shortTitle = "Eurocopa",
        subtitle = "Campeonato Europeo de Selecciones",
        defaultPrize = "Trofeo Henri Delaunay + €10,000,000",
        defaultParticipantCount = 16,
        allowedParticipantCounts = listOf(8, 16, 24),
        defaultFormat = TournamentFormat.GRUPOS_Y_LLAVES,
        supportedFormats = listOf(TournamentFormat.GRUPOS_Y_LLAVES, TournamentFormat.ELIMINATORIA),
        teamPoolRule = TeamCategory.SELECCION,
        trophyTitle = "Copa Europea",
        accentColorHex = 0xFF818CF8
    ),
    EUROPA_LEAGUE(
        id = "europa_league",
        order = 6,
        title = "Europa League",
        shortTitle = "Europa League",
        subtitle = "Copa Continental de Clubes",
        defaultPrize = "Trofeo Europa League + €8,600,000",
        defaultParticipantCount = 16,
        allowedParticipantCounts = listOf(8, 16, 32),
        defaultFormat = TournamentFormat.ELIMINATORIA,
        supportedFormats = listOf(TournamentFormat.ELIMINATORIA, TournamentFormat.GRUPOS_Y_LLAVES),
        teamPoolRule = TeamCategory.CLUB,
        trophyTitle = "Trofeo Europa League",
        accentColorHex = 0xFFFB923C
    ),
    SUPERCOPA(
        id = "supercopa",
        order = 7,
        title = "Supercopa",
        shortTitle = "Supercopa",
        subtitle = "Duelo Supremo entre Campeones",
        defaultPrize = "Supercopa Dorada + €5,000,000",
        defaultParticipantCount = 4,
        allowedParticipantCounts = listOf(2, 4, 8),
        defaultFormat = TournamentFormat.ELIMINATORIA,
        supportedFormats = listOf(TournamentFormat.PARTIDO_UNICO, TournamentFormat.ELIMINATORIA),
        teamPoolRule = TeamCategory.CLUB,
        trophyTitle = "Supercopa de Campeones",
        accentColorHex = 0xFFFBBF24
    ),
    MUNDIAL_DE_CLUBES(
        id = "mundial_de_clubes",
        order = 8,
        title = "Mundial de Clubes",
        shortTitle = "Mundial Clubes",
        subtitle = "Élite Global de Clubes Campeones",
        defaultPrize = "Trofeo Mundial de Clubes + $50,000,000 USD",
        defaultParticipantCount = 16,
        allowedParticipantCounts = listOf(8, 16, 32),
        defaultFormat = TournamentFormat.ELIMINATORIA,
        supportedFormats = listOf(TournamentFormat.ELIMINATORIA, TournamentFormat.GRUPOS_Y_LLAVES),
        teamPoolRule = TeamCategory.CLUB,
        trophyTitle = "Copa Mundial de Clubes",
        accentColorHex = 0xFF34D399
    ),
    ELIMINATORIAS_FIFA(
        id = "eliminatorias_fifa",
        order = 9,
        title = "Eliminatorias FIFA",
        shortTitle = "Eliminatorias",
        subtitle = "Clasificatorias Oficiales por Puntos",
        defaultPrize = "Boleto Oficial al Mundial + Premio FIFA",
        defaultParticipantCount = 8,
        allowedParticipantCounts = listOf(4, 6, 8, 10, 12),
        defaultFormat = TournamentFormat.LIGA_IDA_VUELTA,
        supportedFormats = listOf(TournamentFormat.LIGA, TournamentFormat.LIGA_IDA_VUELTA),
        teamPoolRule = TeamCategory.SELECCION,
        trophyTitle = "Líder de Eliminatorias",
        accentColorHex = 0xFF2DD4BF
    ),
    LIGA_PERSONALIZADA(
        id = "liga_personalizada",
        order = 10,
        title = "Liga personalizada",
        shortTitle = "Liga Propia",
        subtitle = "Crea tu propia liga por puntos",
        defaultPrize = "Copa de Liga + Medalla de Oro",
        defaultParticipantCount = 8,
        allowedParticipantCounts = listOf(4, 6, 8, 10, 12, 16, 20),
        defaultFormat = TournamentFormat.LIGA,
        supportedFormats = listOf(TournamentFormat.LIGA, TournamentFormat.LIGA_IDA_VUELTA),
        teamPoolRule = TeamCategory.MIXTO,
        trophyTitle = "Campeón de Liga",
        accentColorHex = 0xFFA78BFA
    ),
    TORNEO_PERSONALIZADO(
        id = "torneo_personalizado",
        order = 11,
        title = "Torneo personalizado",
        shortTitle = "Torneo Propio",
        subtitle = "Diseña llaves, grupos o copas a medida",
        defaultPrize = "Trofeo Personalizado FIFA CUP",
        defaultParticipantCount = 8,
        allowedParticipantCounts = listOf(4, 8, 16, 24, 32),
        defaultFormat = TournamentFormat.ELIMINATORIA,
        supportedFormats = listOf(
            TournamentFormat.ELIMINATORIA,
            TournamentFormat.GRUPOS_Y_LLAVES,
            TournamentFormat.LIGA
        ),
        teamPoolRule = TeamCategory.MIXTO,
        trophyTitle = "Trofeo Personalizado",
        accentColorHex = 0xFFF472B6
    );

    companion object {
        fun fromId(id: String): CompetitionType {
            return entries.firstOrNull { it.id == id } ?: MUNDIAL
        }
    }
}

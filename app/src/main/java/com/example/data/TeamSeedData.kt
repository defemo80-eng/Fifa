package com.example.data

import com.example.model.BadgePattern
import com.example.model.Confederation
import com.example.model.TeamCategory

object TeamSeedData {

    private fun nation(
        id: String,
        name: String,
        code: String,
        conf: Confederation,
        c1: Long,
        c2: Long,
        c3: Long,
        pattern: BadgePattern,
        rating: Int
    ) = TeamEntity(
        id = id,
        name = name,
        shortCode = code,
        category = TeamCategory.SELECCION.name,
        confederation = conf.name,
        primaryColorHex = c1,
        secondaryColorHex = c2,
        accentColorHex = c3,
        pattern = pattern.name,
        rating = rating
    )

    private fun club(
        id: String,
        name: String,
        code: String,
        conf: Confederation,
        c1: Long,
        c2: Long,
        c3: Long,
        pattern: BadgePattern,
        rating: Int
    ) = TeamEntity(
        id = id,
        name = name,
        shortCode = code,
        category = TeamCategory.CLUB.name,
        confederation = conf.name,
        primaryColorHex = c1,
        secondaryColorHex = c2,
        accentColorHex = c3,
        pattern = pattern.name,
        rating = rating
    )

    val allTeams: List<TeamEntity> = listOf(
        // ==================== SELECCIONES DE AMÉRICA ====================
        nation("sel_col", "Colombia", "COL", Confederation.AMERICA, 0xFFFCD116, 0xFF003893, 0xFFCE1126, BadgePattern.HORIZONTAL_3, 87),
        nation("sel_arg", "Argentina", "ARG", Confederation.AMERICA, 0xFF74ACDF, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.HORIZONTAL_3, 93),
        nation("sel_bra", "Brasil", "BRA", Confederation.AMERICA, 0xFF009C3B, 0xFFFFDF00, 0xFF002776, BadgePattern.STAR_CREST, 91),
        nation("sel_uru", "Uruguay", "URU", Confederation.AMERICA, 0xFF5CA4E5, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.HORIZONTAL_2, 86),
        nation("sel_chi", "Chile", "CHI", Confederation.AMERICA, 0xFF0039A6, 0xFFFFFFFF, 0xFFD52B1E, BadgePattern.HORIZONTAL_2, 80),
        nation("sel_ecu", "Ecuador", "ECU", Confederation.AMERICA, 0xFFFFDD00, 0xFF034EA2, 0xFFED1C24, BadgePattern.HORIZONTAL_3, 83),
        nation("sel_per", "Perú", "PER", Confederation.AMERICA, 0xFFD91023, 0xFFFFFFFF, 0xFFD91023, BadgePattern.VERTICAL_3, 78),
        nation("sel_par", "Paraguay", "PAR", Confederation.AMERICA, 0xFFD52B1E, 0xFFFFFFFF, 0xFF0038A8, BadgePattern.HORIZONTAL_3, 79),
        nation("sel_ven", "Venezuela", "VEN", Confederation.AMERICA, 0xFF7B112C, 0xFFFFCC00, 0xFF00247D, BadgePattern.HORIZONTAL_3, 80),
        nation("sel_bol", "Bolivia", "BOL", Confederation.AMERICA, 0xFFD52B1E, 0xFFF9E300, 0xFF007934, BadgePattern.HORIZONTAL_3, 74),
        nation("sel_mex", "México", "MEX", Confederation.AMERICA, 0xFF006847, 0xFFFFFFFF, 0xFFCE1126, BadgePattern.VERTICAL_3, 83),
        nation("sel_usa", "Estados Unidos", "USA", Confederation.AMERICA, 0xFF002868, 0xFFFFFFFF, 0xFFBF0A30, BadgePattern.HOOPS, 83),
        nation("sel_can", "Canadá", "CAN", Confederation.AMERICA, 0xFFFF0000, 0xFFFFFFFF, 0xFFFF0000, BadgePattern.VERTICAL_3, 81),
        nation("sel_crc", "Costa Rica", "CRC", Confederation.AMERICA, 0xFF002B7F, 0xFFFFFFFF, 0xFFCE1126, BadgePattern.HOOPS, 77),
        nation("sel_pan", "Panamá", "PAN", Confederation.AMERICA, 0xFF005293, 0xFFFFFFFF, 0xFFD21034, BadgePattern.SHIELD_CENTER, 77),
        nation("sel_jam", "Jamaica", "JAM", Confederation.AMERICA, 0xFF009B3A, 0xFFFED100, 0xFF000000, BadgePattern.CROSS, 75),

        // ==================== SELECCIONES DE EUROPA ====================
        nation("sel_esp", "España", "ESP", Confederation.EUROPA, 0xFFAA151B, 0xFFF1BF00, 0xFFAA151B, BadgePattern.HORIZONTAL_3, 92),
        nation("sel_fra", "Francia", "FRA", Confederation.EUROPA, 0xFF002395, 0xFFFFFFFF, 0xFFED2939, BadgePattern.VERTICAL_3, 92),
        nation("sel_ger", "Alemania", "GER", Confederation.EUROPA, 0xFF000000, 0xFFDD0000, 0xFFFFCE00, BadgePattern.HORIZONTAL_3, 89),
        nation("sel_eng", "Inglaterra", "ENG", Confederation.EUROPA, 0xFFFFFFFF, 0xFFCF142B, 0xFF00247D, BadgePattern.CROSS, 90),
        nation("sel_ita", "Italia", "ITA", Confederation.EUROPA, 0xFF009246, 0xFFFFFFFF, 0xFFCE2B37, BadgePattern.VERTICAL_3, 87),
        nation("sel_por", "Portugal", "POR", Confederation.EUROPA, 0xFF006600, 0xFFFF0000, 0xFFFFDF00, BadgePattern.VERTICAL_2, 89),
        nation("sel_ned", "Países Bajos", "NED", Confederation.EUROPA, 0xFFAE1C28, 0xFFFFFFFF, 0xFF21468B, BadgePattern.HORIZONTAL_3, 88),
        nation("sel_bel", "Bélgica", "BEL", Confederation.EUROPA, 0xFF000000, 0xFFFDDA24, 0xFFEF3340, BadgePattern.VERTICAL_3, 86),
        nation("sel_cro", "Croacia", "CRO", Confederation.EUROPA, 0xFFFF0000, 0xFFFFFFFF, 0xFF171796, BadgePattern.HORIZONTAL_3, 85),
        nation("sel_sui", "Suiza", "SUI", Confederation.EUROPA, 0xFFFF0000, 0xFFFFFFFF, 0xFFFF0000, BadgePattern.CROSS, 84),
        nation("sel_den", "Dinamarca", "DEN", Confederation.EUROPA, 0xFFC60C30, 0xFFFFFFFF, 0xFFC60C30, BadgePattern.CROSS, 83),
        nation("sel_pol", "Polonia", "POL", Confederation.EUROPA, 0xFFFFFFFF, 0xFFDC143C, 0xFFFFFFFF, BadgePattern.HORIZONTAL_2, 81),
        nation("sel_swe", "Suecia", "SWE", Confederation.EUROPA, 0xFF006AA7, 0xFFFECC00, 0xFF006AA7, BadgePattern.CROSS, 80),
        nation("sel_nor", "Noruega", "NOR", Confederation.EUROPA, 0xFFBA0C2F, 0xFFFFFFFF, 0xFF00205B, BadgePattern.CROSS, 82),
        nation("sel_aut", "Austria", "AUT", Confederation.EUROPA, 0xFFED2939, 0xFFFFFFFF, 0xFFED2939, BadgePattern.HORIZONTAL_3, 82),
        nation("sel_tur", "Turquía", "TUR", Confederation.EUROPA, 0xFFE30A17, 0xFFFFFFFF, 0xFFE30A17, BadgePattern.STAR_CREST, 81),

        // ==================== SELECCIONES DE ÁFRICA ====================
        nation("sel_mar", "Marruecos", "MAR", Confederation.AFRICA, 0xFFC1272D, 0xFF006233, 0xFFC1272D, BadgePattern.STAR_CREST, 85),
        nation("sel_sen", "Senegal", "SEN", Confederation.AFRICA, 0xFF00853F, 0xFFFDEF42, 0xFFE31B23, BadgePattern.VERTICAL_3, 83),
        nation("sel_egy", "Egipto", "EGY", Confederation.AFRICA, 0xFFCE1126, 0xFFFFFFFF, 0xFF000000, BadgePattern.HORIZONTAL_3, 81),
        nation("sel_nga", "Nigeria", "NGA", Confederation.AFRICA, 0xFF008751, 0xFFFFFFFF, 0xFF008751, BadgePattern.VERTICAL_3, 81),
        nation("sel_cmr", "Camerún", "CMR", Confederation.AFRICA, 0xFF007A5E, 0xFFCE1126, 0xFFFCD116, BadgePattern.VERTICAL_3, 79),
        nation("sel_civ", "Costa de Marfil", "CIV", Confederation.AFRICA, 0xFFF77F00, 0xFFFFFFFF, 0xFF009E60, BadgePattern.VERTICAL_3, 82),
        nation("sel_gha", "Ghana", "GHA", Confederation.AFRICA, 0xFFCF0921, 0xFFFCD20F, 0xFF006B3D, BadgePattern.HORIZONTAL_3, 78),
        nation("sel_alg", "Argelia", "ALG", Confederation.AFRICA, 0xFF006233, 0xFFFFFFFF, 0xFFD21034, BadgePattern.VERTICAL_2, 80),

        // ==================== SELECCIONES DE ASIA ====================
        nation("sel_jpn", "Japón", "JPN", Confederation.ASIA, 0xFFFFFFFF, 0xFFBC002D, 0xFF00205B, BadgePattern.STAR_CREST, 85),
        nation("sel_kor", "Corea del Sur", "KOR", Confederation.ASIA, 0xFFFFFFFF, 0xFFCD2E3A, 0xFF0047A0, BadgePattern.STAR_CREST, 83),
        nation("sel_irn", "Irán", "IRN", Confederation.ASIA, 0xFF239F40, 0xFFFFFFFF, 0xFFDA0000, BadgePattern.HORIZONTAL_3, 81),
        nation("sel_ksa", "Arabia Saudita", "KSA", Confederation.ASIA, 0xFF006C35, 0xFFFFFFFF, 0xFF006C35, BadgePattern.SHIELD_CENTER, 79),
        nation("sel_aus", "Australia", "AUS", Confederation.ASIA, 0xFF00008B, 0xFFFFD700, 0xFFFF0000, BadgePattern.STAR_CREST, 80),
        nation("sel_qat", "Catar", "QAT", Confederation.ASIA, 0xFF8D1B3D, 0xFFFFFFFF, 0xFF8D1B3D, BadgePattern.VERTICAL_2, 77),
        nation("sel_chn", "China", "CHN", Confederation.ASIA, 0xFFDE2910, 0xFFFFDE00, 0xFFDE2910, BadgePattern.STAR_CREST, 74),
        nation("sel_uzb", "Uzbekistán", "UZB", Confederation.ASIA, 0xFF0099B5, 0xFFFFFFFF, 0xFF1EB53A, BadgePattern.HORIZONTAL_3, 77),

        // ==================== SELECCIONES DE OCEANÍA ====================
        nation("sel_nzl", "Nueva Zelanda", "NZL", Confederation.OCEANIA, 0xFF00247D, 0xFFFFFFFF, 0xFFCC142B, BadgePattern.STAR_CREST, 76),
        nation("sel_fij", "Fiyi", "FIJ", Confederation.OCEANIA, 0xFF68BFE5, 0xFF00247D, 0xFFCF142B, BadgePattern.SHIELD_CENTER, 68),
        nation("sel_tah", "Tahití", "TAH", Confederation.OCEANIA, 0xFFCE1126, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.HORIZONTAL_3, 67),

        // ==================== CLUBES DE ÉLITE EUROPEOS ====================
        club("club_rma", "Real Madrid", "RMA", Confederation.EUROPA, 0xFFFFFFFF, 0xFFF5C451, 0xFF1D3557, BadgePattern.DIAGONAL_SASH, 94),
        club("club_bar", "Barcelona", "BAR", Confederation.EUROPA, 0xFF004D98, 0xFFA50044, 0xFFEDBB00, BadgePattern.VERTICAL_2, 92),
        club("club_mci", "Manchester City", "MCI", Confederation.EUROPA, 0xFF6CABDD, 0xFFFFFFFF, 0xFF1C2C5B, BadgePattern.SHIELD_CENTER, 93),
        club("club_mun", "Manchester United", "MUN", Confederation.EUROPA, 0xFFDA291C, 0xFFFBE122, 0xFF000000, BadgePattern.SHIELD_CENTER, 87),
        club("club_liv", "Liverpool", "LIV", Confederation.EUROPA, 0xFFC8102E, 0xFF00B2A9, 0xFFF6EB61, BadgePattern.SHIELD_CENTER, 91),
        club("club_bay", "Bayern Múnich", "BAY", Confederation.EUROPA, 0xFFDC052D, 0xFFFFFFFF, 0xFF0066B2, BadgePattern.SHIELD_CENTER, 92),
        club("club_psg", "PSG", "PSG", Confederation.EUROPA, 0xFF004170, 0xFFDA291C, 0xFFFFFFFF, BadgePattern.VERTICAL_3, 90),
        club("club_juv", "Juventus", "JUV", Confederation.EUROPA, 0xFF111111, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.VERTICAL_3, 87),
        club("club_ars", "Arsenal", "ARS", Confederation.EUROPA, 0xFFEF0107, 0xFFFFFFFF, 0xFF063672, BadgePattern.SHIELD_CENTER, 90),
        club("club_che", "Chelsea", "CHE", Confederation.EUROPA, 0xFF034694, 0xFFFFFFFF, 0xFFDBA111, BadgePattern.SHIELD_CENTER, 87),
        club("club_atm", "Atlético de Madrid", "ATM", Confederation.EUROPA, 0xFFCB3524, 0xFFFFFFFF, 0xFF272E61, BadgePattern.VERTICAL_3, 88),
        club("club_int", "Inter de Milán", "INT", Confederation.EUROPA, 0xFF0068A8, 0xFF000000, 0xFFF5C451, BadgePattern.VERTICAL_2, 90),
        club("club_mil", "AC Milan", "MIL", Confederation.EUROPA, 0xFFFB090B, 0xFF000000, 0xFFFFFFFF, BadgePattern.VERTICAL_2, 87),
        club("club_bvb", "Borussia Dortmund", "BVB", Confederation.EUROPA, 0xFFFDE100, 0xFF000000, 0xFFFDE100, BadgePattern.SHIELD_CENTER, 87),
        club("club_nap", "Napoli", "NAP", Confederation.EUROPA, 0xFF12A0D7, 0xFFFFFFFF, 0xFF003B73, BadgePattern.SHIELD_CENTER, 86),
        club("club_lev", "Bayer Leverkusen", "LEV", Confederation.EUROPA, 0xFFE32221, 0xFF000000, 0xFFFFFFFF, BadgePattern.DIAGONAL_SASH, 88),
        club("club_por", "Porto", "FCP", Confederation.EUROPA, 0xFF003893, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.VERTICAL_2, 84),
        club("club_ben", "Benfica", "SLB", Confederation.EUROPA, 0xFFE30613, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.SHIELD_CENTER, 85),
        club("club_aja", "Ajax", "AJA", Confederation.EUROPA, 0xFFD2122E, 0xFFFFFFFF, 0xFF000000, BadgePattern.VERTICAL_3, 83),
        club("club_tot", "Tottenham", "TOT", Confederation.EUROPA, 0xFFFFFFFF, 0xFF132257, 0xFFF5C451, BadgePattern.SHIELD_CENTER, 85),

        // ==================== CLUBES DE AMÉRICA & RESTO DEL MUNDO ====================
        club("club_fla", "Flamengo", "FLA", Confederation.AMERICA, 0xFFC52613, 0xFF000000, 0xFFFFFFFF, BadgePattern.HOOPS, 86),
        club("club_boc", "Boca Juniors", "BOC", Confederation.AMERICA, 0xFF003F87, 0xFFF5C451, 0xFF003F87, BadgePattern.HORIZONTAL_3, 85),
        club("club_riv", "River Plate", "RIV", Confederation.AMERICA, 0xFFFFFFFF, 0xFFD50032, 0xFF000000, BadgePattern.DIAGONAL_SASH, 86),
        club("club_jun", "Junior de Barranquilla", "JUN", Confederation.AMERICA, 0xFFD50032, 0xFFFFFFFF, 0xFF0033A0, BadgePattern.VERTICAL_3, 83),
        club("club_atn", "Atlético Nacional", "ATN", Confederation.AMERICA, 0xFF00843D, 0xFFFFFFFF, 0xFF005A2B, BadgePattern.VERTICAL_3, 84),
        club("club_pal", "Palmeiras", "PAL", Confederation.AMERICA, 0xFF006437, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.SHIELD_CENTER, 86),
        club("club_sao", "São Paulo", "SAO", Confederation.AMERICA, 0xFFFE0000, 0xFFFFFFFF, 0xFF000000, BadgePattern.HOOPS, 83),
        club("club_mln", "Millonarios", "MILN", Confederation.AMERICA, 0xFF003DA5, 0xFFFFFFFF, 0xFFF5C451, BadgePattern.SHIELD_CENTER, 81),
        club("club_amc", "América de Cali", "AME", Confederation.AMERICA, 0xFFD50032, 0xFF9C1222, 0xFFFFFFFF, BadgePattern.SHIELD_CENTER, 81),
        club("club_mia", "Inter Miami", "MIA", Confederation.AMERICA, 0xFFF7B5CD, 0xFF231F20, 0xFFFFFFFF, BadgePattern.SHIELD_CENTER, 82),
        club("club_caz", "Cruz Azul", "CAZ", Confederation.AMERICA, 0xFF0057B8, 0xFFFFFFFF, 0xFFE4002B, BadgePattern.SHIELD_CENTER, 80),
        club("club_aln", "Al-Nassr", "ALN", Confederation.ASIA, 0xFFFFD100, 0xFF0033A0, 0xFFFFFFFF, BadgePattern.SHIELD_CENTER, 83),
        club("club_alh", "Al-Hilal", "ALH", Confederation.ASIA, 0xFF0055A5, 0xFFFFFFFF, 0xFF38BDF8, BadgePattern.SHIELD_CENTER, 84),
        club("club_ahl", "Al Ahly", "AHL", Confederation.AFRICA, 0xFFD50032, 0xFF000000, 0xFFF5C451, BadgePattern.SHIELD_CENTER, 81)
    )
}

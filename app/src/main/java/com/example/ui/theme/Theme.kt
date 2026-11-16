package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val FifaDarkColorScheme = darkColorScheme(
    primary = FifaEmerald,
    onPrimary = StadiumObsidian,
    primaryContainer = TurfGreen,
    onPrimaryContainer = TextPrimary,
    secondary = FifaGold,
    onSecondary = StadiumObsidian,
    secondaryContainer = FifaGoldDeep,
    onSecondaryContainer = TextPrimary,
    tertiary = InfoCyan,
    onTertiary = StadiumObsidian,
    background = StadiumObsidian,
    onBackground = TextPrimary,
    surface = StadiumNavyDeep,
    onSurface = TextPrimary,
    surfaceVariant = StadiumNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = StadiumBorder,
    error = StatusLoss,
    onError = TextPrimary
)

val FifaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FifaCupTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FifaDarkColorScheme,
        typography = FifaTypography,
        shapes = FifaShapes,
        content = content
    )
}

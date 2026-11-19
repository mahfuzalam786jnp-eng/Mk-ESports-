package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MkEsportsColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = PureWhite,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = CrimsonBright,
    secondary = MoltenGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldBright,
    tertiary = EmeraldWin,
    onTertiary = ObsidianBlack,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldWin,
    background = ObsidianBlack,
    onBackground = PureWhite,
    surface = CarbonSurface,
    onSurface = PureWhite,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = MutedSilver,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = CarbonSurface,
    surfaceContainer = ElevatedSlate,
    surfaceContainerHigh = HighSlate,
    outline = SubtleBorder,
    outlineVariant = DimGray,
    error = ErrorRed,
    onError = ObsidianBlack
)

val MkEsportsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MkEsportsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MkEsportsColorScheme,
        typography = MkEsportsTypography,
        shapes = MkEsportsShapes,
        content = content
    )
}

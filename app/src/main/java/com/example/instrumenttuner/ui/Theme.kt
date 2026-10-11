package com.example.instrumenttuner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TunerPalette(
    val name: String,
    val background: Color, val panel: Color, val card: Color, val sheet: Color,
    val chip: Color, val chipActive: Color, val track: Color,
    val textPrimary: Color, val textSub: Color, val dim: Color,
    val primary: Color, val warn: Color, val danger: Color,
    val bandDim: Color, val bandFaint: Color, val spectrumBar: Color, val needleHub: Color
)

val THEMES = listOf(
    TunerPalette("Midnight",
        Color(0xFF101014), Color(0xFF17171C), Color(0xFF1D1D24), Color(0xFF1A1A20),
        Color(0xFF26262E), Color(0xFF2C3E33), Color(0xFF26262C),
        Color.White, Color(0xFF8E8E93), Color(0xFF5A5A64),
        Color(0xFF34C759), Color(0xFFFFB300), Color(0xFFFF453A),
        Color(0xFFD9D9E2), Color(0xFF3A3A44), Color(0xFF8FB9A0), Color(0xFF141418)),
    TunerPalette("Stage",
        Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF111111), Color(0xFF0D0D0D),
        Color(0xFF1A1A1A), Color(0xFF123320), Color(0xFF1C1C1C),
        Color.White, Color(0xFFA0A0A8), Color(0xFF666666),
        Color(0xFF00E676), Color(0xFFFFAB00), Color(0xFFFF1744),
        Color(0xFFE0E0E0), Color(0xFF333333), Color(0xFF00E5A0), Color.Black),
    TunerPalette("Daylight",
        Color(0xFFF7F7F9), Color.White, Color(0xFFEFEFF2), Color.White,
        Color(0xFFE8E8EC), Color(0xFFD9EFE0), Color(0xFFE2E2E8),
        Color(0xFF14141A), Color(0xFF6E6E78), Color(0xFFA0A0AA),
        Color(0xFF1FA34A), Color(0xFFD98E04), Color(0xFFD70015),
        Color(0xFF3A3A44), Color(0xFFC8C8D0), Color(0xFF2E7D5B), Color(0xFF22222A)),
    TunerPalette("Sepia",
        Color(0xFF1C1610), Color(0xFF241D15), Color(0xFF2A2219), Color(0xFF211A12),
        Color(0xFF332A1F), Color(0xFF3E3222), Color(0xFF2E261B),
        Color(0xFFF2E8D8), Color(0xFFB8A88F), Color(0xFF7A6C57),
        Color(0xFF9CB356), Color(0xFFE0A32E), Color(0xFFC4553B),
        Color(0xFFEADFC8), Color(0xFF4A3F30), Color(0xFFC9A86A), Color(0xFF14100B)),
    TunerPalette("Ocean",
        Color(0xFF0B1220), Color(0xFF101A2C), Color(0xFF14213A), Color(0xFF0E1830),
        Color(0xFF1B2A45), Color(0xFF1E3A52), Color(0xFF16233A),
        Color(0xFFEAF2FF), Color(0xFF8CA0BC), Color(0xFF56677F),
        Color(0xFF2EE6A8), Color(0xFFFFCA6B), Color(0xFFFF6B6B),
        Color(0xFFD6E4F5), Color(0xFF27384F), Color(0xFF6FA8DC), Color(0xFF0A0F18)),
    TunerPalette("Vintage",
        Color(0xFF0D0B07), Color(0xFF14100A), Color(0xFF1A150D), Color(0xFF120E08),
        Color(0xFF241D12), Color(0xFF362B18), Color(0xFF201910),
        Color(0xFFFFE8C2), Color(0xFFC09A5E), Color(0xFF7A6238),
        Color(0xFFFFB000), Color(0xFFE07A3F), Color(0xFFFF5C4D),
        Color(0xFFFFDA9E), Color(0xFF3A2F1C), Color(0xFFE0B060), Color(0xFF0B0905))
)

fun themeByName(name: String): TunerPalette = THEMES.firstOrNull { it.name == name } ?: THEMES[0]

val LocalTunerPalette = staticCompositionLocalOf { THEMES[0] }

@Composable
fun TunerTheme(palette: TunerPalette, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = palette.primary, onPrimary = palette.background,
        secondary = palette.warn,
        background = palette.background, onBackground = palette.textPrimary,
        surface = palette.panel, onSurface = palette.textPrimary,
        surfaceVariant = palette.chip, onSurfaceVariant = palette.textSub,
        error = palette.danger
    )
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalTunerPalette provides palette) { content() }
    }
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.ui.AgentViewModel

private val CyberDarkColorScheme = darkColorScheme(
    primary = HermesOrange,
    onPrimary = CyberObsidian,
    primaryContainer = SlateMedium,
    onPrimaryContainer = HermesOrangeLight,
    secondary = CyberTeal,
    onSecondary = CyberObsidian,
    tertiary = AlertAmber,
    background = CyberObsidian,
    onBackground = TextSlateMain,
    surface = SlateCore,
    onSurface = TextSlateMain,
    surfaceVariant = SlateMedium,
    onSurfaceVariant = TextSlateMuted,
    outline = SlateMedium,
    error = ErrorRed,
    onError = CyberObsidian
)

private val CyberLightColorScheme = lightColorScheme(
    primary = HermesOrange,
    onPrimary = TextSlateMain,
    primaryContainer = SlateCore,
    onPrimaryContainer = HermesOrange,
    secondary = CyberTeal,
    onSecondary = CyberObsidian,
    tertiary = AlertAmber,
    background = SlateCore,
    onBackground = TextSlateMain,
    surface = SlateMedium,
    onSurface = TextSlateMain,
    surfaceVariant = SlateCore,
    onSurfaceVariant = TextSlateMain,
    outline = SlateMedium
)

@Composable
fun MyApplicationTheme(
    viewModel: AgentViewModel? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeModeSetting = viewModel?.themeMode?.collectAsState(initial = "SYSTEM")?.value ?: "SYSTEM"
    val isDark = when (themeModeSetting) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) CyberDarkColorScheme else CyberLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

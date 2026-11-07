package com.sumijojoh.mg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sumijojoh.mg.data.ThemeMode

val JauneConge = Color(0xFFF2C94C)
val VertMission = Color(0xFF2F9E62)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    background = Color(0xFFF7F5F0),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0EEE7),
    onSurfaceVariant = Color(0xFF0F172A)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFF7F5F0),
    onPrimary = Color(0xFF0F172A),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF7F5F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF7F5F0),
    outline = Color(0xFFF7F5F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFF7F5F0)
)

@Composable
fun SumiTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val scheme = when (themeMode) {
        ThemeMode.CLAIR -> LightScheme
        ThemeMode.SOMBRE -> DarkScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

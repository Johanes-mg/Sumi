package com.venajojoh.mg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Fond = Color(0xFFF7F5F0)
val Encre = Color(0xFF0F172A)
val JauneConge = Color(0xFFF2C94C)
val VertTravail = Color(0xFF2F9E62)
val BlancRepos = Color(0xFFFFFFFF)
val BordureDouce = Color(0xFFE4E1D8)
val BordureNoire = Color(0xFF111111)

private val palette = lightColorScheme(
    primary = Encre,
    onPrimary = Color.White,
    background = Fond,
    onBackground = Encre,
    surface = Color.White,
    onSurface = Encre
)

@Composable
fun VenaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette, content = content)
}

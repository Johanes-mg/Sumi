package com.sumijojoh.mg.data

enum class Langue {
    FRANCAIS,
    ANGLAIS
}

enum class ThemeMode {
    CLAIR,
    SOMBRE
}

data class AppSettings(
    val langue: Langue,
    val theme: ThemeMode,
    val deviceId: String = ""
)

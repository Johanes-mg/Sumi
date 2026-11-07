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
    val syncId: String? = null,
    val deviceId: String = ""
)

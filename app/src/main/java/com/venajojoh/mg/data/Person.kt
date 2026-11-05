package com.venajojoh.mg.data

data class Person(
    val id: Long,
    val nom: String,
    val jours: Map<String, DayState>
) {
    fun etatDuJour(dateKey: String): DayState = jours[dateKey] ?: DayState.REPOS
}

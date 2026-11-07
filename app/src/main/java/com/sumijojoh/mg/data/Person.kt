package com.sumijojoh.mg.data

enum class PersonSource {
    LOCAL,
    JSON,
    SCAN
}

data class Person(
    val id: Long,
    val nom: String,
    val jours: Map<String, DayState>,
    val missions: List<MissionInfo>,
    val source: PersonSource = PersonSource.LOCAL,
    val proprietaireDeviceId: String = ""
) {
    fun etatDuJour(dateKey: String): DayState = jours[dateKey] ?: DayState.REPOS
    fun lectureSeule(): Boolean = source == PersonSource.SCAN
}

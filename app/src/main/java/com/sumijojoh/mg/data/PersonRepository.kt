package com.sumijojoh.mg.data

import android.content.Context
import android.content.SharedPreferences

class PersonRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sumi_prefs", Context.MODE_PRIVATE)

    private var cache: List<Person>? = null
    private var cacheProchainId: Long = -1L

    fun charger(): List<Person> {
        cache?.let { return it }
        val brut = prefs.getString(KEY_PERSONNES, "") ?: ""
        val resultat = if (brut.isBlank()) emptyList() else decoder(brut)
        cache = resultat
        var max = 0L
        for (p in resultat) if (p.id > max) max = p.id
        cacheProchainId = max + 1L
        return resultat
    }

    fun sauvegarder(personnes: List<Person>) {
        cache = personnes
        var max = 0L
        for (p in personnes) if (p.id > max) max = p.id
        cacheProchainId = max + 1L
        prefs.edit().putString(KEY_PERSONNES, encoder(personnes)).apply()
    }

    fun prochainId(): Long {
        if (cacheProchainId < 0L) charger()
        val id = cacheProchainId
        cacheProchainId = id + 1L
        return id
    }

    fun remplacerScans(nouvelles: List<Person>) {
        val autres = charger().filterNot { it.source == PersonSource.SCAN }
        val toutes = ArrayList<Person>(autres.size + nouvelles.size)
        toutes.addAll(autres)
        for (p in nouvelles) {
            toutes.add(p.copy(id = prochainId(), source = PersonSource.SCAN))
        }
        sauvegarder(toutes)
    }

    fun ajouterJson(importees: List<Person>) {
        val actuelles = charger()
        val toutes = ArrayList<Person>(actuelles.size + importees.size)
        toutes.addAll(actuelles)
        for (p in importees) {
            toutes.add(p.copy(id = prochainId(), source = PersonSource.JSON))
        }
        sauvegarder(toutes)
    }

    fun supprimerScans() {
        val autres = charger().filterNot { it.source == PersonSource.SCAN }
        sauvegarder(autres)
    }

    private fun encoder(personnes: List<Person>): String {
        if (personnes.isEmpty()) return ""
        val sb = StringBuilder(personnes.size * 32)
        var premier = true
        for (p in personnes) {
            if (!premier) sb.append(SEP_RECORD)
            premier = false
            sb.append(p.id).append(SEP_FIELD).append(p.nom).append(SEP_FIELD)
            var premierJour = true
            for ((clef, etat) in p.jours) {
                if (!premierJour) sb.append(SEP_ITEM)
                premierJour = false
                sb.append(clef).append('=').append(etat.name)
            }
            sb.append(SEP_FIELD)
            var premiereMission = true
            for (m in p.missions) {
                if (!premiereMission) sb.append(SEP_ITEM)
                premiereMission = false
                sb.append(m.dateDebut).append(SEP_SUB).append(m.dateFin).append(SEP_SUB).append(m.motif)
            }
            sb.append(SEP_FIELD).append(p.source.name)
            sb.append(SEP_FIELD).append(p.proprietaireDeviceId)
        }
        return sb.toString()
    }

    private fun decoder(brut: String): List<Person> {
        val resultat = ArrayList<Person>(8)
        var posLigne = 0
        val longueur = brut.length
        while (posLigne < longueur) {
            var finLigne = brut.indexOf(SEP_RECORD, posLigne)
            if (finLigne < 0) finLigne = longueur
            val ligne = brut.substring(posLigne, finLigne)
            posLigne = finLigne + 1

            val sep1 = ligne.indexOf(SEP_FIELD)
            if (sep1 <= 0) continue
            val sep2 = ligne.indexOf(SEP_FIELD, sep1 + 1)
            if (sep2 <= sep1) continue
            val sep3 = ligne.indexOf(SEP_FIELD, sep2 + 1)
            val sep4 = if (sep3 < 0) -1 else ligne.indexOf(SEP_FIELD, sep3 + 1)
            val sep5 = if (sep4 < 0) -1 else ligne.indexOf(SEP_FIELD, sep4 + 1)
            val id = ligne.substring(0, sep1).toLongOrNull() ?: continue
            val nom = ligne.substring(sep1 + 1, sep2)
            val joursBruts = if (sep3 < 0) ligne.substring(sep2 + 1) else ligne.substring(sep2 + 1, sep3)
            val missionsBrutes = if (sep3 < 0) "" else if (sep4 < 0) ligne.substring(sep3 + 1) else ligne.substring(sep3 + 1, sep4)
            val sourceStr = if (sep4 < 0) "" else if (sep5 < 0) ligne.substring(sep4 + 1) else ligne.substring(sep4 + 1, sep5)
            val propStr = if (sep5 < 0) "" else ligne.substring(sep5 + 1)

            val jours = if (joursBruts.isEmpty()) {
                emptyMap()
            } else {
                val map = HashMap<String, DayState>(8)
                var posJ = 0
                val longJ = joursBruts.length
                while (posJ < longJ) {
                    var finJ = joursBruts.indexOf(SEP_ITEM, posJ)
                    if (finJ < 0) finJ = longJ
                    val paire = joursBruts.substring(posJ, finJ)
                    posJ = finJ + 1
                    val eq = paire.indexOf('=')
                    if (eq <= 0 || eq >= paire.length - 1) continue
                    val clef = paire.substring(0, eq)
                    val etat = when (paire.substring(eq + 1)) {
                        "REPOS" -> DayState.REPOS
                        "CONGE" -> DayState.CONGE
                        "MISSION" -> DayState.MISSION
                        else -> continue
                    }
                    map[clef] = etat
                }
                map
            }

            val missions = if (missionsBrutes.isEmpty()) {
                emptyList()
            } else {
                val liste = ArrayList<MissionInfo>(4)
                var posM = 0
                val longM = missionsBrutes.length
                while (posM < longM) {
                    var finM = missionsBrutes.indexOf(SEP_ITEM, posM)
                    if (finM < 0) finM = longM
                    val bloc = missionsBrutes.substring(posM, finM)
                    posM = finM + 1
                    val parts = bloc.split(SEP_SUB)
                    if (parts.size == 3) {
                        liste.add(MissionInfo(parts[0], parts[1], parts[2]))
                    }
                }
                liste
            }

            val source = when (sourceStr) {
                "SCAN" -> PersonSource.SCAN
                "JSON" -> PersonSource.JSON
                else -> PersonSource.LOCAL
            }

            resultat.add(Person(id, nom, jours, missions, source, propStr))
        }
        return resultat
    }

    companion object {
        private const val KEY_PERSONNES = "personnes_v4"
        private const val SEP_RECORD = "\u0001"
        private const val SEP_FIELD = "\u0002"
        private const val SEP_ITEM = "\u0003"
        private const val SEP_SUB = "\u0004"
    }
}

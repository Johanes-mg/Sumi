package com.venajojoh.mg.data

import android.content.Context
import android.content.SharedPreferences

class PersonRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("vena_prefs", Context.MODE_PRIVATE)

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

    private fun encoder(personnes: List<Person>): String {
        if (personnes.isEmpty()) return ""
        val sb = StringBuilder(personnes.size * 24)
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

            val id = ligne.substring(0, sep1).toLongOrNull() ?: continue
            val nom = ligne.substring(sep1 + 1, sep2)
            val joursBruts = ligne.substring(sep2 + 1)

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
                        "TRAVAIL" -> DayState.TRAVAIL
                        else -> continue
                    }
                    map[clef] = etat
                }
                map
            }
            resultat.add(Person(id, nom, jours))
        }
        return resultat
    }

    companion object {
        private const val KEY_PERSONNES = "personnes_v1"
        private const val SEP_RECORD = "\u0001"
        private const val SEP_FIELD = "\u0002"
        private const val SEP_ITEM = "\u0003"
    }
}

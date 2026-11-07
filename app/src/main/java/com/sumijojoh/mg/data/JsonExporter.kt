package com.sumijojoh.mg.data

import org.json.JSONArray
import org.json.JSONObject

object JsonExporter {

    fun exporter(personnes: List<Person>): String {
        val racine = JSONObject()
        racine.put("version", 1)
        val liste = JSONArray()
        for (p in personnes) {
            val obj = JSONObject()
            obj.put("nom", p.nom)
            val jours = JSONArray()
            for ((clef, etat) in p.jours) {
                val j = JSONObject()
                j.put("cle", clef)
                j.put("etat", etat.name)
                jours.put(j)
            }
            obj.put("jours", jours)
            val missions = JSONArray()
            for (m in p.missions) {
                val mo = JSONObject()
                mo.put("debut", m.dateDebut)
                mo.put("fin", m.dateFin)
                mo.put("motif", m.motif)
                missions.put(mo)
            }
            obj.put("missions", missions)
            obj.put("proprietaire", p.proprietaireDeviceId)
            liste.put(obj)
        }
        racine.put("personnes", liste)
        return racine.toString()
    }

    fun importer(json: String): List<Person> {
        val racine = JSONObject(json)
        val liste = racine.optJSONArray("personnes") ?: JSONArray()
        val resultat = ArrayList<Person>(liste.length())
        for (i in 0 until liste.length()) {
            val obj = liste.optJSONObject(i) ?: continue
            val nom = obj.optString("nom", "").trim()
            if (nom.isEmpty()) continue
            val jours = HashMap<String, DayState>(8)
            val joursArr = obj.optJSONArray("jours") ?: JSONArray()
            for (j in 0 until joursArr.length()) {
                val jo = joursArr.optJSONObject(j) ?: continue
                val clef = jo.optString("cle", "")
                if (clef.length != 10) continue
                val etat = when (jo.optString("etat", "")) {
                    "REPOS" -> DayState.REPOS
                    "CONGE" -> DayState.CONGE
                    "MISSION" -> DayState.MISSION
                    else -> continue
                }
                jours[clef] = etat
            }
            val missions = ArrayList<MissionInfo>(4)
            val missionsArr = obj.optJSONArray("missions") ?: JSONArray()
            for (m in 0 until missionsArr.length()) {
                val mo = missionsArr.optJSONObject(m) ?: continue
                val debut = mo.optString("debut", "")
                val fin = mo.optString("fin", "")
                val motif = mo.optString("motif", "")
                if (debut.length == 10 && fin.length == 10 && motif.isNotEmpty()) {
                    missions.add(MissionInfo(debut, fin, motif))
                }
            }
            val prop = obj.optString("proprietaire", "")
            resultat.add(Person(0L, nom, jours, missions, PersonSource.LOCAL, prop))
        }
        return resultat
    }
}

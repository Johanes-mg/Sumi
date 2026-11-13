package com.sumijojoh.mg.data

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object QrSync {

    private const val LIMITE_OCTETS = 2200

    data class ContenuDecode(
        val personne: Person?,
        val syncId: String?
    )

    fun encoderPourQR(personne: Person): String? {
        if (personne.syncId.isNullOrEmpty()) return null
        return runCatching {
            val obj = JSONObject()
            obj.put("v", 2)
            obj.put("syncId", personne.syncId)
            obj.put("nom", personne.nom)
            obj.put("updatedAt", personne.updatedAt)
            val jours = StringBuilder(personne.jours.size * 18)
            var premier = true
            for ((clef, etat) in personne.jours) {
                if (!premier) jours.append(';')
                premier = false
                jours.append(clef).append('=').append(etat.name)
            }
            obj.put("jours", jours.toString())
            val missions = StringBuilder(personne.missions.size * 32)
            var premiere = true
            for (m in personne.missions) {
                if (!premiere) missions.append(';')
                premiere = false
                missions.append(m.dateDebut).append('~').append(m.dateFin).append('~').append(m.motif)
            }
            obj.put("missions", missions.toString())

            val baos = ByteArrayOutputStream()
            GZIPOutputStream(baos).use { it.write(obj.toString().toByteArray(Charsets.UTF_8)) }
            val compresse = baos.toByteArray()
            val b64 = Base64.encodeToString(compresse, Base64.NO_WRAP)
            val complet = "SUMI2:$b64"
            if (complet.length > LIMITE_OCTETS) null else complet
        }.getOrNull()
    }

    fun decoderDepuisQR(contenu: String): ContenuDecode? {
        return runCatching {
            val propre = contenu.trim()
            if (!propre.startsWith("SUMI2:")) return null
            val b64 = propre.substring(6)
            val compresse = Base64.decode(b64, Base64.NO_WRAP)
            val json = GZIPInputStream(ByteArrayInputStream(compresse)).use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val obj = JSONObject(json)
            val syncId = obj.optString("syncId", "").ifEmpty { null }
            val nom = obj.optString("nom", "").trim()
            if (nom.isEmpty() || syncId == null) return null
            val updatedAt = obj.optLong("updatedAt", 0L)
            val joursBruts = obj.optString("jours", "")
            val jours = HashMap<String, DayState>(8)
            if (joursBruts.isNotEmpty()) {
                for (bloc in joursBruts.split(';')) {
                    val eq = bloc.indexOf('=')
                    if (eq <= 0 || eq >= bloc.length - 1) continue
                    val clef = bloc.substring(0, eq)
                    if (clef.length != 10) continue
                    val etat = when (bloc.substring(eq + 1)) {
                        "REPOS" -> DayState.REPOS
                        "CONGE" -> DayState.CONGE
                        "MISSION" -> DayState.MISSION
                        else -> continue
                    }
                    jours[clef] = etat
                }
            }
            val missionsBrutes = obj.optString("missions", "")
            val missions = ArrayList<MissionInfo>(4)
            if (missionsBrutes.isNotEmpty()) {
                for (bloc in missionsBrutes.split(';')) {
                    val parts = bloc.split('~')
                    if (parts.size == 3 && parts[0].length == 10 && parts[1].length == 10 && parts[2].isNotEmpty()) {
                        missions.add(MissionInfo(parts[0], parts[1], parts[2]))
                    }
                }
            }
            val personne = Person(
                id = 0L,
                nom = nom,
                jours = jours,
                missions = missions,
                source = PersonSource.LOCAL,
                proprietaireDeviceId = "",
                syncId = syncId,
                updatedAt = updatedAt
            )
            ContenuDecode(personne, syncId)
        }.getOrNull()
    }

    fun genererBitmap(personne: Person, taille: Int): Bitmap? {
        val contenu = encoderPourQR(personne) ?: return null
        val hints = mapOf(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(contenu, BarcodeFormat.QR_CODE, taille, taille, hints)
        val bitmap = Bitmap.createBitmap(taille, taille, Bitmap.Config.ARGB_8888)
        for (x in 0 until taille) {
            for (y in 0 until taille) {
                bitmap.setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        return bitmap
    }
}

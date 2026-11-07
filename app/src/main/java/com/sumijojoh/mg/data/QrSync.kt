package com.sumijojoh.mg.data

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object QrSync {

    private const val LIMITE_OCTETS = 2200
    private const val SEPARATEUR_SYNC = "|"

    data class ContenuDecode(
        val syncId: String?,
        val personnes: List<Person>
    )

    fun encoderPourQR(personnes: List<Person>, syncId: String?): String? {
        return runCatching {
            val json = JsonExporter.exporter(personnes)
            val baos = ByteArrayOutputStream()
            GZIPOutputStream(baos).use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val compresse = baos.toByteArray()
            val b64 = Base64.encodeToString(compresse, Base64.NO_WRAP)
            val sync = if (syncId.isNullOrEmpty()) UUID.randomUUID().toString() else syncId
            val complet = "$sync$SEPARATEUR_SYNC$b64"
            if (complet.length > LIMITE_OCTETS) return null
            complet
        }.getOrNull()
    }

    fun decoderDepuisQR(contenu: String): ContenuDecode? {
        return runCatching {
            val propre = contenu.trim()
            val syncId: String?
            val partieDonnees: String
            if (propre.contains(SEPARATEUR_SYNC)) {
                val idx = propre.indexOf(SEPARATEUR_SYNC)
                syncId = propre.substring(0, idx).ifEmpty { null }
                partieDonnees = propre.substring(idx + 1)
            } else {
                syncId = null
                partieDonnees = propre
            }
            val compresse = Base64.decode(partieDonnees, Base64.NO_WRAP)
            val json = GZIPInputStream(ByteArrayInputStream(compresse)).use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val personnes = JsonExporter.importer(json)
            ContenuDecode(syncId, personnes)
        }.getOrNull()
    }

    fun genererBitmap(personnes: List<Person>, syncId: String?, taille: Int): Bitmap? {
        val contenu = encoderPourQR(personnes, syncId) ?: return null
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

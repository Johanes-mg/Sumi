package com.sumijojoh.mg.data

import android.util.Log
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object SyncClient {

    private const val BASE = "https://extendsclass.com/api/json-storage/bin"
    private const val TAG = "SumiSync"

    data class Lecture(
        val contenu: String?,
        val modifie: Boolean
    )

    fun creerBin(json: String): String? {
        return try {
            val conn = (URL(BASE).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Sumi-Android")
            }
            conn.outputStream.use { out: OutputStream ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = runCatching {
                    conn.errorStream?.use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()
                Log.e(TAG, "POST failed: $code - $err")
                conn.disconnect()
                return null
            }
            val reponse = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            conn.disconnect()
            val obj = JSONObject(reponse)
            val id = obj.optString("id", "").ifEmpty { obj.optString("binId", "") }
            if (id.isEmpty()) {
                Log.e(TAG, "POST response sans id: $reponse")
                null
            } else {
                Log.d(TAG, "POST ok id=$id")
                id
            }
        } catch (e: Exception) {
            Log.e(TAG, "POST exception: ${e.message}")
            null
        }
    }

    fun lireBin(binId: String): Lecture? {
        if (binId.isBlank()) return null
        return try {
            val conn = (URL("$BASE/$binId").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Sumi-Android")
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return null
            }
            val contenu = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            conn.disconnect()
            Lecture(contenu, true)
        } catch (e: Exception) {
            null
        }
    }

    fun mettreAJourBin(binId: String, json: String): Boolean {
        if (binId.isBlank()) return false
        return try {
            val conn = (URL("$BASE/$binId").openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Sumi-Android")
            }
            conn.outputStream.use { out: OutputStream ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }

    fun supprimerBin(binId: String): Boolean {
        if (binId.isBlank()) return false
        return try {
            val conn = (URL("$BASE/$binId").openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Sumi-Android")
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }
}

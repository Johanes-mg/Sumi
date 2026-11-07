package com.sumijojoh.mg.data

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object SyncClient {

    private const val BASE_URL = "https://jsonblob.com/api/jsonBlob"

    fun genererSyncId(): String = UUID.randomUUID().toString()

    fun urlPour(syncId: String): String = "$BASE_URL/$syncId"

    fun testerConnexion(): Boolean {
        return runCatching {
            val conn = (URL("https://jsonblob.com/api/jsonBlob").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..499
        }.getOrDefault(false)
    }

    fun pousser(syncId: String, json: String): Boolean {
        return runCatching {
            val conn = (URL(urlPour(syncId)).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }
            conn.outputStream.use { out: OutputStream ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        }.getOrDefault(false)
    }

    fun tirer(syncId: String): String? {
        return runCatching {
            val conn = (URL(urlPour(syncId)).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            if (code == 404) {
                conn.disconnect()
                return ""
            }
            if (code !in 200..299) {
                conn.disconnect()
                return null
            }
            val contenu = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            conn.disconnect()
            contenu
        }.getOrNull()
    }
}

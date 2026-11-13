package com.sumijojoh.mg.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class AppSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sumi_settings", Context.MODE_PRIVATE)

    fun charger(): AppSettings {
        val langueNom = prefs.getString(KEY_LANGUE, Langue.FRANCAIS.name) ?: Langue.FRANCAIS.name
        val themeNom = prefs.getString(KEY_THEME, ThemeMode.CLAIR.name) ?: ThemeMode.CLAIR.name
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrBlank()) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        val langue = runCatching { Langue.valueOf(langueNom) }.getOrDefault(Langue.FRANCAIS)
        val theme = runCatching { ThemeMode.valueOf(themeNom) }.getOrDefault(ThemeMode.CLAIR)
        return AppSettings(langue, theme, deviceId)
    }

    fun sauvegarder(reglages: AppSettings) {
        prefs.edit()
            .putString(KEY_LANGUE, reglages.langue.name)
            .putString(KEY_THEME, reglages.theme.name)
            .putString(KEY_DEVICE_ID, reglages.deviceId)
            .apply()
    }

    companion object {
        private const val KEY_LANGUE = "langue"
        private const val KEY_THEME = "theme"
        private const val KEY_DEVICE_ID = "device_id"
    }
}

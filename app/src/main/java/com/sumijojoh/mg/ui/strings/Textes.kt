package com.sumijojoh.mg.ui.strings

import com.sumijojoh.mg.data.Langue

class Textes(val langue: Langue) {

    private fun t(fr: String, en: String): String =
        if (langue == Langue.FRANCAIS) fr else en

    val appName = "Sumi"
    val appSubtitle = t("Constat et calendrier", "Status and calendar")
    val emptyList = t("Aucune personne. Appuyez sur + pour ajouter.", "No people. Tap + to add.")
    val newPerson = t("Nouvelle personne", "New person")
    val rename = t("Renommer", "Rename")
    val delete = t("Supprimer", "Delete")
    val cancel = t("Annuler", "Cancel")
    val validate = t("Valider", "Confirm")
    val confirmDelete = t("Supprimer %s et son calendrier ?", "Delete %s and their calendar?")
    val settings = t("Paramètres", "Settings")
    val language = t("Langue", "Language")
    val french = "Français"
    val english = "English"
    val theme = t("Thème", "Theme")
    val light = t("Clair", "Light")
    val dark = t("Sombre", "Dark")
    val export = t("Exporter en JSON", "Export to JSON")
    val importJson = t("Importer depuis JSON", "Import from JSON")
    val scanQr = t("Scanner un QR code", "Scan a QR code")
    val shareQr = t("Partager cette personne (QR)", "Share this person (QR)")
    val qrTitle = t("QR code de %s", "QR code of %s")
    val qrTooLarge = t("Trop de données pour un QR. Réduisez la période.", "Too much data for a QR. Reduce the period.")
    val qrPreparation = t("Préparation du QR en cours. Réessayez dans quelques secondes.", "Preparing the QR. Try again in a few seconds.")
    val qrRetry = t("Réessayer", "Retry")
    val close = t("Fermer", "Close")
    val readOnly = t("Lecture seule", "Read only")
    val membersSection = t("Membres (lecture seule)", "Members (read only)")
    val realtimeOnline = t("En ligne", "Online")
    val realtimeOffline = t("Hors ligne", "Offline")
    val realtimeSyncing = t("Synchronisation...", "Syncing...")
    val missionTitle = t("Mission à partir du %s", "Mission starting %s")
    val missionEndLabel = t("Date de fin (JJ-MM-AAAA)", "End date (DD-MM-YYYY)")
    val motifLabel = t("Motif", "Reason")
    val invalidFormat = t("Format attendu : JJ-MM-AAAA", "Expected format: DD-MM-YYYY")
    val endBeforeStart = t("La date de fin doit être postérieure à la date de début", "End date must be after start date")
    val motifRequired = t("Le motif est obligatoire", "Reason is required")
    val deleteMissionTitle = t("Supprimer la mission ?", "Delete mission?")
    val deleteMissionBody = t("%s - %s (%s).\n\nTous les jours liés à cette mission repasseront en Bureau.", "%s - %s (%s).\n\nAll days linked to this mission will return to Office.")
    val missionsOfMonth = t("Missions du mois", "Missions of the month")
    val bureau = t("Bureau", "Office")
    val enConge = t("En congé", "On leave")
    val mission = t("Mission", "Mission")
}

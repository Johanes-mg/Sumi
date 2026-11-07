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
    val nameLabel = t("Nom", "Name")
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
    val syncTitle = t("Synchronisation automatique", "Automatic synchronization")
    val syncSubtitle = t("Le QR code active la synchronisation temps réel", "The QR code enables real-time sync")
    val syncTooLarge = t("Données trop volumineuses. Utilisez l'export JSON.", "Data too large. Use JSON export instead.")
    val syncNoData = t("Aucune personne à partager", "No people to share")
    val readOnly = t("Lecture seule", "Read only")
    val membersSection = t("Membres (lecture seule)", "Members (read only)")
    val scanError = t("QR code invalide", "Invalid QR code")
    val importError = t("Fichier invalide", "Invalid file")
    val exportSuccess = t("Export réussi", "Export successful")
    val exportError = t("Export échoué", "Export failed")
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

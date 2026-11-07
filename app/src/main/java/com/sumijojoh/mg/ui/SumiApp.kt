package com.sumijojoh.mg.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sumijojoh.mg.data.AppSettingsRepository
import com.sumijojoh.mg.data.DayState
import com.sumijojoh.mg.data.JsonExporter
import com.sumijojoh.mg.data.Langue
import com.sumijojoh.mg.data.MissionInfo
import com.sumijojoh.mg.data.Person
import com.sumijojoh.mg.data.PersonRepository
import com.sumijojoh.mg.data.PersonSource
import com.sumijojoh.mg.data.QrSync
import com.sumijojoh.mg.data.SyncClient
import com.sumijojoh.mg.data.ThemeMode
import com.sumijojoh.mg.ui.screens.PersonCalendarScreen
import com.sumijojoh.mg.ui.screens.PersonListScreen
import com.sumijojoh.mg.ui.screens.SettingsScreen
import com.sumijojoh.mg.ui.strings.Textes
import com.sumijojoh.mg.ui.theme.SumiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SumiApp(
    repository: PersonRepository,
    settingsRepository: AppSettingsRepository
) {
    var reglages by remember { mutableStateOf(settingsRepository.charger()) }
    var afficherParametres by remember { mutableStateOf(false) }
    val personnes = remember { mutableStateListOf<Person>().also { it.addAll(repository.charger()) } }
    var selectionId by remember { mutableStateOf<Long?>(null) }
    val textes = remember(reglages.langue) { Textes(reglages.langue) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var syncEnCours by remember { mutableStateOf(false) }
    var syncEnLigne by remember { mutableStateOf(true) }

    fun recharger() {
        personnes.clear()
        personnes.addAll(repository.charger())
    }

    fun serialiserMesDonnees(): String {
        val miennes = personnes.filter {
            it.source == PersonSource.LOCAL || it.source == PersonSource.JSON
        }.map { it.copy(proprietaireDeviceId = reglages.deviceId) }
        return JsonExporter.exporter(miennes)
    }

    fun fusionnerMesDonneesAvecDistant(distantJson: String?): String {
        val existantes = if (distantJson.isNullOrEmpty()) {
            emptyList()
        } else {
            runCatching { JsonExporter.importer(distantJson) }.getOrDefault(emptyList())
        }
        val miennes = personnes.filter {
            it.source == PersonSource.LOCAL || it.source == PersonSource.JSON
        }.map { it.copy(proprietaireDeviceId = reglages.deviceId) }
        val parProprietaire = LinkedHashMap<String, MutableList<Person>>()
        for (p in existantes) {
            if (p.proprietaireDeviceId == reglages.deviceId) continue
            parProprietaire.getOrPut(p.proprietaireDeviceId) { mutableListOf() }.add(p)
        }
        parProprietaire[reglages.deviceId] = miennes.toMutableList()
        val fusion = ArrayList<Person>()
        for (liste in parProprietaire.values) fusion.addAll(liste)
        return JsonExporter.exporter(fusion)
    }

    fun pousserMesDonnees() {
        val syncId = reglages.syncId ?: return
        scope.launch(Dispatchers.IO) {
            syncEnCours = true
            val distant = SyncClient.tirer(syncId)
            val fusion = fusionnerMesDonneesAvecDistant(distant)
            val ok = SyncClient.pousser(syncId, fusion)
            withContext(Dispatchers.Main) {
                syncEnLigne = ok
                syncEnCours = false
            }
        }
    }

    fun appliquerScan(importees: List<Person>, syncIdDepuisQR: String?) {
        val syncIdCible = syncIdDepuisQR ?: SyncClient.genererSyncId()
        val importeesFiltrees = importees.filter { it.proprietaireDeviceId != reglages.deviceId }
        repository.remplacerScans(importeesFiltrees)
        recharger()
        val maj = reglages.copy(syncId = syncIdCible)
        reglages = maj
        settingsRepository.sauvegarder(maj)
        pousserMesDonnees()
    }

    fun appliquerJson(importees: List<Person>) {
        if (importees.isEmpty()) return
        repository.ajouterJson(importees)
        recharger()
        pousserMesDonnees()
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contenu = result.contents
        if (contenu != null) {
            val decode = QrSync.decoderDepuisQR(contenu)
            if (decode != null) {
                appliquerScan(decode.personnes, decode.syncId)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = serialiserMesDonnees()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val contenu = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: return@runCatching
                val importees = JsonExporter.importer(contenu)
                appliquerJson(importees)
            }
        }
    }

    LaunchedEffect(reglages.syncId) {
        val syncId = reglages.syncId ?: return@LaunchedEffect
        while (true) {
            val enLigne = withContext(Dispatchers.IO) { SyncClient.testerConnexion() }
            if (!enLigne) {
                withContext(Dispatchers.Main) { syncEnLigne = false }
                delay(10000L)
                continue
            }
            withContext(Dispatchers.Main) { syncEnLigne = true }

            val contenu = withContext(Dispatchers.IO) { SyncClient.tirer(syncId) }
            withContext(Dispatchers.Main) {
                if (!contenu.isNullOrEmpty()) {
                    val tous = runCatching { JsonExporter.importer(contenu) }.getOrNull()
                    if (tous != null) {
                        val autres = tous.filter { it.proprietaireDeviceId != reglages.deviceId }
                        val actuelles = repository.charger().filter { it.source == PersonSource.SCAN }
                        val cleActuelle = actuelles.map { it.proprietaireDeviceId + "|" + it.nom + "|" + it.jours.size }.sorted()
                        val cleNouvelle = autres.map { it.proprietaireDeviceId + "|" + it.nom + "|" + it.jours.size }.sorted()
                        if (cleActuelle != cleNouvelle) {
                            repository.supprimerScans()
                            if (autres.isNotEmpty()) {
                                repository.remplacerScans(autres)
                            }
                            recharger()
                        }
                    }
                }
            }

            delay(5000L)
        }
    }

    fun changerLangue(langue: Langue) {
        val maj = reglages.copy(langue = langue)
        reglages = maj
        settingsRepository.sauvegarder(maj)
    }

    fun changerTheme(theme: ThemeMode) {
        val maj = reglages.copy(theme = theme)
        reglages = maj
        settingsRepository.sauvegarder(maj)
    }

    SumiTheme(themeMode = reglages.theme) {
        val courante = selectionId?.let { id -> personnes.firstOrNull { it.id == id } }

        when {
            afficherParametres -> {
                SettingsScreen(
                    reglages = reglages,
                    personnes = personnes,
                    textes = textes,
                    syncEnCours = syncEnCours,
                    syncEnLigne = syncEnLigne,
                    onRetour = { afficherParametres = false },
                    onChangerLangue = { changerLangue(it) },
                    onChangerTheme = { changerTheme(it) },
                    onExporter = {
                        exportLauncher.launch("sumi-export-${System.currentTimeMillis()}.json")
                    },
                    onImporter = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    onScanner = {
                        scanLauncher.launch(
                            ScanOptions()
                                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                .setPrompt(textes.scanQr)
                                .setBeepEnabled(false)
                                .setOrientationLocked(false)
                        )
                    }
                )
            }
            courante != null -> {
                PersonCalendarScreen(
                    personne = courante,
                    textes = textes,
                    onRetour = { selectionId = null },
                    onChangerJour = { dateKey, etat ->
                        val index = personnes.indexOfFirst { it.id == courante.id }
                        if (index >= 0) {
                            val p = personnes[index]
                            val nouveauxJours = HashMap<String, DayState>(p.jours.size + 1)
                            nouveauxJours.putAll(p.jours)
                            nouveauxJours[dateKey] = etat
                            personnes[index] = p.copy(jours = nouveauxJours)
                            repository.sauvegarder(personnes.toList())
                            pousserMesDonnees()
                        }
                    },
                    onAjouterMission = { mission ->
                        val index = personnes.indexOfFirst { it.id == courante.id }
                        if (index >= 0) {
                            val p = personnes[index]
                            val nouvellesMissions = ArrayList<MissionInfo>(p.missions.size + 1)
                            nouvellesMissions.addAll(p.missions)
                            nouvellesMissions.add(mission)
                            val joursMaj = HashMap<String, DayState>(p.jours.size + 32)
                            joursMaj.putAll(p.jours)
                            var curseur = mission.dateDebut
                            val fin = mission.dateFin
                            while (curseur <= fin) {
                                joursMaj[curseur] = DayState.MISSION
                                curseur = jourSuivant(curseur)
                            }
                            personnes[index] = p.copy(jours = joursMaj, missions = nouvellesMissions)
                            repository.sauvegarder(personnes.toList())
                            pousserMesDonnees()
                        }
                    },
                    onSupprimerMission = { mission ->
                        val index = personnes.indexOfFirst { it.id == courante.id }
                        if (index >= 0) {
                            val p = personnes[index]
                            val missionsMaj = p.missions.filterNot {
                                it.dateDebut == mission.dateDebut &&
                                it.dateFin == mission.dateFin &&
                                it.motif == mission.motif
                            }
                            val joursMaj = HashMap<String, DayState>(p.jours.size)
                            joursMaj.putAll(p.jours)
                            var curseur = mission.dateDebut
                            val fin = mission.dateFin
                            while (curseur <= fin) {
                                if (joursMaj[curseur] == DayState.MISSION) {
                                    joursMaj[curseur] = DayState.REPOS
                                }
                                curseur = jourSuivant(curseur)
                            }
                            personnes[index] = p.copy(jours = joursMaj, missions = missionsMaj)
                            repository.sauvegarder(personnes.toList())
                            pousserMesDonnees()
                        }
                    }
                )
            }
            else -> {
                PersonListScreen(
                    personnes = personnes,
                    textes = textes,
                    onOuvrir = { selectionId = it.id },
                    onAjouter = { nom ->
                        personnes.add(
                            Person(
                                repository.prochainId(),
                                nom,
                                emptyMap(),
                                emptyList(),
                                PersonSource.LOCAL,
                                reglages.deviceId
                            )
                        )
                        repository.sauvegarder(personnes.toList())
                        pousserMesDonnees()
                    },
                    onRenommer = { id, nouveauNom ->
                        val index = personnes.indexOfFirst { it.id == id }
                        if (index >= 0) {
                            personnes[index] = personnes[index].copy(nom = nouveauNom)
                            repository.sauvegarder(personnes.toList())
                            pousserMesDonnees()
                        }
                    },
                    onSupprimer = { id ->
                        val index = personnes.indexOfFirst { it.id == id }
                        if (index >= 0) {
                            personnes.removeAt(index)
                            repository.sauvegarder(personnes.toList())
                            pousserMesDonnees()
                        }
                        if (selectionId == id) selectionId = null
                    },
                    onOuvrirParametres = { afficherParametres = true }
                )
            }
        }
    }
}

private fun jourSuivant(dateKey: String): String {
    val annee = dateKey.substring(0, 4).toInt()
    val mois = dateKey.substring(5, 7).toInt()
    val jour = dateKey.substring(8, 10).toInt()
    val max = when (mois) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((annee % 4 == 0 && annee % 100 != 0) || (annee % 400 == 0)) 29 else 28
        else -> 30
    }
    if (jour < max) {
        val sb = StringBuilder(10)
        sb.append(annee).append('-')
        if (mois < 10) sb.append('0')
        sb.append(mois).append('-')
        val prochainJour = jour + 1
        if (prochainJour < 10) sb.append('0')
        sb.append(prochainJour)
        return sb.toString()
    }
    val prochainMois = if (mois == 12) 1 else mois + 1
    val prochaineAnnee = if (mois == 12) annee + 1 else annee
    val sb = StringBuilder(10)
    sb.append(prochaineAnnee).append('-')
    if (prochainMois < 10) sb.append('0')
    sb.append(prochainMois).append("-01")
    return sb.toString()
}

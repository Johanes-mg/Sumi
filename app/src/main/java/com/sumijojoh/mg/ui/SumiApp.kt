package com.sumijojoh.mg.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sumijojoh.mg.ui.screens.QrDialog
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
    var personneQrId by remember { mutableStateOf<Long?>(null) }
    val textes = remember(reglages.langue) { Textes(reglages.langue) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun recharger() {
        personnes.clear()
        personnes.addAll(repository.charger())
    }

    fun creerBinPersonne(p: Person) {
        if (p.syncId != null) return
        val deviceId = reglages.deviceId
        val avecProprio = p.copy(proprietaireDeviceId = deviceId, updatedAt = System.currentTimeMillis())
        scope.launch(Dispatchers.IO) {
            val json = JsonExporter.exporterUne(avecProprio)
            var binId: String? = null
            for (i in 0 until 3) {
                binId = SyncClient.creerBin(json)
                if (binId != null) break
                delay(2000L)
            }
            if (binId != null) {
                withContext(Dispatchers.Main) {
                    val miseAJour = avecProprio.copy(syncId = binId)
                    repository.mettreAJourPersonne(miseAJour)
                    recharger()
                }
            }
        }
    }

    fun pousserPersonne(p: Person) {
        val binId = p.syncId ?: return
        val deviceId = reglages.deviceId
        val miseAJour = p.copy(proprietaireDeviceId = deviceId, updatedAt = System.currentTimeMillis())
        scope.launch(Dispatchers.IO) {
            val json = JsonExporter.exporterUne(miseAJour)
            SyncClient.mettreAJourBin(binId, json)
        }
    }

    fun supprimerPersonneComplete(p: Person) {
        val binId = p.syncId
        scope.launch(Dispatchers.IO) {
            if (binId != null) SyncClient.supprimerBin(binId)
        }
        repository.supprimerPersonne(p.id)
        recharger()
        if (selectionId == p.id) selectionId = null
        if (personneQrId == p.id) personneQrId = null
    }

    fun appliquerScan(personneScannee: Person) {
        val deviceId = reglages.deviceId
        val reprise = personneScannee.copy(
            proprietaireDeviceId = deviceId,
            updatedAt = System.currentTimeMillis()
        )
        val existante = repository.charger().firstOrNull { it.syncId == personneScannee.syncId }
        if (existante != null) {
            repository.mettreAJourPersonne(reprise.copy(id = existante.id, source = PersonSource.SCAN))
        } else {
            repository.ajouterPersonne(reprise.copy(id = 0L, source = PersonSource.SCAN))
        }
        recharger()
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contenu = result.contents
        if (contenu != null) {
            val decode = QrSync.decoderDepuisQR(contenu)
            if (decode != null) {
                val p = decode.personne
                if (p != null) {
                    appliquerScan(p)
                }
            }
        }
    }

    fun lancerScan() {
        scanLauncher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(textes.scanQr)
                .setBeepEnabled(false)
                .setOrientationLocked(false)
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val snapshot = personnes.toList()
            scope.launch(Dispatchers.IO) {
                val json = JsonExporter.exporter(snapshot)
                withContext(Dispatchers.Main) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val contenu = runCatching {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                }.getOrNull()
                if (!contenu.isNullOrEmpty()) {
                    val importees = runCatching { JsonExporter.importer(contenu) }.getOrDefault(emptyList())
                    if (importees.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val deviceId = reglages.deviceId
                            for (p in importees) {
                                val nouvelle = p.copy(
                                    id = 0L,
                                    source = PersonSource.JSON,
                                    proprietaireDeviceId = deviceId,
                                    syncId = null,
                                    updatedAt = System.currentTimeMillis()
                                )
                                val creee = repository.ajouterPersonne(nouvelle)
                                creerBinPersonne(creee)
                            }
                            recharger()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val locales = repository.charger()
        for (p in locales) {
            if (p.syncId == null && p.source != PersonSource.SCAN) {
                creerBinPersonne(p)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = repository.charger()
            for (p in snapshot) {
                val binId = p.syncId
                if (binId == null) continue
                val lecture = withContext(Dispatchers.IO) { SyncClient.lireBin(binId) }
                if (lecture == null) continue
                val contenuLu = lecture.contenu
                if (contenuLu == null || contenuLu.isEmpty()) continue
                val distantes = runCatching { JsonExporter.importer(contenuLu) }.getOrNull() ?: continue
                val distante = distantes.firstOrNull() ?: continue
                if (distante.updatedAt > p.updatedAt) {
                    val locale = personnes.firstOrNull { it.id == p.id } ?: continue
                    val miseAJour = locale.copy(
                        nom = distante.nom,
                        jours = distante.jours,
                        missions = distante.missions,
                        updatedAt = distante.updatedAt
                    )
                    withContext(Dispatchers.Main) {
                        repository.mettreAJourPersonne(miseAJour)
                        recharger()
                    }
                }
            }
            delay(4000L)
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
                    textes = textes,
                    onRetour = { afficherParametres = false },
                    onChangerLangue = { changerLangue(it) },
                    onChangerTheme = { changerTheme(it) },
                    onExporter = {
                        exportLauncher.launch("sumi-export-${System.currentTimeMillis()}.json")
                    },
                    onImporter = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
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
                            val maj = p.copy(jours = nouveauxJours)
                            personnes[index] = maj
                            repository.mettreAJourPersonne(maj)
                            pousserPersonne(maj)
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
                            val maj = p.copy(jours = joursMaj, missions = nouvellesMissions)
                            personnes[index] = maj
                            repository.mettreAJourPersonne(maj)
                            pousserPersonne(maj)
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
                            val maj = p.copy(jours = joursMaj, missions = missionsMaj)
                            personnes[index] = maj
                            repository.mettreAJourPersonne(maj)
                            pousserPersonne(maj)
                        }
                    }
                )
            }
            else -> {
                PersonListScreen(
                    personnes = personnes,
                    textes = textes,
                    theme = reglages.theme,
                    onOuvrir = { selectionId = it.id },
                    onAjouter = { nom ->
                        val deviceId = reglages.deviceId
                        val nouvelle = Person(
                            id = 0L,
                            nom = nom,
                            jours = emptyMap(),
                            missions = emptyList(),
                            source = PersonSource.LOCAL,
                            proprietaireDeviceId = deviceId,
                            syncId = null,
                            updatedAt = System.currentTimeMillis()
                        )
                        val creee = repository.ajouterPersonne(nouvelle)
                        recharger()
                        creerBinPersonne(creee)
                    },
                    onRenommer = { id, nouveauNom ->
                        val index = personnes.indexOfFirst { it.id == id }
                        if (index >= 0) {
                            val maj = personnes[index].copy(nom = nouveauNom)
                            personnes[index] = maj
                            repository.mettreAJourPersonne(maj)
                            pousserPersonne(maj)
                        }
                    },
                    onSupprimer = { id ->
                        val p = personnes.firstOrNull { it.id == id }
                        if (p != null) supprimerPersonneComplete(p)
                    },
                    onSupprimerScan = { id ->
                        val p = personnes.firstOrNull { it.id == id }
                        if (p != null) supprimerPersonneComplete(p)
                    },
                    onAfficherQr = { personneQrId = it.id },
                    onScannerQr = { lancerScan() },
                    onOuvrirParametres = { afficherParametres = true }
                )
            }
        }

        val pQr = personneQrId?.let { id -> personnes.firstOrNull { it.id == id } }
        if (pQr != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                QrDialog(
                    personne = pQr,
                    textes = textes,
                    onFermer = { personneQrId = null },
                    onRetry = { creerBinPersonne(pQr) }
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

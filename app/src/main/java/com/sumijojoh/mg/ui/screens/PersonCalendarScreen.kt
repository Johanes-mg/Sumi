package com.sumijojoh.mg.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumijojoh.mg.data.DayState
import com.sumijojoh.mg.data.MissionInfo
import com.sumijojoh.mg.data.Person
import com.sumijojoh.mg.ui.strings.Textes
import com.sumijojoh.mg.ui.theme.JauneConge
import com.sumijojoh.mg.ui.theme.VertMission

@Composable
fun PersonCalendarScreen(
    personne: Person,
    textes: Textes,
    onRetour: () -> Unit,
    onChangerJour: (dateKey: String, etat: DayState) -> Unit,
    onAjouterMission: (MissionInfo) -> Unit,
    onSupprimerMission: (MissionInfo) -> Unit
) {
    BackHandler(enabled = true) { onRetour() }

    val lectureSeule = personne.lectureSeule()
    var moisKey by remember { mutableIntStateOf(moisCourantKey()) }
    var missionDebut by remember { mutableStateOf<String?>(null) }
    var missionASupprimer by remember { mutableStateOf<MissionInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onRetour() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "<",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = personne.nom,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )
            if (lectureSeule) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "(${textes.readOnly})",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoutonMois("<") { moisKey -= 1 }
            Text(
                text = libelleMoisInt(moisKey, textes),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            BoutonMois(">") { moisKey += 1 }
        }
        Spacer(Modifier.height(12.dp))

        val cles = remember(moisKey) { clesDuMois(moisKey) }
        val premierJour = remember(moisKey) { premierJourSemaine(moisKey) }
        val cellules: List<String?> = remember(moisKey, premierJour) {
            if (premierJour == 0) cles else List<String?>(premierJour) { null } + cles
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { j ->
                Text(
                    text = j,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(cellules) { clef ->
                if (clef == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val etat = personne.etatDuJour(clef)
                    val numero = clef.substring(8).toInt()
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(couleurCellule(etat))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .then(
                                if (lectureSeule) Modifier
                                else Modifier.clickable {
                                    when (etat) {
                                        DayState.REPOS -> onChangerJour(clef, DayState.CONGE)
                                        DayState.CONGE -> {
                                            onChangerJour(clef, DayState.MISSION)
                                            missionDebut = clef
                                        }
                                        DayState.MISSION -> {
                                            val m = personne.missions.firstOrNull {
                                                clef >= it.dateDebut && clef <= it.dateFin
                                            }
                                            if (m != null) missionASupprimer = m
                                            else onChangerJour(clef, DayState.REPOS)
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = numero.toString(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        BilanMois(personne = personne, jours = cles, textes = textes)
        Spacer(Modifier.height(8.dp))
        MissionsMois(personne = personne, jours = cles, textes = textes)
        Spacer(Modifier.height(16.dp))
    }

    if (!lectureSeule) {
        missionDebut?.let { debutInterne ->
            DialogueMissionCreation(
                dateDebutInterne = debutInterne,
                textes = textes,
                onValider = { finInterne, motif ->
                    onAjouterMission(MissionInfo(debutInterne, finInterne, motif))
                    missionDebut = null
                },
                onAnnuler = {
                    onChangerJour(debutInterne, DayState.REPOS)
                    missionDebut = null
                }
            )
        }

        missionASupprimer?.let { cible ->
            AlertDialog(
                onDismissRequest = { missionASupprimer = null },
                title = { Text(textes.deleteMissionTitle) },
                text = {
                    Text(
                        textes.deleteMissionBody.format(
                            formatAffichage(cible.dateDebut),
                            formatAffichage(cible.dateFin),
                            cible.motif
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val m = cible
                            missionASupprimer = null
                            onSupprimerMission(m)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                    ) { Text(textes.delete) }
                },
                dismissButton = {
                    TextButton(onClick = { missionASupprimer = null }) { Text(textes.cancel) }
                }
            )
        }
    }
}

@Composable
private fun couleurCellule(etat: DayState): Color = when (etat) {
    DayState.CONGE -> JauneConge
    DayState.MISSION -> VertMission
    DayState.REPOS -> MaterialTheme.colorScheme.surface
}

@Composable
private fun BilanMois(personne: Person, jours: List<String>, textes: Textes) {
    val compteurs = remember(personne.jours, jours) {
        var b = 0
        var c = 0
        var m = 0
        for (clef in jours) {
            when (personne.etatDuJour(clef)) {
                DayState.REPOS -> b++
                DayState.CONGE -> c++
                DayState.MISSION -> m++
            }
        }
        intArrayOf(b, c, m)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        LigneBilan(couleur = MaterialTheme.colorScheme.surface, libelle = textes.bureau, valeur = compteurs[0])
        Spacer(Modifier.height(8.dp))
        LigneBilan(couleur = JauneConge, libelle = textes.enConge, valeur = compteurs[1])
        Spacer(Modifier.height(8.dp))
        LigneBilan(couleur = VertMission, libelle = textes.mission, valeur = compteurs[2])
    }
}

@Composable
private fun MissionsMois(personne: Person, jours: List<String>, textes: Textes) {
    val missionsDuMois = remember(personne.missions, jours) {
        if (jours.isEmpty()) emptyList<MissionInfo>()
        else {
            val premier = jours.first()
            val dernier = jours.last()
            personne.missions.filter { it.dateDebut <= dernier && it.dateFin >= premier }
        }
    }
    if (missionsDuMois.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = textes.missionsOfMonth,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        for (m in missionsDuMois) {
            Text(
                text = "${formatAffichage(m.dateDebut)} - ${formatAffichage(m.dateFin)} (${m.motif})",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun LigneBilan(couleur: Color, libelle: String, valeur: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(couleur)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = libelle,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valeur.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DialogueMissionCreation(
    dateDebutInterne: String,
    textes: Textes,
    onValider: (finInterne: String, motif: String) -> Unit,
    onAnnuler: () -> Unit
) {
    var dateFinAffichage by remember { mutableStateOf(formatAffichage(dateDebutInterne)) }
    var motif by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(textes.missionTitle.format(formatAffichage(dateDebutInterne))) },
        text = {
            Column {
                Text(textes.missionEndLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                BasicTextField(
                    value = dateFinAffichage,
                    onValueChange = { dateFinAffichage = it; erreur = null },
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(textes.motifLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                BasicTextField(
                    value = motif,
                    onValueChange = { motif = it; erreur = null },
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                )
                erreur?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = Color(0xFFC62828), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finInterne = convertirAffichageVersInterne(dateFinAffichage)
                    if (finInterne == null) {
                        erreur = textes.invalidFormat
                    } else if (finInterne < dateDebutInterne) {
                        erreur = textes.endBeforeStart
                    } else if (motif.isBlank()) {
                        erreur = textes.motifRequired
                    } else {
                        onValider(finInterne, motif.trim())
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2F9E62))
            ) { Text(textes.validate) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text(textes.cancel) }
        }
    )
}

@Composable
private fun BoutonMois(texte: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onClick() }
    ) {
        Text(
            text = texte,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

private fun formatAffichage(interne: String): String {
    if (interne.length != 10) return interne
    return "${interne.substring(8, 10)}-${interne.substring(5, 7)}-${interne.substring(0, 4)}"
}

private fun convertirAffichageVersInterne(affichage: String): String? {
    val propre = affichage.trim()
    if (propre.length != 10) return null
    if (propre[2] != '-' || propre[5] != '-') return null
    val jj = propre.substring(0, 2).toIntOrNull() ?: return null
    val mm = propre.substring(3, 5).toIntOrNull() ?: return null
    val aaaa = propre.substring(6, 10).toIntOrNull() ?: return null
    if (mm !in 1..12) return null
    val max = maxJourDuMois(aaaa, mm)
    if (jj !in 1..max) return null
    val sb = StringBuilder(10)
    sb.append(aaaa).append('-')
    if (mm < 10) sb.append('0')
    sb.append(mm).append('-')
    if (jj < 10) sb.append('0')
    sb.append(jj)
    return sb.toString()
}

private fun moisCourantKey(): Int {
    val cal = java.util.Calendar.getInstance()
    return cal.get(java.util.Calendar.YEAR) * 12 + cal.get(java.util.Calendar.MONTH)
}

private fun libelleMoisInt(moisKey: Int, textes: Textes): String {
    val annee = moisKey / 12
    val mois = moisKey % 12
    val nomsFr = arrayOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )
    val nomsEn = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val noms = if (textes.langue == com.sumijojoh.mg.data.Langue.FRANCAIS) nomsFr else nomsEn
    return "${noms[mois]} $annee"
}

private fun clesDuMois(moisKey: Int): List<String> {
    val annee = moisKey / 12
    val moisZero = moisKey % 12
    val max = maxJourDuMois(annee, moisZero + 1)
    val resultat = ArrayList<String>(max)
    val sb = StringBuilder(10)
    for (jour in 1..max) {
        sb.setLength(0)
        sb.append(annee)
        sb.append('-')
        if (moisZero + 1 < 10) sb.append('0')
        sb.append(moisZero + 1)
        sb.append('-')
        if (jour < 10) sb.append('0')
        sb.append(jour)
        resultat.add(sb.toString())
    }
    return resultat
}

private fun maxJourDuMois(annee: Int, moisUnBased: Int): Int = when (moisUnBased) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (estBissextile(annee)) 29 else 28
    else -> 30
}

private fun estBissextile(annee: Int): Boolean =
    (annee % 4 == 0 && annee % 100 != 0) || (annee % 400 == 0)

private fun premierJourSemaine(moisKey: Int): Int {
    val annee = moisKey / 12
    val moisUn = (moisKey % 12) + 1
    val m = if (moisUn < 3) moisUn + 12 else moisUn
    val y = if (moisUn < 3) annee - 1 else annee
    val k = y % 100
    val j = y / 100
    val h = (1 + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    return (h + 5) % 7
}

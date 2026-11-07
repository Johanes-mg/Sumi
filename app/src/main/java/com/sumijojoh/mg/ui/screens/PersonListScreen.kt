package com.sumijojoh.mg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumijojoh.mg.R
import com.sumijojoh.mg.data.DayState
import com.sumijojoh.mg.data.Langue
import com.sumijojoh.mg.data.Person
import com.sumijojoh.mg.data.PersonSource
import com.sumijojoh.mg.ui.strings.Textes
import com.sumijojoh.mg.ui.theme.JauneConge
import com.sumijojoh.mg.ui.theme.VertMission
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonListScreen(
    personnes: List<Person>,
    textes: Textes,
    onOuvrir: (Person) -> Unit,
    onAjouter: (String) -> Unit,
    onRenommer: (Long, String) -> Unit,
    onSupprimer: (Long) -> Unit,
    onOuvrirParametres: () -> Unit
) {
    val aujourdHui = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    var dialogueAjout by remember { mutableStateOf(false) }
    var personneARenommer by remember { mutableStateOf<Person?>(null) }
    var personneASupprimer by remember { mutableStateOf<Person?>(null) }

    val editables = personnes.filter { it.source != PersonSource.SCAN }
    val scannees = personnes.filter { it.source == PersonSource.SCAN }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = textes.appName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = textes.appSubtitle,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onOuvrirParametres) {
                Image(
                    painter = painterResource(R.drawable.settings),
                    contentDescription = textes.settings,
                    modifier = Modifier.size(26.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { dialogueAjout = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        if (personnes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textes.emptyList,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(editables, key = { it.id }) { personne ->
                    val etat = personne.etatDuJour(aujourdHui)
                    CartePersonne(
                        personne = personne,
                        etat = etat,
                        textes = textes,
                        lectureSeule = false,
                        onClick = { onOuvrir(personne) },
                        onRenommer = { personneARenommer = personne },
                        onSupprimer = { personneASupprimer = personne }
                    )
                }
                if (scannees.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = textes.membersSection,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    items(scannees, key = { it.id }) { personne ->
                        val etat = personne.etatDuJour(aujourdHui)
                        CartePersonne(
                            personne = personne,
                            etat = etat,
                            textes = textes,
                            lectureSeule = true,
                            onClick = { onOuvrir(personne) },
                            onRenommer = null,
                            onSupprimer = null
                        )
                    }
                }
            }
        }
    }

    if (dialogueAjout) {
        DialogueNom(
            titre = textes.newPerson,
            valeurInitiale = "",
            textes = textes,
            onValider = { nom ->
                dialogueAjout = false
                if (nom.isNotBlank()) onAjouter(nom.trim())
            },
            onAnnuler = { dialogueAjout = false }
        )
    }

    personneARenommer?.let { cible ->
        DialogueNom(
            titre = textes.rename,
            valeurInitiale = cible.nom,
            textes = textes,
            onValider = { nom ->
                personneARenommer = null
                if (nom.isNotBlank()) onRenommer(cible.id, nom.trim())
            },
            onAnnuler = { personneARenommer = null }
        )
    }

    personneASupprimer?.let { cible ->
        AlertDialog(
            onDismissRequest = { personneASupprimer = null },
            title = { Text(textes.delete) },
            text = { Text(textes.confirmDelete.format(cible.nom)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = cible.id
                        personneASupprimer = null
                        onSupprimer(id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                ) { Text(textes.delete) }
            },
            dismissButton = {
                TextButton(onClick = { personneASupprimer = null }) { Text(textes.cancel) }
            }
        )
    }
}

@Composable
private fun CartePersonne(
    personne: Person,
    etat: DayState,
    textes: Textes,
    lectureSeule: Boolean,
    onClick: () -> Unit,
    onRenommer: (() -> Unit)?,
    onSupprimer: (() -> Unit)?
) {
    var menuOuvert by remember { mutableStateOf(false) }
    val couleurBordure = if (lectureSeule) {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline
    }
    val couleurTexte = if (lectureSeule) {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Box {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(couleurEtat(etat, lectureSeule))
                        .border(2.dp, couleurBordure, CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = personne.nom,
                            color = couleurTexte,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (personne.source == PersonSource.JSON) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "JSON",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = libelleEtat(etat, textes.langue),
                        color = couleurTexte.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
                if (onRenommer != null && onSupprimer != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { menuOuvert = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "...",
                            color = couleurTexte.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        if (menuOuvert && onRenommer != null && onSupprimer != null) {
            AlertDialog(
                onDismissRequest = { menuOuvert = false },
                title = { Text(personne.nom) },
                text = { Text("") },
                confirmButton = {
                    TextButton(
                        onClick = { menuOuvert = false; onRenommer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB8860B))
                    ) { Text(textes.rename) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { menuOuvert = false; onSupprimer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                    ) { Text(textes.delete) }
                }
            )
        }
    }
}

@Composable
private fun DialogueNom(
    titre: String,
    valeurInitiale: String,
    textes: Textes,
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit
) {
    var valeur by remember { mutableStateOf(valeurInitiale) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(titre) },
        text = {
            BasicTextField(
                value = valeur,
                onValueChange = { valeur = it },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onValider(valeur) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2F9E62))
            ) { Text(textes.validate) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text(textes.cancel) }
        }
    )
}

@Composable
fun couleurEtat(etat: DayState, attenue: Boolean = false): Color {
    val base = when (etat) {
        DayState.CONGE -> JauneConge
        DayState.MISSION -> VertMission
        DayState.REPOS -> MaterialTheme.colorScheme.surface
    }
    return if (attenue) base.copy(alpha = 0.55f) else base
}

fun libelleEtat(etat: DayState, langue: Langue): String = when (etat) {
    DayState.CONGE -> if (langue == Langue.FRANCAIS) "En congé" else "On leave"
    DayState.MISSION -> "Mission"
    DayState.REPOS -> if (langue == Langue.FRANCAIS) "Bureau" else "Office"
}

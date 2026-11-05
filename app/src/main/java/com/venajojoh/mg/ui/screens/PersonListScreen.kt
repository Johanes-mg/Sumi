package com.venajojoh.mg.ui.screens

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venajojoh.mg.data.DayState
import com.venajojoh.mg.data.Person
import com.venajojoh.mg.ui.theme.BordureNoire
import com.venajojoh.mg.ui.theme.Encre
import com.venajojoh.mg.ui.theme.Fond
import com.venajojoh.mg.ui.theme.JauneConge
import com.venajojoh.mg.ui.theme.VertTravail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonListScreen(
    personnes: List<Person>,
    onOuvrir: (Person) -> Unit,
    onAjouter: (String) -> Unit,
    onRenommer: (Long, String) -> Unit,
    onSupprimer: (Long) -> Unit
) {
    val aujourdHui = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    var dialogueAjout by remember { mutableStateOf(false) }
    var personneARenommer by remember { mutableStateOf<Person?>(null) }
    var personneASupprimer by remember { mutableStateOf<Person?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fond)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vena",
                    color = Encre,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Constat et calendrier",
                    color = Encre.copy(alpha = 0.55f),
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Encre)
                    .clickable { dialogueAjout = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
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
                    text = "Aucune personne. Appuyez sur + pour ajouter.",
                    color = Encre.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(personnes, key = { it.id }) { personne ->
                    val etat = personne.etatDuJour(aujourdHui)
                    CartePersonne(
                        personne = personne,
                        etat = etat,
                        onClick = { onOuvrir(personne) },
                        onRenommer = { personneARenommer = personne },
                        onSupprimer = { personneASupprimer = personne }
                    )
                }
            }
        }
    }

    if (dialogueAjout) {
        DialogueNom(
            titre = "Nouvelle personne",
            valeurInitiale = "",
            onValider = { nom ->
                dialogueAjout = false
                if (nom.isNotBlank()) onAjouter(nom.trim())
            },
            onAnnuler = { dialogueAjout = false }
        )
    }

    personneARenommer?.let { cible ->
        DialogueNom(
            titre = "Renommer",
            valeurInitiale = cible.nom,
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
            title = { Text("Supprimer") },
            text = { Text("Supprimer ${cible.nom} et son calendrier ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = cible.id
                        personneASupprimer = null
                        onSupprimer(id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { personneASupprimer = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun CartePersonne(
    personne: Person,
    etat: DayState,
    onClick: () -> Unit,
    onRenommer: () -> Unit,
    onSupprimer: () -> Unit
) {
    var menuOuvert by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
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
                        .background(couleurEtat(etat))
                        .border(2.dp, BordureNoire, CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = personne.nom,
                        color = Encre,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = libelleEtat(etat),
                        color = Encre.copy(alpha = 0.55f),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { menuOuvert = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "...",
                        color = Encre.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (menuOuvert) {
            AlertDialog(
                onDismissRequest = { menuOuvert = false },
                title = { Text(personne.nom) },
                text = { Text("Que faire ?") },
                confirmButton = {
                    TextButton(
                        onClick = { menuOuvert = false; onRenommer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB8860B))
                    ) {
                        Text("Renommer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { menuOuvert = false; onSupprimer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Text("Supprimer")
                    }
                }
            )
        }
    }
}

@Composable
private fun DialogueNom(
    titre: String,
    valeurInitiale: String,
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
                textStyle = TextStyle(color = Encre, fontSize = 16.sp),
                cursorBrush = SolidColor(Encre),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0EEE7))
                    .padding(14.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onValider(valeur) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2F9E62))
            ) { Text("Valider") }
        },
        dismissButton = {
            TextButton(onClick = { onAnnuler() }) { Text("Annuler") }
        }
    )
}

fun couleurEtat(etat: DayState): Color = when (etat) {
    DayState.CONGE -> JauneConge
    DayState.TRAVAIL -> VertTravail
    DayState.REPOS -> Color.White
}

fun libelleEtat(etat: DayState): String = when (etat) {
    DayState.CONGE -> "En congé"
    DayState.TRAVAIL -> "Au travail"
    DayState.REPOS -> "Libre"
}

package com.venajojoh.mg.ui.screens

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venajojoh.mg.data.DayState
import com.venajojoh.mg.data.Person
import com.venajojoh.mg.ui.theme.BordureNoire
import com.venajojoh.mg.ui.theme.Encre
import com.venajojoh.mg.ui.theme.Fond

@Composable
fun PersonCalendarScreen(
    personne: Person,
    onRetour: () -> Unit,
    onChangerJour: (dateKey: String, etat: DayState) -> Unit
) {
    BackHandler(enabled = true) { onRetour() }

    var moisKey by remember { mutableIntStateOf(moisCourantKey()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fond)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, BordureNoire, CircleShape)
                    .clickable { onRetour() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "<",
                    color = Encre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = personne.nom,
                color = Encre,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoutonMois("<") { moisKey -= 1 }
            Text(
                text = libelleMoisInt(moisKey),
                color = Encre,
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
                    color = Encre.copy(alpha = 0.5f),
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
                            .background(couleurEtat(etat))
                            .border(1.5.dp, BordureNoire, RoundedCornerShape(10.dp))
                            .clickable {
                                onChangerJour(clef, prochainEtat(etat))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = numero.toString(),
                            color = Encre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        BilanMois(personne = personne, jours = cles)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BilanMois(personne: Person, jours: List<String>) {
    val compteurs = remember(personne.jours, jours) {
        var v = 0
        var j = 0
        var b = 0
        for (clef in jours) {
            when (personne.etatDuJour(clef)) {
                DayState.TRAVAIL -> v++
                DayState.CONGE -> j++
                DayState.REPOS -> b++
            }
        }
        intArrayOf(v, j, b)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(2.dp, BordureNoire, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        LigneBilan(couleur = Color(0xFF2F9E62), libelle = "Au travail", valeur = compteurs[0])
        Spacer(Modifier.height(8.dp))
        LigneBilan(couleur = Color(0xFFF2C94C), libelle = "En congé", valeur = compteurs[1])
        Spacer(Modifier.height(8.dp))
        LigneBilan(couleur = Color.White, libelle = "Libre", valeur = compteurs[2])
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
                .border(1.5.dp, BordureNoire, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = libelle,
            color = Encre,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valeur.toString(),
            color = Encre,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BoutonMois(texte: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.5.dp, BordureNoire, CircleShape)
            .clickable { onClick() }
    ) {
        Text(
            text = texte,
            color = Encre,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

private fun prochainEtat(actuel: DayState): DayState = when (actuel) {
    DayState.REPOS -> DayState.CONGE
    DayState.CONGE -> DayState.TRAVAIL
    DayState.TRAVAIL -> DayState.REPOS
}

private fun moisCourantKey(): Int {
    val cal = java.util.Calendar.getInstance()
    return cal.get(java.util.Calendar.YEAR) * 12 + cal.get(java.util.Calendar.MONTH)
}

private fun libelleMoisInt(moisKey: Int): String {
    val annee = moisKey / 12
    val mois = moisKey % 12
    val noms = arrayOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )
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
    val moisZero = moisKey % 12
    val m = moisZero + 1
    val y = if (m <= 2) annee - 1 else annee
    val k = y % 100
    val j = y / 100
    val h = (1 + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    val dowDimancheZero = h
    return (dowDimancheZero + 6) % 7
}

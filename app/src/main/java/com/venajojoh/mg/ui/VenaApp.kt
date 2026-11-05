package com.venajojoh.mg.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.venajojoh.mg.data.Person
import com.venajojoh.mg.data.PersonRepository
import com.venajojoh.mg.ui.screens.PersonCalendarScreen
import com.venajojoh.mg.ui.screens.PersonListScreen
import com.venajojoh.mg.ui.theme.VenaTheme

@Composable
fun VenaApp(repository: PersonRepository) {
    VenaTheme {
        val personnes = remember { mutableStateListOf<Person>().also { it.addAll(repository.charger()) } }
        var selectionId by remember { mutableStateOf<Long?>(null) }

        fun persister() {
            repository.sauvegarder(personnes.toList())
        }

        val courante = selectionId?.let { id -> personnes.firstOrNull { it.id == id } }

        if (courante == null) {
            PersonListScreen(
                personnes = personnes,
                onOuvrir = { selectionId = it.id },
                onAjouter = { nom ->
                    personnes.add(Person(repository.prochainId(), nom, emptyMap()))
                    persister()
                },
                onRenommer = { id, nouveauNom ->
                    val index = personnes.indexOfFirst { it.id == id }
                    if (index >= 0) {
                        personnes[index] = personnes[index].copy(nom = nouveauNom)
                        persister()
                    }
                },
                onSupprimer = { id ->
                    val index = personnes.indexOfFirst { it.id == id }
                    if (index >= 0) {
                        personnes.removeAt(index)
                        persister()
                    }
                    if (selectionId == id) selectionId = null
                }
            )
        } else {
            PersonCalendarScreen(
                personne = courante,
                onRetour = { selectionId = null },
                onChangerJour = { dateKey, etat ->
                    val index = personnes.indexOfFirst { it.id == courante.id }
                    if (index >= 0) {
                        val p = personnes[index]
                        val nouveauxJours = HashMap<String, com.venajojoh.mg.data.DayState>(p.jours.size + 1)
                        nouveauxJours.putAll(p.jours)
                        nouveauxJours[dateKey] = etat
                        personnes[index] = p.copy(jours = nouveauxJours)
                        persister()
                    }
                }
            )
        }
    }
}

package com.sumijojoh.mg.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumijojoh.mg.data.AppSettings
import com.sumijojoh.mg.data.Langue
import com.sumijojoh.mg.data.Person
import com.sumijojoh.mg.data.QrSync
import com.sumijojoh.mg.data.ThemeMode
import com.sumijojoh.mg.ui.strings.Textes

@Composable
fun SettingsScreen(
    reglages: AppSettings,
    personnes: List<Person>,
    textes: Textes,
    syncEnCours: Boolean,
    syncEnLigne: Boolean,
    onRetour: () -> Unit,
    onChangerLangue: (Langue) -> Unit,
    onChangerTheme: (ThemeMode) -> Unit,
    onExporter: () -> Unit,
    onImporter: () -> Unit,
    onScanner: () -> Unit
) {
    BackHandler(enabled = true) { onRetour() }

    val qrBitmap: Bitmap? = remember(personnes, reglages.syncId) {
        if (personnes.isEmpty()) null
        else QrSync.genererBitmap(personnes, reglages.syncId, 600)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
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
                text = textes.settings,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        Text(
            text = textes.syncTitle,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = textes.syncSubtitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    personnes.isEmpty() -> Text(
                        text = textes.syncNoData,
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp
                    )
                    qrBitmap == null -> Text(
                        text = textes.syncTooLarge,
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                    else -> Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = textes.syncTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        BoutonAction(
            texte = textes.scanQr,
            onClick = onScanner
        )

        if (reglages.syncId != null) {
            Spacer(Modifier.height(6.dp))
            val statut = when {
                !syncEnLigne -> textes.realtimeOffline
                syncEnCours -> textes.realtimeSyncing
                else -> textes.realtimeOnline
            }
            Text(
                text = statut,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle(textes.language)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChoixChip(
                texte = textes.french,
                actif = reglages.langue == Langue.FRANCAIS,
                onClick = { onChangerLangue(Langue.FRANCAIS) },
                modifier = Modifier.weight(1f)
            )
            ChoixChip(
                texte = textes.english,
                actif = reglages.langue == Langue.ANGLAIS,
                onClick = { onChangerLangue(Langue.ANGLAIS) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(textes.theme)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChoixChip(
                texte = textes.light,
                actif = reglages.theme == ThemeMode.CLAIR,
                onClick = { onChangerTheme(ThemeMode.CLAIR) },
                modifier = Modifier.weight(1f)
            )
            ChoixChip(
                texte = textes.dark,
                actif = reglages.theme == ThemeMode.SOMBRE,
                onClick = { onChangerTheme(ThemeMode.SOMBRE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("JSON")
        BoutonAction(texte = textes.export, onClick = onExporter)
        Spacer(Modifier.height(8.dp))
        BoutonAction(texte = textes.importJson, onClick = onImporter)

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SectionTitle(texte: String) {
    Text(
        text = texte,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun BoutonAction(texte: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = texte,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp)
        )
    }
}

@Composable
private fun ChoixChip(
    texte: String,
    actif: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (actif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (actif) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Text(
            text = texte,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

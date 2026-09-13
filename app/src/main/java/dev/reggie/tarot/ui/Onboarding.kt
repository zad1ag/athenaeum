package dev.reggie.tarot.ui

import android.content.Intent
import dev.reggie.tarot.R
import dev.reggie.tarot.TarotApp
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val ONBOARDING_PAGES = 5
private val lastPageIndex = ONBOARDING_PAGES - 1

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }

    val saveName: () -> Unit = {
        if (name.isNotBlank()) TarotApp.core.setUserName(name.trim())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (page < lastPageIndex) {
            TextButton(
                onClick = { page = lastPageIndex },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Text("Skip")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (page) {
                    0 -> WelcomeSlide(name) { name = it }
                    1 -> TourSlide()
                    2 -> CustomizeSlide()
                    3 -> PrivacySlide()
                    else -> SupportSlide(
                        onKoFi = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/zad1ag"))
                            )
                        }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ONBOARDING_PAGES) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == page) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (page > 0) {
                    TextButton(onClick = { page-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(72.dp))
                }
                Button(
                    onClick = {
                        if (page < lastPageIndex) {
                            page++
                        } else {
                            saveName()
                            onFinish()
                        }
                    }
                ) {
                    Text(if (page < lastPageIndex) "Next" else "Begin")
                }
            }
        }
    }
}

@Composable
private fun WelcomeSlide(name: String, onNameChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            painter = painterResource(R.drawable.ic_moon_first_quarter),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = "Athenaeum",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "made by zad1ag with love in Iceland",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "Your companion for tarot, astrology, runes, spirits, and more.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("A name, a nickname, a craft-name\u2026") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun TourSlide() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "What's inside",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        TourRow(R.drawable.ic_onboarding_tarot_fool, "Tarot", "Draws, spreads, and every card's meanings - all editable.")
        TourRow(R.drawable.ic_moon_first_quarter, "Astrology", "Today's sun and moon signs, planetary days, the zodiac.")
        TourRow(R.drawable.ic_onboarding_magic, "Magic", "Spells, Icelandic staves, and the Elder Futhark runes.")
        TourRow(R.drawable.ic_skull, "Spirits", "Entities, angels, and deities of many pantheons.")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Trim or disable anything in Settings.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TourRow(iconRes: Int, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomizeSlide() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Make it yours",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Bullet("Everything is editable - card meanings, rune entries, spirit profiles.")
        Bullet("Add your own spells, spirits, and deities from the + buttons.")
        Bullet("Don't use a section? Turn it off in Settings \u2192 Visible sections.")
        Bullet("Back everything up - one file, any app or drive you like.")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The built-in content is a starting point, not gospel.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Bullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PrivacySlide() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            painter = painterResource(R.drawable.ic_entity_wraith),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "Yours alone",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Bullet("Everything stays on this device.")
        Bullet("No account. No analytics. No tracking. Ever.")
        Bullet("For reflection and tradition - not medical, legal, or financial advice.")
    }
}

@Composable
private fun SupportSlide(onKoFi: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            painter = painterResource(R.drawable.ic_kofi_coffee),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "End up enjoying it?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "This app is a labour of love by one independent developer. If it earns its keep, a coffee keeps the candle lit.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onKoFi,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Support on Ko-fi")
        }
        Text(
            text = "There's no paywall and feature-locked nagging - just a small ask, if and when you feel like it.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 8.dp)
        )
    }
}
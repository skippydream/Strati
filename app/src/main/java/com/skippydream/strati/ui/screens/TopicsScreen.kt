package com.skippydream.strati.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skippydream.strati.R
import com.skippydream.strati.data.Topics
import com.skippydream.strati.ui.components.ExpandableCard
import com.skippydream.strati.ui.components.LanguageSwitcher
import com.skippydream.strati.ui.components.TopicCard

private const val GITHUB_URL = "https://www.github.com/skippydream/Strati"
private const val SUGGESTIONS_URL = "https://tally.so/r/mO2pBp"
private const val PAYPAL_URL = "https://paypal.me/michelelana"

@Composable
fun TopicsScreen(
    onTopicSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "language") { LanguageSwitcher() }

        item(key = "instructions") { InstructionsCard() }

        items(items = Topics.all, key = { it.id }) { topic ->
            TopicCard(topic = topic, onClick = { onTopicSelected(topic.id) })
        }

        item(key = "links") {
            ProjectLinksCard(
                onGitHub = { uriHandler.openUri(GITHUB_URL) },
                onSuggest = { uriHandler.openUri(SUGGESTIONS_URL) },
                onDonate = { uriHandler.openUri(PAYPAL_URL) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "footer") {
            Text(
                text = stringResource(R.string.footer_text),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InstructionsCard(modifier: Modifier = Modifier) {
    ExpandableCard(
        title = stringResource(R.string.instructions_title),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.instructions_how),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.instructions_layers, MAX_SKIPS),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.instructions_timer, REFLECTION_SECONDS),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ProjectLinksCard(
    onGitHub: () -> Unit,
    onSuggest: () -> Unit,
    onDonate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column {
            LinkRow(
                icon = Icons.Default.Code,
                text = stringResource(R.string.link_github),
                onClick = onGitHub,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            LinkRow(
                icon = Icons.Default.Lightbulb,
                text = stringResource(R.string.submit_form),
                onClick = onSuggest,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            LinkRow(
                icon = Icons.Default.LocalCafe,
                text = stringResource(R.string.donate_link),
                onClick = onDonate,
            )
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val opensInBrowser = stringResource(R.string.cd_opens_in_browser)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = opensInBrowser,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

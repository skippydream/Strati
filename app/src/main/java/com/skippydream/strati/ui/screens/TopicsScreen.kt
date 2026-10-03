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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.skippydream.strati.data.ProgressStore
import com.skippydream.strati.ui.components.StratiMark
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
    var askingReset by rememberSaveable { mutableStateOf(false) }

    if (askingReset) {
        ResetDialog(
            onConfirm = {
                ProgressStore.reset()
                askingReset = false
            },
            onDismiss = { askingReset = false },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "header") { AppHeader(onReset = { askingReset = true }) }

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
private fun AppHeader(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StratiMark(modifier = Modifier.size(34.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.width(16.dp))
        FilledTonalIconButton(
            onClick = onReset,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.reset_cards),
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        LanguageSwitcher()
    }
}

@Composable
private fun ResetDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.reset_dialog_title)) },
        text = { Text(text = stringResource(R.string.reset_dialog_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.reset_cancel))
            }
        },
    )
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
            text = stringResource(R.string.instructions_deck),
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

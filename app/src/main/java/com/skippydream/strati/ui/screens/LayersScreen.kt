package com.skippydream.strati.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skippydream.strati.R
import com.skippydream.strati.data.Topics
import com.skippydream.strati.ui.components.ErrorState
import com.skippydream.strati.ui.components.LayerCard

@Composable
fun LayersScreen(
    topicId: String?,
    onLayerSelected: (String, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topic = remember(topicId) { Topics.topic(topicId) }

    if (topic == null) {
        ErrorState(
            message = stringResource(R.string.error_invalid_topic),
            onBack = onBack,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(topic.nameRes),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        topic.layers.forEach { layer ->
            LayerCard(
                topicId = topic.id,
                layer = layer,
                onClick = { onLayerSelected(topic.id, layer.id) },
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onBack) {
            Text(text = stringResource(R.string.layers_screen_back_button))
        }
    }
}

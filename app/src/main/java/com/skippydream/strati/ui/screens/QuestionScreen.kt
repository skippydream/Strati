package com.skippydream.strati.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skippydream.strati.R
import com.skippydream.strati.data.ProgressStore
import com.skippydream.strati.data.QuestionsRepository
import com.skippydream.strati.data.Topics
import com.skippydream.strati.ui.components.ErrorState
import kotlin.random.Random

/** Tempo minimo di riflessione fra una domanda e la successiva. */
internal const val REFLECTION_SECONDS = 30

/** Quante domande si possono passare per strato. */
internal const val MAX_SKIPS = 3

private const val REFLECTION_MILLIS = REFLECTION_SECONDS * 1_000L
private const val NOT_STARTED = -1

/**
 * Seme dello shuffle e indice bastano a ricostruire l'ordine esatto delle domande, e la
 * scadenza del timer e' assoluta: da qui rotazione e process death non azzerano la partita.
 */
@Composable
fun QuestionScreen(
    topicId: String?,
    layerId: Int,
    onBack: () -> Unit,
    onGoToLayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topic = remember(topicId) { Topics.topic(topicId) }
    val layer = remember(topicId, layerId) { Topics.layer(topicId, layerId) }
    val nextLayer = remember(topicId, layerId) { Topics.layer(topicId, layerId + 1) }

    if (topic == null || layer == null) {
        ErrorState(
            message = stringResource(
                if (topic == null) R.string.error_invalid_topic else R.string.error_invalid_layer
            ),
            onBack = onBack,
            modifier = modifier,
        )
        return
    }

    val resources = LocalResources.current

    // null = ancora in caricamento, lista vuota = strato senza domande.
    var questions by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(layer.questionsRes) {
        questions = QuestionsRepository.load(resources, layer.questionsRes)
    }

    var seed by rememberSaveable { mutableLongStateOf(Random.nextLong()) }
    var index by rememberSaveable { mutableIntStateOf(NOT_STARTED) }
    var deadline by rememberSaveable { mutableLongStateOf(0L) }
    var skipsLeft by rememberSaveable { mutableIntStateOf(MAX_SKIPS) }

    val order = remember(questions, seed) {
        questions?.indices?.shuffled(Random(seed)).orEmpty()
    }

    // `progress` cambia a ogni frame ma si legge solo dentro una lambda: invalida il
    // disegno dell'anello, non la composizione. `secondsLeft` serve solo a TalkBack.
    var isWaiting by remember { mutableStateOf(false) }
    val progress = remember { mutableFloatStateOf(1f) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    LaunchedEffect(deadline) {
        if (SystemClock.elapsedRealtime() >= deadline) {
            isWaiting = false
            progress.floatValue = 1f
            secondsLeft = 0
            return@LaunchedEffect
        }
        isWaiting = true
        while (true) {
            val left = deadline - SystemClock.elapsedRealtime()
            if (left <= 0) break
            progress.floatValue = 1f - (left.toFloat() / REFLECTION_MILLIS).coerceIn(0f, 1f)
            val seconds = ((left + 999) / 1_000).toInt()
            if (seconds != secondsLeft) secondsLeft = seconds
            withFrameMillis { }
        }
        progress.floatValue = 1f
        secondsLeft = 0
        isWaiting = false
    }

    val started = index != NOT_STARTED
    val finished = started && index >= order.size

    LaunchedEffect(finished) {
        if (finished) ProgressStore.markCompleted(topic.id, layer.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SessionHeader(
            topicName = stringResource(topic.nameRes),
            layerTitle = stringResource(R.string.layer_title, layer.id, stringResource(layer.nameRes)),
            position = if (started && !finished) index + 1 else null,
            total = order.size,
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    questions == null -> CircularProgressIndicator()
                    order.isEmpty() -> QuestionText(stringResource(R.string.error_no_questions))
                    finished -> LayerCompleted()
                    else -> AnimatedContent(
                        targetState = if (started) questions?.getOrNull(order[index]).orEmpty() else "",
                        transitionSpec = {
                            fadeIn(tween(durationMillis = 240)) togetherWith
                                fadeOut(tween(durationMillis = 160))
                        },
                        label = "question",
                    ) { text ->
                        QuestionText(
                            text = text.ifEmpty { stringResource(R.string.question_prompt_not_started) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.heightIn(min = 180.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (finished) {
                CompletedActions(
                    nextLayerId = nextLayer?.id,
                    onGoToLayer = onGoToLayer,
                    onReplay = {
                        seed = Random.nextLong()
                        index = 0
                        skipsLeft = MAX_SKIPS
                        deadline = SystemClock.elapsedRealtime() + REFLECTION_MILLIS
                    },
                )
            } else {
                SessionControls(
                    started = started,
                    enabled = order.isNotEmpty() && !isWaiting,
                    showSkip = started && order.isNotEmpty(),
                    skipsLeft = skipsLeft,
                    progress = { progress.floatValue },
                    timeDescription = pluralStringResource(
                        R.plurals.cd_time_left, secondsLeft, secondsLeft
                    ).takeIf { isWaiting },
                    onNext = {
                        index += 1
                        deadline = SystemClock.elapsedRealtime() + REFLECTION_MILLIS
                    },
                    onSkip = {
                        skipsLeft -= 1
                        index += 1
                        deadline = SystemClock.elapsedRealtime() + REFLECTION_MILLIS
                    },
                )
            }
        }
    }
}

@Composable
private fun SessionHeader(
    topicName: String,
    layerTitle: String,
    position: Int?,
    total: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.question_screen_back_button),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layerTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = topicName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (position != null) {
            val description = stringResource(R.string.cd_question_counter, position, total)
            Text(
                text = stringResource(R.string.question_counter, position, total),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

@Composable
private fun QuestionText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun LayerCompleted(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.layer_completed_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.end_of_layer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CompletedActions(
    nextLayerId: Int?,
    onGoToLayer: (Int) -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (nextLayerId != null) {
            Button(
                onClick = { onGoToLayer(nextLayerId) },
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.button_next_layer, nextLayerId),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onReplay) {
                Text(text = stringResource(R.string.button_replay_layer))
            }
        } else {
            Button(
                onClick = onReplay,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.button_replay_layer),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun SessionControls(
    started: Boolean,
    enabled: Boolean,
    showSkip: Boolean,
    skipsLeft: Int,
    progress: () -> Float,
    timeDescription: String?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (timeDescription != null) {
                Modifier.semantics { stateDescription = timeDescription }
            } else {
                Modifier
            },
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .size(132.dp)
                    .clearAndSetSemantics { },
                color = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 5.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Button(
                onClick = onNext,
                enabled = enabled,
                shape = CircleShape,
                modifier = Modifier.size(108.dp),
                contentPadding = PaddingValues(8.dp),
            ) {
                Text(
                    text = stringResource(
                        if (started) R.string.button_next else R.string.button_start
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lo Spacer tiene l'altezza costante: senza, la card salta quando compare il pulsante.
        if (showSkip) {
            val skipDescription = pluralStringResource(
                R.plurals.cd_skips_left, skipsLeft, skipsLeft
            )
            TextButton(
                onClick = onSkip,
                enabled = skipsLeft > 0,
                modifier = Modifier.semantics { stateDescription = skipDescription },
            ) {
                Text(text = stringResource(R.string.button_skip, skipsLeft))
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

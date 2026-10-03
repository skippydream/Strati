package com.skippydream.strati.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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
import com.skippydream.strati.data.DeckState
import com.skippydream.strati.data.ProgressStore
import com.skippydream.strati.data.QuestionsRepository
import com.skippydream.strati.data.Topics
import com.skippydream.strati.ui.components.ErrorState
import com.skippydream.strati.ui.components.StratiMark
import kotlin.random.Random

/** Tempo minimo di riflessione fra una domanda e la successiva. */
internal const val REFLECTION_SECONDS = 30

/** Quante domande si possono passare per strato. */
internal const val MAX_SKIPS = 3

private const val REFLECTION_MILLIS = REFLECTION_SECONDS * 1_000L
private const val NOT_STARTED = -1

// Spazio lasciato sopra la carta in mano perche' la pila ci sbuchi dentro.
private const val DECK_TOP = 34

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

    // Il mazzo riprende da dove lo avevi lasciato: le carte pescate restano fuori
    // finche' non rimescoli dalla home o non rigiochi lo strato.
    val saved = remember(topicId, layerId) { ProgressStore.deck(topic.id, layer.id) }
    var seed by rememberSaveable { mutableLongStateOf(saved?.seed ?: Random.nextLong()) }
    var index by rememberSaveable { mutableIntStateOf(saved?.drawn ?: NOT_STARTED) }
    var skipsLeft by rememberSaveable { mutableIntStateOf(saved?.skipsLeft ?: MAX_SKIPS) }
    var deadline by rememberSaveable { mutableLongStateOf(0L) }

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

    LaunchedEffect(seed, index, skipsLeft, order.size) {
        if (index != NOT_STARTED && order.isNotEmpty()) {
            ProgressStore.saveDeck(
                topicId = topic.id,
                layerId = layer.id,
                state = DeckState(seed, index, skipsLeft, order.size),
            )
        }
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

        Deck(
            depth = deckDepth(order.size, index, finished),
            faceUp = started,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                questions == null -> CircularProgressIndicator()
                order.isEmpty() -> QuestionText(stringResource(R.string.error_no_questions))
                else -> AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        // La carta nuova arriva dall'alto della pila.
                        (
                            fadeIn(tween(260)) +
                                slideInVertically(tween(260)) { -it / 14 } +
                                scaleIn(tween(260), initialScale = 0.97f)
                            ) togetherWith (
                            fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 1.02f)
                            )
                    },
                    label = "card",
                ) { drawn ->
                    when {
                        drawn == NOT_STARTED -> CardBack()
                        drawn >= order.size -> LayerCompleted()
                        else -> QuestionText(questions?.getOrNull(order[drawn]).orEmpty())
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

/** Quante carte restano visibili dietro a quella in mano: la pila cala man mano. */
private fun deckDepth(total: Int, index: Int, finished: Boolean): Int {
    if (total == 0 || finished) return 0
    val remaining = total - index.coerceAtLeast(0)
    if (remaining <= 1) return 0
    val fraction = remaining.toFloat() / total
    return when {
        fraction > 0.66f -> 3
        fraction > 0.33f -> 2
        else -> 1
    }
}

/**
 * La carta in mano, con la pila delle rimanenti che sbuca da sopra.
 *
 * Le carte dietro sono coperte e sfalsate verso l'alto; quella davanti e' sempre alla
 * stessa altezza, cosi' il testo non si sposta quando il mazzo si assottiglia.
 */
@Composable
private fun Deck(
    depth: Int,
    faceUp: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val faceColor by animateColorAsState(
        targetValue = if (faceUp) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 280),
        label = "faceColor",
    )

    val backColor = MaterialTheme.colorScheme.primaryContainer
    val background = MaterialTheme.colorScheme.background

    Box(modifier = modifier) {
        // Ogni carta dietro e' piu' stretta, piu' alta, inclinata di poco e piu' vicina
        // al colore dello sfondo: e' la distanza a farle sembrare dietro, non un bordo.
        for (behind in depth downTo 1) {
            val lift = (behind * 7).dp
            val tilt = if (behind % 2 == 0) 0.9f + behind * 0.8f else -(0.9f + behind * 0.8f)

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = DECK_TOP.dp)
                    .graphicsLayer {
                        translationY = -lift.toPx()
                        rotationZ = tilt
                        scaleX = 1f - behind * 0.035f
                    },
                colors = CardDefaults.cardColors(
                    containerColor = lerp(backColor, background, behind * 0.17f),
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = (depth - behind + 3).dp,
                ),
            ) {}
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = DECK_TOP.dp),
            colors = CardDefaults.cardColors(
                containerColor = faceColor,
                contentColor = if (faceUp) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

/** Il dorso: quello che vedi prima di pescare. */
@Composable
private fun CardBack(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StratiMark(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.question_prompt_not_started),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
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

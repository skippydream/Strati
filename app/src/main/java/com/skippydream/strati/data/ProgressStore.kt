package com.skippydream.strati.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

/** Dove sei arrivato dentro il mazzo di uno strato. */
@Immutable
data class DeckState(
    val seed: Long,
    val drawn: Int,
    val skipsLeft: Int,
    val total: Int,
)

internal fun DeckState.encode(): String = "$seed|$drawn|$skipsLeft|$total"

/** Torna null su valori assenti o corrotti: un mazzo illeggibile riparte intero. */
internal fun decodeDeck(raw: String?): DeckState? {
    val parts = raw?.split('|') ?: return null
    if (parts.size != 4) return null
    return DeckState(
        seed = parts[0].toLongOrNull() ?: return null,
        drawn = parts[1].toIntOrNull() ?: return null,
        skipsLeft = parts[2].toIntOrNull() ?: return null,
        total = parts[3].toIntOrNull() ?: return null,
    )
}

/** Strati completati e mazzi in corso, persistiti fra un avvio e l'altro. */
object ProgressStore {

    private const val PREFS_NAME = "strati_progress"
    private const val KEY_COMPLETED = "completed_layers"
    private const val DECK_PREFIX = "deck:"

    private var appContext: Context? = null

    var completed by mutableStateOf<Set<String>>(emptySet())
        private set

    var decks by mutableStateOf<Map<String, DeckState>>(emptyMap())
        private set

    fun init(context: Context) {
        if (appContext != null) return
        val application = context.applicationContext
        appContext = application

        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        completed = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty().toSet()
        decks = prefs.all
            .filterKeys { it.startsWith(DECK_PREFIX) }
            .mapNotNull { (key, value) ->
                decodeDeck(value as? String)?.let { key.removePrefix(DECK_PREFIX) to it }
            }
            .toMap()
    }

    /** Il mazzo di uno strato, o null se e' ancora intero. */
    fun deck(topicId: String, layerId: Int): DeckState? = decks[key(topicId, layerId)]

    fun saveDeck(topicId: String, layerId: Int, state: DeckState) {
        val id = key(topicId, layerId)
        if (decks[id] == state) return

        decks = decks + (id to state)
        prefs()?.edit { putString(DECK_PREFIX + id, state.encode()) }
    }

    fun markCompleted(topicId: String, layerId: Int) {
        val id = key(topicId, layerId)
        if (id in completed) return

        completed = completed + id
        prefs()?.edit { putStringSet(KEY_COMPLETED, completed) }
    }

    /** Rimescola tutti i mazzi e riporta gli strati a "da giocare". */
    fun reset() {
        completed = emptySet()
        decks = emptyMap()
        prefs()?.edit { clear() }
    }

    fun isCompleted(topicId: String, layerId: Int): Boolean = key(topicId, layerId) in completed

    fun completedCount(topic: Topic): Int = topic.layers.count { isCompleted(topic.id, it.id) }

    private fun prefs() = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(topicId: String, layerId: Int): String = "$topicId:$layerId"
}

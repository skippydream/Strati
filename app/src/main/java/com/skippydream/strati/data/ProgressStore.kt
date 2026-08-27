package com.skippydream.strati.data

import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Strati completati, persistiti fra un avvio e l'altro ed esposti come stato Compose. */
object ProgressStore {

    private const val PREFS_NAME = "strati_progress"
    private const val KEY_COMPLETED = "completed_layers"

    private var appContext: Context? = null

    var completed by mutableStateOf<Set<String>>(emptySet())
        private set

    fun init(context: Context) {
        if (appContext != null) return
        val application = context.applicationContext
        appContext = application
        completed = application
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMPLETED, emptySet())
            .orEmpty()
            .toSet()
    }

    fun markCompleted(topicId: String, layerId: Int) {
        val key = key(topicId, layerId)
        if (key in completed) return

        completed = completed + key
        appContext
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit { putStringSet(KEY_COMPLETED, completed) }
    }

    fun isCompleted(topicId: String, layerId: Int): Boolean = key(topicId, layerId) in completed

    fun completedCount(topic: Topic): Int = topic.layers.count { isCompleted(topic.id, it.id) }

    private fun key(topicId: String, layerId: Int): String = "$topicId:$layerId"
}

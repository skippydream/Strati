package com.skippydream.strati.data

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.core.os.ConfigurationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/** Carica e tiene in cache le domande di uno strato. */
object QuestionsRepository {

    private val cache = ConcurrentHashMap<String, List<String>>()

    suspend fun load(resources: Resources, @RawRes questionsRes: Int): List<String> {
        val key = cacheKey(resources, questionsRes)
        cache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            val questions = runCatching {
                resources.openRawResource(questionsRes).bufferedReader().useLines { lines ->
                    lines.map(String::trim).filter(String::isNotEmpty).toList()
                }
            }.getOrDefault(emptyList())

            if (questions.isNotEmpty()) cache[key] = questions
            questions
        }
    }

    // La stessa risorsa da' testi diversi a seconda della lingua: la chiave le distingue.
    private fun cacheKey(resources: Resources, @RawRes questionsRes: Int): String {
        val language = ConfigurationCompat.getLocales(resources.configuration)[0]?.language.orEmpty()
        return "$language:$questionsRes"
    }
}

package com.skippydream.strati.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Controlli di integrita' sul catalogo: un id sbagliato qui diventa una schermata di errore. */
class TopicsTest {

    @Test
    fun `topic ids are unique`() {
        val ids = Topics.all.map(Topic::id)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every topic has at least one layer`() {
        assertTrue(Topics.all.all { it.layers.isNotEmpty() })
    }

    @Test
    fun `layer ids are progressive starting from one`() {
        Topics.all.forEach { topic ->
            assertEquals(
                "Strati non progressivi in '${topic.id}'",
                List(topic.layers.size) { it + 1 },
                topic.layers.map(Layer::id),
            )
        }
    }

    @Test
    fun `every layer points to a distinct questions file`() {
        val resources = Topics.all.flatMap { it.layers }.map(Layer::questionsRes)
        assertEquals(resources.size, resources.toSet().size)
    }

    @Test
    fun `lookups resolve known ids and reject unknown ones`() {
        assertNotNull(Topics.topic("default"))
        assertNotNull(Topics.layer("default", 1))

        assertNull(Topics.topic(null))
        assertNull(Topics.topic("does-not-exist"))
        assertNull(Topics.layer("default", 99))
        assertNull(Topics.layer("does-not-exist", 1))
    }
}

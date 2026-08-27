package com.skippydream.strati.ui

sealed interface Screen {

    val route: String

    data object Topics : Screen {
        override val route: String = "topics"
    }

    data object Layers : Screen {
        const val ARG_TOPIC_ID = "topicId"
        override val route: String = "layers/{$ARG_TOPIC_ID}"
        fun createRoute(topicId: String): String = "layers/$topicId"
    }

    data object Question : Screen {
        const val ARG_TOPIC_ID = "topicId"
        const val ARG_LAYER_ID = "layerId"
        override val route: String = "question/{$ARG_TOPIC_ID}/{$ARG_LAYER_ID}"
        fun createRoute(topicId: String, layerId: Int): String = "question/$topicId/$layerId"
    }
}

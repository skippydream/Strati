package com.skippydream.strati.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skippydream.strati.ui.screens.LayersScreen
import com.skippydream.strati.ui.screens.QuestionScreen
import com.skippydream.strati.ui.screens.TopicsScreen

@Composable
fun StratiApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Topics.route,
            // Con targetSdk 36 l'edge-to-edge e' forzato: senza questo il contenuto
            // finisce sotto la status bar e la barra di navigazione.
            modifier = Modifier.safeDrawingPadding(),
        ) {
            composable(Screen.Topics.route) {
                TopicsScreen(
                    onTopicSelected = { topicId ->
                        navController.navigate(Screen.Layers.createRoute(topicId)) {
                            launchSingleTop = true // doppio tap non impila due schermate
                        }
                    },
                )
            }

            composable(
                route = Screen.Layers.route,
                arguments = listOf(
                    navArgument(Screen.Layers.ARG_TOPIC_ID) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                LayersScreen(
                    topicId = backStackEntry.arguments?.getString(Screen.Layers.ARG_TOPIC_ID),
                    onLayerSelected = { topicId, layerId ->
                        navController.navigate(Screen.Question.createRoute(topicId, layerId)) {
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.Question.route,
                arguments = listOf(
                    navArgument(Screen.Question.ARG_TOPIC_ID) { type = NavType.StringType },
                    navArgument(Screen.Question.ARG_LAYER_ID) { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getString(Screen.Question.ARG_TOPIC_ID)
                QuestionScreen(
                    topicId = topicId,
                    layerId = backStackEntry.arguments?.getInt(Screen.Question.ARG_LAYER_ID) ?: 0,
                    onBack = { navController.popBackStack() },
                    onGoToLayer = { nextLayerId ->
                        if (topicId != null) {
                            // Sostituisce lo strato corrente: indietro torna all'elenco.
                            navController.navigate(
                                Screen.Question.createRoute(topicId, nextLayerId)
                            ) {
                                popUpTo(Screen.Question.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
        }
    }
}

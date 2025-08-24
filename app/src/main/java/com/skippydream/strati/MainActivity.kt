package com.skippydream.strati

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skippydream.strati.ui.theme.StratiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class per rappresentare una singola domanda
data class Question(val text: String)

// Data class per rappresentare uno strato di domande
data class Layer(val id: Int, val nameResId: Int, val resourceId: Int)

// Data class per rappresentare un topic, che contiene più strati
data class Topic(val id: String, val nameResId: Int, val icon: ImageVector, val layers: List<Layer>)

// Definiamo tutti i topic, i loro strati e i file di risorse corrispondenti
@Composable
fun getTopicsList(): List<Topic> {
    return listOf(
        Topic(
            id = "default",
            nameResId = R.string.topic_name_default,
            icon = Icons.Default.Diversity3,
            layers = listOf(
                Layer(1, R.string.layer_name_default_1, R.raw.default_1),
                Layer(2, R.string.layer_name_default_2, R.raw.default_2),
                Layer(3, R.string.layer_name_default_3, R.raw.default_3),
                Layer(4, R.string.layer_name_default_4, R.raw.default_4)
            )
        ),
        Topic(
            id = "love",
            nameResId = R.string.topic_name_love,
            icon = Icons.Default.LocalFireDepartment,
            layers = listOf(
                Layer(1, R.string.layer_name_love_1, R.raw.love_1),
                Layer(2, R.string.layer_name_love_2, R.raw.love_2),
                Layer(3, R.string.layer_name_love_3, R.raw.love_3)
            )
        ),
        Topic(
            id = "thc",
            nameResId = R.string.topic_name_thc,
            icon = Icons.Default.Star,
            layers = listOf(
                Layer(1, R.string.layer_name_thc_1, R.raw.thc_1),
                Layer(2, R.string.layer_name_thc_2, R.raw.thc_2),
                Layer(3, R.string.layer_name_thc_3, R.raw.thc_3)
            )
        )
    )
}

// Funzione per caricare le domande dal file raw
private fun loadQuestionsFromRaw(context: Context, resourceId: Int): List<Question> {
    return try {
        context.resources.openRawResource(resourceId)
            .bufferedReader()
            .useLines { lines ->
                lines.map { line -> Question(line.trim()) }.toList()
            }
    } catch (e: Exception) {
        emptyList()
    }
}

// Schermate per la navigazione
sealed class Screen(val route: String) {
    object TopicsSelection : Screen("topics_selection")
    object LayersSelection : Screen("layers_selection/{topicId}") {
        fun createRoute(topicId: String) = "layers_selection/$topicId"
    }
    object Question : Screen("question/{topicId}/{layerId}") {
        fun createRoute(topicId: String, layerId: Int) = "question/$topicId/$layerId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StratiApp()
                }
            }
        }
    }
}

@Composable
fun StratiApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.TopicsSelection.route) {
        composable(Screen.TopicsSelection.route) {
            TopicsScreen(navController = navController)
        }
        composable(Screen.LayersSelection.route) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            if (topicId != null) {
                LayersScreen(topicId = topicId, navController = navController)
            } else {
                Text(text = stringResource(R.string.error_invalid_topic))
            }
        }
        composable(Screen.Question.route) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val layerId = backStackEntry.arguments?.getString("layerId")?.toIntOrNull()
            if (topicId != null && layerId != null) {
                QuestionScreen(topicId = topicId, layerId = layerId, navController = navController)
            } else {
                Text(text = stringResource(R.string.error_invalid_layer))
            }
        }
    }
}
@Composable
fun TopicsScreen(navController: NavController) {
    val topics = getTopicsList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.topics_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        InstructionsCard()

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(topics) { topic ->
                TopicCard(topic = topic) {
                    navController.navigate(Screen.LayersSelection.createRoute(topic.id))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "      _\n     / \\\n    /   \\\n   /     \\\n  /_______\\\n /  _____  \\\n/  | | | |  \\\n/___|_|_|_|___\\",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.footer_text),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
@Composable
fun InstructionsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.instructions_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.instructions_text_1),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.instructions_text_2),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun TopicCard(topic: Topic, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = stringResource(topic.nameResId),
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = stringResource(topic.nameResId),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun LayersScreen(topicId: String, navController: NavController) {
    val topics = getTopicsList()
    val topic = topics.find { it.id == topicId }
    if (topic == null) {
        Text(stringResource(R.string.error_invalid_topic))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Topic: ${stringResource(topic.nameResId)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(topic.layers) { layer ->
                LayerCard(layer = layer) {
                    navController.navigate(Screen.Question.createRoute(topicId, layer.id))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { navController.popBackStack() }) {
            Text(text = stringResource(R.string.layers_screen_back_button))
        }
    }
}

@Composable
fun LayerCard(layer: Layer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Strato ${layer.id}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(layer.nameResId),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun QuestionScreen(topicId: String, layerId: Int, navController: NavController) {
    val topics = getTopicsList()
    val context = LocalContext.current
    val topic = topics.find { it.id == topicId }
    val layer = topic?.layers?.find { it.id == layerId }

    var questions by remember { mutableStateOf(emptyList<Question>()) }
    var questionsShown by remember { mutableStateOf(emptySet<Question>()) }
    var currentQuestion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    var timerValue by remember { mutableIntStateOf(0) }
    var isTimerActive by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = topicId, key2 = layerId) {
        if (layer != null) {
            questions = loadQuestionsFromRaw(context, layer.resourceId)
        }
    }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            for (i in 30 downTo 0) {
                timerValue = i
                delay(1000)
            }
            isTimerActive = false
        }
    }

    // Aggiunto un CoroutineScope per poter usare delay() nel Button onClick
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center // Allineiamo l'intero Box al centro
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Topic: ${stringResource(topic?.nameResId ?: 0)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Strato ${layer?.id ?: ""} - ${stringResource(layer?.nameResId ?: 0)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (!isStarted) {
                        Text(
                            text = stringResource(R.string.question_prompt_not_started),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = currentQuestion,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Aggiunto un padding per non far toccare i bordi
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(50.dp), contentAlignment = Alignment.Center) { // Box vuoto per allineare il pulsante
                    // Il timer sarà qui, visibile solo quando attivo
                    if (isTimerActive) {
                        Text(
                            text = "$timerValue",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Button(
                    onClick = {
                        isLoading = true
                        isTimerActive = true
                        coroutineScope.launch {
                            if (!isStarted) {
                                isStarted = true
                                val initialQuestion = questions.random()
                                currentQuestion = initialQuestion.text
                                questionsShown = questionsShown + initialQuestion
                                isLoading = false
                            } else {
                                delay(500) // Il ritardo richiesto è qui
                                val remainingQuestions = questions.filter { it !in questionsShown }
                                if (remainingQuestions.isNotEmpty()) {
                                    val nextQuestion = remainingQuestions.random()
                                    currentQuestion = nextQuestion.text
                                    questionsShown = questionsShown + nextQuestion
                                } else {
                                    currentQuestion = stringResource(R.string.end_of_layer)
                                    questionsShown = emptySet()
                                }
                                isLoading = false
                            }
                        }
                    },
                    enabled = questions.isNotEmpty() && !isLoading && !isTimerActive
                ) {
                    if (!isStarted) {
                        Text(text = stringResource(R.string.button_start))
                    } else {
                        Text(text = stringResource(R.string.button_next))
                    }
                }
                Box(modifier = Modifier.width(50.dp), contentAlignment = Alignment.Center) {
                    // Spazio per il timer invisibile, per mantenere il pulsante centrato
                    if (isTimerActive) {
                        Text(
                            text = "$timerValue",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text(text = stringResource(R.string.question_screen_back_button))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StratiTheme {
        StratiApp()
    }
}
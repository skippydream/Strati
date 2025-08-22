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
data class Layer(val id: Int, val name: String, val resourceId: Int)

// Data class per rappresentare un topic, che contiene più strati
data class Topic(val id: String, val name: String, val icon: ImageVector, val layers: List<Layer>)

// Definiamo tutti i topic, i loro strati e i file di risorse corrispondenti
val topics = listOf(
    Topic(
        id = "default",
        name = "Conoscersi Meglio",
        icon = Icons.Default.Diversity3,
        layers = listOf(
            Layer(1, "Sconosciuti", R.raw.default_1),
            Layer(2, "Percezioni", R.raw.default_2),
            Layer(3, "Connessione", R.raw.default_3),
            Layer(4, "Vulnerabilità", R.raw.default_4)
        )
    ),
    Topic(
        id = "love",
        name = "Sotto la Pelle",
        icon = Icons.Default.LocalFireDepartment,
        layers = listOf(
            Layer(1, "L'inizio", R.raw.love_1),
            Layer(2, "Il Cuore", R.raw.love_2),
            Layer(3, "Il Profondo", R.raw.love_3)
        )
    ),
    Topic(
        id = "thc",
        name = "Sognare a Occhi Aperti",
        icon = Icons.Default.Star,
        layers = listOf(
            Layer(1, "La Fila", R.raw.thc_1),
            Layer(2, "La Botta", R.raw.thc_2),
            Layer(3, "Il Trip", R.raw.thc_3)
        )
    )
)

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
                Text(text = "Errore: Topic non valido")
            }
        }
        composable(Screen.Question.route) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val layerId = backStackEntry.arguments?.getString("layerId")?.toIntOrNull()
            if (topicId != null && layerId != null) {
                QuestionScreen(topicId = topicId, layerId = layerId, navController = navController)
            } else {
                Text(text = "Errore: Strato non valido")
            }
        }
    }
}

@Composable
fun TopicsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Seleziona un Topic",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
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
    }
}

@Composable
fun TopicCard(topic: Topic, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = topic.name,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun LayersScreen(topicId: String, navController: NavController) {
    val topic = topics.find { it.id == topicId }
    if (topic == null) {
        Text("Errore: Topic non trovato")
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
            text = "Topic: ${topic.name}",
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
            Text(text = "Torna ai Topic")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Strato ${layer.id}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = layer.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun QuestionScreen(topicId: String, layerId: Int, navController: NavController) {
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
            for (i in 15 downTo 0) {
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
                text = "Topic: ${topic?.name ?: ""}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Strato ${layer?.id ?: ""} - ${layer?.name ?: ""}",
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
                            text = "Clicca su inizia quando sei prontə.",
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
                                    currentQuestion = "Hai visto tutte le domande di questo strato!"
                                    questionsShown = emptySet()
                                }
                                isLoading = false
                            }
                        }
                    },
                    enabled = questions.isNotEmpty() && !isLoading && !isTimerActive
                ) {
                    if (!isStarted) {
                        Text(text = "Inizia")
                    } else {
                        Text(text = "Prossima domanda")
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
                Text(text = "Torna indietro")
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

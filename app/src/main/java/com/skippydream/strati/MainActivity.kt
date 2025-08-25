package com.skippydream.strati

import android.annotation.SuppressLint
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
import java.util.Locale
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalUriHandler



// Data class per rappresentare una singola domanda
data class Question(val text: String)

// Data class per rappresentare uno strato di domande
data class Layer(
    val id: Int,
    val nameResId: Int,
    val resourceId: Int,
    val resourceIdEn: Int? = null
)

// Data class per rappresentare un topic, che contiene più strati
data class Topic(val id: String, val nameResId: Int, val icon: ImageVector, val layers: List<Layer>)

// Definiamo tutti i topic, i loro strati e i file di risorse corrispondenti
fun getTopicsList(): List<Topic> {
    return listOf(
        Topic(
            id = "default",
            nameResId = R.string.topic_name_default,
            icon = Icons.Default.Diversity3,
            layers = listOf(
                Layer(1, R.string.layer_name_default_1, R.raw.default_1, R.raw.default_1_en),
                Layer(2, R.string.layer_name_default_2, R.raw.default_2, R.raw.default_2_en),
                Layer(3, R.string.layer_name_default_3, R.raw.default_3, R.raw.default_3_en),
                Layer(4, R.string.layer_name_default_4, R.raw.default_4, R.raw.default_4_en)
            )
        ),
        Topic(
            id = "love",
            nameResId = R.string.topic_name_love,
            icon = Icons.Default.LocalFireDepartment,
            layers = listOf(
                Layer(1, R.string.layer_name_love_1, R.raw.love_1, R.raw.love_1_en),
                Layer(2, R.string.layer_name_love_2, R.raw.love_2, R.raw.love_2_en),
                Layer(3, R.string.layer_name_love_3, R.raw.love_3, R.raw.love_3_en)
            )
        ),
        Topic(
            id = "thc",
            nameResId = R.string.topic_name_thc,
            icon = Icons.Default.Star,
            layers = listOf(
                Layer(1, R.string.layer_name_thc_1, R.raw.thc_1, R.raw.thc_1_en),
                Layer(2, R.string.layer_name_thc_2, R.raw.thc_2, R.raw.thc_2_en),
                Layer(3, R.string.layer_name_thc_3, R.raw.thc_3, R.raw.thc_3_en)
            )
        )
    )
}

// Funzione per caricare le domande dal file raw
private fun loadQuestionsFromRaw(context: Context, layer: Layer): List<Question> {
    val locale = context.resources.configuration.locales.get(0)
    val isEnglish = locale.language.startsWith("en")

    val resIdToLoad = if (isEnglish && layer.resourceIdEn != null) {
        layer.resourceIdEn
    } else {
        layer.resourceId
    }

    return try {
        context.resources.openRawResource(resIdToLoad)
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

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun StratiApp() {
    val navController = rememberNavController()

    var selectedLocale by remember { mutableStateOf(Locale.getDefault()) }

    val context = LocalContext.current
    val localizedContext = remember(selectedLocale) {
        context.createConfigurationContext(
            android.content.res.Configuration(context.resources.configuration).apply {
                setLocale(selectedLocale)
            }
        )
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        NavHost(navController = navController, startDestination = Screen.TopicsSelection.route) {
            composable(Screen.TopicsSelection.route) {
                TopicsScreen(
                    navController = navController,
                    selectedLocale = selectedLocale,
                    onLocaleChange = { locale -> selectedLocale = locale }
                )
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
                val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
                val layerId = backStackEntry.arguments?.getString("layerId")?.toIntOrNull() ?: 0
                QuestionScreen(topicId = topicId, layerId = layerId, navController = navController)
            }
        }

    }
}

@Composable
fun TopicsScreen(
    navController: NavController,
    selectedLocale: Locale,
    onLocaleChange: (Locale) -> Unit
) {
    val topics = getTopicsList()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp), // top e bottom un po' più equilibrati
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // meglio usare Top per scrollare correttamente
    ) {
        // Pulsanti cambio lingua in alto a destra
        LanguageSwitcher(
            selectedLocale = selectedLocale,
            onLocaleChange = onLocaleChange
        )
        InstructionsCard()


        Spacer(modifier = Modifier.height(16.dp))  // separazione da lista

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)  // prende spazio disponibile
        ) {
            items(topics) { topic ->
                TopicCard(topic = topic) {
                    navController.navigate(Screen.LayersSelection.createRoute(topic.id))
                }

            }


        }
        DonationLinkButton() // aggiunta donation

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.footer_text),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "GitHub",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val url = "https://www.github.com/skippydream/Strati"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    .padding(top = 4.dp)
            )
        }
    }
}



@Composable
fun InstructionsCard() {
    var expanded by remember { mutableStateOf(false) }

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
            // Titolo cliccabile per espandere/chiudere
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.instructions_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (expanded) {
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
                text = stringResource(R.string.layer_label) + " ${layer.id}",
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

    val endOfLayerMessage = stringResource(R.string.end_of_layer)

    // Caricamento iniziale delle domande
    LaunchedEffect(key1 = topicId, key2 = layerId) {
        if (layer != null) {
            questions = loadQuestionsFromRaw(context, layer)
        }
    }

    // Timer countdown
    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            for (i in 30 downTo 0) {
                timerValue = i
                delay(1000)
            }
            isTimerActive = false
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Topic: ${stringResource(topic?.nameResId ?: 0)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.layer_label) + " ${layer?.id ?: ""} - ${stringResource(layer?.nameResId ?: 0)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Card contenente la domanda
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
                    when {
                        isLoading -> CircularProgressIndicator()
                        !isStarted -> Text(
                            text = stringResource(R.string.question_prompt_not_started),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        else -> Text(
                            text = currentQuestion,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Pulsante e timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer sinistra (solo se attivo)
                Box(modifier = Modifier.width(50.dp), contentAlignment = Alignment.Center) {
                    if (isTimerActive) {
                        Text(
                            text = "$timerValue",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Bottone centrale
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                if (!isStarted) {
                                    isStarted = true
                                    isTimerActive = true  // Avvia il timer anche alla prima domanda
                                    delay(1000) // opzionale, per sincronizzare
                                    val initialQuestion = questions.randomOrNull()
                                    if (initialQuestion != null) {
                                        currentQuestion = initialQuestion.text
                                        questionsShown = setOf(initialQuestion)
                                    } else {
                                        currentQuestion = endOfLayerMessage
                                    }
                                } else {
                                    isTimerActive = true
                                    delay(1000)

                                    val remaining = questions.filter { it !in questionsShown }
                                    if (remaining.isNotEmpty()) {
                                        val next = remaining.random()
                                        currentQuestion = next.text
                                        questionsShown = questionsShown + next
                                    } else {
                                        currentQuestion = endOfLayerMessage
                                        questionsShown = emptySet()
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = questions.isNotEmpty() && !isLoading && !isTimerActive,
                    modifier = Modifier
                        .padding(vertical = 15.dp)
                ) {
                    Text(
                        text = if (!isStarted)
                            stringResource(R.string.button_start)
                        else
                            stringResource(R.string.button_next),
                        fontSize = 25.sp
                    )
                }


                // Timer destra (invisibile per simmetria)
                Box(modifier = Modifier.width(50.dp), contentAlignment = Alignment.Center) {
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

@Composable
fun DonationLinkButton() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) stringResource(R.string.donate_hide) else stringResource(R.string.donate_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (!expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (!expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.donate_text),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(2, 5, 10).forEach { amount ->
                    OutlinedButton(
                        onClick = {
                            val url = "https://paypal.me/michelelana/$amount"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer)
                    ) {
                        Text(text = "€$amount")
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSwitcher(
    selectedLocale: Locale,
    onLocaleChange: (Locale) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        val selectedColor = MaterialTheme.colorScheme.primary
        val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

        TextButton(onClick = { onLocaleChange(Locale("it")) }) {
            Text(
                text = "IT",
                color = if (selectedLocale.language == "it") selectedColor else unselectedColor
            )
        }
        TextButton(onClick = { onLocaleChange(Locale("en")) }) {
            Text(
                text = "EN",
                color = if (selectedLocale.language == "en") selectedColor else unselectedColor
            )
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
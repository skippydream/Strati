package com.skippydream.strati.data

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.skippydream.strati.R

@Immutable
data class Layer(
    val id: Int,
    @StringRes val nameRes: Int,
    // Una sola risorsa: res/raw e' l'italiano, res/raw-en l'inglese, la variante la sceglie Android.
    @RawRes val questionsRes: Int,
)

@Immutable
data class Topic(
    val id: String,
    @StringRes val nameRes: Int,
    val icon: ImageVector,
    val layers: List<Layer>,
)

object Topics {

    val all: List<Topic> = listOf(
        Topic(
            id = "default",
            nameRes = R.string.topic_name_default,
            icon = Icons.Default.Diversity3,
            layers = listOf(
                Layer(1, R.string.layer_name_default_1, R.raw.default_1),
                Layer(2, R.string.layer_name_default_2, R.raw.default_2),
                Layer(3, R.string.layer_name_default_3, R.raw.default_3),
                Layer(4, R.string.layer_name_default_4, R.raw.default_4),
            ),
        ),
        Topic(
            id = "love",
            nameRes = R.string.topic_name_love,
            icon = Icons.Default.LocalFireDepartment,
            layers = listOf(
                Layer(1, R.string.layer_name_love_1, R.raw.love_1),
                Layer(2, R.string.layer_name_love_2, R.raw.love_2),
                Layer(3, R.string.layer_name_love_3, R.raw.love_3),
            ),
        ),
        Topic(
            id = "thc",
            nameRes = R.string.topic_name_thc,
            icon = Icons.Default.AutoAwesome,
            layers = listOf(
                Layer(1, R.string.layer_name_thc_1, R.raw.thc_1),
                Layer(2, R.string.layer_name_thc_2, R.raw.thc_2),
                Layer(3, R.string.layer_name_thc_3, R.raw.thc_3),
            ),
        ),
    )

    private val byId: Map<String, Topic> = all.associateBy(Topic::id)

    fun topic(id: String?): Topic? = id?.let(byId::get)

    fun layer(topicId: String?, layerId: Int): Layer? =
        topic(topicId)?.layers?.firstOrNull { it.id == layerId }
}

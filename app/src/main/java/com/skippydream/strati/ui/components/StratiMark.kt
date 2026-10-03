package com.skippydream.strati.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/** I tre strati del logo, disegnati con i colori del tema invece che con un asset fisso. */
@Composable
fun StratiMark(modifier: Modifier = Modifier) {
    val top = MaterialTheme.colorScheme.tertiary
    val middle = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val bottom = MaterialTheme.colorScheme.primary

    Canvas(modifier) {
        val unit = size.minDimension / 108f

        fun layer(centerY: Float, halfWidth: Float, color: Color) {
            val path = Path().apply {
                moveTo(54f * unit, (centerY - 8f) * unit)
                lineTo((54f + halfWidth) * unit, centerY * unit)
                lineTo(54f * unit, (centerY + 8f) * unit)
                lineTo((54f - halfWidth) * unit, centerY * unit)
                close()
            }
            drawPath(path, color)
        }

        layer(35f, 21f, top)
        layer(54f, 24f, middle)
        layer(73f, 27f, bottom)
    }
}

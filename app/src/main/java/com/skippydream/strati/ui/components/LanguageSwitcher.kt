package com.skippydream.strati.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.skippydream.strati.data.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    // Cambiare lingua ricrea l'Activity: rileggere a ogni composizione basta.
    val current = AppLanguage.current()
    val languages = AppLanguage.entries

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        languages.forEachIndexed { index, language ->
            val selected = language == current
            val description = stringResource(language.nameRes)

            SegmentedButton(
                selected = selected,
                onClick = { if (!selected) AppLanguage.apply(language) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
                icon = {},
                modifier = Modifier.semantics { contentDescription = description },
            ) {
                Text(text = language.label)
            }
        }
    }
}

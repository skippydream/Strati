package com.skippydream.strati.data

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.skippydream.strati.R
import java.util.Locale

/** Lingue supportate. [AppCompatDelegate] persiste la scelta e la espone alle impostazioni di sistema. */
enum class AppLanguage(
    val tag: String,
    val label: String,
    @StringRes val nameRes: Int,
) {
    ITALIAN("it", "IT", R.string.cd_language_italian),
    ENGLISH("en", "EN", R.string.cd_language_english),
    ;

    companion object {

        /** Lingua attiva: la scelta esplicita dell'utente o, in mancanza, quella di sistema. */
        fun current(): AppLanguage {
            val chosen = AppCompatDelegate.getApplicationLocales()[0]?.language
                ?: Locale.getDefault().language
            return entries.firstOrNull { it.tag == chosen } ?: ITALIAN
        }

        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
}

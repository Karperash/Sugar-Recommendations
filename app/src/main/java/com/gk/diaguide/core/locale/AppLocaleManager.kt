package com.gk.diaguide.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLocaleManager @Inject constructor() {

    fun applyPreferredLanguage(languageTag: String) {
        if (languageTag.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        }
    }
}

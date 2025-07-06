package com.dvhamham.manager.ui.language

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

class LanguageManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_FRENCH = "fr"
        const val LANGUAGE_ARABIC = "ar"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        updateLocale(languageCode, forceRecreate = true)
    }
    
    fun updateLocale(languageCode: String, forceRecreate: Boolean = true) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Force recreation of the activity to apply language changes
        if (forceRecreate && context is Activity) {
            context.recreate()
        }
    }
    
    fun getLayoutDirection(): LayoutDirection {
        return when (getCurrentLanguage()) {
            LANGUAGE_ARABIC -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_FRENCH -> "Français"
            LANGUAGE_ARABIC -> "العربية"
            else -> "English"
        }
    }
    
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English", "English"),
            Language(LANGUAGE_FRENCH, "Français", "French"),
            Language(LANGUAGE_ARABIC, "العربية", "Arabic")
        )
    }
}

data class Language(
    val code: String,
    val displayName: String,
    val englishName: String
)

@Composable
fun rememberLanguageManager(): LanguageManager {
    val context = LocalContext.current
    return remember { LanguageManager(context) }
} 
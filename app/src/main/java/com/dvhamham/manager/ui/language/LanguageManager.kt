package com.dvhamham.manager.ui.language

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

class LanguageManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        private const val TAG = "LanguageManager"
        
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_FRENCH = "fr"
        const val LANGUAGE_ARABIC = "ar"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentLanguage(): String {
        val savedLanguage = prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
        Log.d(TAG, "Getting current language: $savedLanguage")
        return savedLanguage
    }
    
    fun setLanguage(languageCode: String) {
        Log.d(TAG, "Setting language to: $languageCode")
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        Log.d(TAG, "Language saved to SharedPreferences")
        
        // Verify the save
        val savedLanguage = prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH)
        Log.d(TAG, "Verified saved language: $savedLanguage")
        
        updateLocale(languageCode, forceRecreate = true)
    }
    
    fun updateLocale(languageCode: String, forceRecreate: Boolean = true) {
        Log.d(TAG, "Updating locale to: $languageCode, forceRecreate: $forceRecreate")
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Update the configuration for the current context
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Force recreation of the activity to apply language changes
        if (forceRecreate && context is Activity) {
            Log.d(TAG, "Recreating activity to apply language changes")
            context.recreate()
        }
    }
    
    fun getLayoutDirection(): LayoutDirection {
        val currentLanguage = getCurrentLanguage()
        val direction = when (currentLanguage) {
            LANGUAGE_ARABIC -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }
        Log.d(TAG, "Getting layout direction for language $currentLanguage: $direction")
        return direction
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        val currentLanguage = getCurrentLanguage()
        return when (languageCode) {
            LANGUAGE_ENGLISH -> when (currentLanguage) {
                LANGUAGE_FRENCH -> "Anglais"
                LANGUAGE_ARABIC -> "الإنجليزية"
                else -> "English"
            }
            LANGUAGE_FRENCH -> when (currentLanguage) {
                LANGUAGE_FRENCH -> "Français"
                LANGUAGE_ARABIC -> "الفرنسية"
                else -> "French"
            }
            LANGUAGE_ARABIC -> when (currentLanguage) {
                LANGUAGE_FRENCH -> "Arabe"
                LANGUAGE_ARABIC -> "العربية"
                else -> "Arabic"
            }
            else -> "English"
        }
    }
    
    fun getAvailableLanguages(): List<Language> {
        val currentLanguage = getCurrentLanguage()
        return listOf(
            Language(
                LANGUAGE_ENGLISH, 
                when (currentLanguage) {
                    LANGUAGE_FRENCH -> "Anglais"
                    LANGUAGE_ARABIC -> "الإنجليزية"
                    else -> "English"
                },
                "English"
            ),
            Language(
                LANGUAGE_FRENCH, 
                when (currentLanguage) {
                    LANGUAGE_FRENCH -> "Français"
                    LANGUAGE_ARABIC -> "الفرنسية"
                    else -> "French"
                },
                "French"
            ),
            Language(
                LANGUAGE_ARABIC, 
                when (currentLanguage) {
                    LANGUAGE_FRENCH -> "Arabe"
                    LANGUAGE_ARABIC -> "العربية"
                    else -> "Arabic"
                },
                "Arabic"
            )
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
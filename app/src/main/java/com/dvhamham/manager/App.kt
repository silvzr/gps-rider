package com.dvhamham.manager

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.dvhamham.manager.ui.language.LanguageManager
import java.util.Locale

class App : Application() {
    
    companion object {
        private const val TAG = "App"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "App.onCreate() called")
        // Initialize saved language at app startup
        initializeLanguage()
    }

    override fun attachBaseContext(base: Context?) {
        Log.d(TAG, "App.attachBaseContext() called")
        if (base != null) {
            val languageManager = LanguageManager(base)
            val savedLanguage = languageManager.getCurrentLanguage()
            Log.d(TAG, "Reading saved language in attachBaseContext: $savedLanguage")
            
            val locale = Locale(savedLanguage)
            Log.d(TAG, "Setting locale in attachBaseContext: $locale")
            
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            
            val context = base.createConfigurationContext(config)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(base)
        }
    }
    
    override fun getResources(): Resources {
        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getCurrentLanguage()
        Log.d(TAG, "Reading saved language in getResources: $savedLanguage")
        
        val locale = Locale(savedLanguage)
        Log.d(TAG, "Setting locale in getResources: $locale")
        
        val config = Configuration(super.getResources().configuration)
        config.setLocale(locale)
        
        return createConfigurationContext(config).resources
    }
    
    private fun initializeLanguage() {
        Log.d(TAG, "App.initializeLanguage() called")
        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getCurrentLanguage()
        Log.d(TAG, "Reading saved language in initializeLanguage: $savedLanguage")
        
        val locale = Locale(savedLanguage)
        Log.d(TAG, "Setting default locale: $locale")
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        Log.d(TAG, "Updating configuration with locale: $locale")
        resources.updateConfiguration(config, resources.displayMetrics)
        Log.d(TAG, "Language initialization completed")
    }
} 
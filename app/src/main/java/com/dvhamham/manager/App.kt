package com.dvhamham.manager

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.dvhamham.manager.ui.language.LanguageManager
import java.util.Locale

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val languageManager = LanguageManager(this)
        languageManager.updateLocale(languageManager.getCurrentLanguage(), forceRecreate = false)
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            val languageManager = LanguageManager(base)
            val locale = languageManager.getCurrentLanguage()
            val config = Configuration(base.resources.configuration)
            config.setLocale(Locale(locale))
            val context = base.createConfigurationContext(config)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(base)
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val languageManager = LanguageManager(this)
        val locale = Locale(languageManager.getCurrentLanguage())
        newConfig.setLocale(locale)
        createConfigurationContext(newConfig)
    }
    
    override fun getResources(): Resources {
        val languageManager = LanguageManager(this)
        val locale = languageManager.getCurrentLanguage()
        val config = Configuration(super.getResources().configuration)
        config.setLocale(Locale(locale))
        return createConfigurationContext(config).resources
    }
} 
// PreferencesUtil.kt
package com.dvhamham.xposed.utils

import com.google.gson.Gson
import com.dvhamham.data.*
import com.dvhamham.data.model.LastClickedLocation
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge

object PreferencesUtil {
    private const val TAG = "[PreferencesUtil]"

    private val preferences: XSharedPreferences = XSharedPreferences(MANAGER_APP_PACKAGE_NAME, SHARED_PREFS_FILE).apply {
        makeWorldReadable()
        reload()
    }

    fun getIsPlaying(): Boolean? {
        return getPreference<Boolean>(KEY_IS_PLAYING)
    }

    fun getLastClickedLocation(): LastClickedLocation? {
        return getPreference<LastClickedLocation>(KEY_LAST_CLICKED_LOCATION)
    }

    fun getUseAccuracy(): Boolean? {
        return getPreference<Boolean>(KEY_USE_ACCURACY)
    }

    fun getAccuracy(): Double? {
        return getPreference<Double>(KEY_ACCURACY)
    }

    fun getUseAltitude(): Boolean? {
        return getPreference<Boolean>(KEY_USE_ALTITUDE)
    }

    fun getAltitude(): Double? {
        return getPreference<Double>(KEY_ALTITUDE)
    }

    fun getUseRandomize(): Boolean? {
        return getPreference<Boolean>(KEY_USE_RANDOMIZE)
    }

    fun getRandomizeRadius(): Double? {
        return getPreference<Double>(KEY_RANDOMIZE_RADIUS)
    }

    fun getUseVerticalAccuracy(): Boolean? {
        return getPreference<Boolean>(KEY_USE_VERTICAL_ACCURACY)
    }

    fun getVerticalAccuracy(): Float? {
        return getPreference<Float>(KEY_VERTICAL_ACCURACY)
    }

    fun getUseMeanSeaLevel(): Boolean? {
        return getPreference<Boolean>(KEY_USE_MEAN_SEA_LEVEL)
    }

    fun getMeanSeaLevel(): Double? {
        return getPreference<Double>(KEY_MEAN_SEA_LEVEL)
    }

    fun getUseMeanSeaLevelAccuracy(): Boolean? {
        return getPreference<Boolean>(KEY_USE_MEAN_SEA_LEVEL_ACCURACY)
    }

    fun getMeanSeaLevelAccuracy(): Float? {
        return getPreference<Float>(KEY_MEAN_SEA_LEVEL_ACCURACY)
    }

    fun getUseSpeed(): Boolean? {
        return getPreference<Boolean>(KEY_USE_SPEED)
    }

    fun getSpeed(): Float? {
        return getPreference<Float>(KEY_SPEED)
    }

    fun getUseSpeedAccuracy(): Boolean? {
        return getPreference<Boolean>(KEY_USE_SPEED_ACCURACY)
    }

    fun getSpeedAccuracy(): Float? {
        return getPreference<Float>(KEY_SPEED_ACCURACY)
    }

    private inline fun <reified T> getPreference(key: String): T? {
        preferences.reload()
        return when (T::class) {
            Double::class -> {
                val defaultValue = when (key) {
                    KEY_ACCURACY -> java.lang.Double.doubleToRawLongBits(DEFAULT_ACCURACY)
                    KEY_ALTITUDE -> java.lang.Double.doubleToRawLongBits(DEFAULT_ALTITUDE)
                    KEY_RANDOMIZE_RADIUS -> java.lang.Double.doubleToRawLongBits(DEFAULT_RANDOMIZE_RADIUS)
                    KEY_MEAN_SEA_LEVEL -> java.lang.Double.doubleToRawLongBits(DEFAULT_MEAN_SEA_LEVEL)
                    else -> -1L
                }
                val bits = preferences.getLong(key, defaultValue)
                java.lang.Double.longBitsToDouble(bits) as? T
            }
            Float::class -> {
                val defaultValue = when (key) {
                    KEY_VERTICAL_ACCURACY -> DEFAULT_VERTICAL_ACCURACY
                    KEY_MEAN_SEA_LEVEL_ACCURACY -> DEFAULT_MEAN_SEA_LEVEL_ACCURACY
                    KEY_SPEED -> DEFAULT_SPEED
                    KEY_SPEED_ACCURACY -> DEFAULT_SPEED_ACCURACY
                    else -> -1f
                }
                preferences.getFloat(key, defaultValue) as? T
            }
            Boolean::class -> preferences.getBoolean(key, false) as? T
            else -> {
                val json = preferences.getString(key, null)
                if (json != null) {
                    try {
                        Gson().fromJson(json, T::class.java).also {
                            XposedBridge.log("$TAG Retrieved $key: $it")
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("$TAG Error parsing $key JSON: ${e.message}")
                        null
                    }
                } else {
                    XposedBridge.log("$TAG $key not found in preferences.")
                    null
                }
            }
        }
    }
}
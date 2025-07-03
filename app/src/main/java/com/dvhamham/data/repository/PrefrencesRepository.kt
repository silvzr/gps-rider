// PreferencesRepository.kt
package com.dvhamham.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.dvhamham.data.*
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.data.model.LastClickedLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SHARED_PREFS_FILE)

class PreferencesRepository(private val context: Context) {
    private val tag = "PreferencesRepository"
    
    // Legacy SharedPreferences for Xposed Module compatibility
    @SuppressLint("WorldReadableFiles")
    private val sharedPrefs = try {
        context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        Log.w(tag, "MODE_WORLD_READABLE not available: ${e.message}")
        context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    // DataStore preference keys
    private object PreferenceKeys {
        val IS_PLAYING = booleanPreferencesKey(KEY_IS_PLAYING)
        val LAST_CLICKED_LOCATION = stringPreferencesKey(KEY_LAST_CLICKED_LOCATION)
        val MAP_ZOOM = doublePreferencesKey("map_zoom")
        val MAP_LATITUDE = doublePreferencesKey("map_latitude")
        val MAP_LONGITUDE = doublePreferencesKey("map_longitude")
        val USE_ACCURACY = booleanPreferencesKey(KEY_USE_ACCURACY)
        val ACCURACY = doublePreferencesKey(KEY_ACCURACY)
        val USE_ALTITUDE = booleanPreferencesKey(KEY_USE_ALTITUDE)
        val ALTITUDE = doublePreferencesKey(KEY_ALTITUDE)
        val USE_RANDOMIZE = booleanPreferencesKey(KEY_USE_RANDOMIZE)
        val RANDOMIZE_RADIUS = doublePreferencesKey(KEY_RANDOMIZE_RADIUS)
        val USE_VERTICAL_ACCURACY = booleanPreferencesKey(KEY_USE_VERTICAL_ACCURACY)
        val VERTICAL_ACCURACY = floatPreferencesKey(KEY_VERTICAL_ACCURACY)
        val USE_MEAN_SEA_LEVEL = booleanPreferencesKey(KEY_USE_MEAN_SEA_LEVEL)
        val MEAN_SEA_LEVEL = doublePreferencesKey(KEY_MEAN_SEA_LEVEL)
        val USE_MEAN_SEA_LEVEL_ACCURACY = booleanPreferencesKey(KEY_USE_MEAN_SEA_LEVEL_ACCURACY)
        val MEAN_SEA_LEVEL_ACCURACY = floatPreferencesKey(KEY_MEAN_SEA_LEVEL_ACCURACY)
        val USE_SPEED = booleanPreferencesKey(KEY_USE_SPEED)
        val SPEED = floatPreferencesKey(KEY_SPEED)
        val USE_SPEED_ACCURACY = booleanPreferencesKey(KEY_USE_SPEED_ACCURACY)
        val SPEED_ACCURACY = floatPreferencesKey(KEY_SPEED_ACCURACY)
        val FAVORITES = stringPreferencesKey(KEY_FAVORITES)
        val USE_SYSTEM_HOOK = booleanPreferencesKey("use_system_hook")
        val DISABLE_NIGHT_MAP_MODE = booleanPreferencesKey("disable_night_map_mode")
    }

    // Generic helper for DataStore flows with error handling
    private fun <T> getPreferenceFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    // Helper to write both to DataStore and legacy SharedPreferences
    private suspend inline fun <reified T> savePreference(
        key: Preferences.Key<T>,
        value: T,
        sharedPrefsKey: String,
        sharedPrefsValue: Any
    ) {
        try {
            // Save to DataStore
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
            
            // Save to legacy SharedPreferences for Xposed Module
            when (value) {
                is Boolean -> sharedPrefs.edit().putBoolean(sharedPrefsKey, value).apply()
                is String -> sharedPrefs.edit().putString(sharedPrefsKey, value).apply()
                is Float -> sharedPrefs.edit().putFloat(sharedPrefsKey, value).apply()
                is Double -> {
                    val bits = java.lang.Double.doubleToRawLongBits(value)
                    sharedPrefs.edit().putLong(sharedPrefsKey, bits).apply()
                }
                is Long -> sharedPrefs.edit().putLong(sharedPrefsKey, value).apply()
                is Int -> sharedPrefs.edit().putInt(sharedPrefsKey, value).apply()
            }
            
            Log.d(tag, "Saved $sharedPrefsKey: $value")
        } catch (e: Exception) {
            Log.e(tag, "Error saving preference $sharedPrefsKey: ${e.message}")
        }
    }

    // Is Playing
    fun getIsPlayingFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.IS_PLAYING, DEFAULT_USE_ACCURACY)
    }
    
    suspend fun saveIsPlaying(isPlaying: Boolean) {
        savePreference(PreferenceKeys.IS_PLAYING, isPlaying, KEY_IS_PLAYING, isPlaying)
    }
    
    // For backward compatibility
    fun getIsPlaying(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_PLAYING, false)
    }

    // Last Clicked Location
    fun getLastClickedLocationFlow(): Flow<LastClickedLocation?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferenceKeys.LAST_CLICKED_LOCATION]
                if (json != null) {
                    try {
                        gson.fromJson(json, LastClickedLocation::class.java)
                    } catch (e: JsonSyntaxException) {
                        Log.e(tag, "Error parsing LastClickedLocation: ${e.message}")
                        null
                    }
                } else {
                    null
                }
            }
    }
    
    suspend fun saveLastClickedLocation(latitude: Double, longitude: Double) {
        try {
            val location = LastClickedLocation(latitude, longitude)
            val json = gson.toJson(location)
            savePreference(PreferenceKeys.LAST_CLICKED_LOCATION, json, KEY_LAST_CLICKED_LOCATION, json)
        } catch (e: Exception) {
            Log.e(tag, "Error saving LastClickedLocation: ${e.message}")
        }
    }
    
    // For backward compatibility
    fun getLastClickedLocation(): LastClickedLocation? {
        val json = sharedPrefs.getString(KEY_LAST_CLICKED_LOCATION, null)
        return if (json != null) {
            try {
                gson.fromJson(json, LastClickedLocation::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(tag, "Error parsing LastClickedLocation: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    suspend fun clearLastClickedLocation() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferenceKeys.LAST_CLICKED_LOCATION)
            }
            
            sharedPrefs.edit()
                .remove(KEY_LAST_CLICKED_LOCATION)
                .apply()
                
            saveIsPlaying(false)
            Log.d(tag, "Cleared 'LastClickedLocation' from preferences and set 'IsPlaying' to false")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing LastClickedLocation: ${e.message}")
        }
    }

    // Use Accuracy
    fun getUseAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_ACCURACY, DEFAULT_USE_ACCURACY)
    }
    
    suspend fun saveUseAccuracy(useAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_ACCURACY, useAccuracy, KEY_USE_ACCURACY, useAccuracy)
    }
    
    // For backward compatibility
    fun getUseAccuracy(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_ACCURACY, DEFAULT_USE_ACCURACY)
    }

    // Accuracy
    fun getAccuracyFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.ACCURACY, DEFAULT_ACCURACY)
    }
    
    suspend fun saveAccuracy(accuracy: Double) {
        savePreference(PreferenceKeys.ACCURACY, accuracy, KEY_ACCURACY, accuracy)
    }
    
    // For backward compatibility
    fun getAccuracy(): Double {
        val bits = sharedPrefs.getLong(KEY_ACCURACY, java.lang.Double.doubleToRawLongBits(DEFAULT_ACCURACY))
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Use Altitude
    fun getUseAltitudeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_ALTITUDE, DEFAULT_USE_ALTITUDE)
    }
    
    suspend fun saveUseAltitude(useAltitude: Boolean) {
        savePreference(PreferenceKeys.USE_ALTITUDE, useAltitude, KEY_USE_ALTITUDE, useAltitude)
    }
    
    // For backward compatibility
    fun getUseAltitude(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_ALTITUDE, DEFAULT_USE_ALTITUDE)
    }

    // Altitude
    fun getAltitudeFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.ALTITUDE, DEFAULT_ALTITUDE)
    }
    
    suspend fun saveAltitude(altitude: Double) {
        savePreference(PreferenceKeys.ALTITUDE, altitude, KEY_ALTITUDE, altitude)
    }
    
    // For backward compatibility
    fun getAltitude(): Double {
        val bits = sharedPrefs.getLong(KEY_ALTITUDE, java.lang.Double.doubleToRawLongBits(DEFAULT_ALTITUDE))
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Use Randomize
    fun getUseRandomizeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_RANDOMIZE, DEFAULT_USE_RANDOMIZE)
    }
    
    suspend fun saveUseRandomize(randomize: Boolean) {
        savePreference(PreferenceKeys.USE_RANDOMIZE, randomize, KEY_USE_RANDOMIZE, randomize)
    }
    
    // For backward compatibility
    fun getUseRandomize(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_RANDOMIZE, DEFAULT_USE_RANDOMIZE)
    }

    // Randomize Radius
    fun getRandomizeRadiusFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.RANDOMIZE_RADIUS, DEFAULT_RANDOMIZE_RADIUS)
    }
    
    suspend fun saveRandomizeRadius(radius: Double) {
        savePreference(PreferenceKeys.RANDOMIZE_RADIUS, radius, KEY_RANDOMIZE_RADIUS, radius)
    }
    
    // For backward compatibility
    fun getRandomizeRadius(): Double {
        val bits = sharedPrefs.getLong(
            KEY_RANDOMIZE_RADIUS,
            java.lang.Double.doubleToRawLongBits(DEFAULT_RANDOMIZE_RADIUS)
        )
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Favorites
    fun getFavoritesFlow(): Flow<List<FavoriteLocation>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferenceKeys.FAVORITES]
                if (json != null) {
                    try {
                        val type = object : TypeToken<List<FavoriteLocation>>() {}.type
                        gson.fromJson(json, type)
                    } catch (e: JsonSyntaxException) {
                        Log.e(tag, "Error parsing Favorites: ${e.message}")
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }
    }
    
    suspend fun addFavorite(favorite: FavoriteLocation) {
        Log.d(tag, "=== ADD_FAVORITE CALLED ===")
        Log.d(tag, "Adding favorite: $favorite")
        try {
            Log.d(tag, "Getting existing favorites...")
            val existingFavorites = getFavorites()
            Log.d(tag, "Existing favorites count: ${existingFavorites.size}")
            Log.d(tag, "Existing favorites: $existingFavorites")
            
            val existingFavoritesList = existingFavorites.toMutableList()
            // Check if favorite with same name already exists
            val existingIndex = existingFavoritesList.indexOfFirst { it.name == favorite.name }
            if (existingIndex != -1) {
                // Replace existing favorite
                existingFavoritesList[existingIndex] = favorite
                Log.d(tag, "Updated existing favorite at index $existingIndex: ${favorite.name}")
            } else {
                // Add new favorite
                existingFavoritesList.add(favorite)
                Log.d(tag, "Added new favorite: ${favorite.name}")
            }
            
            Log.d(tag, "Final favorites list count: ${existingFavoritesList.size}")
            Log.d(tag, "Final favorites list: $existingFavoritesList")
            
            Log.d(tag, "Calling saveFavorites...")
            saveFavorites(existingFavoritesList)
            Log.d(tag, "Successfully saved favorites")
        } catch (e: Exception) {
            Log.e(tag, "Error adding favorite: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun saveFavorites(favorites: List<FavoriteLocation>) {
        Log.d(tag, "=== SAVE_FAVORITES CALLED ===")
        Log.d(tag, "Saving ${favorites.size} favorites: $favorites")
        try {
            val json = gson.toJson(favorites)
            Log.d(tag, "JSON string: $json")
            
            Log.d(tag, "Calling savePreference...")
            savePreference(PreferenceKeys.FAVORITES, json, KEY_FAVORITES, json)
            Log.d(tag, "Successfully called savePreference")
            
            // Verify the save by reading back
            Log.d(tag, "Verifying save by reading back...")
            val savedFavorites = getFavorites()
            Log.d(tag, "Read back ${savedFavorites.size} favorites: $savedFavorites")
            
            Log.d(tag, "Saved ${favorites.size} favorites successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error saving favorites: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun removeFavorite(favorite: FavoriteLocation) {
        try {
            val existingFavorites = getFavorites().toMutableList()
            existingFavorites.removeAll { it.name == favorite.name }
            saveFavorites(existingFavorites)
            Log.d(tag, "Removed favorite: ${favorite.name}")
        } catch (e: Exception) {
            Log.e(tag, "Error removing favorite: ${e.message}")
        }
    }
    
    // For backward compatibility
    fun getFavorites(): List<FavoriteLocation> {
        val json = sharedPrefs.getString(KEY_FAVORITES, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<FavoriteLocation>>() {}.type
                gson.fromJson(json, type)
            } catch (e: JsonSyntaxException) {
                Log.e(tag, "Error parsing Favorites: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Vertical Accuracy
    fun getUseVerticalAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_VERTICAL_ACCURACY, DEFAULT_USE_VERTICAL_ACCURACY)
    }
    
    suspend fun saveUseVerticalAccuracy(useVerticalAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_VERTICAL_ACCURACY, useVerticalAccuracy, KEY_USE_VERTICAL_ACCURACY, useVerticalAccuracy)
    }
    
    // For backward compatibility
    fun getUseVerticalAccuracy(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_VERTICAL_ACCURACY, DEFAULT_USE_VERTICAL_ACCURACY)
    }

    // Vertical Accuracy Value
    fun getVerticalAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.VERTICAL_ACCURACY, DEFAULT_VERTICAL_ACCURACY)
    }
    
    suspend fun saveVerticalAccuracy(verticalAccuracy: Float) {
        savePreference(PreferenceKeys.VERTICAL_ACCURACY, verticalAccuracy, KEY_VERTICAL_ACCURACY, verticalAccuracy)
    }
    
    // For backward compatibility
    fun getVerticalAccuracy(): Float {
        return sharedPrefs.getFloat(KEY_VERTICAL_ACCURACY, DEFAULT_VERTICAL_ACCURACY)
    }

    // Use Mean Sea Level
    fun getUseMeanSeaLevelFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_MEAN_SEA_LEVEL, DEFAULT_USE_MEAN_SEA_LEVEL)
    }
    
    suspend fun saveUseMeanSeaLevel(useMeanSeaLevel: Boolean) {
        savePreference(PreferenceKeys.USE_MEAN_SEA_LEVEL, useMeanSeaLevel, KEY_USE_MEAN_SEA_LEVEL, useMeanSeaLevel)
    }
    
    // For backward compatibility
    fun getUseMeanSeaLevel(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_MEAN_SEA_LEVEL, DEFAULT_USE_MEAN_SEA_LEVEL)
    }

    // Mean Sea Level
    fun getMeanSeaLevelFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.MEAN_SEA_LEVEL, DEFAULT_MEAN_SEA_LEVEL)
    }
    
    suspend fun saveMeanSeaLevel(meanSeaLevel: Double) {
        savePreference(PreferenceKeys.MEAN_SEA_LEVEL, meanSeaLevel, KEY_MEAN_SEA_LEVEL, meanSeaLevel)
    }
    
    // For backward compatibility
    fun getMeanSeaLevel(): Double {
        val bits = sharedPrefs.getLong(KEY_MEAN_SEA_LEVEL, java.lang.Double.doubleToRawLongBits(DEFAULT_MEAN_SEA_LEVEL))
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Use Mean Sea Level Accuracy
    fun getUseMeanSeaLevelAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY)
    }
    
    suspend fun saveUseMeanSeaLevelAccuracy(useMeanSeaLevelAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_MEAN_SEA_LEVEL_ACCURACY, useMeanSeaLevelAccuracy, KEY_USE_MEAN_SEA_LEVEL_ACCURACY, useMeanSeaLevelAccuracy)
    }
    
    // For backward compatibility
    fun getUseMeanSeaLevelAccuracy(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY)
    }

    // Mean Sea Level Accuracy
    fun getMeanSeaLevelAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.MEAN_SEA_LEVEL_ACCURACY, DEFAULT_MEAN_SEA_LEVEL_ACCURACY)
    }
    
    suspend fun saveMeanSeaLevelAccuracy(meanSeaLevelAccuracy: Float) {
        savePreference(PreferenceKeys.MEAN_SEA_LEVEL_ACCURACY, meanSeaLevelAccuracy, KEY_MEAN_SEA_LEVEL_ACCURACY, meanSeaLevelAccuracy)
    }
    
    // For backward compatibility
    fun getMeanSeaLevelAccuracy(): Float {
        return sharedPrefs.getFloat(KEY_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_MEAN_SEA_LEVEL_ACCURACY)
    }

    // Use Speed
    fun getUseSpeedFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_SPEED, DEFAULT_USE_SPEED)
    }
    
    suspend fun saveUseSpeed(useSpeed: Boolean) {
        savePreference(PreferenceKeys.USE_SPEED, useSpeed, KEY_USE_SPEED, useSpeed)
    }
    
    // For backward compatibility
    fun getUseSpeed(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_SPEED, DEFAULT_USE_SPEED)
    }

    // Speed
    fun getSpeedFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.SPEED, DEFAULT_SPEED)
    }
    
    suspend fun saveSpeed(speed: Float) {
        savePreference(PreferenceKeys.SPEED, speed, KEY_SPEED, speed)
    }
    
    // For backward compatibility
    fun getSpeed(): Float {
        return sharedPrefs.getFloat(KEY_SPEED, DEFAULT_SPEED)
    }

    // Use Speed Accuracy
    fun getUseSpeedAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_SPEED_ACCURACY, DEFAULT_USE_SPEED_ACCURACY)
    }
    
    suspend fun saveUseSpeedAccuracy(useSpeedAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_SPEED_ACCURACY, useSpeedAccuracy, KEY_USE_SPEED_ACCURACY, useSpeedAccuracy)
    }
    
    // For backward compatibility
    fun getUseSpeedAccuracy(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_SPEED_ACCURACY, DEFAULT_USE_SPEED_ACCURACY)
    }

    // Speed Accuracy
    fun getSpeedAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.SPEED_ACCURACY, DEFAULT_SPEED_ACCURACY)
    }
    
    suspend fun saveSpeedAccuracy(speedAccuracy: Float) {
        savePreference(PreferenceKeys.SPEED_ACCURACY, speedAccuracy, KEY_SPEED_ACCURACY, speedAccuracy)
    }
    
    // For backward compatibility
    fun getSpeedAccuracy(): Float {
        return sharedPrefs.getFloat(KEY_SPEED_ACCURACY, DEFAULT_SPEED_ACCURACY)
    }

    // Map Zoom
    fun getMapZoomFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.MAP_ZOOM, DEFAULT_MAP_ZOOM)
    }
    
    suspend fun saveMapZoom(zoom: Double) {
        savePreference(PreferenceKeys.MAP_ZOOM, zoom, "map_zoom", zoom)
    }
    
    // For backward compatibility
    fun getMapZoom(): Double {
        val bits = sharedPrefs.getLong("map_zoom", java.lang.Double.doubleToRawLongBits(DEFAULT_MAP_ZOOM))
        return java.lang.Double.longBitsToDouble(bits)
    }
    
    // Reset map zoom to default
    suspend fun resetMapZoom() {
        saveMapZoom(DEFAULT_MAP_ZOOM)
    }

    // Map Latitude
    fun getMapLatitudeFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.MAP_LATITUDE, DEFAULT_MAP_LATITUDE)
    }
    
    suspend fun saveMapLatitude(latitude: Double) {
        savePreference(PreferenceKeys.MAP_LATITUDE, latitude, "map_latitude", latitude)
    }
    
    // For backward compatibility
    fun getMapLatitude(): Double {
        val bits = sharedPrefs.getLong("map_latitude", java.lang.Double.doubleToRawLongBits(DEFAULT_MAP_LATITUDE))
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Map Longitude
    fun getMapLongitudeFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.MAP_LONGITUDE, DEFAULT_MAP_LONGITUDE)
    }
    
    suspend fun saveMapLongitude(longitude: Double) {
        savePreference(PreferenceKeys.MAP_LONGITUDE, longitude, "map_longitude", longitude)
    }
    
    // For backward compatibility
    fun getMapLongitude(): Double {
        val bits = sharedPrefs.getLong("map_longitude", java.lang.Double.doubleToRawLongBits(DEFAULT_MAP_LONGITUDE))
        return java.lang.Double.longBitsToDouble(bits)
    }

    // Location History
    fun getLocationHistory(): List<String> {
        val json = sharedPrefs.getString("location_history", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type)
            } catch (e: JsonSyntaxException) {
                Log.e(tag, "Error parsing Location History: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun addToLocationHistory(location: String) {
        try {
            val history = getLocationHistory().toMutableList()
            history.add("${System.currentTimeMillis()}: $location")
            // Keep only last 100 entries
            if (history.size > 100) {
                history.removeAt(0)
            }
            val json = gson.toJson(history)
            savePreference(stringPreferencesKey("location_history"), json, "location_history", json)
        } catch (e: Exception) {
            Log.e(tag, "Error adding to location history: ${e.message}")
        }
    }
    
    suspend fun clearLocationHistory() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey("location_history"))
            }
            sharedPrefs.edit().remove("location_history").apply()
            Log.d(tag, "Location history cleared")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing location history: ${e.message}")
        }
    }

    fun getUseSystemHookFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_SYSTEM_HOOK, false)
    }
    suspend fun saveUseSystemHook(use: Boolean) {
        savePreference(PreferenceKeys.USE_SYSTEM_HOOK, use, "use_system_hook", use)
    }

    // Disable Night Map Mode
    fun getDisableNightMapModeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.DISABLE_NIGHT_MAP_MODE, false)
    }

    suspend fun saveDisableNightMapMode(disable: Boolean) {
        savePreference(PreferenceKeys.DISABLE_NIGHT_MAP_MODE, disable, "disable_night_map_mode", disable)
    }
}
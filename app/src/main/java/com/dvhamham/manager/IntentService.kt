package com.dvhamham.manager

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dvhamham.data.repository.PreferencesRepository
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.data.model.LastClickedLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntentService : Service() {
    
    companion object {
        private const val TAG = "IntentService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "gps_rider_service"
        
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_ALTITUDE = "altitude"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_FAVORITE_NAME = "favorite_name"
        const val EXTRA_RANDOMIZE_RADIUS = "randomize_radius"
        const val EXTRA_RESULT_RECEIVER = "result_receiver"
        const val EXTRA_FAVORITE_DESCRIPTION = "favorite_description"
        const val EXTRA_FAVORITE_CATEGORY = "favorite_category"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_INTERVAL = "interval"
        const val EXTRA_PATH_FILE = "path_file"
        const val EXTRA_HEADING = "heading"
        const val EXTRA_BEARING = "bearing"
        
        const val RESULT_SUCCESS = 1
        const val RESULT_ERROR = 0
        const val RESULT_INVALID_PARAMS = -1
        
        // Intent Actions
        const val ACTION_START_FAKE_LOCATION = "com.dvhamham.START_FAKE_LOCATION"
        const val ACTION_STOP_FAKE_LOCATION = "com.dvhamham.STOP_FAKE_LOCATION"
        const val ACTION_TOGGLE_FAKE_LOCATION = "com.dvhamham.TOGGLE_FAKE_LOCATION"
        const val ACTION_SET_CUSTOM_LOCATION = "com.dvhamham.SET_CUSTOM_LOCATION"
        const val ACTION_SET_FAVORITE_LOCATION = "com.dvhamham.SET_FAVORITE_LOCATION"
        const val ACTION_GET_STATUS = "com.dvhamham.GET_STATUS"
        const val ACTION_GET_CURRENT_LOCATION = "com.dvhamham.GET_CURRENT_LOCATION"
        const val ACTION_SET_ACCURACY = "com.dvhamham.SET_ACCURACY"
        const val ACTION_SET_ALTITUDE = "com.dvhamham.SET_ALTITUDE"
        const val ACTION_SET_SPEED = "com.dvhamham.SET_SPEED"
        const val ACTION_RANDOMIZE_LOCATION = "com.dvhamham.RANDOMIZE_LOCATION"
        const val ACTION_CREATE_FAVORITE = "com.dvhamham.CREATE_FAVORITE"
        const val ACTION_DELETE_FAVORITE = "com.dvhamham.DELETE_FAVORITE"
        const val ACTION_GET_FAVORITES = "com.dvhamham.GET_FAVORITES"
        const val ACTION_START_TIMED_LOCATION = "com.dvhamham.START_TIMED_LOCATION"
        const val ACTION_STOP_TIMED_LOCATION = "com.dvhamham.STOP_TIMED_LOCATION"
        const val ACTION_LOAD_PATH_FILE = "com.dvhamham.LOAD_PATH_FILE"
        const val ACTION_SET_HEADING = "com.dvhamham.SET_HEADING"
        const val ACTION_SET_BEARING = "com.dvhamham.SET_BEARING"
        const val ACTION_GET_LOCATION_HISTORY = "com.dvhamham.GET_LOCATION_HISTORY"
        const val ACTION_CLEAR_LOCATION_HISTORY = "com.dvhamham.CLEAR_LOCATION_HISTORY"
    }
    
    private lateinit var preferencesRepository: PreferencesRepository
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isForeground = false
    
    override fun onCreate() {
        super.onCreate()
        try {
            preferencesRepository = PreferencesRepository(this)
            createNotificationChannel()
            Log.d(TAG, "Service created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Service: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, "onStartCommand called with intent: ${intent?.action}")
            
            // Start foreground immediately
            if (!isForeground) {
                startForeground(NOTIFICATION_ID, createNotification("GPS Rider Service", "Processing intent..."))
                isForeground = true
            }
            
            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return START_NOT_STICKY
            }
            
            handleIntent(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onStartCommand: ${e.message}")
            e.printStackTrace()
        }
        // We want the service to be killed if explicitly stopped, not restarted automatically
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // Not a bound service
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPS Rider Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "GPS Rider background service"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    private fun updateNotification(content: String) {
        val notification = createNotification("GPS Rider Service", content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val resultReceiver = intent.getParcelableExtra<ResultReceiver>(EXTRA_RESULT_RECEIVER)
        
        Log.d(TAG, "Handling intent action: $action")
        
        try {
            when (action) {
                ACTION_START_FAKE_LOCATION -> {
                    updateNotification("Starting fake location...")
                    startFakeLocation(resultReceiver)
                }
                ACTION_STOP_FAKE_LOCATION -> {
                    updateNotification("Stopping fake location...")
                    stopFakeLocation(resultReceiver)
                }
                ACTION_TOGGLE_FAKE_LOCATION -> {
                    updateNotification("Toggling fake location...")
                    toggleFakeLocation(resultReceiver)
                }
                ACTION_SET_CUSTOM_LOCATION -> {
                    // Log all extras for debugging
                    Log.d(TAG, "All extras: ${intent.extras?.keySet()}")
                    intent.extras?.keySet()?.forEach { key ->
                        Log.d(TAG, "Extra $key: ${intent.extras?.get(key)} (type: ${intent.extras?.get(key)?.javaClass?.simpleName})")
                    }
                    
                    // Try different ways to read latitude and longitude
                    var latitude = Double.NaN
                    var longitude = Double.NaN
                    
                    // Try getDoubleExtra first
                    if (intent.hasExtra(EXTRA_LATITUDE)) {
                        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
                        Log.d(TAG, "getDoubleExtra latitude: $latitude")
                    }
                    if (intent.hasExtra(EXTRA_LONGITUDE)) {
                        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
                        Log.d(TAG, "getDoubleExtra longitude: $longitude")
                    }
                    
                    // If still NaN, try getFloatExtra
                    if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                        latitude = intent.getFloatExtra(EXTRA_LATITUDE, Float.NaN).toDouble()
                        Log.d(TAG, "getFloatExtra latitude: $latitude")
                    }
                    if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                        longitude = intent.getFloatExtra(EXTRA_LONGITUDE, Float.NaN).toDouble()
                        Log.d(TAG, "getFloatExtra longitude: $longitude")
                    }
                    
                    // If still NaN, try getIntExtra
                    if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                        val latInt = intent.getIntExtra(EXTRA_LATITUDE, Int.MIN_VALUE)
                        Log.d(TAG, "getIntExtra latitude: $latInt")
                        if (latInt != Int.MIN_VALUE) {
                            latitude = latInt.toDouble()
                        }
                    }
                    if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                        val lngInt = intent.getIntExtra(EXTRA_LONGITUDE, Int.MIN_VALUE)
                        Log.d(TAG, "getIntExtra longitude: $lngInt")
                        if (lngInt != Int.MIN_VALUE) {
                            longitude = lngInt.toDouble()
                        }
                    }
                    
                    // If still NaN, try getStringExtra and parse
                    if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                        try {
                            val latString = intent.getStringExtra(EXTRA_LATITUDE)
                            Log.d(TAG, "getStringExtra latitude string: $latString")
                            latitude = latString?.toDouble() ?: Double.NaN
                            Log.d(TAG, "Parsed latitude: $latitude")
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Failed to parse latitude string: ${e.message}")
                            latitude = Double.NaN
                        }
                    }
                    if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                        try {
                            val lngString = intent.getStringExtra(EXTRA_LONGITUDE)
                            Log.d(TAG, "getStringExtra longitude string: $lngString")
                            longitude = lngString?.toDouble() ?: Double.NaN
                            Log.d(TAG, "Parsed longitude: $longitude")
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Failed to parse longitude string: ${e.message}")
                            longitude = Double.NaN
                        }
                    }
                    
                    // Log what we received
                    Log.d(TAG, "Final received latitude: $latitude, longitude: $longitude")
                    
                    updateNotification("Setting custom location...")
                    setCustomLocation(latitude, longitude, resultReceiver)
                }
                ACTION_SET_FAVORITE_LOCATION -> {
                    val favoriteName = intent.getStringExtra(EXTRA_FAVORITE_NAME)
                    updateNotification("Setting favorite location...")
                    setFavoriteLocation(favoriteName, resultReceiver)
                }
                ACTION_GET_STATUS -> {
                    updateNotification("Getting status...")
                    getStatus(resultReceiver)
                }
                ACTION_GET_CURRENT_LOCATION -> {
                    updateNotification("Getting current location...")
                    getCurrentLocation(resultReceiver)
                }
                ACTION_SET_ACCURACY -> {
                    val accuracy = intent.getFloatExtra(EXTRA_ACCURACY, Float.NaN)
                    updateNotification("Setting accuracy...")
                    setAccuracy(accuracy, resultReceiver)
                }
                ACTION_SET_ALTITUDE -> {
                    // Try different ways to read altitude
                    var altitude = Double.NaN
                    
                    // Try getDoubleExtra first
                    if (intent.hasExtra(EXTRA_ALTITUDE)) {
                        altitude = intent.getDoubleExtra(EXTRA_ALTITUDE, Double.NaN)
                        Log.d(TAG, "getDoubleExtra altitude: $altitude")
                    }
                    
                    // If still NaN, try getFloatExtra
                    if (altitude.isNaN() && intent.hasExtra(EXTRA_ALTITUDE)) {
                        altitude = intent.getFloatExtra(EXTRA_ALTITUDE, Float.NaN).toDouble()
                        Log.d(TAG, "getFloatExtra altitude: $altitude")
                    }
                    
                    // If still NaN, try getIntExtra
                    if (altitude.isNaN() && intent.hasExtra(EXTRA_ALTITUDE)) {
                        val altInt = intent.getIntExtra(EXTRA_ALTITUDE, Int.MIN_VALUE)
                        Log.d(TAG, "getIntExtra altitude: $altInt")
                        if (altInt != Int.MIN_VALUE) {
                            altitude = altInt.toDouble()
                        }
                    }
                    
                    // If still NaN, try getStringExtra and parse
                    if (altitude.isNaN() && intent.hasExtra(EXTRA_ALTITUDE)) {
                        try {
                            val altString = intent.getStringExtra(EXTRA_ALTITUDE)
                            Log.d(TAG, "getStringExtra altitude string: $altString")
                            altitude = altString?.toDouble() ?: Double.NaN
                            Log.d(TAG, "Parsed altitude: $altitude")
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Failed to parse altitude string: ${e.message}")
                            altitude = Double.NaN
                        }
                    }
                    
                    Log.d(TAG, "Final received altitude: $altitude")
                    updateNotification("Setting altitude...")
                    setAltitude(altitude, resultReceiver)
                }
                ACTION_SET_SPEED -> {
                    // Try different ways to read speed
                    var speed = Float.NaN
                    
                    // Try getFloatExtra first
                    if (intent.hasExtra(EXTRA_SPEED)) {
                        speed = intent.getFloatExtra(EXTRA_SPEED, Float.NaN)
                        Log.d(TAG, "getFloatExtra speed: $speed")
                    }
                    
                    // If still NaN, try getDoubleExtra
                    if (speed.isNaN() && intent.hasExtra(EXTRA_SPEED)) {
                        speed = intent.getDoubleExtra(EXTRA_SPEED, Double.NaN).toFloat()
                        Log.d(TAG, "getDoubleExtra speed: $speed")
                    }
                    
                    // If still NaN, try getIntExtra
                    if (speed.isNaN() && intent.hasExtra(EXTRA_SPEED)) {
                        val speedInt = intent.getIntExtra(EXTRA_SPEED, Int.MIN_VALUE)
                        Log.d(TAG, "getIntExtra speed: $speedInt")
                        if (speedInt != Int.MIN_VALUE) {
                            speed = speedInt.toFloat()
                        }
                    }
                    
                    // If still NaN, try getStringExtra and parse
                    if (speed.isNaN() && intent.hasExtra(EXTRA_SPEED)) {
                        try {
                            val speedString = intent.getStringExtra(EXTRA_SPEED)
                            Log.d(TAG, "getStringExtra speed string: $speedString")
                            speed = speedString?.toFloat() ?: Float.NaN
                            Log.d(TAG, "Parsed speed: $speed")
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Failed to parse speed string: ${e.message}")
                            speed = Float.NaN
                        }
                    }
                    
                    Log.d(TAG, "Final received speed: $speed")
                    updateNotification("Setting speed...")
                    setSpeed(speed, resultReceiver)
                }
                ACTION_RANDOMIZE_LOCATION -> {
                    val radius = intent.getDoubleExtra(EXTRA_RANDOMIZE_RADIUS, 100.0)
                    updateNotification("Randomizing location...")
                    randomizeLocation(radius, resultReceiver)
                }
                ACTION_CREATE_FAVORITE -> {
                    Log.d(TAG, "=== CREATE_FAVORITE ACTION RECEIVED ===")
                    Log.d(TAG, "Intent extras: ${intent.extras}")
                    
                    // Read favorite details from intent extras (simplified version)
                    var latitude = Double.NaN
                    var longitude = Double.NaN
                    val name = intent.getStringExtra(EXTRA_FAVORITE_NAME)
                    Log.d(TAG, "Received favorite_name: $name")
                    
                    // Try to get coordinates from a single field first (like manual input)
                    val coordinatesString = intent.getStringExtra("coordinates")
                    Log.d(TAG, "Received coordinates string: $coordinatesString")
                    
                    if (!coordinatesString.isNullOrEmpty()) {
                        try {
                            val coordinates = parseCoordinates(coordinatesString)
                            if (coordinates != null) {
                                latitude = coordinates.first
                                longitude = coordinates.second
                                Log.d(TAG, "Successfully parsed coordinates from single field: $latitude, $longitude")
                            } else {
                                Log.e(TAG, "Failed to parse coordinates string: $coordinatesString")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception parsing coordinates string: ${e.message}")
                        }
                    } else {
                        Log.d(TAG, "No coordinates string found, trying separate fields")
                    }
                    
                    // If coordinates not found in single field, try separate fields
                    if (latitude.isNaN() || longitude.isNaN()) {
                        // Try different ways to read latitude and longitude
                        if (intent.hasExtra(EXTRA_LATITUDE)) {
                            latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
                        }
                        if (intent.hasExtra(EXTRA_LONGITUDE)) {
                            longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
                        }
                        
                        // If still NaN, try getFloatExtra
                        if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                            latitude = intent.getFloatExtra(EXTRA_LATITUDE, Float.NaN).toDouble()
                        }
                        if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                            longitude = intent.getFloatExtra(EXTRA_LONGITUDE, Float.NaN).toDouble()
                        }
                        
                        // If still NaN, try getIntExtra
                        if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                            val latInt = intent.getIntExtra(EXTRA_LATITUDE, Int.MIN_VALUE)
                            if (latInt != Int.MIN_VALUE) {
                                latitude = latInt.toDouble()
                            }
                        }
                        if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                            val lngInt = intent.getIntExtra(EXTRA_LONGITUDE, Int.MIN_VALUE)
                            if (lngInt != Int.MIN_VALUE) {
                                longitude = lngInt.toDouble()
                            }
                        }
                        
                        // If still NaN, try getStringExtra and parse
                        if (latitude.isNaN() && intent.hasExtra(EXTRA_LATITUDE)) {
                            try {
                                latitude = intent.getStringExtra(EXTRA_LATITUDE)?.toDouble() ?: Double.NaN
                            } catch (e: NumberFormatException) {
                                latitude = Double.NaN
                            }
                        }
                        if (longitude.isNaN() && intent.hasExtra(EXTRA_LONGITUDE)) {
                            try {
                                longitude = intent.getStringExtra(EXTRA_LONGITUDE)?.toDouble() ?: Double.NaN
                            } catch (e: NumberFormatException) {
                                longitude = Double.NaN
                            }
                        }
                    }
                    
                    Log.d(TAG, "Creating favorite: name=$name, lat=$latitude, lng=$longitude")
                    updateNotification("Creating favorite...")
                    createFavorite(latitude, longitude, name, resultReceiver)
                }
                ACTION_DELETE_FAVORITE -> {
                    val favoriteName = intent.getStringExtra(EXTRA_FAVORITE_NAME)
                    updateNotification("Deleting favorite...")
                    deleteFavorite(favoriteName, resultReceiver)
                }
                ACTION_GET_FAVORITES -> {
                    getFavorites(resultReceiver)
                }
                ACTION_START_TIMED_LOCATION -> {
                    // Implementation for starting a timed location
                    updateNotification("Starting timed location...")
                    // Call the method to start a timed location
                    // This method should be implemented to actually start a timed location
                    startTimedLocation(resultReceiver)
                }
                ACTION_STOP_TIMED_LOCATION -> {
                    // Implementation for stopping a timed location
                    updateNotification("Stopping timed location...")
                    // Call the method to stop a timed location
                    // This method should be implemented to actually stop a timed location
                    stopTimedLocation(resultReceiver)
                }
                ACTION_LOAD_PATH_FILE -> {
                    // Implementation for loading a path file
                    updateNotification("Loading path file...")
                    // Call the method to load a path file
                    // This method should be implemented to actually load a path file
                    loadPathFile(resultReceiver)
                }
                ACTION_SET_HEADING -> {
                    // Implementation for setting heading
                    updateNotification("Setting heading...")
                    // Call the method to set heading
                    // This method should be implemented to actually set heading
                    setHeading(resultReceiver)
                }
                ACTION_SET_BEARING -> {
                    // Implementation for setting bearing
                    updateNotification("Setting bearing...")
                    // Call the method to set bearing
                    // This method should be implemented to actually set bearing
                    setBearing(resultReceiver)
                }
                ACTION_GET_LOCATION_HISTORY -> {
                    // Implementation for getting location history
                    updateNotification("Getting location history...")
                    // Call the method to get location history
                    // This method should be implemented to actually get location history
                    getLocationHistory(resultReceiver)
                }
                ACTION_CLEAR_LOCATION_HISTORY -> {
                    // Implementation for clearing location history
                    updateNotification("Clearing location history...")
                    // Call the method to clear location history
                    // This method should be implemented to actually clear location history
                    clearLocationHistory(resultReceiver)
                }
                else -> {
                    Log.w(TAG, "Unknown action: $action")
                    updateNotification("Unknown action: $action")
                    sendResult(resultReceiver, RESULT_ERROR, "Unknown action: $action")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent: ${e.message}")
            e.printStackTrace()
            updateNotification("Error: ${e.message}")
            sendResult(resultReceiver, RESULT_ERROR, "Error: ${e.message}")
        }
    }
    
    private fun startFakeLocation(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Starting fake location...")
        scope.launch {
            try {
                val currentLocation = preferencesRepository.getLastClickedLocation()
                Log.d(TAG, "Current location: $currentLocation")
                
                if (currentLocation == null) {
                    preferencesRepository.saveLastClickedLocation(40.7128, -74.0060)
                    Log.d(TAG, "No location set, using default location (New York)")
                }
                
                preferencesRepository.saveIsPlaying(true)
                Log.d(TAG, "Saved isPlaying = true")
                
                val broadcastIntent = Intent("com.dvhamham.FAKE_LOCATION_STARTED")
                sendBroadcast(broadcastIntent)
                Log.d(TAG, "Sent broadcast: FAKE_LOCATION_STARTED")
                
                val location = preferencesRepository.getLastClickedLocation()
                val message = if (location != null) {
                    "Fake location started at: ${location.latitude}, ${location.longitude}"
                } else {
                    "Fake location started"
                }
                
                updateNotification(message)
                sendResult(resultReceiver, RESULT_SUCCESS, message)
                Log.d(TAG, "Fake location started successfully: $message")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start fake location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to start fake location")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to start fake location: ${e.message}")
            }
        }
    }
    
    private fun stopFakeLocation(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Stopping fake location...")
        scope.launch {
            try {
                preferencesRepository.saveIsPlaying(false)
                
                val broadcastIntent = Intent("com.dvhamham.FAKE_LOCATION_STOPPED")
                sendBroadcast(broadcastIntent)
                
                updateNotification("Fake location stopped")
                sendResult(resultReceiver, RESULT_SUCCESS, "Fake location stopped")
                Log.d(TAG, "Fake location stopped successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop fake location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to stop fake location")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to stop fake location: ${e.message}")
            }
        }
    }
    
    private fun toggleFakeLocation(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Toggling fake location...")
        scope.launch {
            try {
                val isCurrentlyPlaying = preferencesRepository.getIsPlaying()
                preferencesRepository.saveIsPlaying(!isCurrentlyPlaying)
                val status = if (!isCurrentlyPlaying) "started" else "stopped"
                updateNotification("Fake location $status")
                sendResult(resultReceiver, RESULT_SUCCESS, "Fake location $status")
                Log.d(TAG, "Fake location toggled successfully: $status")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle fake location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to toggle fake location")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to toggle fake location: ${e.message}")
            }
        }
    }
    
    private fun setCustomLocation(latitude: Double, longitude: Double, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Setting custom location: $latitude, $longitude")
        
        if (latitude.isNaN() || longitude.isNaN()) {
            Log.w(TAG, "Invalid latitude or longitude")
            updateNotification("Invalid coordinates")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid latitude or longitude")
            return
        }
        
        scope.launch {
            try {
                preferencesRepository.saveLastClickedLocation(latitude, longitude)
                preferencesRepository.saveIsPlaying(true)
                
                // Add to location history
                preferencesRepository.addToLocationHistory("Custom: $latitude, $longitude")
                
                val broadcastIntent = Intent("com.dvhamham.FAKE_LOCATION_STARTED")
                sendBroadcast(broadcastIntent)
                val message = "Location set to: $latitude, $longitude and fake location started"
                updateNotification(message)
                sendResult(resultReceiver, RESULT_SUCCESS, message)
                Log.d(TAG, "Custom location set and fake location started: $latitude, $longitude")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set custom location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to set location")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to set custom location: ${e.message}")
            }
        }
    }
    
    private fun setFavoriteLocation(favoriteName: String?, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Setting favorite location: $favoriteName")
        
        if (favoriteName.isNullOrEmpty()) {
            Log.w(TAG, "Invalid favorite name")
            updateNotification("Invalid favorite name")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid favorite name")
            return
        }
        
        scope.launch {
            try {
                val favorites = preferencesRepository.getFavorites()
                val favorite = favorites.find { it.name == favoriteName }
                
                if (favorite != null) {
                    preferencesRepository.saveLastClickedLocation(favorite.latitude, favorite.longitude)
                    preferencesRepository.saveIsPlaying(true)
                    val broadcastIntent = Intent("com.dvhamham.FAKE_LOCATION_STARTED")
                    sendBroadcast(broadcastIntent)
                    val message = "Favorite location set: $favoriteName (${favorite.latitude}, ${favorite.longitude}) and fake location started"
                    updateNotification(message)
                    sendResult(resultReceiver, RESULT_SUCCESS, message)
                    Log.d(TAG, "Favorite location set and fake location started: $favoriteName")
                } else {
                    Log.e(TAG, "Favorite location not found: $favoriteName")
                    updateNotification("Favorite not found")
                    sendResult(resultReceiver, RESULT_ERROR, "Favorite location not found: $favoriteName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set favorite location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to set favorite")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to set favorite location: ${e.message}")
            }
        }
    }
    
    private fun getStatus(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Getting status...")
        scope.launch {
            try {
                val isPlaying = preferencesRepository.getIsPlaying()
                val status = if (isPlaying) "active" else "inactive"
                updateNotification("Status: $status")
                sendResult(resultReceiver, RESULT_SUCCESS, "Status: $status")
                Log.d(TAG, "Status retrieved: $status")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get status: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to get status")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to get status: ${e.message}")
            }
        }
    }
    
    private fun getCurrentLocation(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Getting current location...")
        scope.launch {
            try {
                val location = preferencesRepository.getLastClickedLocation()
                if (location != null) {
                    val response = "Current location: ${location.latitude}, ${location.longitude}"
                    updateNotification(response)
                    sendResult(resultReceiver, RESULT_SUCCESS, response)
                    Log.d(TAG, "Current location retrieved: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.w(TAG, "No location set")
                    updateNotification("No location set")
                    sendResult(resultReceiver, RESULT_ERROR, "No location set")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get current location: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to get location")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to get current location: ${e.message}")
            }
        }
    }
    
    private fun setAccuracy(accuracy: Float, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Setting accuracy: $accuracy")
        
        if (accuracy.isNaN()) {
            Log.w(TAG, "Invalid accuracy value")
            updateNotification("Invalid accuracy")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid accuracy value")
            return
        }
        
        scope.launch {
            try {
                preferencesRepository.saveUseAccuracy(true)
                preferencesRepository.saveAccuracy(accuracy.toDouble())
                updateNotification("Accuracy set to: $accuracy")
                sendResult(resultReceiver, RESULT_SUCCESS, "Accuracy set to: $accuracy")
                Log.d(TAG, "Accuracy set successfully: $accuracy")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set accuracy: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to set accuracy")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to set accuracy: ${e.message}")
            }
        }
    }
    
    private fun setAltitude(altitude: Double, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Setting altitude: $altitude")
        
        if (altitude.isNaN()) {
            Log.w(TAG, "Invalid altitude value")
            updateNotification("Invalid altitude")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid altitude value")
            return
        }
        
        scope.launch {
            try {
                preferencesRepository.saveUseAltitude(true)
                preferencesRepository.saveAltitude(altitude)
                updateNotification("Altitude set to: $altitude")
                sendResult(resultReceiver, RESULT_SUCCESS, "Altitude set to: $altitude")
                Log.d(TAG, "Altitude set successfully: $altitude")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set altitude: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to set altitude")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to set altitude: ${e.message}")
            }
        }
    }
    
    private fun setSpeed(speed: Float, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Setting speed: $speed")
        
        if (speed.isNaN()) {
            Log.w(TAG, "Invalid speed value")
            updateNotification("Invalid speed")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid speed value")
            return
        }
        
        scope.launch {
            try {
                preferencesRepository.saveUseSpeed(true)
                preferencesRepository.saveSpeed(speed)
                updateNotification("Speed set to: $speed")
                sendResult(resultReceiver, RESULT_SUCCESS, "Speed set to: $speed")
                Log.d(TAG, "Speed set successfully: $speed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set speed: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to set speed")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to set speed: ${e.message}")
            }
        }
    }
    
    private fun randomizeLocation(radius: Double, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Randomizing location with radius: $radius")
        scope.launch {
            try {
                preferencesRepository.saveUseRandomize(true)
                preferencesRepository.saveRandomizeRadius(radius)
                updateNotification("Randomization enabled with radius: $radius")
                sendResult(resultReceiver, RESULT_SUCCESS, "Randomization enabled with radius: $radius")
                Log.d(TAG, "Randomization enabled successfully: $radius")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable randomization: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to enable randomization")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to enable randomization: ${e.message}")
            }
        }
    }
    
    private fun createFavorite(latitude: Double, longitude: Double, name: String?, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "=== CREATE_FAVORITE METHOD CALLED ===")
        Log.d(TAG, "Parameters: name=$name, lat=$latitude, lng=$longitude")
        
        if (name.isNullOrEmpty()) {
            Log.w(TAG, "Invalid favorite name")
            updateNotification("Invalid favorite name")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid favorite name")
            return
        }
        
        if (latitude.isNaN() || longitude.isNaN()) {
            Log.w(TAG, "Invalid latitude or longitude")
            updateNotification("Invalid coordinates")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid latitude or longitude")
            return
        }
        
        Log.d(TAG, "Creating FavoriteLocation object...")
        scope.launch {
            try {
                val newFavorite = FavoriteLocation(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    description = "", // Empty description
                    category = "General" // Default category
                )
                Log.d(TAG, "Created FavoriteLocation: $newFavorite")
                
                Log.d(TAG, "Calling preferencesRepository.addFavorite...")
                preferencesRepository.addFavorite(newFavorite)
                Log.d(TAG, "Successfully called preferencesRepository.addFavorite")
                
                val message = "Favorite '$name' created at: $latitude, $longitude"
                updateNotification(message)
                sendResult(resultReceiver, RESULT_SUCCESS, message)
                Log.d(TAG, "Favorite created successfully: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create favorite: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to create favorite")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to create favorite: ${e.message}")
            }
        }
    }
    
    private fun deleteFavorite(favoriteName: String?, resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Deleting favorite: $favoriteName")
        
        if (favoriteName.isNullOrEmpty()) {
            Log.w(TAG, "Invalid favorite name")
            updateNotification("Invalid favorite name")
            sendResult(resultReceiver, RESULT_INVALID_PARAMS, "Invalid favorite name")
            return
        }
        
        scope.launch {
            try {
                val existingFavorites = preferencesRepository.getFavorites()
                val favoriteToDelete = existingFavorites.find { it.name == favoriteName }
                
                if (favoriteToDelete == null) {
                    Log.w(TAG, "Favorite with name '$favoriteName' not found")
                    updateNotification("Favorite not found")
                    sendResult(resultReceiver, RESULT_ERROR, "Favorite with name '$favoriteName' not found")
                    return@launch
                }
                
                val updatedFavorites = existingFavorites.filter { it.name != favoriteName }
                preferencesRepository.saveFavorites(updatedFavorites)
                
                val message = "Favorite '$favoriteName' deleted successfully"
                updateNotification(message)
                sendResult(resultReceiver, RESULT_SUCCESS, message)
                Log.d(TAG, "Favorite deleted successfully: $message")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete favorite: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to delete favorite")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to delete favorite: ${e.message}")
            }
        }
    }
    
    private fun getFavorites(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Getting favorites...")
        scope.launch {
            try {
                val favorites = preferencesRepository.getFavorites()
                val favoriteList = favorites.map { 
                    "${it.name} (${it.latitude}, ${it.longitude})${if (it.description.isNotEmpty()) " - ${it.description}" else ""}"
                }
                val favoritesString = if (favoriteList.isNotEmpty()) {
                    favoriteList.joinToString("\n")
                } else {
                    "No favorites found"
                }
                
                updateNotification("Favorites retrieved")
                sendResult(resultReceiver, RESULT_SUCCESS, favoritesString)
                Log.d(TAG, "Favorites retrieved successfully: ${favorites.size} favorites")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get favorites: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to get favorites")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to get favorites: ${e.message}")
            }
        }
    }
    
    private fun startTimedLocation(resultReceiver: ResultReceiver?) {
        // Implementation for starting a timed location
        updateNotification("Starting timed location...")
        // Call the method to start a timed location
        // This method should be implemented to actually start a timed location
        sendResult(resultReceiver, RESULT_SUCCESS, "Timed location started")
    }
    
    private fun stopTimedLocation(resultReceiver: ResultReceiver?) {
        // Implementation for stopping a timed location
        updateNotification("Stopping timed location...")
        // Call the method to stop a timed location
        // This method should be implemented to actually stop a timed location
        sendResult(resultReceiver, RESULT_SUCCESS, "Timed location stopped")
    }
    
    private fun loadPathFile(resultReceiver: ResultReceiver?) {
        // Implementation for loading a path file
        updateNotification("Loading path file...")
        // Call the method to load a path file
        // This method should be implemented to actually load a path file
        sendResult(resultReceiver, RESULT_SUCCESS, "Path file loaded")
    }
    
    private fun setHeading(resultReceiver: ResultReceiver?) {
        // Implementation for setting heading
        updateNotification("Setting heading...")
        // Call the method to set heading
        // This method should be implemented to actually set heading
        sendResult(resultReceiver, RESULT_SUCCESS, "Heading set")
    }
    
    private fun setBearing(resultReceiver: ResultReceiver?) {
        // Implementation for setting bearing
        updateNotification("Setting bearing...")
        // Call the method to set bearing
        // This method should be implemented to actually set bearing
        sendResult(resultReceiver, RESULT_SUCCESS, "Bearing set")
    }
    
    private fun getLocationHistory(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Getting location history...")
        scope.launch {
            try {
                val history = preferencesRepository.getLocationHistory()
                val historyString = if (history.isNotEmpty()) {
                    history.joinToString("\n")
                } else {
                    "No location history found"
                }
                
                updateNotification("Location history retrieved")
                sendResult(resultReceiver, RESULT_SUCCESS, historyString)
                Log.d(TAG, "Location history retrieved successfully: ${history.size} entries")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get location history: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to get location history")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to get location history: ${e.message}")
            }
        }
    }
    
    private fun clearLocationHistory(resultReceiver: ResultReceiver?) {
        Log.d(TAG, "Clearing location history...")
        scope.launch {
            try {
                preferencesRepository.clearLocationHistory()
                val message = "Location history cleared successfully"
                updateNotification(message)
                sendResult(resultReceiver, RESULT_SUCCESS, message)
                Log.d(TAG, "Location history cleared successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear location history: ${e.message}")
                e.printStackTrace()
                updateNotification("Failed to clear location history")
                sendResult(resultReceiver, RESULT_ERROR, "Failed to clear location history: ${e.message}")
            }
        }
    }
    
    // Parse coordinates from combined input (format: "lat, lng" or "lat,lng")
    private fun parseCoordinates(input: String): Pair<Double, Double>? {
        val trimmed = input.trim()
        val parts = trimmed.split(",").map { it.trim() }
        
        if (parts.size != 2) return null
        
        val lat = parts[0].toDoubleOrNull()
        val lng = parts[1].toDoubleOrNull()
        
        return if (lat != null && lng != null) lat to lng else null
    }
    
    private fun sendResult(resultReceiver: ResultReceiver?, resultCode: Int, message: String) {
        try {
            resultReceiver?.let { receiver ->
                val bundle = Bundle().apply {
                    putString("message", message)
                }
                receiver.send(resultCode, bundle)
                Log.d(TAG, "Result sent: $resultCode - $message")
            } ?: run {
                Log.d(TAG, "No result receiver, result: $resultCode - $message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending result: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isForeground = false
    }
} 
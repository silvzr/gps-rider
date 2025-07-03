package com.dvhamham.manager.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dvhamham.data.DEFAULT_MAP_ZOOM
import com.dvhamham.data.WORLD_MAP_ZOOM
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.data.model.LatLng
import com.dvhamham.data.repository.PreferencesRepository
import com.dvhamham.manager.IntentHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Sealed classes to represent different dialog states
 */
sealed class DialogState {
    object Hidden : DialogState()
    object Visible : DialogState()
}

/**
 * Sealed class to represent different loading states
 */
sealed class LoadingState {
    object Loading : LoadingState()
    object Loaded : LoadingState()
}

/**
 * ViewModel for the Map screen that manages map-related state and operations.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)
    private val locationService = LocationService(application)

    /**
     * Represents field input state with value and validation error message
     */
    data class InputFieldState(val value: String = "", val errorMessage: String? = null)

    /**
     * Represents the UI state for the favorites input dialog
     */
    data class FavoritesInputState(
        val name: InputFieldState = InputFieldState(),
        val coordinates: InputFieldState = InputFieldState()
    )

    /**
     * Represents the complete UI state for the Map screen
     */
    data class MapUiState(
        val isPlaying: Boolean = false,
        val lastClickedLocation: LatLng? = null,
        val fakeLocationPoint: LatLng? = null,
        val userLocation: LatLng? = null,
        val loadingState: LoadingState = LoadingState.Loading,
        val mapZoom: Double? = null,
        val mapLatitude: Double? = null,
        val mapLongitude: Double? = null,
        val goToPointDialogState: DialogState = DialogState.Hidden,
        val addToFavoritesDialogState: DialogState = DialogState.Hidden,
        val goToPointState: InputFieldState = InputFieldState(),
        val addToFavoritesState: FavoritesInputState = FavoritesInputState()
    ) {
        val isFabClickable: Boolean
            get() = lastClickedLocation != null
    }

    // Private mutable state
    private val _uiState = MutableStateFlow(MapUiState())
    
    // Public immutable state
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Events
    private val _goToPointEvent = MutableSharedFlow<LatLng>()
    val goToPointEvent: SharedFlow<LatLng> = _goToPointEvent.asSharedFlow()

    private val _centerMapEvent = MutableSharedFlow<Unit>()
    val centerMapEvent: SharedFlow<Unit> = _centerMapEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            // Load initial isPlaying state
            preferencesRepository.getIsPlayingFlow().collectLatest { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }
        
        viewModelScope.launch {
            // Load initial lastClickedLocation
            preferencesRepository.getLastClickedLocationFlow().collectLatest { location ->
                val latLng = location?.let { LatLng(it.latitude, it.longitude) }
                _uiState.update { it.copy(lastClickedLocation = latLng) }
            }
        }
        
        viewModelScope.launch {
            // Load initial map zoom
            preferencesRepository.getMapZoomFlow().collectLatest { zoom ->
                _uiState.update { it.copy(mapZoom = zoom) }
            }
        }
        
        viewModelScope.launch {
            // Load initial map latitude
            preferencesRepository.getMapLatitudeFlow().collectLatest { latitude ->
                _uiState.update { it.copy(mapLatitude = latitude) }
            }
        }
        
        viewModelScope.launch {
            // Load initial map longitude
            preferencesRepository.getMapLongitudeFlow().collectLatest { longitude ->
                _uiState.update { it.copy(mapLongitude = longitude) }
            }
        }
        
        // Get user location on init
        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                location?.let { updateUserLocation(it) }
            } catch (e: Exception) {
                // Handle location error
            }
        }
    }

    fun togglePlaying() {
        val currentIsPlaying = !_uiState.value.isPlaying
        val currentState = _uiState.value
        
        if (currentIsPlaying) {
            // تفعيل الموقع المزيف
            val locationToUse = currentState.fakeLocationPoint ?: currentState.lastClickedLocation
            
            if (locationToUse != null) {
                _uiState.update { it.copy(
                    isPlaying = true,
                    fakeLocationPoint = if (it.fakeLocationPoint == null) locationToUse else it.fakeLocationPoint
                ) }
                
                viewModelScope.launch {
                    preferencesRepository.saveIsPlaying(true)
                    IntentHelper.setCustomLocation(getApplication(), locationToUse.latitude, locationToUse.longitude)
                    IntentHelper.startFakeLocation(getApplication())
                }
            }
        } else {
            // إيقاف الموقع المزيف
            _uiState.update { it.copy(isPlaying = false, fakeLocationPoint = null) }
            
            viewModelScope.launch {
                preferencesRepository.saveIsPlaying(false)
                IntentHelper.stopFakeLocation(getApplication())
            }
        }
    }

    fun updateUserLocation(location: LatLng) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun refreshUserLocation() {
        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                location?.let { updateUserLocation(it) }
            } catch (e: Exception) {
                // Handle location error silently
            }
        }
    }

    fun updateClickedLocation(latLng: LatLng?) {
        val currentState = _uiState.value
        
        // تحديث lastClickedLocation فقط إذا لم يكن الموقع المزيف مفعل
        if (!currentState.isPlaying) {
            _uiState.update { it.copy(lastClickedLocation = latLng) }
            
            viewModelScope.launch {
                latLng?.let {
                    preferencesRepository.saveLastClickedLocation(
                        it.latitude,
                        it.longitude
                    )
                } ?: preferencesRepository.clearLastClickedLocation()
            }
        }
    }

    // تحديث lastClickedLocation من المفضلة (حتى لو كان الموقع المزيف مفعل)
    fun updateClickedLocationFromFavorite(latLng: LatLng?) {
        _uiState.update { it.copy(lastClickedLocation = latLng) }
        
        viewModelScope.launch {
            latLng?.let {
                preferencesRepository.saveLastClickedLocation(
                    it.latitude,
                    it.longitude
                )
            } ?: preferencesRepository.clearLastClickedLocation()
        }
    }

    // تحديث النقطة المزيفة (يستخدم فقط عند إيقاف التزييف ووضعه على نقطة جديدة)
    fun updateFakeLocationPoint(latLng: LatLng?) {
        _uiState.update { it.copy(fakeLocationPoint = latLng) }
    }

    // مسح النقطة المزيفة
    fun clearFakeLocationPoint() {
        _uiState.update { it.copy(fakeLocationPoint = null) }
    }

    fun addFavoriteLocation(favoriteLocation: FavoriteLocation) {
        viewModelScope.launch {
            preferencesRepository.addFavorite(favoriteLocation)
        }
    }

    // تفعيل الموقع المزيف من المفضلة
    fun activateFakeLocationFromFavorite(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val currentState = _uiState.value
        
        // تحديث lastClickedLocation من المفضلة (حتى لو كان الموقع المزيف مفعل)
        updateClickedLocationFromFavorite(latLng)
        
        // تحديث fakeLocationPoint فقط إذا لم يكن الموقع المزيف مفعل بالفعل
        val shouldUpdateFakeLocation = !currentState.isPlaying
        
        _uiState.update { it.copy(
            fakeLocationPoint = if (shouldUpdateFakeLocation) latLng else it.fakeLocationPoint,
            isPlaying = true
        ) }
        
        viewModelScope.launch {
            preferencesRepository.saveIsPlaying(true)
            IntentHelper.setCustomLocation(getApplication(), latitude, longitude)
            IntentHelper.startFakeLocation(getApplication())
        }
    }

    // Update specific fields in the FavoritesInputState
    fun updateAddToFavoritesField(fieldName: String, newValue: String) {
        val currentState = _uiState.value.addToFavoritesState
        val errorMessage = when (fieldName) {
            "name" -> if (newValue.isBlank()) "Please provide a name" else null
            "coordinates" -> validateCoordinatesInput(newValue)
            else -> null
        }

        val updatedState = when (fieldName) {
            "name" -> currentState.copy(name = currentState.name.copy(value = newValue, errorMessage = errorMessage))
            "coordinates" -> currentState.copy(coordinates = currentState.coordinates.copy(value = newValue, errorMessage = errorMessage))
            else -> currentState
        }
        
        _uiState.update { it.copy(addToFavoritesState = updatedState) }
    }

    // Go to point logic
    fun goToPoint(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val latLng = LatLng(latitude, longitude)
            _goToPointEvent.emit(latLng)
        }
    }

    // Update specific fields in the GoToPointDialog state
    fun updateGoToPointField(fieldName: String, newValue: String) {
        val currentGoToPointState = _uiState.value.goToPointState
        val updatedGoToPointState = when (fieldName) {
            "coordinates" -> currentGoToPointState.copy(value = newValue)
            else -> currentGoToPointState
        }
        
        _uiState.update { it.copy(goToPointState = updatedGoToPointState) }
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

    // Validate combined coordinates input
    private fun validateCoordinatesInput(input: String): String? {
        if (input.isBlank()) return "Please enter coordinates"
        
        val coordinates = parseCoordinates(input)
        if (coordinates == null) {
            return "Invalid format. Use: latitude, longitude (e.g., 40.7128, -74.0060)"
        }
        
        val (lat, lng) = coordinates
        if (lat !in -90.0..90.0) {
            return "Latitude must be between -90 and 90"
        }
        if (lng !in -180.0..180.0) {
            return "Longitude must be between -180 and 180"
        }
        
        return null
    }

    // Validate GoToPoint inputs
    fun validateAndGo(onSuccess: (latitude: Double, longitude: Double) -> Unit) {
        val currentGoToPointState = _uiState.value.goToPointState
        val errorMessage = validateCoordinatesInput(currentGoToPointState.value)

        val updatedGoToPointState = currentGoToPointState.copy(errorMessage = errorMessage)
        _uiState.update { it.copy(goToPointState = updatedGoToPointState) }

        if (errorMessage == null) {
            val coordinates = parseCoordinates(currentGoToPointState.value)
            coordinates?.let { (lat, lng) ->
                onSuccess(lat, lng)
            }
        }
    }

    // Center map
    fun triggerCenterMapEvent() {
        viewModelScope.launch {
            try {
                // Refresh user location first
                refreshUserLocation()
                
                // Wait a very short time for location to be updated
                kotlinx.coroutines.delay(200)
                
                // Check if we have location now
                val currentLocation = uiState.value.userLocation
                if (currentLocation != null) {
                    // Emit the center event immediately
                    _centerMapEvent.emit(Unit)
                } else {
                    // Try one more time with shorter delay
                    refreshUserLocation()
                    kotlinx.coroutines.delay(100)
                    
                    val finalLocation = uiState.value.userLocation
                    if (finalLocation != null) {
                        _centerMapEvent.emit(Unit)
                    } else {
                        // Location still not available, emit anyway to show error
                        _centerMapEvent.emit(Unit)
                    }
                }
            } catch (e: Exception) {
                // Emit event anyway to show error message
                _centerMapEvent.emit(Unit)
            }
        }
    }

    fun setLoadingStarted() {
        _uiState.update { it.copy(loadingState = LoadingState.Loading) }
    }

    // Set loading finished
    fun setLoadingFinished() {
        _uiState.update { it.copy(loadingState = LoadingState.Loaded) }
    }

    // Dialog show/hide logic
    fun showGoToPointDialog() { 
        _uiState.update { it.copy(goToPointDialogState = DialogState.Visible) }
    }
    
    fun hideGoToPointDialog() {
        _uiState.update { it.copy(goToPointDialogState = DialogState.Hidden) }
        clearGoToPointInputs()
    }

    fun showAddToFavoritesDialog() { 
        _uiState.update { it.copy(addToFavoritesDialogState = DialogState.Visible) }
    }
    
    fun hideAddToFavoritesDialog() {
        _uiState.update { it.copy(addToFavoritesDialogState = DialogState.Hidden) }
        clearAddToFavoritesInputs()
    }

    // Helper for input validation
    private fun validateInput(
        input: String, range: ClosedRange<Double>, errorMessage: String
    ): String? {
        val value = input.toDoubleOrNull()
        return if (value == null || value !in range) errorMessage else null
    }

    // Clear GoToPoint inputs
    fun clearGoToPointInputs() {
        _uiState.update { 
            it.copy(goToPointState = InputFieldState())
        }
    }

    // Prefill AddToFavorites coordinates with marker values (if available)
    fun prefillCoordinatesFromMarker(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) {
            val coordinatesField = InputFieldState(value = "$latitude, $longitude")
            
            _uiState.update { currentState ->
                val favState = currentState.addToFavoritesState
                currentState.copy(
                    addToFavoritesState = favState.copy(
                        coordinates = coordinatesField
                    )
                )
            }
        }
    }

    // Validate and add favorite location
    fun validateAndAddFavorite(onSuccess: (name: String, latitude: Double, longitude: Double) -> Unit) {
        val currentState = _uiState.value.addToFavoritesState

        val coordinatesError = validateCoordinatesInput(currentState.coordinates.value)
        val nameError = if (currentState.name.value.isBlank()) "Please provide a name" else null

        val updatedState = currentState.copy(
            name = currentState.name.copy(errorMessage = nameError),
            coordinates = currentState.coordinates.copy(errorMessage = coordinatesError)
        )
        
        _uiState.update { it.copy(addToFavoritesState = updatedState) }

        if (coordinatesError == null && nameError == null) {
            val coordinates = parseCoordinates(currentState.coordinates.value)
            coordinates?.let { (lat, lng) ->
                onSuccess(currentState.name.value, lat, lng)
            }
        }
    }

    // Clear AddToFavorites inputs
    fun clearAddToFavoritesInputs() {
        _uiState.update { 
            it.copy(addToFavoritesState = FavoritesInputState())
        }
    }

    // Update map zoom
    fun updateMapZoom(zoom: Double) {
        _uiState.update { it.copy(mapZoom = zoom) }
        
        viewModelScope.launch {
            preferencesRepository.saveMapZoom(zoom)
        }
    }
    
    // Update map position
    fun updateMapPosition(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(mapLatitude = latitude, mapLongitude = longitude) }
        
        viewModelScope.launch {
            preferencesRepository.saveMapLatitude(latitude)
            preferencesRepository.saveMapLongitude(longitude)
        }
    }
    
    // Reset map zoom to default
    fun resetMapZoom() {
        viewModelScope.launch {
            preferencesRepository.resetMapZoom()
            _uiState.update { it.copy(mapZoom = DEFAULT_MAP_ZOOM) }
        }
    }
}

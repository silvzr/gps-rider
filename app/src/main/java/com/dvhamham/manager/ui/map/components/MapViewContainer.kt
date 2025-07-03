package com.dvhamham.manager.ui.map.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dvhamham.data.DEFAULT_MAP_ZOOM
import com.dvhamham.data.USER_LOCATION_ZOOM
import com.dvhamham.data.WORLD_MAP_ZOOM
import com.dvhamham.data.model.LatLng
import com.dvhamham.R
import com.dvhamham.manager.ui.map.DialogState
import com.dvhamham.manager.ui.map.LoadingState
import com.dvhamham.manager.ui.map.MapViewModel
import com.dvhamham.manager.ui.theme.LocalThemeManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@Composable
fun MapViewContainer(
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    val themeManager = LocalThemeManager.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme by themeManager.isDarkMode.collectAsState()

    // Extract state from uiState
    val loadingState = uiState.loadingState
    val lastClickedLocation = uiState.lastClickedLocation
    val isPlaying = uiState.isPlaying
    val mapZoom = uiState.mapZoom
    val userLocation = uiState.userLocation

    // Convert LatLng to Google LatLng for Google Maps
    val lastClickedGoogleLatLng = lastClickedLocation?.let { 
        GoogleLatLng(it.latitude, it.longitude) 
    }
    val userGoogleLatLng = userLocation?.let { 
        GoogleLatLng(it.latitude, it.longitude) 
    }

    // Always restore camera position from ViewModel state
    val initialCameraPosition = remember(uiState.isPlaying, uiState.mapZoom, uiState.mapLatitude, uiState.mapLongitude, lastClickedGoogleLatLng) {
        val mapLat = uiState.mapLatitude
        val mapLng = uiState.mapLongitude
        val zoom = (uiState.mapZoom ?: DEFAULT_MAP_ZOOM).toFloat()
        if (uiState.isPlaying && lastClickedGoogleLatLng != null) {
            CameraPosition.fromLatLngZoom(lastClickedGoogleLatLng, zoom)
        } else if (mapLat != null && mapLng != null) {
            CameraPosition.fromLatLngZoom(GoogleLatLng(mapLat, mapLng), zoom)
        } else {
            CameraPosition.fromLatLngZoom(GoogleLatLng(0.0, 0.0), WORLD_MAP_ZOOM.toFloat())
        }
    }
    val cameraPositionState = rememberCameraPositionState { position = initialCameraPosition }
    HandleCenterMapEvent(userGoogleLatLng, mapViewModel, cameraPositionState)
    HandleGoToPointEvent(mapViewModel, cameraPositionState)
    CenterMapOnUserLocation(userGoogleLatLng, lastClickedGoogleLatLng, mapZoom, mapViewModel)

    // Display loading spinner or MapView
    if (loadingState == LoadingState.Loading) {
        LoadingSpinner()
    } else {
        DisplayGoogleMap(
            lastClickedGoogleLatLng = lastClickedGoogleLatLng,
            userGoogleLatLng = userGoogleLatLng,
            isDarkTheme = isDarkTheme,
            isPlaying = isPlaying,
            onMapClick = { googleLatLng ->
                val latLng = LatLng(googleLatLng.latitude, googleLatLng.longitude)
                mapViewModel.updateClickedLocation(latLng)
            },
            mapViewModel = mapViewModel,
            cameraPositionState = cameraPositionState
        )
    }
}

@Composable
private fun HandleCenterMapEvent(
    userGoogleLatLng: GoogleLatLng?,
    mapViewModel: MapViewModel,
    cameraPositionState: CameraPositionState
) {
    val context = LocalContext.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        mapViewModel.centerMapEvent.collect {
            try {
                // Get current user location
                val currentUserLocation = mapViewModel.uiState.value.userLocation
                
                if (currentUserLocation != null) {
                    val googleLatLng = GoogleLatLng(currentUserLocation.latitude, currentUserLocation.longitude)
                    // Use USER_LOCATION_ZOOM for better view of user location
                    val zoom = USER_LOCATION_ZOOM.toFloat()
                    
                    // Move camera directly with faster animation
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(googleLatLng, zoom),
                        durationMs = 500
                    )
                } else {
                    // Try to refresh location one more time with shorter delay
                    mapViewModel.refreshUserLocation()
                    kotlinx.coroutines.delay(300)
                    
                    val refreshedLocation = mapViewModel.uiState.value.userLocation
                    if (refreshedLocation != null) {
                        val googleLatLng = GoogleLatLng(refreshedLocation.latitude, refreshedLocation.longitude)
                        // Use USER_LOCATION_ZOOM for better view of user location
                        val zoom = USER_LOCATION_ZOOM.toFloat()
                        
                        // Move camera directly with faster animation
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(googleLatLng, zoom),
                            durationMs = 500
                        )
                    } else {
                        Toast.makeText(context, "User location not available. Please check location permissions and GPS.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error moving to location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun HandleGoToPointEvent(
    mapViewModel: MapViewModel,
    cameraPositionState: CameraPositionState
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        mapViewModel.goToPointEvent.collect { latLng ->
            try {
                val googleLatLng = GoogleLatLng(latLng.latitude, latLng.longitude)
                
                // Update clicked location for marker
                mapViewModel.updateClickedLocation(latLng)
                
                // Move camera to the selected location with zoom 15
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(googleLatLng, 15f),
                    durationMs = 500
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

@Composable
private fun CenterMapOnUserLocation(
    userGoogleLatLng: GoogleLatLng?,
    lastClickedGoogleLatLng: GoogleLatLng?,
    mapZoom: Double?,
    mapViewModel: MapViewModel
) {
    LaunchedEffect(userGoogleLatLng, lastClickedGoogleLatLng) {
        if (lastClickedGoogleLatLng != null) {
            // Center on marker location
            val zoom = mapZoom?.toFloat() ?: DEFAULT_MAP_ZOOM.toFloat()
            mapViewModel.updateMapZoom(zoom.toDouble())
            mapViewModel.setLoadingFinished()
        } else if (userGoogleLatLng != null) {
            // Center on user location
            mapViewModel.updateMapZoom(USER_LOCATION_ZOOM)
            mapViewModel.setLoadingFinished()
        } else {
            // Set default location
            mapViewModel.updateMapZoom(WORLD_MAP_ZOOM)
            mapViewModel.setLoadingFinished()
        }
    }
}

@Composable
private fun DisplayGoogleMap(
    lastClickedGoogleLatLng: GoogleLatLng?,
    userGoogleLatLng: GoogleLatLng?,
    isDarkTheme: Boolean,
    isPlaying: Boolean,
    onMapClick: (GoogleLatLng) -> Unit,
    mapViewModel: MapViewModel,
    cameraPositionState: CameraPositionState
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Load map style based on theme
    val mapStyleOptions = remember(isDarkTheme) {
        if (isDarkTheme) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } else {
            null
        }
    }
    
    // Save zoom level and position when camera position changes, but with debouncing to prevent rapid updates
    var lastSavedPosition by remember { mutableStateOf<CameraPosition?>(null) }
    LaunchedEffect(cameraPositionState.position) {
        val position = cameraPositionState.position
        val lastSaved = lastSavedPosition
        // Only save if position changed significantly (to prevent shaking)
        if (lastSaved == null || 
            kotlin.math.abs(position.zoom - lastSaved.zoom) > 0.1f ||
            kotlin.math.abs(position.target.latitude - lastSaved.target.latitude) > 0.0001 ||
            kotlin.math.abs(position.target.longitude - lastSaved.target.longitude) > 0.0001) {
            // Add small delay to debounce rapid changes
            kotlinx.coroutines.delay(100)
            // Check if position is still the same after delay
            if (cameraPositionState.position == position) {
                mapViewModel.updateMapZoom(position.zoom.toDouble())
                mapViewModel.updateMapPosition(
                    position.target.latitude,
                    position.target.longitude
                )
                lastSavedPosition = position
            }
        }
    }

    // Update marker when lastClickedLocation changes
    LaunchedEffect(uiState.lastClickedLocation) {
        // This will trigger recomposition when lastClickedLocation changes
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL,
            mapStyleOptions = mapStyleOptions
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            compassEnabled = true
        )
    ) {
        // Show clicked location marker
        lastClickedGoogleLatLng?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Selected Location",
                snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
            )
        }
    }
}

@Composable
private fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Updating Map...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

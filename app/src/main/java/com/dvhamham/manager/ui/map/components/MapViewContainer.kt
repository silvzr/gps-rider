package com.dvhamham.manager.ui.map.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dvhamham.data.DEFAULT_MAP_ZOOM
import com.dvhamham.data.USER_LOCATION_ZOOM
import com.dvhamham.data.WORLD_MAP_ZOOM
import com.dvhamham.data.model.LatLng
import com.dvhamham.R
import com.dvhamham.manager.ui.map.LoadingState
import kotlin.math.abs
import com.dvhamham.manager.ui.map.MapViewModel
import com.dvhamham.manager.ui.theme.LocalThemeManager
import com.dvhamham.manager.ui.settings.SettingsViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun MapViewContainer(
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    val themeManager = LocalThemeManager.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme by themeManager.isDarkMode.collectAsState()
    val settingsViewModel: SettingsViewModel = viewModel()
    val disableNightMapMode by settingsViewModel.disableNightMapMode.collectAsState()

    // Extract state from uiState
    val loadingState = uiState.loadingState
    val lastClickedLocation = uiState.lastClickedLocation
    val isPlaying = uiState.isPlaying
    val mapZoom = uiState.mapZoom
    val userLocation = uiState.userLocation

    // Initialize OSMDroid configuration and set loading finished
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        
        // Mark loading as finished since OSM doesn't need API keys or async setup
        mapViewModel.setLoadingFinished()
    }
    
    // Safety timeout for loading state (in case something goes wrong)
    LaunchedEffect(loadingState) {
        if (loadingState == LoadingState.Loading) {
            kotlinx.coroutines.delay(3000) // Wait 3 seconds max
            if (mapViewModel.uiState.value.loadingState == LoadingState.Loading) {
                mapViewModel.setLoadingFinished() // Force finish loading
            }
        }
    }

    // No automatic map position/zoom updates - let user control the map

    // Handle events
    HandleCenterMapEvent(userLocation, mapViewModel)
    HandleGoToPointEvent(mapViewModel)

    // Display loading spinner or MapView
    if (loadingState == LoadingState.Loading) {
        LoadingSpinner()
    } else {
        val mapShouldBeDark = if (disableNightMapMode) false else isDarkTheme
        DisplayMap(
            lastClickedLocation = lastClickedLocation,
            userLocation = userLocation,
            isDarkTheme = mapShouldBeDark,
            isPlaying = isPlaying,
            mapZoom = mapZoom,
            mapViewModel = mapViewModel
        )
    }
}

@Composable
private fun HandleCenterMapEvent(
    userLocation: LatLng?,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        mapViewModel.centerMapEvent.collect {
            try {
                if (userLocation != null) {
                    // Only update the ViewModel state, don't force map movement
                    mapViewModel.updateMapPosition(userLocation.latitude, userLocation.longitude)
                    mapViewModel.updateMapZoom(USER_LOCATION_ZOOM)
                    // Show info to user but let them manually move map
                    Toast.makeText(context, "Location: ${userLocation.latitude}, ${userLocation.longitude}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.user_location_not_available), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_moving_to_location, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun HandleGoToPointEvent(
    mapViewModel: MapViewModel
) {
    LaunchedEffect(Unit) {
        mapViewModel.goToPointEvent.collect { latLng ->
            try {
                // Only update clicked location for marker - don't force camera movement
                mapViewModel.updateClickedLocation(latLng)
                // Let user manually navigate to see the marker
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

@Composable
private fun DisplayMap(
    lastClickedLocation: LatLng?,
    userLocation: LatLng?,
    isDarkTheme: Boolean,
    isPlaying: Boolean,
    mapZoom: Double?,
    mapViewModel: MapViewModel
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                // Configure map basic settings
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                
                // Disable OSM built-in controls to prevent white buttons
                setBuiltInZoomControls(false)
                setFlingEnabled(true)
                
                // Improve tile quality and rendering
                isTilesScaledToDpi = true
                
                // Set high zoom limits for better detail
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
                
                // Set map bounds to prevent tiles from repeating globally
                setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude, 0)
                setScrollableAreaLimitLongitude(MapView.getTileSystem().minLongitude, MapView.getTileSystem().maxLongitude, 0)
                
                // Prevent tile wrapping - disable infinite repetition completely
                setHorizontalMapRepetitionEnabled(false)
                setVerticalMapRepetitionEnabled(false)
                
                // Set dark theme overlay if needed  
                if (isDarkTheme) {
                    overlayManager.tilesOverlay.setColorFilter(
                        android.graphics.ColorMatrixColorFilter(
                            floatArrayOf(
                                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                            )
                        )
                    )
                }
                
                // Set initial position and zoom
                val initialPosition = when {
                    lastClickedLocation != null -> 
                        GeoPoint(lastClickedLocation.latitude, lastClickedLocation.longitude)
                    userLocation != null -> 
                        GeoPoint(userLocation.latitude, userLocation.longitude)
                    else -> GeoPoint(0.0, 0.0)
                }
                
                // Use zoom 3.5 as default, higher zoom only for specific locations
                val zoom = when {
                    lastClickedLocation != null -> 15.0  // Higher zoom for selected location
                    userLocation != null -> 15.0  // Higher zoom for user location
                    else -> 3.5
                }
                
                controller.setZoom(zoom)
                controller.setCenter(initialPosition)
                
                // Set click listener
                overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                    override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                        val projection = mapView?.projection
                        val geoPoint = projection?.fromPixels(e?.x?.toInt() ?: 0, e?.y?.toInt() ?: 0)
                        geoPoint?.let { 
                            val latLng = LatLng(it.latitude, it.longitude)
                            mapViewModel.updateClickedLocation(latLng)
                        }
                        return true
                    }
                })
                
                // No map event listeners to prevent feedback loops - user controls map
                
                mapView = this
            }
        },
        update = { view ->
            // Update marker when lastClickedLocation changes
            lastClickedLocation?.let { location ->
                // Remove existing marker
                marker?.let { 
                    view.overlays.remove(it) 
                    marker = null
                }
                
                // Add new marker
                val newMarker = Marker(view).apply {
                    position = GeoPoint(location.latitude, location.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Selected Location"
                    snippet = "Lat: ${String.format("%.6f", location.latitude)}, Lng: ${String.format("%.6f", location.longitude)}"
                    
                    // Try to set custom marker icon, fallback to default if not found
                    try {
                        val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                        drawable?.let { icon = it }
                    } catch (e: Exception) {
                        // Use default marker if custom icon not found
                    }
                }
                
                view.overlays.add(newMarker)
                marker = newMarker
                view.invalidate()
            } ?: run {
                // Remove marker if no location selected
                marker?.let { 
                    view.overlays.remove(it)
                    marker = null
                    view.invalidate()
                }
            }
            
            // Update theme overlay only
            if (isDarkTheme) {
                view.overlayManager.tilesOverlay.setColorFilter(
                    android.graphics.ColorMatrixColorFilter(
                        floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                        )
                    )
                )
            } else {
                view.overlayManager.tilesOverlay.setColorFilter(null)
            }
        }
    )
    
    // Update ViewModel only when component is disposed - no automatic sync
    DisposableEffect(mapView) {
        onDispose {
            mapView?.let { map ->
                // Save final position and zoom when leaving the screen
                val center = map.mapCenter
                mapViewModel.updateMapPosition(center.latitude, center.longitude)
                mapViewModel.updateMapZoom(map.zoomLevelDouble)
            }
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
                text = "Loading OpenStreetMap...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

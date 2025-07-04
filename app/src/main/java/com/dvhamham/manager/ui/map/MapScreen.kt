package com.dvhamham.manager.ui.map

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.manager.ui.map.components.AddToFavoritesDialog
import com.dvhamham.manager.ui.map.components.MapViewContainer
import com.dvhamham.manager.ui.navigation.Screen
import kotlinx.coroutines.launch
import com.dvhamham.manager.ui.theme.FlatBlue
import com.dvhamham.manager.ui.theme.FlatGreen
import com.dvhamham.manager.ui.theme.FlatRed
import com.dvhamham.manager.ui.theme.FlatOrange
import com.dvhamham.manager.ui.theme.FlatGray
import com.dvhamham.manager.ui.theme.FlatWhite
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dvhamham.manager.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    
    // Extract values from UI state
    val isPlaying = uiState.isPlaying
    val isFabClickable = uiState.isFabClickable
    val isLoading = uiState.loadingState == LoadingState.Loading
    
    // Dialog states
    val showAddToFavoritesDialog = uiState.addToFavoritesDialogState == DialogState.Visible

    var showOptionsMenu by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        MapViewContainer(mapViewModel)
        
        // Permanent Go to Point input field at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            val focusManager = LocalFocusManager.current
            val settingsViewModel: SettingsViewModel = viewModel()
            val disableNightMapMode by settingsViewModel.disableNightMapMode.collectAsState()
            val fieldColors = if (disableNightMapMode) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.outline
                )
            } else {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            }
            OutlinedTextField(
                value = uiState.goToPointState.value,
                onValueChange = { mapViewModel.updateGoToPointField("coordinates", it) },
                placeholder = { Text("Enter coordinates (lat, lng)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Go to coordinates",
                        tint = if (disableNightMapMode) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 0.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        val input = uiState.goToPointState.value
                        val error = mapViewModel.run {
                            val err = try {
                                val parts = input.split(",").map { it.trim() }
                                if (parts.size != 2) "Invalid format" else null
                            } catch (e: Exception) { "Invalid format" }
                            err ?: mapViewModel.validateCoordinatesInput(input)
                        }
                        if (error == null) {
                            val (lat, lng) = input.split(",").map { it.trim().toDouble() }
                            mapViewModel.goToPoint(lat, lng)
                            mapViewModel.activateFakeLocationFromFavorite(lat, lng)
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Go to coordinates",
                            tint = if (disableNightMapMode) Color.Black else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                isError = uiState.goToPointState.errorMessage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { keyEvent ->
                        when (keyEvent.key) {
                            Key.Enter -> {
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    Toast.makeText(context, "Enter pressed!", Toast.LENGTH_SHORT).show()
                                    mapViewModel.validateAndGo { latitude, longitude ->
                                        Toast.makeText(context, "Going to: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                                        mapViewModel.goToPoint(latitude, longitude)
                                    }
                                    true
                                } else false
                            }
                            else -> false
                        }
                    },
                singleLine = true,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    Toast.makeText(context, "Done pressed!", Toast.LENGTH_SHORT).show()
                    mapViewModel.validateAndGo { latitude, longitude ->
                        Toast.makeText(context, "Going to: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                        mapViewModel.goToPoint(latitude, longitude)
                    }
                }),
                shape = RoundedCornerShape(16.dp)
            )
            
            val errorMessage = uiState.goToPointState.errorMessage
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Floating Action Buttons in bottom right corner
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp, top = 16.dp), // Added top padding of 16dp
            verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing from 12dp to 16dp
        ) {
            // Add to Favorites FAB
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    mapViewModel.showAddToFavoritesDialog()
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Add to Favorites",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Center to My Location FAB
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    mapViewModel.triggerCenterMapEvent()
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center to My Location",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Toggle Fake Location FAB - Main Button
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isFabClickable) {
                        mapViewModel.togglePlaying()
                    } else {
                        Toast.makeText(context, "Please select a location on the map first", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.size(56.dp),
                containerColor = if (isFabClickable) {
                    if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isFabClickable) {
                    if (isPlaying) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp,
                    hoveredElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop Fake Location" else "Start Fake Location",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showAddToFavoritesDialog) {
        // Prefill coordinates from the last clicked location (marker)
        val lastClickedLocation = uiState.lastClickedLocation

        LaunchedEffect(lastClickedLocation) {
            mapViewModel.prefillCoordinatesFromMarker(
                lastClickedLocation?.latitude,
                lastClickedLocation?.longitude
            )
        }

        AddToFavoritesDialog(
            mapViewModel = mapViewModel,
            onDismissRequest = { mapViewModel.hideAddToFavoritesDialog() },
            onAddFavorite = { name, latitude, longitude ->
                val favorite = FavoriteLocation(name, latitude, longitude)
                mapViewModel.addFavoriteLocation(favorite)
                mapViewModel.hideAddToFavoritesDialog()
            }
        )
    }
}

package com.dvhamham.manager.ui.favorites

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.data.model.LatLng
import com.dvhamham.manager.ui.map.MapViewModel
import com.dvhamham.manager.ui.navigation.Screen
import com.dvhamham.manager.ui.theme.FlatBlue
import com.dvhamham.manager.ui.theme.FlatGreen
import com.dvhamham.manager.ui.theme.FlatWhite
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.ui.zIndex
import android.util.Log
import androidx.compose.ui.layout.onGloballyPositioned

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var favorites by remember { mutableStateOf(listOf<FavoriteLocation>()) }
    val favoritesState by favoritesViewModel.favorites.collectAsState()
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Sync local state with ViewModel
    LaunchedEffect(favoritesState) {
        favorites = favoritesState
    }

    // Drag state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    val listState = rememberLazyListState()

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                reader.close()
                inputStream?.close()

                val gson = Gson()
                val type = object : TypeToken<List<FavoriteLocation>>() {}.type
                val importedFavorites = gson.fromJson<List<FavoriteLocation>>(jsonString, type)

                if (importedFavorites != null && importedFavorites.isNotEmpty()) {
                    favoritesViewModel.importFavorites(importedFavorites)
                    importErrorMessage = null
                    showImportDialog = false
                } else {
                    importErrorMessage = "File is empty or invalid"
                }
            } catch (e: Exception) {
                importErrorMessage = "Error reading file: ${e.message}"
            }
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val gson = Gson()
                val jsonString = gson.toJson(favorites)
                
                val outputStream = context.contentResolver.openOutputStream(selectedUri)
                val writer = OutputStreamWriter(outputStream)
                writer.write(jsonString)
                writer.close()
                outputStream?.close()

                exportSuccessMessage = "Exported ${favorites.size} locations successfully"
                showExportDialog = false
            } catch (e: Exception) {
                exportSuccessMessage = "Error exporting file: ${e.message}"
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Import/Export Buttons in a Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Import",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import")
                }

                Button(
                    onClick = {
                        if (favorites.isNotEmpty()) {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("favorites_$timestamp.json")
                        } else {
                            exportSuccessMessage = "No locations to export"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }

        // Success/Error Messages
        if (exportSuccessMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = exportSuccessMessage!!,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                exportSuccessMessage = null
            }
        }

        // Favorites List
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No favorite locations.")
            }
        } else {
            Box(Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    itemsIndexed(favorites, key = { _, item -> item.name }) { index, favorite ->
                        val isDragged = draggedIndex == index
                        val elevation by animateDpAsState(if (isDragged) 8.dp else 0.dp)
                        val offsetY = if (isDragged) dragOffsetY.roundToInt() else 0
                        
                        FavoriteItem(
                            favorite = favorite,
                            onClick = {
                                if (draggedIndex == null) {
                                    mapViewModel.activateFakeLocationFromFavorite(favorite.latitude, favorite.longitude)
                                    navController.navigate(Screen.Map.route)
                                    GlobalScope.launch {
                                        kotlinx.coroutines.delay(100)
                                        mapViewModel.goToPoint(favorite.latitude, favorite.longitude)
                                    }
                                }
                            },
                            onDelete = {
                                favoritesViewModel.removeFavorite(favorite)
                            },
                            modifier = Modifier
                                .zIndex(if (isDragged) 1f else 0f)
                                .offset { IntOffset(0, offsetY) }
                                .pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedIndex = index
                                            dragOffsetY = 0f
                                        },
                                        onDragEnd = {
                                            draggedIndex?.let { fromIndex ->
                                                // Calculate target index based on drag distance
                                                val itemHeight = 120f
                                                val dragDistance = dragOffsetY
                                                
                                                // Simple calculation: if dragged more than half item height, move one position
                                                val targetIndex = if (dragDistance > itemHeight / 2) {
                                                    // Dragged down more than half item height
                                                    (fromIndex + 1).coerceIn(0, favorites.lastIndex)
                                                } else if (dragDistance < -itemHeight / 2) {
                                                    // Dragged up more than half item height
                                                    (fromIndex - 1).coerceIn(0, favorites.lastIndex)
                                                } else {
                                                    // Dragged less than half item height, stay in same position
                                                    fromIndex
                                                }
                                                
                                                Log.d("FavoritesScreen", "Drag stopped: fromIndex=$fromIndex, dragDistance=$dragDistance, targetIndex=$targetIndex")
                                                
                                                if (fromIndex != targetIndex) {
                                                    val newList = favorites.toMutableList()
                                                    val item = newList.removeAt(fromIndex)
                                                    newList.add(targetIndex, item)
                                                    favorites = newList
                                                    favoritesViewModel.saveFavoritesOrder(newList)
                                                    Log.d("FavoritesScreen", "Item moved from $fromIndex to $targetIndex")
                                                }
                                            }
                                            dragOffsetY = 0f
                                            draggedIndex = null
                                        },
                                        onDragCancel = {
                                            dragOffsetY = 0f
                                            draggedIndex = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggedIndex == index) {
                                                dragOffsetY += dragAmount.y
                                            }
                                        }
                                    )
                                },
                            showDragHandle = true,
                            isDragged = isDragged
                        )
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Locations") },
            text = { 
                Column {
                    Text("You are about to import locations from a JSON file. Do you want to continue?")
                    if (importErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = importErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { importLauncher.launch("application/json") }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FavoriteItem(
    favorite: FavoriteLocation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = false,
    isDragged: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragged) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: ${String.format("%.6f", favorite.latitude)}, Lng: ${String.format("%.6f", favorite.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
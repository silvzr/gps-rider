package com.dvhamham.manager.ui.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dvhamham.R
import com.dvhamham.manager.ui.map.MapViewModel

@Composable
fun AddToFavoritesDialog(
    mapViewModel: MapViewModel,
    onDismissRequest: () -> Unit,
    onAddFavorite: (name: String, latitude: Double, longitude: Double) -> Unit
) {
    // Access UI state through StateFlow
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val addToFavoritesState = uiState.addToFavoritesState
    
    val favoriteNameInput = addToFavoritesState.name.value
    val favoriteCoordinatesInput = addToFavoritesState.coordinates.value
    val favoriteNameError = addToFavoritesState.name.errorMessage
    val favoriteCoordinatesError = addToFavoritesState.coordinates.errorMessage

    AlertDialog(
        onDismissRequest = {
            mapViewModel.clearAddToFavoritesInputs()
            onDismissRequest()
        },
        title = { Text(stringResource(R.string.add_to_favorites)) },
        text = {
            Column {
                OutlinedTextField(
                    value = favoriteNameInput,
                    onValueChange = { mapViewModel.updateAddToFavoritesField("name", it) },
                    label = { Text(stringResource(R.string.location_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = favoriteNameError != null
                )
                if (favoriteNameError != null) {
                    Text(
                        text = favoriteNameError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = favoriteCoordinatesInput,
                    onValueChange = { mapViewModel.updateAddToFavoritesField("coordinates", it) },
                    label = { Text(stringResource(R.string.coordinates)) },
                    placeholder = { Text(stringResource(R.string.latitude_longitude)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = favoriteCoordinatesError != null
                )
                if (favoriteCoordinatesError != null) {
                    Text(
                        text = favoriteCoordinatesError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    mapViewModel.validateAndAddFavorite { name, latitude, longitude ->
                        onAddFavorite(name, latitude, longitude)
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    mapViewModel.clearAddToFavoritesInputs()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

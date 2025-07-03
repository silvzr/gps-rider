package com.dvhamham.manager.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dvhamham.data.model.FavoriteLocation
import com.dvhamham.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)

    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> get() = _favorites

    init {
        viewModelScope.launch {
            preferencesRepository.getFavoritesFlow().collectLatest { favorites ->
                _favorites.value = favorites
            }
        }
    }

    fun removeFavorite(favorite: FavoriteLocation) {
        viewModelScope.launch {
            preferencesRepository.removeFavorite(favorite)
        }
    }

    fun importFavorites(favorites: List<FavoriteLocation>) {
        viewModelScope.launch {
            favorites.forEach { favorite ->
                preferencesRepository.addFavorite(favorite)
            }
        }
    }

    fun saveFavoritesOrder(newOrder: List<FavoriteLocation>) {
        viewModelScope.launch {
            preferencesRepository.saveFavorites(newOrder)
        }
    }
}
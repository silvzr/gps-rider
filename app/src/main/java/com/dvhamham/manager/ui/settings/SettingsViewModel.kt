//SettingsViewModel.kt
package com.dvhamham.manager.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dvhamham.data.*
import com.dvhamham.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)

    // Generic state holders for different types of preferences
    private class BooleanPreference(
        initialValue: Boolean,
        private val flow: Flow<Boolean>,
        private val saveOperation: suspend (Boolean) -> Unit,
        private val viewModelScope: kotlinx.coroutines.CoroutineScope
    ) {
        private val _state = MutableStateFlow(initialValue)
        val state: StateFlow<Boolean> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                flow.collect { _state.value = it }
            }
        }

        fun setValue(value: Boolean) {
            _state.value = value
            viewModelScope.launch {
                try {
                    saveOperation(value)
                } catch (e: Exception) {
                    // Add error handling if needed
                }
            }
        }
    }

    private class DoublePreference(
        initialValue: Double,
        private val flow: Flow<Double>,
        private val saveOperation: suspend (Double) -> Unit,
        private val viewModelScope: kotlinx.coroutines.CoroutineScope
    ) {
        private val _state = MutableStateFlow(initialValue)
        val state: StateFlow<Double> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                flow.collect { _state.value = it }
            }
        }

        fun setValue(value: Double) {
            _state.value = value
            viewModelScope.launch {
                try {
                    saveOperation(value)
                } catch (e: Exception) {
                    // Add error handling if needed
                }
            }
        }
    }

    private class FloatPreference(
        initialValue: Float,
        private val flow: Flow<Float>,
        private val saveOperation: suspend (Float) -> Unit,
        private val viewModelScope: kotlinx.coroutines.CoroutineScope
    ) {
        private val _state = MutableStateFlow(initialValue)
        val state: StateFlow<Float> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                flow.collect { _state.value = it }
            }
        }

        fun setValue(value: Float) {
            _state.value = value
            viewModelScope.launch {
                try {
                    saveOperation(value)
                } catch (e: Exception) {
                    // Add error handling if needed
                }
            }
        }
    }

    // Preferences for Accuracy
    private val _useAccuracyPreference = BooleanPreference(
        DEFAULT_USE_ACCURACY,
        preferencesRepository.getUseAccuracyFlow(),
        preferencesRepository::saveUseAccuracy,
        viewModelScope
    )
    val useAccuracy: StateFlow<Boolean> = _useAccuracyPreference.state

    private val _accuracyPreference = DoublePreference(
        DEFAULT_ACCURACY,
        preferencesRepository.getAccuracyFlow(),
        preferencesRepository::saveAccuracy,
        viewModelScope
    )
    val accuracy: StateFlow<Double> = _accuracyPreference.state

    // Preferences for Altitude
    private val _useAltitudePreference = BooleanPreference(
        DEFAULT_USE_ALTITUDE,
        preferencesRepository.getUseAltitudeFlow(),
        preferencesRepository::saveUseAltitude,
        viewModelScope
    )
    val useAltitude: StateFlow<Boolean> = _useAltitudePreference.state

    private val _altitudePreference = DoublePreference(
        DEFAULT_ALTITUDE,
        preferencesRepository.getAltitudeFlow(),
        preferencesRepository::saveAltitude,
        viewModelScope
    )
    val altitude: StateFlow<Double> = _altitudePreference.state

    // Preferences for Randomize
    private val _useRandomizePreference = BooleanPreference(
        DEFAULT_USE_RANDOMIZE,
        preferencesRepository.getUseRandomizeFlow(),
        preferencesRepository::saveUseRandomize,
        viewModelScope
    )
    val useRandomize: StateFlow<Boolean> = _useRandomizePreference.state

    private val _randomizeRadiusPreference = DoublePreference(
        DEFAULT_RANDOMIZE_RADIUS,
        preferencesRepository.getRandomizeRadiusFlow(),
        preferencesRepository::saveRandomizeRadius,
        viewModelScope
    )
    val randomizeRadius: StateFlow<Double> = _randomizeRadiusPreference.state

    // Preferences for Vertical Accuracy
    private val _useVerticalAccuracyPreference = BooleanPreference(
        DEFAULT_USE_VERTICAL_ACCURACY,
        preferencesRepository.getUseVerticalAccuracyFlow(),
        preferencesRepository::saveUseVerticalAccuracy,
        viewModelScope
    )
    val useVerticalAccuracy: StateFlow<Boolean> = _useVerticalAccuracyPreference.state

    private val _verticalAccuracyPreference = FloatPreference(
        DEFAULT_VERTICAL_ACCURACY,
        preferencesRepository.getVerticalAccuracyFlow(),
        preferencesRepository::saveVerticalAccuracy,
        viewModelScope
    )
    val verticalAccuracy: StateFlow<Float> = _verticalAccuracyPreference.state

    // Preferences for Mean Sea Level
    private val _useMeanSeaLevelPreference = BooleanPreference(
        DEFAULT_USE_MEAN_SEA_LEVEL,
        preferencesRepository.getUseMeanSeaLevelFlow(),
        preferencesRepository::saveUseMeanSeaLevel,
        viewModelScope
    )
    val useMeanSeaLevel: StateFlow<Boolean> = _useMeanSeaLevelPreference.state

    private val _meanSeaLevelPreference = DoublePreference(
        DEFAULT_MEAN_SEA_LEVEL,
        preferencesRepository.getMeanSeaLevelFlow(),
        preferencesRepository::saveMeanSeaLevel,
        viewModelScope
    )
    val meanSeaLevel: StateFlow<Double> = _meanSeaLevelPreference.state

    // Preferences for Mean Sea Level Accuracy
    private val _useMeanSeaLevelAccuracyPreference = BooleanPreference(
        DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY,
        preferencesRepository.getUseMeanSeaLevelAccuracyFlow(),
        preferencesRepository::saveUseMeanSeaLevelAccuracy,
        viewModelScope
    )
    val useMeanSeaLevelAccuracy: StateFlow<Boolean> = _useMeanSeaLevelAccuracyPreference.state

    private val _meanSeaLevelAccuracyPreference = FloatPreference(
        DEFAULT_MEAN_SEA_LEVEL_ACCURACY,
        preferencesRepository.getMeanSeaLevelAccuracyFlow(),
        preferencesRepository::saveMeanSeaLevelAccuracy,
        viewModelScope
    )
    val meanSeaLevelAccuracy: StateFlow<Float> = _meanSeaLevelAccuracyPreference.state

    // Preferences for Speed
    private val _useSpeedPreference = BooleanPreference(
        DEFAULT_USE_SPEED,
        preferencesRepository.getUseSpeedFlow(),
        preferencesRepository::saveUseSpeed,
        viewModelScope
    )
    val useSpeed: StateFlow<Boolean> = _useSpeedPreference.state

    private val _speedPreference = FloatPreference(
        DEFAULT_SPEED,
        preferencesRepository.getSpeedFlow(),
        preferencesRepository::saveSpeed,
        viewModelScope
    )
    val speed: StateFlow<Float> = _speedPreference.state

    // Preferences for Speed Accuracy
    private val _useSpeedAccuracyPreference = BooleanPreference(
        DEFAULT_USE_SPEED_ACCURACY,
        preferencesRepository.getUseSpeedAccuracyFlow(),
        preferencesRepository::saveUseSpeedAccuracy,
        viewModelScope
    )
    val useSpeedAccuracy: StateFlow<Boolean> = _useSpeedAccuracyPreference.state

    private val _speedAccuracyPreference = FloatPreference(
        DEFAULT_SPEED_ACCURACY,
        preferencesRepository.getSpeedAccuracyFlow(),
        preferencesRepository::saveSpeedAccuracy,
        viewModelScope
    )
    val speedAccuracy: StateFlow<Float> = _speedAccuracyPreference.state

    // Preferences for System Hook
    private val _useSystemHookPreference = BooleanPreference(
        false,
        preferencesRepository.getUseSystemHookFlow(),
        preferencesRepository::saveUseSystemHook,
        viewModelScope
    )
    val useSystemHook: StateFlow<Boolean> = _useSystemHookPreference.state

    // Preferences for disabling night map mode
    private val _disableNightMapModePreference = BooleanPreference(
        false,
        preferencesRepository.getDisableNightMapModeFlow(),
        preferencesRepository::saveDisableNightMapMode,
        viewModelScope
    )
    val disableNightMapMode: StateFlow<Boolean> = _disableNightMapModePreference.state

    // Setter methods for all preferences
    fun setUseAccuracy(value: Boolean) = _useAccuracyPreference.setValue(value)
    fun setAccuracy(value: Double) = _accuracyPreference.setValue(value)
    fun setUseAltitude(value: Boolean) = _useAltitudePreference.setValue(value)
    fun setAltitude(value: Double) = _altitudePreference.setValue(value)
    fun setUseRandomize(value: Boolean) = _useRandomizePreference.setValue(value)
    fun setRandomizeRadius(value: Double) = _randomizeRadiusPreference.setValue(value)
    fun setUseVerticalAccuracy(value: Boolean) = _useVerticalAccuracyPreference.setValue(value)
    fun setVerticalAccuracy(value: Float) = _verticalAccuracyPreference.setValue(value)
    fun setUseMeanSeaLevel(value: Boolean) = _useMeanSeaLevelPreference.setValue(value)
    fun setMeanSeaLevel(value: Double) = _meanSeaLevelPreference.setValue(value)
    fun setUseMeanSeaLevelAccuracy(value: Boolean) = _useMeanSeaLevelAccuracyPreference.setValue(value)
    fun setMeanSeaLevelAccuracy(value: Float) = _meanSeaLevelAccuracyPreference.setValue(value)
    fun setUseSpeed(value: Boolean) = _useSpeedPreference.setValue(value)
    fun setSpeed(value: Float) = _speedPreference.setValue(value)
    fun setUseSpeedAccuracy(value: Boolean) = _useSpeedAccuracyPreference.setValue(value)
    fun setSpeedAccuracy(value: Float) = _speedAccuracyPreference.setValue(value)
    fun setUseSystemHook(value: Boolean) = _useSystemHookPreference.setValue(value)
    fun setDisableNightMapMode(value: Boolean) {
        _disableNightMapModePreference.setValue(value)
    }
}
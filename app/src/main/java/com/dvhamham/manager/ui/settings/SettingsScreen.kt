//SettingsScreen.kt
package com.dvhamham.manager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.dvhamham.manager.ui.theme.ThemeMode
import com.dvhamham.manager.ui.theme.LocalThemeManager

// Dimension constants
private object Dimensions {
    val SPACING_EXTRA_SMALL = 4.dp
    val SPACING_SMALL = 8.dp
    val SPACING_MEDIUM = 16.dp
    val SPACING_LARGE = 24.dp
    val CARD_CORNER_RADIUS = 12.dp
    val CARD_ELEVATION = 2.dp
    val CATEGORY_SPACING = 32.dp
}

// Setting definitions to reduce duplication
private object SettingDefinitions {
    // Define setting categories
    val CATEGORIES = mapOf(
        "Appearance" to listOf("Dark Mode"),
        "Location" to listOf("Randomize Nearby Location", "Custom Horizontal Accuracy", "Custom Vertical Accuracy"),
        "Altitude" to listOf("Custom Altitude", "Custom MSL", "Custom MSL Accuracy"),
        "Movement" to listOf("Custom Speed", "Custom Speed Accuracy"),
        "Advanced" to listOf("Hook System Location")
    )
    
    // Define all settings with their parameters
    @Composable
    fun getSettings(viewModel: SettingsViewModel): List<SettingData> = listOf(
        // Dark Mode Setting
        ThemeSettingData(),
        // Randomize Nearby Location
        DoubleSettingData(
            title = "Randomize Nearby Location",
            description = "Randomly places your location within the specified radius",
            useValueState = viewModel.useRandomize.collectAsState(),
            valueState = viewModel.randomizeRadius.collectAsState(),
            setUseValue = viewModel::setUseRandomize,
            setValue = viewModel::setRandomizeRadius,
            label = "Randomization Radius",
            unit = "m",
            minValue = 0f,
            maxValue = 2000f,
            step = 0.1f
        ),
        // Custom Horizontal Accuracy
        DoubleSettingData(
            title = "Custom Horizontal Accuracy",
            description = "Sets the horizontal accuracy of your location",
            useValueState = viewModel.useAccuracy.collectAsState(),
            valueState = viewModel.accuracy.collectAsState(),
            setUseValue = viewModel::setUseAccuracy,
            setValue = viewModel::setAccuracy,
            label = "Horizontal Accuracy",
            unit = "m",
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Vertical Accuracy
        FloatSettingData(
            title = "Custom Vertical Accuracy",
            description = "Sets the vertical accuracy of your location",
            useValueState = viewModel.useVerticalAccuracy.collectAsState(),
            valueState = viewModel.verticalAccuracy.collectAsState(),
            setUseValue = viewModel::setUseVerticalAccuracy,
            setValue = viewModel::setVerticalAccuracy,
            label = "Vertical Accuracy",
            unit = "m",
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Altitude
        DoubleSettingData(
            title = "Custom Altitude",
            description = "Sets a custom altitude for your location",
            useValueState = viewModel.useAltitude.collectAsState(),
            valueState = viewModel.altitude.collectAsState(),
            setUseValue = viewModel::setUseAltitude,
            setValue = viewModel::setAltitude,
            label = "Altitude",
            unit = "m",
            minValue = 0f,
            maxValue = 2000f,
            step = 0.5f
        ),
        // Custom MSL
        DoubleSettingData(
            title = "Custom MSL",
            description = "Sets a custom mean sea level value",
            useValueState = viewModel.useMeanSeaLevel.collectAsState(),
            valueState = viewModel.meanSeaLevel.collectAsState(),
            setUseValue = viewModel::setUseMeanSeaLevel,
            setValue = viewModel::setMeanSeaLevel,
            label = "MSL",
            unit = "m",
            minValue = -400f,
            maxValue = 2000f,
            step = 0.5f
        ),
        // Custom MSL Accuracy
        FloatSettingData(
            title = "Custom MSL Accuracy",
            description = "Sets the accuracy of the mean sea level value",
            useValueState = viewModel.useMeanSeaLevelAccuracy.collectAsState(),
            valueState = viewModel.meanSeaLevelAccuracy.collectAsState(),
            setUseValue = viewModel::setUseMeanSeaLevelAccuracy,
            setValue = viewModel::setMeanSeaLevelAccuracy,
            label = "MSL Accuracy",
            unit = "m",
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Speed
        FloatSettingData(
            title = "Custom Speed",
            description = "Sets a custom speed for your location",
            useValueState = viewModel.useSpeed.collectAsState(),
            valueState = viewModel.speed.collectAsState(),
            setUseValue = viewModel::setUseSpeed,
            setValue = viewModel::setSpeed,
            label = "Speed",
            unit = "m/s",
            minValue = 0f,
            maxValue = 30f,
            step = 0.1f
        ),
        // Custom Speed Accuracy
        FloatSettingData(
            title = "Custom Speed Accuracy",
            description = "Sets the accuracy of your speed value",
            useValueState = viewModel.useSpeedAccuracy.collectAsState(),
            valueState = viewModel.speedAccuracy.collectAsState(),
            setUseValue = viewModel::setUseSpeedAccuracy,
            setValue = viewModel::setSpeedAccuracy,
            label = "Speed Accuracy",
            unit = "m/s",
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Hook System Location (Advanced)
        HookSystemSettingData(
            useValueState = viewModel.useSystemHook.collectAsState(),
            setUseValue = viewModel::setUseSystemHook
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val themeManager = LocalThemeManager.current
    val allSettings = SettingDefinitions.getSettings(settingsViewModel)
    var showLanguageMenu by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }

        Column(
        modifier = modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = Dimensions.SPACING_MEDIUM)
            .padding(top = Dimensions.SPACING_LARGE)
        ) {
            SettingDefinitions.CATEGORIES.forEach { (category, settingsInCategory) ->
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SPACING_SMALL),
                    shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                        settingsInCategory.forEach { settingTitle ->
                            val setting = allSettings.find { it.title == settingTitle }
                            setting?.let {
                            if (setting is DoubleSettingData || setting is FloatSettingData || setting is HookSystemSettingData) {
                                SettingDialogButton(setting)
                            } else {
                                when (setting) {
                                    is ThemeSettingData -> ThemeSettingComposable(setting, themeManager)
                                    is DoubleSettingData -> {}
                                    is FloatSettingData -> {}
                                    is HookSystemSettingData -> SettingDialogButton(setting)
                                }
                                }
                                if (settingTitle != settingsInCategory.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Dimensions.SPACING_SMALL),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
        }
        Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
    }
}

@Composable
fun SettingDialogButton(setting: SettingData) {
    if (setting is HookSystemSettingData) {
        SettingDialogButton(setting as HookSystemSettingData)
        return
    }
    var showDialog by remember { mutableStateOf(false) }
    val isEnabled = when (setting) {
        is DoubleSettingData -> setting.useValueState.value
        is FloatSettingData -> setting.useValueState.value
        else -> false
    }
    val valueText = when (setting) {
        is DoubleSettingData -> setting.valueState.value.toString()
        is FloatSettingData -> setting.valueState.value.toString()
        else -> ""
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = setting.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
        )
        Text(
            text = if (isEnabled) valueText + " " + (setting.unit.takeIf { isEnabled } ?: "") else "Disabled",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
        )
    }
    if (showDialog) {
        when (setting) {
            is DoubleSettingData -> {
                SettingValueDialog(
                    title = setting.title,
                    value = setting.valueState.value,
                    onValueChange = setting.setValue,
                    enabled = setting.useValueState.value,
                    onEnabledChange = setting.setUseValue,
                    minValue = setting.minValue,
                    maxValue = setting.maxValue,
                    step = setting.step,
                    onDismiss = { showDialog = false },
                    titleTextStyle = MaterialTheme.typography.titleMedium,
                    unit = setting.unit
                )
            }
            is FloatSettingData -> {
                SettingValueDialog(
                    title = setting.title,
                    value = setting.valueState.value,
                    onValueChange = setting.setValue,
                    enabled = setting.useValueState.value,
                    onEnabledChange = setting.setUseValue,
                    minValue = setting.minValue,
                    maxValue = setting.maxValue,
                    step = setting.step,
                    onDismiss = { showDialog = false },
                    titleTextStyle = MaterialTheme.typography.titleMedium,
                    unit = setting.unit
                )
            }
            else -> {}
        }
    }
}

@Composable
fun <T : Number> SettingValueDialog(
    title: String,
    value: T,
    onValueChange: (T) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    minValue: Float,
    maxValue: Float,
    step: Float,
    onDismiss: () -> Unit,
    titleTextStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * 0.9),
    unit: String = ""
) {
    var tempValue by remember { mutableStateOf(value.toString()) }
    var tempEnabled by remember { mutableStateOf(enabled) }
    var hasError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    style = titleTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (tempEnabled) "Enabled" else "Disabled", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = tempEnabled, onCheckedChange = { tempEnabled = it })
                }
                if (tempEnabled) {
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = {
                            tempValue = it
                            hasError = false
                        },
                        label = { Text("Value") },
                        isError = hasError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${minValue.toInt()} - ${maxValue.toInt()}${if (unit.isNotBlank()) unit else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (hasError) {
                        Text("Value must be between $minValue and $maxValue", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (tempEnabled) {
                    val num = tempValue.toFloatOrNull()
                    if (num != null && num in minValue..maxValue) {
                        onValueChange(
                            when (value) {
                                is Double -> num.toDouble() as T
                                is Float -> num as T
                                else -> value
                            }
                        )
                        onEnabledChange(tempEnabled)
                        onDismiss()
                    } else {
                        hasError = true
                    }
                } else {
                    onEnabledChange(tempEnabled)
                    onDismiss()
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.SPACING_SMALL)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier
                .weight(2f)
                .padding(start = Dimensions.SPACING_MEDIUM),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ThemeSettingComposable(
    setting: ThemeSettingData,
    themeManager: com.dvhamham.manager.ui.theme.ThemeManager
) {
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.SPACING_SMALL)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Theme icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_SMALL))
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Enable dark theme for the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimensions.SPACING_EXTRA_SMALL)
                )
            }
            
            Switch(
                checked = isDarkMode,
                onCheckedChange = { isEnabled ->
                    if (isEnabled) {
                        themeManager.setThemeMode(ThemeMode.DARK)
                    } else {
                        themeManager.setThemeMode(ThemeMode.LIGHT)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.semantics { 
                    contentDescription = if (isDarkMode) "Disable Dark Mode" else "Enable Dark Mode" 
                }
            )
        }
    }
}

@Composable
fun DoubleSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it.toDouble() }
    )
}

@Composable
fun FloatSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Number> SettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float,
    valueFormatter: (T) -> String,
    parseValue: (Float) -> T
) {
    var showTooltip by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.SPACING_SMALL)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = { showTooltip = !showTooltip },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "More information about $title",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (showTooltip) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimensions.SPACING_EXTRA_SMALL)
                    )
                }
            }
            
            Switch(
                checked = useValue,
                onCheckedChange = onUseValueChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.semantics { 
                    contentDescription = if (useValue) "Disable $title" else "Enable $title" 
                }
            )
        }

        if (useValue) {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
            
            var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }
            var showExactValue by remember { mutableStateOf(false) }
            
            LaunchedEffect(value) {
                if (sliderValue != value.toFloat()) {
                    sliderValue = value.toFloat()
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL),
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayText = "$label: ${valueFormatter(parseValue(sliderValue))} $unit"
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showExactValue = !showExactValue }
                )
                
                // Add +/- buttons for precise adjustment
                OutlinedIconButton(
                    onClick = { 
                        val newValue = (sliderValue - step).coerceAtLeast(minValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue > minValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "−",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                
                OutlinedIconButton(
                    onClick = { 
                        val newValue = (sliderValue + step).coerceAtMost(maxValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue < maxValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            
            // Min and max value labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.SPACING_SMALL)
            ) {
                Text(
                    text = "${minValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${maxValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    onValueChange(parseValue(sliderValue))
                },
                valueRange = minValue..maxValue,
                steps = ((maxValue - minValue) / step).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Adjust $title value"
                    }
            )
        }
    }
}

sealed class SettingData {
    abstract val title: String
    abstract val description: String
    abstract val useValueState: State<Boolean>
    abstract val setUseValue: (Boolean) -> Unit
    abstract val label: String
    abstract val unit: String
    abstract val minValue: Float
    abstract val maxValue: Float
    abstract val step: Float
}

data class DoubleSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Double>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Double) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

data class FloatSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Float>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Float) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

data class ThemeSettingData(
    override val title: String = "Dark Mode",
    override val description: String = "Choose your preferred theme",
    override val useValueState: State<Boolean> = mutableStateOf(true),
    override val setUseValue: (Boolean) -> Unit = {},
    override val label: String = "Theme",
    override val unit: String = "",
    override val minValue: Float = 0f,
    override val maxValue: Float = 1f,
    override val step: Float = 1f
) : SettingData()

@Composable
fun DoubleSettingComposable(
    setting: DoubleSettingData
) {
    DoubleSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}

@Composable
fun FloatSettingComposable(
    setting: FloatSettingData
) {
    FloatSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}

// تعريف إعداد Hook System Location كعنصر إعدادات
data class HookSystemSettingData(
    override val title: String = "Hook System Location",
    override val description: String = "Enable system-wide location hook (requires module)",
    override val useValueState: State<Boolean>,
    override val setUseValue: (Boolean) -> Unit,
    override val label: String = "Hook System Location",
    override val unit: String = "",
    override val minValue: Float = 0f,
    override val maxValue: Float = 1f,
    override val step: Float = 1f
) : SettingData()

// عرض HookSystemSettingData كسطر فيه نص وسويتش فقط (بدون حوار)
@Composable
fun SettingDialogButton(setting: HookSystemSettingData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setting.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = setting.useValueState.value,
            onCheckedChange = { setting.setUseValue(it) },
            enabled = true,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
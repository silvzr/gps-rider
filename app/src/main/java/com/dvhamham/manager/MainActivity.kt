package com.dvhamham.manager

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.dvhamham.manager.ui.navigation.MainNavGraphWithBottomBarAndPermissions
import com.dvhamham.manager.ui.theme.GPSRiderTheme
import com.dvhamham.manager.ui.theme.LocalThemeManager
import com.dvhamham.manager.ui.theme.rememberThemeManager
import com.dvhamham.manager.ui.theme.StatusBarDark
import com.dvhamham.manager.ui.theme.StatusBarLight
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import android.util.Log
import com.dvhamham.manager.ui.theme.StatusBarModernDark
import com.dvhamham.manager.XposedChecker
import com.dvhamham.manager.ui.components.ModuleNotActiveDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import com.dvhamham.BuildConfig

class MainActivity : ComponentActivity() {
    
    private var broadcastReceiver: GPSRiderBroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Register broadcast receiver
            registerBroadcastReceiver()
            
            // Handle incoming intents
            handleIncomingIntent(intent)
            
            Log.d("MainActivity", "MainActivity created successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
        }
        
        // Enable edge to edge
        enableEdgeToEdge()
        
        // Set window flags for immediate start
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // Disable window animations
        window.attributes.windowAnimations = 0
        // Fix content under status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val themeManager = rememberThemeManager()
            val isDarkMode = themeManager.isDarkMode.collectAsState().value
            
            // --- LSPosed Module Check ---
            var showModuleDialog by remember { mutableStateOf(false) }
            val isModuleActive = XposedChecker.isModuleActive()
            showModuleDialog = !isModuleActive
            
            // --- Update Check State ---
            var showMinorUpdateDialog by remember { mutableStateOf(false) }
            var showMajorUpdateDialog by remember { mutableStateOf(false) }
            var updateDownloadUrl by remember { mutableStateOf("") }
            var updateVersion by remember { mutableStateOf("") }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        val url = URL("https://raw.githubusercontent.com/dvhamham/gps-rider/main/update.json")
                        val connection = withContext(Dispatchers.IO) { url.openConnection() as HttpURLConnection }
                        connection.connectTimeout = 3000
                        connection.readTimeout = 3000
                        connection.requestMethod = "GET"

                        val response = withContext(Dispatchers.IO) {
                            connection.inputStream.bufferedReader().use { it.readText() }
                        }

                        val json = JSONObject(response)
                        val status = json.optBoolean("status", false)
                        val remoteVersion = json.optString("version", "")
                        val downloadUrl = json.optString("download", "")
                        val currentVersion = BuildConfig.VERSION_NAME

                        updateDownloadUrl = downloadUrl
                        updateVersion = remoteVersion
                        showMajorUpdateDialog = false
                        showMinorUpdateDialog = false

                   if (status && remoteVersion.isNotEmpty()) {
                    checkUpdate(currentVersion, remoteVersion, isMajor = true) {
                        showMajorUpdateDialog = true
                        return@checkUpdate
                    }
                    if (!showMajorUpdateDialog) {
                        checkUpdate(currentVersion, remoteVersion, isMajor = false) {
                            showMinorUpdateDialog = true
                        }
                    }
                }
                   
                    } catch (e: Exception) {
                        Log.e("UpdateCheck", "Error: ", e)
                    }
                }
            }
            
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                GPSRiderTheme(darkTheme = isDarkMode) {
                    // Update status bar after theme is applied
                    LaunchedEffect(isDarkMode) {
                        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
                        if (isDarkMode) {
                            window.statusBarColor = StatusBarDark.toArgb()
                            window.navigationBarColor = StatusBarDark.toArgb()
                            windowInsetsController.isAppearanceLightStatusBars = false
                            windowInsetsController.isAppearanceLightNavigationBars = false
                        } else {
                            window.statusBarColor = StatusBarModernDark.toArgb()
                            window.navigationBarColor = StatusBarModernDark.toArgb()
                            windowInsetsController.isAppearanceLightStatusBars = true
                            windowInsetsController.isAppearanceLightNavigationBars = true
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        MainNavGraphWithBottomBarAndPermissions(
                            navController = navController
                        )
                        if (showModuleDialog) {
                            ModuleNotActiveDialog()
                        }
                        if (showMinorUpdateDialog) {
                            AlertDialog(
                                onDismissRequest = { showMinorUpdateDialog = false },
                                title = { Text("Update Available") },
                                text = { Text("A new minor update ($updateVersion) is available. Would you like to update?") },
                                confirmButton = {
                                    Button(onClick = {
                                        showMinorUpdateDialog = false
                                        if (updateDownloadUrl.isNotEmpty()) {
                                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(updateDownloadUrl))
                                            startActivity(intent)
                                        }
                                    }) { Text("Update") }
                                },
                                dismissButton = {
                                    Button(onClick = { showMinorUpdateDialog = false }) { Text("Later") }
                                }
                            )
                        }
                        if (showMajorUpdateDialog) {
                            AlertDialog(
                                onDismissRequest = {}, // Not dismissible
                                title = { Text("Update Required") },
                                text = { Text("A major update ($updateVersion) is required to continue using the app.") },
                                confirmButton = {
                                    Button(onClick = {
                                        if (updateDownloadUrl.isNotEmpty()) {
                                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(updateDownloadUrl))
                                            startActivity(intent)
                                        }
                                    }) { Text("Update Now") }
                                },
                                dismissButton = null
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // Unregister broadcast receiver
            unregisterBroadcastReceiver()
            Log.d("MainActivity", "MainActivity destroyed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onDestroy: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        try {
            handleIncomingIntent(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling new intent: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun registerBroadcastReceiver() {
        try {
            broadcastReceiver = GPSRiderBroadcastReceiver.register(this)
            Log.d("MainActivity", "BroadcastReceiver registered successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error registering BroadcastReceiver: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun unregisterBroadcastReceiver() {
        try {
            broadcastReceiver?.let { receiver ->
                GPSRiderBroadcastReceiver.unregister(this, receiver)
                broadcastReceiver = null
                Log.d("MainActivity", "BroadcastReceiver unregistered successfully")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering BroadcastReceiver: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun handleIncomingIntent(intent: Intent?) {
        try {
            intent?.let { incomingIntent ->
                val action = incomingIntent.action
                Log.d("MainActivity", "Handling incoming intent: $action")
                
                if (action != null && action.startsWith("com.dvhamham.")) {
                    // Forward the intent to IntentService
                    val serviceIntent = Intent(this, IntentService::class.java).apply {
                        this.action = action
                        // Copy all extras
                        incomingIntent.extras?.let { extras ->
                            putExtras(extras)
                            Log.d("MainActivity", "Copied extras: ${extras.keySet()}")
                        }
                    }
                    
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                        Log.d("MainActivity", "Intent forwarded to IntentService")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting service: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    Log.d("MainActivity", "Ignoring non-GPS Rider intent: $action")
                }
            } ?: run {
                Log.d("MainActivity", "No incoming intent to handle")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling incoming intent: ${e.message}")
            e.printStackTrace()
        }
    }

    fun checkUpdate(currentVersion: String, remoteVersion: String, isMajor: Boolean, onShowDialog: () -> Unit) {
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val remoteParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }
        if (remoteParts.size >= 3 && currentParts.size >= 3) {
            if (isMajor) {
                if (remoteParts[0] > currentParts[0]) {
                    onShowDialog()
                }
            } else {
                if (remoteParts[0] == currentParts[0] && remoteParts[1] > currentParts[1]) {
                    onShowDialog()
                }
            }
        }
    }
} 
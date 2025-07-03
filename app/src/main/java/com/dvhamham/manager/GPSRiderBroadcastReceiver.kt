package com.dvhamham.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Broadcast Receiver for handling intents from external apps
 * This receiver is registered in the manifest and handles all GPS Rider intents
 */
class GPSRiderBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "GPSRiderBroadcast"
        
        fun register(context: Context): GPSRiderBroadcastReceiver {
            val receiver = GPSRiderBroadcastReceiver()
            val filter = IntentFilter().apply {
                addAction(IntentHelper.ACTION_START_FAKE_LOCATION)
                addAction(IntentHelper.ACTION_STOP_FAKE_LOCATION)
                addAction(IntentHelper.ACTION_TOGGLE_FAKE_LOCATION)
                addAction(IntentHelper.ACTION_SET_CUSTOM_LOCATION)
                addAction(IntentHelper.ACTION_SET_FAVORITE_LOCATION)
                addAction(IntentHelper.ACTION_GET_STATUS)
                addAction(IntentHelper.ACTION_GET_CURRENT_LOCATION)
                addAction(IntentHelper.ACTION_SET_ACCURACY)
                addAction(IntentHelper.ACTION_SET_ALTITUDE)
                addAction(IntentHelper.ACTION_SET_SPEED)
                addAction(IntentHelper.ACTION_RANDOMIZE_LOCATION)
                addAction(IntentHelper.ACTION_CREATE_FAVORITE)
                addAction(IntentHelper.ACTION_DELETE_FAVORITE)
                addAction(IntentHelper.ACTION_GET_FAVORITES)
                addAction(IntentHelper.ACTION_START_TIMED_LOCATION)
                addAction(IntentHelper.ACTION_STOP_TIMED_LOCATION)
                addAction(IntentHelper.ACTION_LOAD_PATH_FILE)
                addAction(IntentHelper.ACTION_SET_HEADING)
                addAction(IntentHelper.ACTION_SET_BEARING)
                addAction(IntentHelper.ACTION_GET_LOCATION_HISTORY)
                addAction(IntentHelper.ACTION_CLEAR_LOCATION_HISTORY)
            }
            try {
                ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
                Log.d(TAG, "BroadcastReceiver registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error registering receiver: ${e.message}")
                e.printStackTrace()
            }
            return receiver
        }
        
        fun unregister(context: Context, receiver: GPSRiderBroadcastReceiver) {
            try {
                context.unregisterReceiver(receiver)
                Log.d(TAG, "BroadcastReceiver unregistered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            Log.d(TAG, "onReceive called")
            
            if (context == null) {
                Log.w(TAG, "Received null context")
                return
            }
            
            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }
            
            val action = intent.action
            Log.d(TAG, "Received action: $action")
            
            if (action == null) {
                Log.w(TAG, "Received null action")
                return
            }
            
            if (!action.startsWith("com.dvhamham.")) {
                Log.d(TAG, "Ignoring non-GPS Rider intent: $action")
                return
            }
            
            Log.d(TAG, "Processing GPS Rider intent: $action")
            
            // Forward to IntentService
            val serviceIntent = Intent(context, IntentService::class.java).apply {
                this.action = action
                // Copy all extras safely
                intent.extras?.let { extras ->
                    putExtras(extras)
                    Log.d(TAG, "Copied extras: ${extras.keySet()}")
                }
            }
            
            Log.d(TAG, "Starting IntentService with action: $action")
            
            // Use startForegroundService for Android 8+ to avoid background restrictions
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d(TAG, "IntentService started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service: ${e.message}")
                e.printStackTrace()
                
                // Fallback: try to start activity instead
                try {
                    val activityIntent = Intent(context, MainActivity::class.java).apply {
                        this.action = action
                        intent.extras?.let { extras ->
                            putExtras(extras)
                        }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(activityIntent)
                    Log.d(TAG, "Fallback: Started MainActivity instead")
                } catch (e2: Exception) {
                    Log.e(TAG, "Error starting activity fallback: ${e2.message}")
                    e2.printStackTrace()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onReceive: ${e.message}")
            e.printStackTrace()
        }
    }
} 
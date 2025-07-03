// مثال تطبيق اختبار لـ Intent API
// يمكنك نسخ هذا الكود في تطبيق Android جديد لاختبار الوظائف

package com.example.intenttest

import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "IntentTest"
        
        // Intent Actions
        const val ACTION_START_FAKE_LOCATION = "com.dvhamham.START_FAKE_LOCATION"
        const val ACTION_STOP_FAKE_LOCATION = "com.dvhamham.STOP_FAKE_LOCATION"
        const val ACTION_TOGGLE_FAKE_LOCATION = "com.dvhamham.TOGGLE_FAKE_LOCATION"
        const val ACTION_SET_CUSTOM_LOCATION = "com.dvhamham.SET_CUSTOM_LOCATION"
        const val ACTION_GET_STATUS = "com.dvhamham.GET_STATUS"
        const val ACTION_GET_CURRENT_LOCATION = "com.dvhamham.GET_CURRENT_LOCATION"
        
        // Intent Extras
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_RESULT_RECEIVER = "result_receiver"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupButtons()
    }
    
    private fun setupButtons() {
        // تشغيل الموقع المزيف
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startFakeLocation()
        }
        
        // إيقاف الموقع المزيف
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopFakeLocation()
        }
        
        // تبديل حالة الموقع المزيف
        findViewById<Button>(R.id.btnToggle).setOnClickListener {
            toggleFakeLocation()
        }
        
        // تعيين موقع مخصص (الرياض)
        findViewById<Button>(R.id.btnSetRiyadh).setOnClickListener {
            setCustomLocation(24.7136, 46.6753, "الرياض")
        }
        
        // تعيين موقع مخصص (دبي)
        findViewById<Button>(R.id.btnSetDubai).setOnClickListener {
            setCustomLocation(25.2048, 55.2708, "دبي")
        }
        
        // معرفة الحالة
        findViewById<Button>(R.id.btnGetStatus).setOnClickListener {
            getStatus()
        }
        
        // معرفة الموقع الحالي
        findViewById<Button>(R.id.btnGetCurrentLocation).setOnClickListener {
            getCurrentLocation()
        }
    }
    
    private fun startFakeLocation() {
        val intent = Intent(ACTION_START_FAKE_LOCATION).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري تشغيل الموقع المزيف...")
    }
    
    private fun stopFakeLocation() {
        val intent = Intent(ACTION_STOP_FAKE_LOCATION).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري إيقاف الموقع المزيف...")
    }
    
    private fun toggleFakeLocation() {
        val intent = Intent(ACTION_TOGGLE_FAKE_LOCATION).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري تبديل حالة الموقع المزيف...")
    }
    
    private fun setCustomLocation(latitude: Double, longitude: Double, locationName: String) {
        val intent = Intent(ACTION_SET_CUSTOM_LOCATION).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_LATITUDE, latitude)
            putExtra(EXTRA_LONGITUDE, longitude)
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري تعيين الموقع إلى $locationName...")
    }
    
    private fun getStatus() {
        val intent = Intent(ACTION_GET_STATUS).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري استعلام الحالة...")
    }
    
    private fun getCurrentLocation() {
        val intent = Intent(ACTION_GET_CURRENT_LOCATION).apply {
            setPackage("com.dvhamham")
            putExtra(EXTRA_RESULT_RECEIVER, createResultReceiver())
        }
        startService(intent)
        showToast("جاري استعلام الموقع الحالي...")
    }
    
    private fun createResultReceiver(): ResultReceiver {
        return object : ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val message = resultData?.getString("message") ?: "نتيجة غير معروفة"
                
                runOnUiThread {
                    when (resultCode) {
                        1 -> {
                            Log.d(TAG, "نجح العملية: $message")
                            showToast("✅ $message")
                        }
                        0 -> {
                            Log.e(TAG, "فشل العملية: $message")
                            showToast("❌ $message")
                        }
                        -1 -> {
                            Log.w(TAG, "معاملات غير صحيحة: $message")
                            showToast("⚠️ $message")
                        }
                    }
                }
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// مثال لاستخدام IntentHelper (إذا كان متاحاً)
class IntentHelperExample {
    
    fun testIntentHelper(context: android.content.Context) {
        // تشغيل الموقع المزيف
        com.dvhamham.manager.IntentHelper.startFakeLocation(context) { success, message ->
            if (success) {
                Log.d("IntentHelper", "تم تشغيل الموقع المزيف: $message")
            } else {
                Log.e("IntentHelper", "فشل في تشغيل الموقع المزيف: $message")
            }
        }
        
        // تعيين موقع مخصص
        com.dvhamham.manager.IntentHelper.setCustomLocation(context, 24.7136, 46.6753) { success, message ->
            Log.d("IntentHelper", "نتيجة تعيين الموقع: $message")
        }
        
        // معرفة الحالة
        com.dvhamham.manager.IntentHelper.getStatus(context) { success, message ->
            Log.d("IntentHelper", "حالة الموقع المزيف: $message")
        }
    }
} 
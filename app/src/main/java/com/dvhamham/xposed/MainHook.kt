// MainHook.kt
package com.dvhamham.xposed

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.dvhamham.data.MANAGER_APP_PACKAGE_NAME
import com.dvhamham.xposed.hooks.LocationApiHooks
import com.dvhamham.xposed.hooks.SystemServicesHooks
import com.dvhamham.xposed.utils.PreferencesUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {
    val tag = "[MainHook]"

    lateinit var context: Context

    private var locationApiHooks: LocationApiHooks? = null
    private var systemServicesHooks: SystemServicesHooks? = null

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XposedBridge.log("$tag Loading package: ${lpparam.packageName}")
        
        // Avoid hooking own app to prevent recursion
        if (lpparam.packageName == MANAGER_APP_PACKAGE_NAME) {
            XposedBridge.log("$tag Skipping own app: ${lpparam.packageName}")
            return
        }

        // Check if fake location is enabled
        val isPlaying = PreferencesUtil.getIsPlaying()
        XposedBridge.log("$tag Is playing: $isPlaying")
        
        // If not playing or null, do not proceed with hooking
        if (isPlaying != true) {
            XposedBridge.log("$tag Fake location not enabled, skipping hooks")
            return
        }

        // Hook system services if user asked for system wide hooks
        if (lpparam.packageName == "android") {
            XposedBridge.log("$tag Hooking system services")
            systemServicesHooks = SystemServicesHooks(lpparam).also { it.initHooks() }
        }

        initHookingLogic(lpparam)
    }

    private fun initHookingLogic(lpparam: LoadPackageParam) {
        XposedBridge.log("$tag Initializing hooks for: ${lpparam.packageName}")
        
        XposedHelpers.findAndHookMethod(
            "android.app.Instrumentation",
            lpparam.classLoader,
            "callApplicationOnCreate",
            Application::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    context = (param.args[0] as Application).applicationContext.also {
                        XposedBridge.log("$tag Target App's context has been acquired successfully.")
                        Toast.makeText(it, "Fake Location Is Active!", Toast.LENGTH_SHORT).show()
                    }
                    locationApiHooks = LocationApiHooks(lpparam).also { it.initHooks() }
                    XposedBridge.log("$tag Location API hooks initialized for: ${lpparam.packageName}")
                }
            }
        )
    }
}
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
        
        // --- HOOK XposedChecker.isModuleActive() in main app ---
        if (lpparam.packageName == MANAGER_APP_PACKAGE_NAME) {
            try {
                XposedHelpers.findAndHookMethod(
                    "com.dvhamham.manager.XposedChecker",
                    lpparam.classLoader,
                    "isModuleActive",
                    object : de.robv.android.xposed.XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam): Any {
                            XposedBridge.log("$tag XposedChecker.isModuleActive() hooked: returning true")
                            return true
                        }
                    }
                )
            } catch (e: Exception) {
                XposedBridge.log("$tag Failed to hook XposedChecker.isModuleActive: ${e.message}")
            }
            XposedBridge.log("$tag Skipping own app: ${lpparam.packageName}")
            return
        }

        // Hook system services فقط إذا كان useSystemHook مفعل
        if (lpparam.packageName == "android") {
            com.dvhamham.xposed.utils.PreferencesUtil.reloadPrefs()
            if (com.dvhamham.xposed.utils.PreferencesUtil.getUseSystemHook() == true) {
                XposedBridge.log("$tag Hooking system services (system_server) [System Hook ENABLED]")
                systemServicesHooks = SystemServicesHooks(lpparam).also { it.initHooks() }
            } else {
                XposedBridge.log("$tag System hook is DISABLED by user preference.")
            }
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
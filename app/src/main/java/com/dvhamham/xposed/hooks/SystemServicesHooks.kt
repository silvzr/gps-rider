// SystemServicesHooks.kt
package com.dvhamham.xposed.hooks

import android.location.Location
import android.location.LocationRequest
import android.os.Build
import com.dvhamham.xposed.utils.LocationUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class SystemServicesHooks(val appLpparam: LoadPackageParam) {
    private val tag = "[SystemServicesHooks]"

    fun initHooks() {
        hookSystemServices(appLpparam.classLoader)
        XposedBridge.log("$tag Instantiated hooks successfully")
    }

    private fun hookSystemServices(classLoader: ClassLoader) {
        try {
            val locationManagerServiceClass = XposedHelpers.findClass("com.android.server.LocationManagerService", classLoader)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                XposedHelpers.findAndHookMethod(
                    locationManagerServiceClass,
                    "getLastLocation",
                    LocationRequest::class.java,
                    String::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            XposedBridge.log("$tag [SystemHook] Entered method getLastLocation(locationRequest, packageName)")
                            XposedBridge.log("\t Request comes from: ${param.args[1] as String}")
                            com.dvhamham.xposed.utils.PreferencesUtil.reloadPrefs()
                            if (com.dvhamham.xposed.utils.PreferencesUtil.getIsPlaying() == true) {
                                val fakeLocation = com.dvhamham.xposed.utils.LocationUtil.createFakeLocation()
                                param.result = fakeLocation
                                XposedBridge.log("\t Modified to: $fakeLocation (original method not executed)")
                            }
                        }
                    })
            } else {
                XposedBridge.log("$tag API level too low. System services hooks are not available.")
            }

            val methodsToReplace = arrayOf(
                "addGnssBatchingCallback",
                "addGnssMeasurementsListener",
                "addGnssNavigationMessageListener"
            )

            for (methodName in methodsToReplace) {
                XposedHelpers.findAndHookMethod(
                    locationManagerServiceClass,
                    methodName,
                    XC_MethodReplacement.returnConstant(false)
                )
            }


            XposedHelpers.findAndHookMethod(
                XposedHelpers.findClass("com.android.server.LocationManagerService\$Receiver", classLoader),
                "callLocationChangedLocked",
                Location::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("$tag [SystemHook] Entered method callLocationChangedLocked(location)")
                        com.dvhamham.xposed.utils.PreferencesUtil.reloadPrefs()
                        if (com.dvhamham.xposed.utils.PreferencesUtil.getIsPlaying() == true) {
                            val fakeLocation = com.dvhamham.xposed.utils.LocationUtil.createFakeLocation(param.args[0] as? Location)
                            param.args[0] = fakeLocation
                            XposedBridge.log("\t Modified to: $fakeLocation")
                        }
                    }
                })

        } catch (e: Exception) {
            XposedBridge.log("$tag Error hooking system services")
            XposedBridge.log(e)
        }
    }
}
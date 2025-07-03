// LocationApiHooks.kt
package com.dvhamham.xposed.hooks

import android.location.Location
import com.dvhamham.xposed.utils.LocationUtil
import com.dvhamham.xposed.utils.PreferencesUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LocationApiHooks(val appLpparam: LoadPackageParam) {
    private val tag = "[LocationApiHooks]"

    fun initHooks() {
        hookLocationAPI()
        XposedBridge.log("$tag Instantiated hooks successfully")
    }

    private fun hookLocationAPI() {
        hookLocation(appLpparam.classLoader)
        hookLocationManager(appLpparam.classLoader)
    }

    private fun hookLocation(classLoader: ClassLoader) {
        try {
            val locationClass = XposedHelpers.findClass("android.location.Location", classLoader)

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getLatitude",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getLatitude()")
                        XposedBridge.log("\t Original latitude: ${param.result as Double}")
                        param.result = LocationUtil.latitude
                        XposedBridge.log("\t Modified to: ${LocationUtil.latitude}")
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getLongitude",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getLongitude()")
                        XposedBridge.log("\t Original longitude: ${param.result as Double}")
                        param.result =  LocationUtil.longitude
                        XposedBridge.log("\t Modified to: ${LocationUtil.longitude}")
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getAccuracy",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getAccuracy()")
                        XposedBridge.log("\t Original accuracy: ${param.result as Float}")
                        if (PreferencesUtil.getUseAccuracy() == true) {
                            param.result =  LocationUtil.accuracy
                            XposedBridge.log("\t Modified to: ${LocationUtil.accuracy}")
                        }
                    }

                    })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getAltitude",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getAltitude()")
                        XposedBridge.log("\t Original altitude: ${param.result as Double}")
                        if (PreferencesUtil.getUseAltitude() == true) {
                            param.result =  LocationUtil.altitude
                            XposedBridge.log("\t Modified to: ${LocationUtil.altitude}")
                        }
                    }

                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getVerticalAccuracyMeters",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getVerticalAccuracyMeters()")
                        XposedBridge.log("\tOriginal vertical accuracy: ${param.result as Float}")
                        if (PreferencesUtil.getUseVerticalAccuracy() == true) {
                            param.result = LocationUtil.verticalAccuracy
                            XposedBridge.log("\tModified to: ${LocationUtil.verticalAccuracy}")
                        }
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getSpeed",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getSpeed()")
                        XposedBridge.log("\tOriginal speed: ${param.result as Float}")
                        if (PreferencesUtil.getUseSpeed() == true) {
                            param.result = LocationUtil.speed
                            XposedBridge.log("\tModified to: ${LocationUtil.speed}")
                        }
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getSpeedAccuracyMetersPerSecond",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        LocationUtil.updateLocation()
                        XposedBridge.log("$tag Leaving method getSpeedAccuracyMetersPerSecond()")
                        XposedBridge.log("\tOriginal speed accuracy: ${param.result as Float}")
                        if (PreferencesUtil.getUseSpeedAccuracy() == true) {
                            param.result = LocationUtil.speedAccuracy
                            XposedBridge.log("\tModified to: ${LocationUtil.speedAccuracy}")
                        }
                    }
                })

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                XposedHelpers.findAndHookMethod(
                    locationClass,
                    "getMslAltitudeMeters",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            LocationUtil.updateLocation()
                            XposedBridge.log("$tag Leaving method getMslAltitudeMeters()")
                            val originalMslAltitude = param.result as? Double
                            XposedBridge.log("\tOriginal MSL altitude: $originalMslAltitude")
                            if (PreferencesUtil.getUseMeanSeaLevel() == true) {
                                param.result = LocationUtil.meanSeaLevel
                                XposedBridge.log("\tModified to: ${LocationUtil.meanSeaLevel}")
                            }
                        }
                    })

                // Hook getMslAltitudeAccuracyMeters()
                XposedHelpers.findAndHookMethod(
                    locationClass,
                    "getMslAltitudeAccuracyMeters",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            LocationUtil.updateLocation()
                            XposedBridge.log("$tag Leaving method getMslAltitudeAccuracyMeters()")
                            val originalMslAltitudeAccuracy = param.result as? Float
                            XposedBridge.log("\tOriginal MSL altitude accuracy: $originalMslAltitudeAccuracy")
                            if (PreferencesUtil.getUseMeanSeaLevelAccuracy() == true) {
                                param.result = LocationUtil.meanSeaLevelAccuracy
                                XposedBridge.log("\tModified to: ${LocationUtil.meanSeaLevelAccuracy}")
                            }
                        }
                    })
            } else {
                XposedBridge.log("$tag getMslAltitudeMeters() and getMslAltitudeAccuracyMeters() not available on this API level")
            }

        } catch (e: Exception) {
            XposedBridge.log("$tag Error hooking Location class - ${e.message}")
        }
    }

    private fun hookLocationManager(classLoader: ClassLoader) {
        try {
            val locationManagerClass = XposedHelpers.findClass("android.location.LocationManager", classLoader)

            XposedHelpers.findAndHookMethod(
                locationManagerClass,
                "getLastKnownLocation",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("$tag Leaving method getLastKnownLocation(provider)")
                        XposedBridge.log("\t Original location: ${param.result as? Location}")
                        val provider = param.args[0] as String
                        XposedBridge.log("\t Requested data from: $provider")
                        val fakeLocation =  LocationUtil.createFakeLocation(provider = provider)
                        param.result = fakeLocation
                        XposedBridge.log("\t Modified location: $fakeLocation")
                    }
                })

        } catch (e: Exception) {
            XposedBridge.log("$tag Error hooking LocationManager - ${e.message}")
        }
    }
}
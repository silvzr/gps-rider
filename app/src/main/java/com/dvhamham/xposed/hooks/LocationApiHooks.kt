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
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true) {
                            LocationUtil.updateLocation()
                            param.result = LocationUtil.latitude
                        }
                        // else: leave param.result as is (real location)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getLongitude",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true) {
                            LocationUtil.updateLocation()
                            param.result =  LocationUtil.longitude
                        }
                        // else: leave param.result as is (real location)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getAccuracy",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseAccuracy() == true) {
                            LocationUtil.updateLocation()
                            param.result =  LocationUtil.accuracy
                        }
                        // else: leave param.result as is (real accuracy)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getAltitude",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseAltitude() == true) {
                            LocationUtil.updateLocation()
                            param.result =  LocationUtil.altitude
                        }
                        // else: leave param.result as is (real altitude)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getVerticalAccuracyMeters",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseVerticalAccuracy() == true) {
                            LocationUtil.updateLocation()
                            param.result = LocationUtil.verticalAccuracy
                        }
                        // else: leave param.result as is (real vertical accuracy)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getSpeed",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseSpeed() == true) {
                            LocationUtil.updateLocation()
                            param.result = LocationUtil.speed
                        }
                        // else: leave param.result as is (real speed)
                    }
                })

            XposedHelpers.findAndHookMethod(
                locationClass,
                "getSpeedAccuracyMetersPerSecond",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseSpeedAccuracy() == true) {
                            LocationUtil.updateLocation()
                            param.result = LocationUtil.speedAccuracy
                        }
                        // else: leave param.result as is (real speed accuracy)
                    }
                })

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                XposedHelpers.findAndHookMethod(
                    locationClass,
                    "getMslAltitudeMeters",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            PreferencesUtil.reloadPrefs()
                            if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseMeanSeaLevel() == true) {
                                LocationUtil.updateLocation()
                                param.result = LocationUtil.meanSeaLevel
                            }
                            // else: leave param.result as is (real MSL altitude)
                        }
                    })

                // Hook getMslAltitudeAccuracyMeters()
                XposedHelpers.findAndHookMethod(
                    locationClass,
                    "getMslAltitudeAccuracyMeters",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            PreferencesUtil.reloadPrefs()
                            if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseMeanSeaLevelAccuracy() == true) {
                                LocationUtil.updateLocation()
                                param.result = LocationUtil.meanSeaLevelAccuracy
                            }
                            // else: leave param.result as is (real MSL altitude accuracy)
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
                        PreferencesUtil.reloadPrefs()
                        if (PreferencesUtil.getIsPlaying() == true) {
                            XposedBridge.log("$tag Leaving method getLastKnownLocation(provider)")
                            XposedBridge.log("\t Original location: "+ (param.result as? Location))
                            val provider = param.args[0] as String
                            XposedBridge.log("\t Requested data from: $provider")
                            val fakeLocation =  LocationUtil.createFakeLocation(provider = provider)
                            param.result = fakeLocation
                            XposedBridge.log("\t Modified location: $fakeLocation")
                        }
                        // else: leave param.result as is (real location)
                    }
                })

        } catch (e: Exception) {
            XposedBridge.log("$tag Error hooking LocationManager - ${e.message}")
        }
    }
}
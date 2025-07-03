package com.dvhamham.manager

object XposedChecker {
    @JvmStatic
    fun isModuleActive(): Boolean = false // Will be hooked by Xposed
} 
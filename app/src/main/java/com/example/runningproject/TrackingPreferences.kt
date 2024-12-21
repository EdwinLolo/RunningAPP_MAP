package com.example.runningproject

import android.content.Context
import android.content.SharedPreferences

object TrackingPreferences {
    private const val PREFS_NAME = "tracking_prefs"
    private const val KEY_IS_TRACKING = "is_tracking"

    fun setTracking(context: Context, isTracking: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_TRACKING, isTracking).apply()
    }

    fun isTracking(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_TRACKING, false)
    }
}
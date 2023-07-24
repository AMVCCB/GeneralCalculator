package com.tools.calculator.sharepreference

import android.content.Context
import android.preference.PreferenceManager

class MyPreferences(context: Context) {

    // https://proandroiddev.com/dark-mode-on-android-app-with-kotlin-dc759fc5f0e1
    companion object {
        private const val KEY_VIBRATION_STATUS = "KEY_VIBRATION_STATUS"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var vibrationMode = preferences.getBoolean(KEY_VIBRATION_STATUS, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION_STATUS, value).apply()
}

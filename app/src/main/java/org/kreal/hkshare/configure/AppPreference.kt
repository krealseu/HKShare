package org.kreal.hkshare.configure

import android.content.Context
import android.preference.PreferenceManager

class AppPreference(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val edit = sharedPreferences.edit()

    fun getPort(): Int = sharedPreferences.getInt(keyPort, 8080)

    fun setPort(port: Int) = edit.putInt(keyPort, port).commit()

    fun getIsShareApp(): Boolean = sharedPreferences.getBoolean(keyShareApp, true)

    fun setIsShareApp(share: Boolean) = edit.putBoolean(keyShareApp, share).commit()

    fun getIsShareDirectory(): Boolean = sharedPreferences.getBoolean(keyShareDirectory, true)

    fun setIsShareDirectory(share: Boolean) = edit.putBoolean(keyShareDirectory, share).commit()

    fun getCustomRout(): Set<String> = sharedPreferences.getStringSet(keyCustomRout, setOf())

    fun setCustomRout(set: Set<String>) = edit.putStringSet(keyCustomRout, set)

    companion object {
        const val keyShareApp = "share_app"
        const val keyShareDirectory = "share_directory"
        const val keyCustomRout = "custom_rout"
        const val keyPort = "Port"
    }
}
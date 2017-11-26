package org.kreal.hkshare

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import com.squareup.leakcanary.LeakCanary

/**
 * Created by lthee on 2017/10/13.
 */
class APP : Application() {
    val USE_LEAK_CANARY = true
    override fun onCreate() {
        super.onCreate()
        if (USE_LEAK_CANARY) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
//        startActivity(Intent(baseContext, PermissionsRequestActivity::class.java))
    }
}
package org.kreal.hkshare

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import org.kreal.hkshare.configure.AppPreference

/**
 * Created by lthee on 2017/10/13.
 * 应用启动前加载 部件
 */
class APP : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
        preference = AppPreference(this)
    }

    companion object {
        lateinit var preference: AppPreference
            private set
    }
}
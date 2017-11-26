package org.kreal.hkshare.extensions

import android.util.Log

/**
 * Created by lthee on 2017/10/17.
 */

val debug = true

infix fun Any.logi(info: String) {
    if (debug)
        Log.i(this.javaClass.simpleName, info)
}
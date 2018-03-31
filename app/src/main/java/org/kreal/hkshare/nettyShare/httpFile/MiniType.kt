package org.kreal.hkshare.nettyShare.httpFile

import android.webkit.MimeTypeMap

internal fun getTypeForName(name: String): String {
    val lastDot = name.lastIndexOf('.')
    if (lastDot >= 0) {
        val extension = name.substring(lastDot + 1).toLowerCase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mime != null) {
            return mime
        }
    }
    return "application/octet-stream"
}
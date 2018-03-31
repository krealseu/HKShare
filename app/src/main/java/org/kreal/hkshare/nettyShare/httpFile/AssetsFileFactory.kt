package org.kreal.hkshare.nettyShare.httpFile

import android.content.res.AssetManager

/**
 * Created by lthee on 2017/10/22.
 *
 */
class AssetsFileFactory(private val assetManager: AssetManager, private val rootHttp: String) : HttpFileFactory {
    init {
        if (!rootHttp.endsWith('/'))
            throw Exception("The root Path must end with '/'")
    }

    override fun newHttpFile(path: String): HttpFile = when (path) {
        "", "/" -> EmptyHttpFile()
        else -> AssetsHttpFile(assetManager, path, "$rootHttp$path")
    }
}
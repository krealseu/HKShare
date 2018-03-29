package org.kreal.hkshare.nettyShare.httpFile

import android.content.res.AssetManager

/**
 * Created by lthee on 2017/10/22.
 *
 */
class AssetsFileFactory(private val assetManager: AssetManager, private val root: String) : HttpFileFactory {
    override fun newHttpFile(path: String): HttpFile {
        val filename = path.replaceFirst(root + "/", "")
        return AssetsHttpFile(assetManager, filename, path)
    }
}
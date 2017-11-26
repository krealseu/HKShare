package org.kreal.hkshare.nettyShare.HttpFile

import java.io.File

/**
 * Created by lthee on 2017/10/22.
 */
class NativeFileFactory : HttpFileFactory {
    private val root: String = "/"
    private val rootFile = File("/sdcard")
    override fun newHttpFile(path: String): HttpFile {
        var file = path.replaceFirst(root, "")
        return NativeHttpFile(File(rootFile, file), path)
    }
}
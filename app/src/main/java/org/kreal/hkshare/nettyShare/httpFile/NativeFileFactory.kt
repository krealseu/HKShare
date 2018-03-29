package org.kreal.hkshare.nettyShare.httpFile

import java.io.File

/**
 * Created by lthee on 2017/10/22.
 *
 */
class NativeFileFactory(private val uri: String, private val root: File) : HttpFileFactory {
    override fun newHttpFile(path: String): HttpFile {
        val fileName = path.replaceFirst(uri, "")
        return NativeHttpFile(File(root, fileName), path)
    }
}
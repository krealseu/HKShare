package org.kreal.hkshare.nettyShare.httpFile

import java.io.File

/**
 * Created by lthee on 2017/10/22.
 *
 */
class NativeFileFactory(private val root: File, private val rootHttp: String) : HttpFileFactory {
    init {
        if (!rootHttp.endsWith('/'))
            throw Exception("The root Path must end with '/'")
    }
    override fun newHttpFile(path: String): HttpFile = NativeHttpFile(File(root, path), "$rootHttp$path")
}
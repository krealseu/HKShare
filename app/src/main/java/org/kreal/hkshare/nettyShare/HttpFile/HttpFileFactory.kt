package org.kreal.hkshare.nettyShare.HttpFile

/**
 * Created by lthee on 2017/10/29.
 */
interface HttpFileFactory {
    fun newHttpFile(path: String): HttpFile
}
package org.kreal.hkshare.nettyShare.HttpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/7.
 */

interface HttpFile {
    val isFile: Boolean
    val etag: String
    val name: String
    val isDirectory: Boolean
    val uri: String
    val channel: FileChannel?
    fun exist(): Boolean
    fun listfile(): Array<HttpFile>
    @Throws(IOException::class)
    fun getInputStream(pos: Long): InputStream

    fun getmimetype(): String
    fun length(): Long
    fun lastModified(): Long
}

package org.kreal.hkshare.nettyShare.httpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/7.
 * HttpFile的统一接口
 */

interface HttpFile {
    val isFile: Boolean
    val eTag: String
    val name: String
    val isDirectory: Boolean
    val uri: String
    val channel: FileChannel?
    fun exist(): Boolean
    fun listFiles(): Array<HttpFile>
    @Throws(IOException::class)
    fun getInputStream(pos: Long): InputStream
    fun getMimeType(): String
    fun length(): Long
    fun lastModified(): Long
}

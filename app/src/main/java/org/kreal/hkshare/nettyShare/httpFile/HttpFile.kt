package org.kreal.hkshare.nettyShare.httpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/7.
 * HttpFile的统一接口
 */

interface HttpFile {
    fun exist(): Boolean
    val isFile: Boolean
    val isDirectory: Boolean
    val isReadable: Boolean
    fun length(): Long
    fun lastModified(): Long
    val eTag: String
    val name: String
    val uri: String
    fun getMimeType(): String
    fun listFiles(): Array<HttpFile>
    val channel: FileChannel?
    @Throws(IOException::class)
    fun getInputStream(pos: Long): InputStream
}

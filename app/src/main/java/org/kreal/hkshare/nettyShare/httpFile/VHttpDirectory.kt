package org.kreal.hkshare.nettyShare.httpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2018/3/17.
 *虚拟的目录文件
 */
class VHttpDirectory(override val uri: String, override val name: String) : HttpFile {
    override val isReadable: Boolean = false
    override val isFile: Boolean = false
    override val eTag: String = ""
    override val isDirectory: Boolean = true
    override fun exist(): Boolean = true
    override fun getMimeType(): String = ""
    override fun length(): Long = 0
    override fun lastModified(): Long = 0
    override fun listFiles(): Array<HttpFile> = httpFiles.toTypedArray()

    override val channel: FileChannel? = null

    @Throws(IOException::class)
    override fun getInputStream(pos: Long): InputStream {
        throw IOException("This is directory")
    }

    private val httpFiles: MutableList<HttpFile> = ArrayList()

    fun add(httpFile: HttpFile) {
        httpFiles.add(httpFile)
    }
}

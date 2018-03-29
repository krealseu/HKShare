package org.kreal.hkshare.nettyShare.httpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2018/3/17.
 *
 */
class VHttpDirectory(override val uri: String, override val name: String) : HttpFile {
    override val isFile: Boolean
        get() = false
    override val eTag: String
        get() = ""
    override val isDirectory: Boolean
        get() = true
    override val channel: FileChannel? = null

    override fun exist(): Boolean = true

    private val httpFiles: MutableList<HttpFile> = ArrayList()

    fun add(httpFile: HttpFile) {
        httpFiles.add(httpFile)
    }

    override fun listFiles(): Array<HttpFile> = httpFiles.toTypedArray()


    @Throws(IOException::class)
    override fun getInputStream(pos: Long): InputStream {
        throw IOException("This is directory")
    }

    override fun getMimeType(): String = ""

    override fun length(): Long = 0

    override fun lastModified(): Long = 0
}
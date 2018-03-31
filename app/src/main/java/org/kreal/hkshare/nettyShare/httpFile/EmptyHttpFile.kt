package org.kreal.hkshare.nettyShare.httpFile

import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

class EmptyHttpFile(override val name: String = "/", override val uri: String = "/") : HttpFile {
    constructor(uri: String) : this(uri.substring(if (uri.lastIndexOf('/') == -1) 0 else uri.lastIndexOf('/')), uri)

    override val isFile: Boolean = false
    override val isReadable: Boolean = false
    override val eTag: String = "empty"
    override val isDirectory: Boolean = false
    override val channel: FileChannel? = null
    override fun exist(): Boolean = false
    override fun listFiles(): Array<HttpFile> = emptyArray()

    override fun getInputStream(pos: Long): InputStream = throw IOException()

    override fun getMimeType(): String = "."

    override fun length(): Long = 0

    override fun lastModified(): Long = 0
}
package org.kreal.hkshare.nettyShare.httpFile

import org.kreal.hkshare.extensions.getMimeType
import java.io.*
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/17.
 *
 */
class ApkHttpFile(private val file: File, httpPath: String, label: String? = null) : HttpFile {
    override val isReadable: Boolean = file.canRead()
    override val channel: FileChannel
        get() = FileInputStream(file).channel

    override val uri: String = httpPath//.replace("/+".toRegex(), "/")

    override val isFile: Boolean = file.exists() && file.isFile

    override val eTag: String = Integer.toHexString((this.file.absolutePath + this.file.lastModified() + this.file.length()).hashCode())

    override val name: String = label ?: file.name

    override val isDirectory: Boolean = false

    override fun listFiles(): Array<HttpFile> = emptyArray()

    @Throws(IOException::class)
    override fun getInputStream(pos: Long): InputStream {
        val raf = RandomAccessFile(this.file, "r")
        raf.seek(pos)
        return object : FileInputStream(raf.fd) {
            @Throws(IOException::class)
            override fun close() {
                super.close()
                raf.close()
            }
        }

    }

    override fun getMimeType(): String = getTypeForName("app.apk")

    override fun length(): Long = this.file.length()

    override fun lastModified(): Long = this.file.lastModified()

    override fun exist(): Boolean = this.file.exists()
}
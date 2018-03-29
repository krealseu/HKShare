package org.kreal.hkshare.nettyShare.httpFile

import android.webkit.MimeTypeMap
import java.io.*
import java.nio.channels.FileChannel
import java.util.*

class NativeHttpFile(private val file: File, httpPath: String) : HttpFile {
    override val channel: FileChannel
        get() = FileInputStream(file).channel

    override val uri: String = httpPath.replace("/+".toRegex(), "/")

    override val isFile: Boolean = file.exists() && file.isFile

    override val eTag: String = Integer.toHexString((this.file.absolutePath + this.file.lastModified() + this.file.length()).hashCode())

    override val name: String = file.name

    override val isDirectory: Boolean = file.exists() && file.isDirectory

    override fun listFiles(): Array<HttpFile> {
        val files = this.file.listFiles({ file -> !file.name.startsWith(".") })
        Arrays.sort(files, Comparator { f1, f2 ->
            if (f1.isDirectory && f2.isFile)
                return@Comparator -1
            else if (f1.isFile && f2.isDirectory)
                1
            else f1.name.compareTo(f2.name, ignoreCase = true)
        })
        return Array(files.size) { i ->
            NativeHttpFile(files[i], "$uri/${files[i].name}")
        }
    }

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

    override fun getMimeType(): String = if (isDirectory) "" else getTypeForName(name)

    override fun length(): Long = this.file.length()

    override fun lastModified(): Long = this.file.lastModified()

    override fun exist(): Boolean = this.file.exists()

    private fun getTypeForName(name: String): String {
        val lastDot = name.lastIndexOf('.')
        if (lastDot >= 0) {
            val extension = name.substring(lastDot + 1).toLowerCase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mime != null) {
                return mime
            }
        }
        return "application/octet-stream"
    }
/*
    private fun isPhoto(name: String): Boolean {
        return if (name.matches("(bmp|jpg|jpeg|png|gif)$".toRegex()))
            true
        else
            false
    }

    private fun isVideo(name: String): Boolean {
        return if (name.matches("(rmvb|avi|mp4|mkv|flv)$".toRegex()))
            true
        else
            false
    }

    private fun isMusic(name: String): Boolean {
        return if (name.matches("(mp3|ape|ogg|wav|ape|cda|au|midi|acc)$".toRegex()))
            true
        else
            false
    }

    private fun isTxt(name: String): Boolean {
        return if (name.matches("(txt|xml|html|ini)$".toRegex()))
            true
        else
            false
    }

    private fun initResponseHeader(name: String): String {
        var name = name
        name = name.toLowerCase()
        val lastname = name.substring(name.lastIndexOf(".") + 1, name.length)
        return if (isPhoto(lastname))
            String.format("image/%s", lastname)
        else if (isVideo(lastname))
            String.format("video/%s", lastname)
        else if (isMusic(lastname))
            String.format("audio/%s", lastname)
        else if (isTxt(lastname))
            String.format("text/%s", lastname)
        else {
            String.format("application/%s", lastname)
        }
    }
    */

}

package org.kreal.hkshare.nettyShare.HttpFile

import org.kreal.hkshare.extensions.getMimeType
import java.io.*
import java.nio.channels.FileChannel
import java.util.*

class NativeHttpFile(private val file: File, httppath: String) : HttpFile {
    override val channel: FileChannel
        get() = FileInputStream(file).channel

    override val uri: String = httppath.replace("/+".toRegex(), "/")

    override val isFile: Boolean = file.exists() && file.isFile

    override val etag: String = Integer.toHexString((this.file.absolutePath + this.file.lastModified() + this.file.length()).hashCode())

    override val name: String = file.name

    override val isDirectory: Boolean = file.exists() && file.isDirectory

    override fun listfile(): Array<HttpFile> {
        val files = this.file.listFiles({ file -> !file.name.startsWith(".") })
        Arrays.sort(files, Comparator { f1, f2 ->
            if (f1.isDirectory && f2.isFile)
                return@Comparator -1
            if (f1.isFile && f2.isDirectory) 1 else f1.name.compareTo(f2.name, ignoreCase = true)
        })
        return Array(files.size) { i ->
            NativeHttpFile(files[i], uri + '/' + files[i].name)
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

    override fun getmimetype(): String = this.file.name.getMimeType()

    override fun length(): Long = this.file.length()

    override fun lastModified(): Long = this.file.lastModified()

    override fun exist(): Boolean = this.file.exists()


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

package org.kreal.hkshare.NanoShare.HttpFile

import android.content.res.AssetManager
import org.kreal.hkshare.extensions.getMimeType
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/15.
 */
class AssetsHttpFile(val assetManager: AssetManager, filepath: String, httppath: String) : HttpFile {
    override val channel: FileChannel
        get() = FileInputStream(assetManager.openFd(filePath).fileDescriptor).channel
    override val uri = httppath.replace("/+".toRegex(), "/")
    private val filePath = when (filepath.startsWith('/')) {
        true -> filepath.substring(1)
        false -> filepath
    }

    constructor(assetsHttpFile: AssetsHttpFile, name: String)
            : this(assetsHttpFile.assetManager,
            (assetsHttpFile.filePath + '/' + name).replace("/+".toRegex(), "/"),
            assetsHttpFile.uri + '/' + name)

    override val isFile: Boolean
        get() {
            var result = false
            try {
                assetManager.open(filePath).close()
                result = true
            } catch (e: IOException) {
                result = false
            }
            return result
        }

    override val etag: String = Integer.toHexString((uri + lastModified()).hashCode())

    override val name: String =
            when (filePath.length) {
                1, 0 -> ""
                else -> filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length)
            }

    override val isDirectory: Boolean = false

    override fun exist(): Boolean = assetManager.list(filePath).isEmpty()

    override fun listfile(): Array<HttpFile> = emptyArray()


    override fun getInputStream(pos: Long): InputStream {
        val input = assetManager.open(filePath, AssetManager.ACCESS_RANDOM)
        input.skip(pos)
        return input
    }

    override fun getmimetype(): String = (uri + filePath).getMimeType()

    override fun length(): Long {
        var result = 0
        try {
            val input = assetManager.open(filePath)
            result = input.available()
            input.close()
        } catch (e: IOException) {
            result = 0
        }
        return result.toLong()
    }

    override fun lastModified(): Long = 0

}
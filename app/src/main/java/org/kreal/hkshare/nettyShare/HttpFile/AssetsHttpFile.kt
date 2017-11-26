package org.kreal.hkshare.nettyShare.HttpFile

import android.content.res.AssetManager
import org.kreal.hkshare.extensions.getMimeType
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel
import java.util.*

/**
 * Created by lthee on 2017/10/15.
 */
class AssetsHttpFile(val assetManager: AssetManager, filename: String, httppath: String) : HttpFile {

    override val channel: FileChannel? = null

    override val uri = httppath

    private val filename = filename

    override val isFile: Boolean
        get() {
            var result: Boolean
            try {
                assetManager.open(filename).close()
                result = true
            } catch (e: IOException) {
                result = false
            }
            return result
        }

    override val etag: String = Integer.toHexString((uri + lastModified()).hashCode())

    override val name: String = filename

    override val isDirectory: Boolean = false

    override fun exist(): Boolean = assetManager.list(filename).isEmpty()

    override fun listfile(): Array<HttpFile> = emptyArray()


    override fun getInputStream(pos: Long): InputStream {
        val input = assetManager.open(filename, AssetManager.ACCESS_RANDOM)
        input.skip(pos)
        return input
    }

    override fun getmimetype(): String = filename.getMimeType()

    override fun length(): Long {
        var result: Int
        try {
            val input = assetManager.open(filename)
            result = input.available()
            input.close()
        } catch (e: IOException) {
            result = 0
        }
        return result.toLong()
    }

    override fun lastModified(): Long = Date().time - 600000

}
package org.kreal.hkshare.nettyShare.httpFile

import android.content.res.AssetManager
import org.kreal.hkshare.extensions.getMimeType
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel
import java.util.*

/**
 * Created by lthee on 2017/10/15.
 *
 */
class AssetsHttpFile(private val assetManager: AssetManager, private val filename: String, httpPath: String) : HttpFile {

    override val channel: FileChannel? = null

    override val uri = httpPath

    override val isFile: Boolean
        get() = try {
            assetManager.open(filename).close()
            true
        } catch (e: IOException) {
            false
        }


    override val eTag: String = Integer.toHexString((uri + lastModified()).hashCode())

    override val name: String = filename

    override val isDirectory: Boolean = false

    override fun exist(): Boolean = assetManager.list(filename).isEmpty()

    override fun listFiles(): Array<HttpFile> = emptyArray()


    override fun getInputStream(pos: Long): InputStream {
        val input = assetManager.open(filename, AssetManager.ACCESS_RANDOM)
        input.skip(pos)
        return input
    }

    override fun getMimeType(): String = filename.getMimeType()

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
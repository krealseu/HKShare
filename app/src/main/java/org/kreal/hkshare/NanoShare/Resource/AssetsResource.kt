package org.kreal.hkshare.NanoShare.Resource

import android.content.res.AssetManager
import fi.iki.elonen.NanoHTTPD
import org.kreal.hkshare.NanoShare.HttpFile.AssetsHttpFile
import org.kreal.hkshare.extensions.logi

/**
 * Created by lthee on 2017/10/15.
 */
class AssetsResource(assetManager: AssetManager, private val path: String) : Resource() {
    override val matchs: Array<String> = arrayOf((path + "/[\\d\\D]*"))
    private val rootAssets = AssetsHttpFile(assetManager, "", path)
    override fun doGet(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        logi(session.uri)
        var uri = session.uri.trim()
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'))
        if (uri.contains("../"))
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                    "FORBIDDEN: Won't server ../ for security reasons.")
        val filename = uri.replaceFirst(path, "")
        var file = AssetsHttpFile(rootAssets, filename)
        if (!file.exist())
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                    "Error 404,resource not fount ..")
        return serverfile(session, file)
    }


}
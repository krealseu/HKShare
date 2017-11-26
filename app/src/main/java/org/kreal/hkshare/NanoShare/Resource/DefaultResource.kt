package org.kreal.hkshare.NanoShare.Resource

import fi.iki.elonen.NanoHTTPD
import org.kreal.hkshare.NanoShare.HttpFile.HttpFile
import org.kreal.hkshare.NanoShare.HttpFile.NativeHttpFile
import java.io.File

/**
 * Created by lthee on 2017/10/9.
 */
class DefaultResource : Resource() {
    private val wwwRoot = File("/sdcard")
    override val matchs: Array<String> = arrayOf("/*")

    override fun doGet(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        var uri = session.uri.trim()
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'))
        if (uri.contains("../"))
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                    "FORBIDDEN: Won't server ../ for security reasons.")
        var file = File(wwwRoot, session.uri)
        if (!file.exists())
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                    "Error 404,resource not fount ..")
        if (file.isDirectory) {
//            val fileindex = File(file, "index.html")
//            if (fileindex.exists())
//                file = fileindex
//            else {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML,
                        listDirectory1(NativeHttpFile(file, uri)))
                //listdirectory
//            }
        }
        return serverfile(session, NativeHttpFile(file, uri))
    }

    fun listDirectory1(file: HttpFile): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<html><head><title>" + file.name
                + "</title><style><!--\n" +
                "span.dirname { font-weight: bold; }\n" +
                "span.filesize { font-size: 75%; }\n" +
                "// -->\n" +
                "</style>" +
                "</head><body><h1>" + file.name + "</h1>")

        htmlBuilder.append("<ul>")
        for (f in file.listfile()) {
            if (f.isDirectory) {
                htmlBuilder.append("<li>")
                htmlBuilder.append("<a href=\"${f.uri}\">")
                htmlBuilder.append("<span class=\"dirname\">")
                htmlBuilder.append(f.name)
                htmlBuilder.append("</span>")
                htmlBuilder.append("</a>")
                htmlBuilder.append("</li>")
            } else if (f.isFile) {
                htmlBuilder.append("<li>")
                htmlBuilder.append("<a href=\"${f.uri}\">")
                htmlBuilder.append("<span class=\"filename\">")
                htmlBuilder.append(f.name)
                htmlBuilder.append("</span>")
                htmlBuilder.append("&nbsp;<span class=\"filesize\">")
                val len = f.length()
                htmlBuilder.append("&nbsp;<span class=\"filesize\">(")
                if (len < 1024) {
                    htmlBuilder.append(len).append(" bytes")
                } else if (len < 1024 * 1024) {
                    htmlBuilder.append(len / 1024).append('.').append(len % 1024 / 10 % 100)
                            .append(" KB")
                } else {
                    htmlBuilder.append(len / (1024 * 1024)).append('.')
                            .append(len % (1024 * 1024) / 10 % 100).append(" MB")
                }
                htmlBuilder.append(")</span>")
                htmlBuilder.append("</a>")
                htmlBuilder.append("</li>")
            }
        }
        htmlBuilder.append("</ul>")
        htmlBuilder.append("</body></html>")
        return htmlBuilder.toString()
    }

    private fun listDirectory(uri: String, f: HttpFile): String {
        val heading = "Directory " + uri
        val msg = StringBuilder("<html><head><title>" + heading
                + "</title><style><!--\n" +
                "span.dirname { font-weight: bold; }\n" +
                "span.filesize { font-size: 75%; }\n" +
                "// -->\n" +
                "</style>" +
                "</head><body><h1>" + heading + "</h1>")

        var up: String? = null
        if (uri.length > 1) {
            val u = uri.substring(0, uri.length - 1)
            val slash = u.lastIndexOf('/')
            if (slash >= 0 && slash < u.length) {
                up = uri.substring(0, slash + 1)
            }
        }

        val files = f.listfile()
        val directories = f.listfile()
        if (up != null || directories.size + files.size > 0) {
            msg.append("<ul>")
            if (up != null || directories.size > 0) {
                msg.append("<section class=\"directories\">")
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up)
                            .append("\"><span class=\"dirname\">..</span></a></b></li>")
                }
                for (directory in directories) {
                    val dir = directory.name + "/"
                    msg.append("<li><a rel=\"directory\" href=\"")
//                            .append(encodeUriBetweenSlashes(uri + dir))
                            .append("\"><span class=\"dirname\">").append(dir)
                            .append("</span></a></b></li>")
                }
                msg.append("</section>")
            }
            if (files.size > 0) {
                msg.append("<section class=\"files\">")
                for (file in files) {
                    msg.append("<li><a href=\"")
//                            .append(encodeUriBetweenSlashes(uri + file))
                            .append("\"><span class=\"filename\">").append(file)
                            .append("</span></a>")
//                    val curFile = File(f, file)
                    val len = f.length()
                    msg.append("&nbsp;<span class=\"filesize\">(")
                    if (len < 1024) {
                        msg.append(len).append(" bytes")
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append('.').append(len % 1024 / 10 % 100)
                                .append(" KB")
                    } else {
                        msg.append(len / (1024 * 1024)).append('.')
                                .append(len % (1024 * 1024) / 10 % 100).append(" MB")
                    }
                    msg.append(")</span></li>")
                }
                msg.append("</section>")
            }
            msg.append("</ul>")
        }
        msg.append("</body></html>")
        return msg.toString()
    }


}
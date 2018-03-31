package org.kreal.hkshare.extensions

/**
 * Created by lthee on 2017/10/15.
 *
 */
fun String.getMimeType(): String {
    val dot = this.lastIndexOf('.')
    var mini: String? = null
    if (dot >= 0) {
        mini = MIMETYPES[this.substring(dot + 1).toLowerCase()]
    }
    return if (mini == null) "application/octet-stream" else mini
}

fun String.getAppico(): String {
    val dot = this.lastIndexOf('.')
    val ico = this.substring(dot + 1).toLowerCase()
    return "/assets/appico/" + when (ico) {
        "mp3", "mp4", "mkv", "apk", "ass", "doc", "docx", "exe", "html", "xml", "pdf", "ppt", "pptx", "txt", "xls", "xlsx", "zip", "torrent"
            , "rar", "iso", "ini", "bin", "7z"-> "$ico.png"
        else -> "Unknown.png"
    }
}

val MIMETYPES = hashMapOf<String, String>(
        "css" to "text/css",
        "htm" to "text/html",
        "html" to "text/html",
        "xml" to "text/xml",
        "java" to "text/x-java-source, text/java",
        "md" to "text/plain",
        "txt" to "text/plain",
        "asc" to "text/plain",
        "gif" to "image/gif",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "svg" to "image/svg+xml",
        "mp3" to "audio/mpeg",
        "m3u" to "audio/mpeg-url",
        "mp4" to "video/mp4",
        "ogv" to "Fvideo/ogg",
        "flv" to "video/x-flv",
        "mov" to "video/quicktime",
        "swf" to "application/x-shockwave-flash",
        "js" to "application/javascript",
        "pdf" to "application/pdf",
        "doc" to "application/msword",
        "ogg" to "application/x-ogg",
        "zip" to "application/octet-stream",
        "exe" to "application/octet-stream",
        "class" to "application/octet-stream",
        "m3u8" to "application/vnd.apple.mpegurl",
        "ts" to "video/mp2t"
)
val ASSETSICO = hashMapOf<String, String>(
        "css" to "text/css",
        "htm" to "text/html",
        "html" to "text/html",
        "xml" to "text/xml",
        "java" to "text/x-java-source, text/java",
        "md" to "text/plain",
        "txt" to "text/plain",
        "asc" to "text/plain",
        "gif" to "image/gif",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "svg" to "image/svg+xml",
        "mp3" to "audio/mpeg",
        "m3u" to "audio/mpeg-url",
        "mp4" to "video/mp4",
        "ogv" to "Fvideo/ogg",
        "flv" to "video/x-flv",
        "mov" to "video/quicktime",
        "swf" to "application/x-shockwave-flash",
        "js" to "application/javascript",
        "pdf" to "application/pdf",
        "doc" to "application/msword",
        "ogg" to "application/x-ogg",
        "zip" to "application/octet-stream",
        "exe" to "application/octet-stream",
        "class" to "application/octet-stream",
        "m3u8" to "application/vnd.apple.mpegurl",
        "ts" to "video/mp2t"
)
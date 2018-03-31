package org.kreal.hkshare.nettyShare.httpFile

/**
 * Created by lthee on 2017/10/29.
 *
 */
class HttpFileSystem private constructor() : HttpFileFactory {
    private val illegalPath = "[<>\\\\\"|?*:]".toRegex()
    private var directoryMap: MutableMap<String, HttpFileFactory> = sortedMapOf()
    private var fileMap: MutableMap<String, HttpFile> = sortedMapOf()
    private val defaultRootHttpFile: VHttpDirectory = VHttpDirectory("/", "/")

    fun route(rout: String, httpFileFactory: HttpFileFactory): Boolean {
        if (rout.contains(illegalPath) || rout.contains("(^../|/../)".toRegex()) || !rout.endsWith('/'))
            return false
        directoryMap[rout] = httpFileFactory
        defaultRootHttpFile.add(httpFileFactory.newHttpFile(""))
        directoryMap = directoryMap.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
        return true
    }

    fun route(rout: String, httpFile: HttpFile): Boolean {
        if (rout.contains(illegalPath) || rout.contains("(^../|/../)".toRegex()) || rout.endsWith('/'))
            return false
        fileMap[rout] = httpFile
        defaultRootHttpFile.add(httpFile)
        fileMap = fileMap.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
        return true
    }

    fun remove(rout: String) {
        directoryMap.remove(rout)
        fileMap.remove(rout)
    }

    override fun newHttpFile(path: String): HttpFile {
        for ((k, httpFile) in fileMap) {
            if (path == k)
                return httpFile
        }
        for ((k, fileFactory) in directoryMap) {
            if (path.startsWith(k))
                return fileFactory.newHttpFile(path.removePrefix(k))
        }
        return defaultRootHttpFile
    }

    companion object {
        val instance: HttpFileSystem = HttpFileSystem()
    }
}
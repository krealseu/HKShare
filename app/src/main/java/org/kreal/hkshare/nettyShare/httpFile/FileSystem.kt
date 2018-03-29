package org.kreal.hkshare.nettyShare.httpFile

import java.util.*

/**
 * Created by lthee on 2017/10/29.
 *
 */
class FileSystem private constructor() : HttpFileFactory {

    private var routMap1: SortedMap<String, HttpFileFactory> = sortedMapOf()
    private var routMap2: SortedMap<String, HttpFileFactory> = sortedMapOf()

    fun route(rout: String, httpFileFactory: HttpFileFactory) {
        if (rout.endsWith("/*")) {
            routMap2[rout.substring(0, rout.length - 1)] = httpFileFactory
            if (!routMap1.containsKey(rout.substring(0, rout.length - 2)))
                routMap1[rout.substring(0, rout.length - 2)] = httpFileFactory
        } else routMap1[rout] = httpFileFactory

        routMap1 = routMap1.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
        routMap2 = routMap2.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
    }


    fun remove(rout: String) {
        if (routMap1.containsKey(rout))
            routMap1.remove(rout)
        if (routMap2.containsKey(rout.substring(0, rout.length - 1)))
            routMap2.remove(rout.substring(0, rout.length - 1))
    }

    override fun newHttpFile(path: String): HttpFile {
        for ((k, v) in routMap1) {
            if (path == k)
                return v.newHttpFile(path)
        }
        for ((k, v) in routMap2) {
            if (path.startsWith(k))
                return v.newHttpFile(path)
        }
        return newHttpFile("/")
    }

    companion object {
        val instance: FileSystem = FileSystem()
    }
}
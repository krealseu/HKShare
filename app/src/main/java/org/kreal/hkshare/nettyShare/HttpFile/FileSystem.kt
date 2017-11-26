package org.kreal.hkshare.nettyShare.HttpFile

import java.util.*

/**
 * Created by lthee on 2017/10/29.
 */
class FileSystem private constructor() : HttpFileFactory {

    private var rountmap1: SortedMap<String, HttpFileFactory> = sortedMapOf()
    private var rountmap2: SortedMap<String, HttpFileFactory> = sortedMapOf()

    fun route(rout: String, httpFileFactory: HttpFileFactory) {
        if (rout.endsWith("/*")) {
            rountmap2.put(rout.substring(0, rout.length - 1), httpFileFactory)
            if (!rountmap1.containsKey(rout.substring(0, rout.length - 2)))
                rountmap1.put(rout.substring(0, rout.length - 2), httpFileFactory)
        } else rountmap1.put(rout, httpFileFactory)

        rountmap1 = rountmap1.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
        rountmap2 = rountmap2.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
    }


    fun remove(rout: String) {
        if (rountmap1.containsKey(rout))
            rountmap1.remove(rout)
        if (rountmap2.containsKey(rout.substring(0, rout.length - 1)))
            rountmap2.remove(rout.substring(0, rout.length - 1))
    }

    override fun newHttpFile(path: String): HttpFile {
        for ((k, v) in rountmap1) {
            if (path == k)
                return v.newHttpFile(path)
        }
        for ((k, v) in rountmap2) {
            if (path.startsWith(k))
                return v.newHttpFile(path)
        }
        return newHttpFile("/")
    }

    companion object {
        val instance: FileSystem = FileSystem()
    }
}
package org.kreal.hkshare.configure

import java.io.File

/**
 * Created by lthee on 2018/3/18.
 *
 */
class Config(private val dataFile: File) {

    private var isConfigureChanged: Boolean = false

    private val routList: MutableList<Rout> = arrayListOf()

    fun save() {
        dataFile.writeText("wo")
        isConfigureChanged = false
    }

    fun load() {}

    fun add(rout: Rout) {
        if (!routList.contains(rout)) {
            isConfigureChanged = true
            routList.add(rout)
        }
    }

    fun delete(rout: Rout) {
        routList.remove(rout)
    }

    companion object {
        val DATAFILENAME = "configure.json"
    }
}
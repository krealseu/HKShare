package org.kreal.hkshare

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.kreal.hkshare.configure.Configures
import org.kreal.hkshare.configure.Rout
import org.kreal.hkshare.extensions.ip
import org.kreal.hkshare.extensions.logi


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start.setOnClickListener {
            HKService startWith applicationContext
        }
        stop.setOnClickListener {
            HKService stopWith applicationContext
        }
        this logi ip
        val arr = Array(3) { i: Int ->
            Rout("sdcard$i", "/sdcard$i", true)
        }
        val config = Configures(arr)
//        val config = Rout("sdcard", "/sdcard", true)
        val string = Gson().toJson(config, Configures::class.java)
        val file = getFileStreamPath("data")
        logi(file.path)
        file.writeText(string)
        Log.i("asd", file.readText())
        val clzc = Rout::class
        val data = Gson().fromJson(file.readText(), Configures::class.java)
        Log.i("asd", data.toString())
        button.setOnClickListener {
            RoutDialogFragment().show(fragmentManager,"w")
        }
    }
}

package org.kreal.hkshare

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
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
    }
}

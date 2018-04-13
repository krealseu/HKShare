package org.kreal.hkshare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.kreal.hkshare.extensions.ip
import org.kreal.widget.qrshow.QRShow

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val serviceConnection: HKService.HKServiceClientBinder = HKService.HKServiceClientBinder()
    private var isRunningState: Boolean = false
    private val handler = Handler()
    private val refreshRunnable: Runnable = object : Runnable {
        override fun run() {
            if (serviceConnection.isConnected() && isRunningState != serviceConnection.isServing())
                updateView()
            handler.postDelayed(this, 500)
        }
    }

    private fun updateView() {
        if (serviceConnection.isServing()) {
            isRunningState = true
            button.text = "Stop"
            imageView.visibility = View.VISIBLE
            QRShow(imageView) show "http://$ip:${APP.preference.getPort()}"
        } else {
            isRunningState = false
            button.text = "Start"
            imageView.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        baseContext.bindService(Intent(baseContext, HKService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        button.setOnClickListener {
            when (serviceConnection.isServing()) {
                true -> actionStop()
                false -> actionStart()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ip_address.text = "Address:$ip"
        port.text = "Port:${APP.preference.getPort()}"
        handler.post(refreshRunnable)
        updateView()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unbindService(serviceConnection)
    }

    private fun actionStart() {
        HKService startWith baseContext
    }

    private fun actionStop() {
        HKService stopWith baseContext
    }
}

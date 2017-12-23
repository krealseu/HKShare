package org.kreal.hkshare

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.kreal.hkshare.extensions.ip
import org.kreal.hkshare.extensions.logi
import org.kreal.hkshare.nettyShare.HttpFile.FileSystem
import org.kreal.hkshare.nettyShare.HttpFile.NativeFileFactory
import org.kreal.hkshare.nettyShare.HttpService
import org.kreal.hkshare.nettyShare.HttpFile.ApkFileFactory
import org.kreal.hkshare.nettyShare.HttpFile.AssetsFileFactory

class HKService : Service() {
    //    private val nanoShare = HKHttpServive(6533)
    private val port = 8080
    private val nettyShare = HttpService(port)
    private val bb = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
        }

        infix fun registerWith(context: Context) {
            val intentFilterNet = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            intentFilterNet.addCategory(Intent.CATEGORY_DEFAULT)
            context.registerReceiver(this, intentFilterNet)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val act = intent?.action  ?: return Service.START_NOT_STICKY
        when {
            act.equals(ACTION_START) -> {
                if (!nettyShare.isAlive()) {
                    FileSystem.instance.route("/assets/*", AssetsFileFactory(baseContext.assets, "/assets"))
                    FileSystem.instance.route("/*", NativeFileFactory())
                    FileSystem.instance.route("/app/*", ApkFileFactory(baseContext.packageManager, "/app"))
                    nettyShare.start()
                    startForeground(startId, createNotification(baseContext))
                }
            }
            act.equals(ACTION_STOP) -> {
                stopForeground(true)
                nettyShare.stop()
                stopSelf()
            }
        }
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        nettyShare.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return HKServiceBinder(nettyShare)
    }

    private fun createNotification(context: Context): Notification? {
        val builder = NotificationCompat.Builder(context)
        val ipString = "Http://$ip:$port"
        builder.setContentTitle("Http Sharing - - - - > > > > >")
                .setContentText(ipString)
                .setSmallIcon(R.drawable.ic_stat_hkshare)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                QRActivity.intent(context, ipString), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_action_stat_reply,
                        "stop",
                        PendingIntent.getService(context, 0, HKService.stopIntent(context), PendingIntent.FLAG_CANCEL_CURRENT))
        return builder.build()
    }

    companion object {
        private val TAG = "HKService"
        private val ACTION_START = "org.kreal.hkshare.HKService.START"
        private val ACTION_STOP = "org.kreal.hkshare.HKService.STOP"
        infix fun startWith(context: Context) {
            val intent = Intent(context, HKService::class.java)
            intent.action = ACTION_START
            context.startService(intent)
        }

        infix fun stopWith(context: Context) {
            val intent = Intent(context, HKService::class.java)
            intent.action = ACTION_STOP
            context.startService(intent)
        }

        infix fun stopIntent(context: Context): Intent {
            val intent = Intent(context, HKService::class.java)
            intent.action = ACTION_STOP
            return intent
        }
    }

    class HKServiceBinder(val nettyShare: HttpService) : Binder()
    class HKServiceClientBinder : ServiceConnection {
        private var state: Boolean = false
        private var hkServiceBinder: HKService.HKServiceBinder? = null
        fun isServing() = state && hkServiceBinder?.let { hkServiceBinder -> hkServiceBinder.nettyShare.isAlive() } ?: false
        override fun onServiceDisconnected(p0: ComponentName?) {
            logi("Disconnect")
            state = false
            hkServiceBinder = null
        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            logi("connect")
            if (binder is HKService.HKServiceBinder) {
                state = true
                hkServiceBinder = binder
            }
        }

    }
}

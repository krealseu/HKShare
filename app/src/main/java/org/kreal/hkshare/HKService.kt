package org.kreal.hkshare

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import org.kreal.hkshare.extensions.ip
import org.kreal.hkshare.nettyShare.HttpService
import org.kreal.hkshare.nettyShare.httpFile.ApkFileFactory
import org.kreal.hkshare.nettyShare.httpFile.AssetsFileFactory
import org.kreal.hkshare.nettyShare.httpFile.HttpFileSystem
import org.kreal.hkshare.nettyShare.httpFile.NativeFileFactory
import org.kreal.storage.Storage
import org.kreal.widget.qrshow.QRShow
import java.io.File

class HKService : Service() {
    private val port = APP.preference.getPort()

    private val nettyShare = HttpService(port)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val act = intent?.action ?: return Service.START_NOT_STICKY
        when (act) {
            ACTION_START -> {
                if (!nettyShare.isAlive()) {
                    val storage = Storage(baseContext)
                    storage.getAvailableVolumes().forEach {
                        HttpFileSystem.instance.route("/${it.uuid}/", NativeFileFactory(File(it.path), "/${it.uuid}/"))
                    }
                    HttpFileSystem.instance.route("/Download/", NativeFileFactory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/Download/"))
                    HttpFileSystem.instance.route("/assets/", AssetsFileFactory(baseContext.assets, "/assets/"))
                    HttpFileSystem.instance.route("/app/", httpFileFactory = ApkFileFactory(baseContext.packageManager, "/app/"))
                    nettyShare.start()
                    startForeground(startId, createNotification(baseContext))
                }
            }
            ACTION_STOP -> {
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

    override fun onBind(intent: Intent): IBinder? = HKServiceBinder(nettyShare)

    private fun createNotification(context: Context): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
//            chan.enableLights(true)
//            chan.enableVibration(false)
//            chan.vibrationPattern = longArrayOf(0)
//            chan.lightColor = Color.RED
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val nm = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            nm.createNotificationChannel(chan)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val ipString = "http://$ip:$port"
        builder.setContentTitle(contentTitle)
                .setContentText(ipString)
                .setContentInfo(contentText)
                .setSmallIcon(R.drawable.ic_stat_hkshare)
                .setLargeIcon(QRShow().show(ipString).get())
                .setSubText(contentSub)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                Intent(baseContext, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_action_stat_reply,
                        "stop",
                        PendingIntent.getService(context, 0, HKService.stopIntent(context), PendingIntent.FLAG_CANCEL_CURRENT))

        return builder.build()
    }

    companion object {
        private const val TAG = "HKService"
        private const val ACTION_START = "org.kreal.hkshare.HKService.START"
        private const val ACTION_STOP = "org.kreal.hkshare.HKService.STOP"
        private const val CHANNEL_ID = "HKSHARE_SERVICE_ID"
        private const val CHANNEL_NAME = "HK"

        private const val contentSub = "HKShare"
        private const val contentTitle = "Http Sharing - - - - > > > > >"
        private const val contentText = "Http Sharing"

        infix fun startWith(context: Context) {
            val intent = Intent(context, HKService::class.java)
            intent.action = ACTION_START
            context.startService(intent)
//            when {
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> context.startForegroundService(intent)
//                else -> context.startService(intent)
//            }

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
        fun isServing() = state && hkServiceBinder?.nettyShare?.isAlive() ?: false
        fun isConnected() = state

        override fun onServiceDisconnected(p0: ComponentName?) {
            state = false
            hkServiceBinder = null
        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            if (binder is HKService.HKServiceBinder) {
                state = true
                hkServiceBinder = binder
            }
        }

    }
}

package org.kreal.hkshare

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@TargetApi(Build.VERSION_CODES.N)
class QuickSettingService : TileService() {
    private val serviceConnection: HKService.HKServiceClientBinder = HKService.HKServiceClientBinder()

    private val handler = Handler()

    private val refreshRunnable: Runnable = object : Runnable {
        override fun run() {
            when {
                serviceConnection.isServing() && (qsTile.state == Tile.STATE_INACTIVE) -> {
                    setActive()
                    qsTile.updateTile()
                }
                !serviceConnection.isServing() && (qsTile.state == Tile.STATE_ACTIVE) -> {
                    setInactive()
                    qsTile.updateTile()
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        baseContext.bindService(Intent(baseContext, HKService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unbindService(serviceConnection)
    }

    override fun onStartListening() {
        handler.post(refreshRunnable)
    }

    override fun onClick() {
        super.onClick()
        when (serviceConnection.isServing()) {
            true -> HKService stopWith applicationContext
            false -> HKService startWith applicationContext
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun setActive() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.label = "Sharing"
    }

    private fun setInactive() {
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.label = "HKShare"
    }
}

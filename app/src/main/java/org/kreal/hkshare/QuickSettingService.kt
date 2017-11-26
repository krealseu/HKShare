package org.kreal.hkshare

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@TargetApi(Build.VERSION_CODES.N)
class QuickSettingService : TileService() {
    private val serviceConnection = HKService.HKServiceClientBinder()
    override fun onCreate() {
        super.onCreate()
        baseContext.bindService(Intent(baseContext, HKService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unbindService(serviceConnection)
    }

    override fun onStartListening() {
//        Log.i("adf", "startinging")
        super.onStartListening()
        if (serviceConnection.isServing())
            setActive()
        else
            setInactive()
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        when (serviceConnection.isServing()) {
            true -> {
                HKService stopWith applicationContext
                setInactive()
            }
            false -> {
                HKService startWith applicationContext
                setActive()
            }
        }
        qsTile.updateTile()
    }

    override fun onStopListening() {
//        Log.i("adf", "stoping")
        super.onStopListening()
        if (serviceConnection.isServing())
            setActive()
        else
            setInactive()
        qsTile.updateTile()
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

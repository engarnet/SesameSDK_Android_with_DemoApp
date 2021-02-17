package co.candyhouse.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHConfiguration
import co.utils.L
import co.utils.SharedPreferencesUtils
import no.nordicsemi.android.dfu.DfuServiceInitiator

class CandyHouseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        L.d("hcia", "🌱:" + BuildConfig.BUILD_TYPE + BuildConfig.VERSION_NAME + BuildConfig.GIT_HASH)
        SharedPreferencesUtils.init(this)


        CHBleManager(this)
//        CHConfiguration.CLIENT_ID = "your CLIENT_ID default github CLIENT_ID"
//        CHConfiguration.API_KEY = "your API_KEY default github API_KEY"

    }


}

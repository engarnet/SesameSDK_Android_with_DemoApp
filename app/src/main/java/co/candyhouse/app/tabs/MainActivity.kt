package co.candyhouse.app.tabs

import android.os.Bundle
import co.candyhouse.app.base.BaseActivity
import co.candyhouse.sesame.open.CHBleManager


class MainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        CHBleManager.enableScan {}
    }


    companion object {
        var activity: MainActivity? = null
    }
}


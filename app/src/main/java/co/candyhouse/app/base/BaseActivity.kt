package co.candyhouse.app.base

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import co.candyhouse.app.R
import co.candyhouse.app.tabs.MainActivity
import co.candyhouse.app.tabs.setupWithNavController
import co.candyhouse.sesame.open.CHBleManager
import co.utils.L
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

open class BaseActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    lateinit var currentNavController: LiveData<NavController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
        setContentView(R.layout.activity_main)
        currentNavController = bottom_nav.setupWithNavController(//BottomNavigationView
                navGraphIds = listOf(
                        R.navigation.devices_ng,
                ),
                fragmentManager = supportFragmentManager,
                containerId = R.id.nav_host_container,
                intent = intent
        )


    }

    private fun getPermissions() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            L.d("hcia", "get PR 啟動掃描::" )
            CHBleManager.enableScan {}
        } else {
            EasyPermissions.requestPermissions(
                    this, getString(R.string.launching_why_need_location_permission), 0,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(
                applicationContext,
                getString(R.string.launching_why_need_location_permission),
                Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        CHBleManager.enableScan {}
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onSupportNavigateUp(): Boolean {
        L.d("hcia", "onSupportNavigateUp:")
        return currentNavController.value?.navigateUp() ?: false
    }

}

fun MainActivity.hideMenu() {
//    L.d("hcia", "hideMenu:" )
    bottom_nav.visibility = View.GONE
}

fun MainActivity.showMenu() {
//    L.d("hcia", "showMenu:" )
    bottom_nav.visibility = View.VISIBLE
}

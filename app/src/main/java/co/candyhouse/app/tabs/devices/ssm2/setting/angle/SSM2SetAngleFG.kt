package co.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.os.Bundle
import android.view.View
import co.candyhouse.app.base.BaseDeviceFG
import co.candyhouse.app.R
import co.candyhouse.app.tabs.MainActivity
import co.candyhouse.sesame.open.device.*
import co.utils.alertview.fragments.toastMSG
import kotlinx.android.synthetic.main.fg_set_angle.*
import kotlinx.android.synthetic.main.activity_main.*

class SSM2SetAngleFG : BaseDeviceFG(R.layout.fg_set_angle) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        ssmView?.setLock(mDeviceModel.ssmLockLiveData.value as CHSesame2)
        ssmView?.setOnClickListener { (mDeviceModel.ssmLockLiveData.value as CHSesame2).toggle {} }

        setunlock_zone?.setOnClickListener {
            if ((mDeviceModel.ssmLockLiveData.value as CHSesame2).deviceStatus?.value == CHDeviceLoginStatus.UnLogin) {
                return@setOnClickListener
            }
            if (kotlin.math.abs((mDeviceModel.ssmLockLiveData.value as CHSesame2).mechStatus!!.position - (mDeviceModel.ssmLockLiveData.value as CHSesame2).mechSetting!!.lockPosition) < 50) {
                toastMSG(getString(R.string.too_close))
                return@setOnClickListener
            }
            (mDeviceModel.ssmLockLiveData.value as CHSesame2).configureLockPosition((mDeviceModel.ssmLockLiveData.value as CHSesame2).mechSetting!!.lockPosition, (mDeviceModel.ssmLockLiveData.value as CHSesame2).mechStatus!!.position) {
                ssmView?.setLock((mDeviceModel.ssmLockLiveData.value as CHSesame2))
            }
        }
        setlock_zone?.setOnClickListener {
            if ((mDeviceModel.ssmLockLiveData.value as CHSesame2).deviceStatus?.value == CHDeviceLoginStatus.UnLogin) {
                return@setOnClickListener
            }
            if (kotlin.math.abs((mDeviceModel.ssmLockLiveData.value as CHSesame2).mechStatus!!.position - (mDeviceModel.ssmLockLiveData.value as CHSesame2).mechSetting!!.unlockPosition) < 50) {
                toastMSG(getString(R.string.too_close))
                return@setOnClickListener
            }
            (mDeviceModel.ssmLockLiveData.value as CHSesame2).configureLockPosition((mDeviceModel.ssmLockLiveData.value as CHSesame2).mechStatus!!.position, (mDeviceModel.ssmLockLiveData.value as CHSesame2).mechSetting!!.unlockPosition) {
//                L.d("hcia", "設定完狀態 status:")
                ssmView?.setLock((mDeviceModel.ssmLockLiveData.value as CHSesame2))
            }
        }

        mDeviceModel.ssmosLockDelegates[(mDeviceModel.ssmLockLiveData.value as CHSesame2)] = object : CHSesame2Delegate {
            override fun onMechStatusChanged(device: CHSesame2, status: CHSesame2MechStatus, intention: CHSesame2Intention) {
                ssmView?.setLock(device)
            }
        }


    }//end view created


}


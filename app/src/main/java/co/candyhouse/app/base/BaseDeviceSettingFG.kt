package co.candyhouse.app.base

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import co.candyhouse.app.R
import co.candyhouse.app.tabs.devices.ssm2.*
import co.candyhouse.app.tabs.devices.ssm2.setting.DfuService
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHBleStatisDelegate
import co.candyhouse.sesame.open.device.*
import co.utils.L
import co.utils.alertview.AlertView
import co.utils.alertview.enums.AlertActionStyle
import co.utils.alertview.enums.AlertStyle
import co.utils.alertview.objects.AlertAction
import no.nordicsemi.android.dfu.DfuServiceInitiator


interface BleStatusUpdate {
    fun onChange()
}

interface DevicestatusChange {
    fun onChangeStatus(status: CHSesame2Status)
}

open class BaseDeviceSettingFG(layout: Int) : BaseDeviceFG(layout), BleStatusUpdate, DevicestatusChange {

    override fun onResume() {
        super.onResume()
        onChange()

        CHBleManager.statusDelegate = object : CHBleStatisDelegate {
            override fun didScanChange(ss: CHScanStatus) {
                onChange()
            }
        }
        mDeviceModel.ssmosLockDelegates[mDeviceModel.ssmLockLiveData.value!!] = object : CHSesameStatusDelegate {
            override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                onChange()
                onChangeStatus(status)
                if (status.value == CHDeviceLoginStatus.Login) {
                    mDeviceModel.ssmLockLiveData.value?.getVersionTag {
                        it.onSuccess {
                            view?.findViewById<TextView>(R.id.device_version_txt)?.post {
                                view?.findViewById<TextView>(R.id.device_version_txt)?.text = it.data
                            }
                        }
                    }
                }
            }

        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        L.d("hcia", "loginVM.gUserState:" + loginVM.gUserState.value)

        getView()?.findViewById<View>(R.id.dfu_zone)?.setOnClickListener {
            AlertView(resources.getResourceEntryName(mDeviceModel.targetDevice().getFirZip()).toString(), "", AlertStyle.IOS).apply {
                addAction(AlertAction(getString(R.string.ssm_update), AlertActionStyle.NEGATIVE) { action ->
                    mDeviceModel.targetDevice().updateFirmware { res ->
                        res.onSuccess {
                            val starter = DfuServiceInitiator(it.data.address)
                            starter.setZip(mDeviceModel.targetDevice().getFirZip())
                            starter.setPacketsReceiptNotificationsEnabled(true)
                            starter.setPrepareDataObjectDelay(400)
                            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
                            starter.setDisableNotification(false)
                            starter.setForeground(true)
                            starter.start(activity!!, DfuService::class.java)
                        }
                    }
                })
                show(activity as AppCompatActivity)
            }
        }


        getView()?.findViewById<View>(R.id.drop_zone)?.setOnClickListener {
            mDeviceModel.targetDevice().dropKey {
                it.onSuccess {
                    findNavController().navigateUp()
                    findNavController().navigateUp()
                    mDeviceModel.updateDevices()
                }
            }
        }
        getView()?.findViewById<View>(R.id.reset_zone)?.setOnClickListener {
            mDeviceModel.targetDevice().reset {
                findNavController().navigateUp()
                findNavController().navigateUp()
                mDeviceModel.updateDevices()
            }

        }

    }


    override fun onChange() {
        when {
            CHBleManager.mScanning == CHScanStatus.BleClose -> {
                view?.findViewById<View>(R.id.err_zone)?.visibility = View.VISIBLE
                view?.findViewById<TextView>(R.id.err_title)?.text = getString(R.string.noble)
            }
            mDeviceModel.targetDevice().deviceStatus == CHSesame2Status.NoBleSignal -> {
                view?.findViewById<View>(R.id.err_zone)?.visibility = View.VISIBLE
                view?.findViewById<TextView>(R.id.err_title)?.text = getString(R.string.NoBleSignal)
            }
            mDeviceModel.targetDevice().deviceStatus.value == CHDeviceLoginStatus.UnLogin -> {
                view?.findViewById<View>(R.id.err_zone)?.visibility = View.VISIBLE
                view?.findViewById<TextView>(R.id.err_title)?.text = mDeviceModel.targetDevice().deviceStatus.toString()
            }
            else -> {
                view?.findViewById<View>(R.id.err_zone)?.visibility = View.GONE
            }
        }

    }


    override fun onChangeStatus(status: CHSesame2Status) {
    }
}



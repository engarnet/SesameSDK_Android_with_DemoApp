package co.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseDeviceFG
import co.candyhouse.app.tabs.MainActivity
import co.candyhouse.app.tabs.devices.model.CHDeviceViewModel
import co.candyhouse.app.tabs.devices.ssm2.*
import co.candyhouse.app.tabs.devices.ssm2.setting.DfuService
import co.candyhouse.sesame.open.device.CHWifiModule2
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHBleManagerDelegate
import co.candyhouse.sesame.open.device.*
import co.utils.L
import co.utils.alertview.AlertView
import co.utils.alertview.enums.AlertActionStyle
import co.utils.alertview.enums.AlertStyle
import co.utils.alertview.objects.AlertAction
import co.utils.recycle.GenericAdapter
import kotlinx.android.synthetic.main.fg_rg_device.*
import no.nordicsemi.android.dfu.DfuServiceInitiator
import java.lang.Math.pow
import java.util.*
import kotlin.math.pow
import kotlinx.android.synthetic.main.activity_main.*
import android.bluetooth.BluetoothAdapter

class ScanNewDeviceFG : BaseDeviceFG(R.layout.fg_rg_device) {

    var mDeviceList = ArrayList<CHDevices>()
    private val mDeviceViewModel: CHDeviceViewModel by activityViewModels()
    override fun onResume() {
        super.onResume()

        BluetoothAdapter.getDefaultAdapter().enable()

        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverUnRegisteredCHDevices(decices: List<CHDevices>) {
                mDeviceList.clear()
                mDeviceList.addAll(decices.sortedBy {
                    (10.0.pow(((it.txPowerLevel!! - (it.rssi?.toDouble()
                            ?: 0.0) - 62.0) / 20.0)) * 100).toInt()
                })
                mDeviceList.firstOrNull()?.connect { }
                leaderboard_list?.post {
                    (leaderboard_list?.adapter as? GenericAdapter<*>)?.notifyDataSetChanged()
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        top_back_img.setOnClickListener { findNavController().navigateUp() }
        leaderboard_list.apply {
            setEmptyView(empty_view)
            adapter = object : GenericAdapter<CHDevices>(mDeviceList) {
                override fun getLayoutId(position: Int, obj: CHDevices): Int {
                    when (obj) {
                        is CHWifiModule2 -> return R.layout.cell_wm2_unregist
                        is CHSesameBot -> return R.layout.cell_device_bot_unregist
                        is CHSesameBike -> return R.layout.cell_device_bike_unregist
                        else -> return R.layout.cell_device_unregist
                    }
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {

                    if (viewType == R.layout.cell_wm2_unregist) {
                        return object : RecyclerView.ViewHolder(view), Binder<CHWifiModule2> {
                            var customName: TextView = itemView.findViewById(R.id.title)
                            var uuidTxt: TextView = itemView.findViewById(R.id.title_txt)
                            var statusTxt: TextView = itemView.findViewById(R.id.subtitle_txt)
                            var sub_title: TextView = itemView.findViewById(R.id.sub_title)

                            @SuppressLint("SetTextI18n")
                            override fun bind(wm2: CHWifiModule2, pos: Int) {

                                itemView.setOnClickListener {
                                    registerWm2(wm2)
                                }
                                val distance: Int = (10.0.pow(((wm2.txPowerLevel!! - (wm2.rssi?.toDouble()
                                        ?: 0.0) - 62.0) / 20.0)) * 100).toInt()
                                customName.text = "" + (if (wm2.rssi == null) "-" else distance.toString() + " cm")
                                uuidTxt.text = wm2.deviceId.toString().toUpperCase()
                                statusTxt.text = wm2.deviceStatus.toString()
                                sub_title.text = getString(R.string.WM2)
                            }

                            private fun registerWm2(wm2: CHWifiModule2) {
                                wm2.connect {}
                                doRegisterwm2(wm2)
                                wm2.delegate = object : CHWifiModule2Delegate {
                                    override fun onBleDeviceStatusChanged(device: CHWifiModule2, status: CHSesame2Status) {
                                        doRegisterwm2(wm2)
                                    }
                                }
                            }

                            private fun doRegisterwm2(wm2: CHWifiModule2) {
                                if (wm2.deviceStatus == CHSesame2Status.ReadyToRegister) {
                                    wm2.register {
                                        mDeviceViewModel.updateDevices()

                                        activity?.runOnUiThread {
                                            L.d("hcia wm2LiveData", "設定:")
                                            mDeviceViewModel.wm2LiveData.value = wm2
                                            mDeviceViewModel.targetModel = CHProductModel.WM2

                                            findNavController().navigateUp()
                                            MainActivity.activity?.currentNavController?.value?.navigate(R.id.to_WM2SettingFG)

                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (viewType == R.layout.cell_device_bike_unregist) {
                        return object : RecyclerView.ViewHolder(view), Binder<CHSesameBike> {
                            var customName: TextView = itemView.findViewById(R.id.title)
                            var uuidTxt: TextView = itemView.findViewById(R.id.title_txt)
                            var statusTxt: TextView = itemView.findViewById(R.id.subtitle_txt)
                            var sub_title: TextView = itemView.findViewById(R.id.sub_title)

                            @SuppressLint("SetTextI18n")
                            override fun bind(bike: CHSesameBike, pos: Int) {

                                itemView.setOnLongClickListener {
                                    AlertView(getString(R.string.ssm_update), "", AlertStyle.IOS).apply {
                                        addAction(AlertAction(getResources().getResourceEntryName(bike.getFirZip()).toString(), AlertActionStyle.NEGATIVE) { action ->
                                            bike.updateFirmware { res ->
                                                res.onSuccess {
                                                    val starter = DfuServiceInitiator(it.data.address)
                                                    starter.setZip(bike.getFirZip())
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
                                    true
                                }
                                itemView.setOnClickListener {
                                    registerSesameBike(bike)
                                }
                                val distance: Int = (pow(10.0, ((bike.txPowerLevel!! - bike.rssi!!.toDouble() - 62.0) / 20.0)) * 100).toInt()
                                customName.text = "" + (if (bike.rssi == null) "-" else distance.toString() + " cm")
                                uuidTxt.text = bike.deviceId.toString().toUpperCase()
                                statusTxt.text = bike.deviceStatus.toString()
                                sub_title.text = "SSMBike"
                            }

                            private fun registerSesameBike(bikeLock: CHSesameBike) {

                                bikeLock.connect {}

                                doRegisterBike(bikeLock)
                                bikeLock.delegate = object : CHSesameBikeDelegate {
                                    override fun onBleDeviceStatusChanged(ssmbot: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                        doRegisterBike(bikeLock)
                                    }
                                }
                            }

                            private fun doRegisterBike(ssmbot: CHSesameBike) {
                                if (ssmbot.deviceStatus == CHSesame2Status.ReadyToRegister) {
                                    ssmbot.register {
                                        it.onSuccess {
                                            mDeviceViewModel.updateDevices()


                                            activity?.runOnUiThread {
                                                findNavController().navigateUp()

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (viewType == R.layout.cell_device_bot_unregist) {
                        return object : RecyclerView.ViewHolder(view), Binder<CHSesameBot> {
                            var customName: TextView = itemView.findViewById(R.id.title)
                            var uuidTxt: TextView = itemView.findViewById(R.id.title_txt)
                            var statusTxt: TextView = itemView.findViewById(R.id.subtitle_txt)
                            var sub_title: TextView = itemView.findViewById(R.id.sub_title)

                            @SuppressLint("SetTextI18n")
                            override fun bind(bot: CHSesameBot, pos: Int) {

                                itemView.setOnLongClickListener {
                                    AlertView(getString(R.string.ssm_update), "", AlertStyle.IOS).apply {
                                        addAction(AlertAction(getResources().getResourceEntryName(bot.getFirZip()).toString(), AlertActionStyle.NEGATIVE) { action ->
                                            bot.updateFirmware { res ->
                                                res.onSuccess {
                                                    val starter = DfuServiceInitiator(it.data.address)
                                                    starter.setZip(bot.getFirZip())
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
                                    true
                                }
                                itemView.setOnClickListener {
                                    registerSesameBot(bot)
                                }
                                val distance: Int = (10.0.pow(((bot.txPowerLevel!! - (bot.rssi?.toDouble()
                                        ?: 0.0) - 62.0) / 20.0)) * 100).toInt()

                                customName.text = "" + (if (bot.rssi == null) "-" else distance.toString() + " cm")
                                uuidTxt.text = bot.deviceId.toString().toUpperCase()
                                statusTxt.text = bot.deviceStatus.toString()
                                sub_title.text = "SSMBOT"
                            }

                            private fun registerSesameBot(ssmbot: CHSesameBot) {
                                ssmbot.connect {}
                                doRegisterSSb(ssmbot)
                                ssmbot.delegate = object : CHSesameBotDelegate {
                                    override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                        doRegisterSSb(ssmbot)
                                    }
                                }
                            }

                            private fun doRegisterSSb(ssmbot: CHSesameBot) {
                                if (ssmbot.deviceStatus == CHSesame2Status.ReadyToRegister) {
                                    ssmbot.register {
                                        it.onSuccess {

                                            activity?.runOnUiThread {
                                                findNavController().navigateUp()
                                            }

                                            mDeviceViewModel.updateDevices()

                                        }
                                    }
                                }
                            }
                        }
                    }
                    return object : RecyclerView.ViewHolder(view), Binder<CHSesame2> {
                        var customName: TextView = itemView.findViewById(R.id.title)
                        var uuidTxt: TextView = itemView.findViewById(R.id.title_txt)
                        var statusTxt: TextView = itemView.findViewById(R.id.subtitle_txt)
                        var sub_title: TextView = itemView.findViewById(R.id.sub_title)

                        @SuppressLint("SetTextI18n")
                        override fun bind(sesame: CHSesame2, pos: Int) {

                            itemView.setOnLongClickListener {
                                AlertView(getString(R.string.ssm_update), "", AlertStyle.IOS).apply {
                                    addAction(AlertAction(getResources().getResourceEntryName(sesame.getFirZip()).toString(), AlertActionStyle.NEGATIVE) { action ->
                                        sesame.updateFirmware { res ->
                                            res.onSuccess {
                                                val starter = DfuServiceInitiator(it.data.address)
                                                starter.setZip(sesame.getFirZip())
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
                                true
                            }
                            itemView.setOnClickListener {
                                registerSSM(sesame)
                            }
                            val distance: Int = (10.0.pow(((sesame.txPowerLevel!! - sesame.rssi!!.toDouble() - 62.0) / 20.0)) * 100).toInt()
                            customName.text = "" + (if (sesame.rssi == null) "-" else distance.toString() + " cm")
                            uuidTxt.text = sesame.deviceId.toString().toUpperCase()
                            statusTxt.text = sesame.deviceStatus.toString()
                            sub_title.text = getString(R.string.Sesame)

                        }

                        private fun registerSSM(sesame: CHSesame2) {
                            sesame.connect {}
                            doRegisterSSM(sesame)
                            sesame.delegate = object : CHSesame2Delegate {
                                override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                    if (status == CHSesame2Status.ReadyToRegister) {
                                        doRegisterSSM(sesame)
                                    }
                                }
                            }
                        }

                        private fun doRegisterSSM(sesame: CHSesame2) {
                            sesame.register { res ->

                                res.onSuccess {
                                    sesame.configureLockPosition(0, 256) {}//1024 --> 360,256-->90
                                    sesame.setHistoryTag("tag1".toByteArray()) {}

                                    mDeviceViewModel.updateDevices()
                                    activity?.runOnUiThread {
                                        mDeviceViewModel.ssmLockLiveData.value = sesame
                                        mDeviceViewModel.targetModel = CHProductModel.SS2
                                        findNavController().navigateUp()
                                        MainActivity.activity?.currentNavController?.value?.navigate(R.id.action_to_SSM2SetAngleFG)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    override fun onPause() {
        super.onPause()
        CHBleManager.delegate = null

    }

}


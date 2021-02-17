package co.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseFG
import co.candyhouse.app.tabs.MainActivity
import co.candyhouse.app.tabs.devices.ssm2.setting.angle.SSMCellView
import co.candyhouse.app.tabs.devices.model.CHDeviceViewModel
import co.candyhouse.app.tabs.devices.ssm2.setting.angle.SSMBikeCellView
import co.candyhouse.app.tabs.devices.ssm2.setting.angle.SSMBotCellView
import co.candyhouse.sesame.open.device.CHWifiModule2
import co.candyhouse.sesame.open.device.*
import co.utils.L
import co.utils.recycle.GenericAdapter
import kotlinx.android.synthetic.main.fg_devicelist.*

class DeviceListFG : BaseFG(R.layout.fg_devicelist) {
    private val mDeviceViewModel: CHDeviceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDeviceViewModel.updateDevices()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        leaderboard_list.setEmptyView(empty_view)
        leaderboard_list.adapter = object : GenericAdapter<Any>(mDeviceViewModel.myChDevices.value) {
            override fun getLayoutId(position: Int, obj: Any): Int {
                when (obj) {
                    is CHWifiModule2 -> return R.layout.wm2_layout
                    is CHSesameBot -> return R.layout.ssmbot_layout
                    is CHSesameBike -> return R.layout.ssmbike_layout
                    else -> return R.layout.sesame_layout  //CHSesame2
                }
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                when (viewType) {

                    R.layout.wm2_layout -> return object : RecyclerView.ViewHolder(view), Binder<CHWifiModule2> {

                        val customName: TextView = view.findViewById(R.id.title)

                        val iotStatus: TextView = view.findViewById(R.id.sub_title_2)

                        val wifi_img: ImageView = view.findViewById(R.id.wifi_img)

                        @SuppressLint("SetTextI18n")
                        override fun bind(wm2: CHWifiModule2, pos: Int) {

                            mDeviceViewModel.wm2Delegates.put(wm2, object : CHWifiModule2Delegate {
                                override fun onNetWorkStatusChanged(device: CHWifiModule2, settings: CHWifiModule2NetWorkStatus) {
                                    setupWm2(wm2)
                                }

                                override fun onBleDeviceStatusChanged(device: CHWifiModule2, status: CHSesame2Status) {
                                    setupWm2(wm2)
                                }
                            })
                            setupWm2(wm2)
                            view.setOnClickListener {
                                mDeviceViewModel.wm2LiveData.value = wm2
                                mDeviceViewModel.targetModel = CHProductModel.WM2
                                findNavController().navigate(R.id.to_WM2SettingFG)
                            }
                        }

                        private fun setupWm2(wm2: CHWifiModule2) {
                            customName.text = wm2.deviceId.toString()

                            iotStatus.text = "IOT:" + wm2.networkStatus?.isIOTWork.toString()
                            iotStatus.setTextColor(ContextCompat.getColor(view.context, if (wm2.networkStatus?.isIOTWork == true) R.color.unlock_blue else R.color.lock_red))
//                            wm2Status.text = wm2.deviceStatus.toString()
//                            wm2Status.visibility = if (wm2.deviceStatus.value == CHDeviceLoginStatus.Login || wm2.deviceStatus == CHSesame2Status.NoBleSignal) View.GONE else View.VISIBLE
                            wifi_img.setImageResource(if (wm2.networkStatus?.isIOTWork == true) R.drawable.wifi_green else R.drawable.wifi_grey)
//                            bl_img.setImageResource(if (wm2.deviceStatus.value == CHDeviceLoginStatus.Login) R.drawable.bl_green else R.drawable.bl_grey)
                        }
                    }
                    R.layout.ssmbike_layout -> return object : RecyclerView.ViewHolder(view), Binder<CHSesameBike> {

                        var ssmView: SSMBikeCellView = view.findViewById(R.id.ssmView)
                        var customName: TextView = view.findViewById(R.id.title)
                        var sesame2Status: TextView = view.findViewById(R.id.sub_title)
                        var shadowStatusTxt: TextView = view.findViewById(R.id.sub_title_2)
                        var battery_percent: TextView = view.findViewById(R.id.battery_percent)
                        var batteryImg: ImageView = view.findViewById(R.id.battery)
                        var bl_img: ImageView = view.findViewById(R.id.bl_img)
                        var wifi_img: ImageView = view.findViewById(R.id.wifi_img)

                        @SuppressLint("SetTextI18n")
                        override fun bind(ssmBike: CHSesameBike, pos: Int) {

                            ssmView.setOnClickListener { ssmBike.unlock { } }
                            view.setOnClickListener {
                                mDeviceViewModel.ssmLockLiveData.value = ssmBike
                                mDeviceViewModel.targetModel = CHProductModel.BiKeLock
                                findNavController().navigate(R.id.action_deviceListPG_to_sesameBikeSettingFG)
                            }
                            mDeviceViewModel.ssmosLockDelegates.put(ssmBike, object : CHSesameBikeDelegate {
                                override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                    setUpBike(ssmBike)
                                }
                            })
                            setUpBike(ssmBike)
                        }

                        private fun setUpBike(ssmBike: CHSesameBike) {
                            customName.text = ssmBike.deviceId.toString()

                            sesame2Status.text = ssmBike.deviceStatus.toString()
                            bl_img.setImageResource(if (ssmBike.deviceStatus.value == CHDeviceLoginStatus.Login) R.drawable.bl_green else R.drawable.bl_grey)

                            ssmView.setLockImage(ssmBike)
                            battery_percent.text = ssmBike.mechStatus?.getBatteryPrecentage().toString() + "%"
                            batteryImg.setImageResource(if (ssmBike.mechStatus?.getBatteryPrecentage() ?: 0 < 50) R.drawable.bt0 else if (ssmBike.mechStatus?.getBatteryPrecentage() ?: 0 < 70) R.drawable.bt50 else R.drawable.bt100)
                            sesame2Status.text = ssmBike.deviceStatus.toString()
                            sesame2Status.visibility = if (ssmBike.deviceStatus.value == CHDeviceLoginStatus.Login || ssmBike.deviceStatus == CHSesame2Status.NoBleSignal) View.GONE else View.VISIBLE

                            shadowStatusTxt.text = ssmBike.deviceShadowStatus.toString()

                            bl_img.setImageResource(if (ssmBike.deviceStatus.value == CHDeviceLoginStatus.Login) R.drawable.bl_green else R.drawable.bl_grey)
                            wifi_img.setImageResource(if (ssmBike.deviceShadowStatus?.value == CHDeviceLoginStatus.Login) R.drawable.wifi_green else R.drawable.wifi_grey)
                        }
                    }
                    R.layout.ssmbot_layout -> return object : RecyclerView.ViewHolder(view), Binder<CHSesameBot> {

                        var ssmBotView: SSMBotCellView = view.findViewById(R.id.ssmBotView)
                        var customName: TextView = view.findViewById(R.id.title)
                        var sesame2Status: TextView = view.findViewById(R.id.sub_title)
                        var shadowStatusTxt: TextView = view.findViewById(R.id.sub_title_2)
                        var battery_percent: TextView = view.findViewById(R.id.battery_percent)
                        var batteryImg: ImageView = view.findViewById(R.id.battery)
                        var bl_img: ImageView = view.findViewById(R.id.bl_img)
                        var wifi_img: ImageView = view.findViewById(R.id.wifi_img)

                        @SuppressLint("SetTextI18n")
                        override fun bind(ssmBot: CHSesameBot, pos: Int) {
//                                L.d("hcia", "ssmBot:" + ssmBot.deviceId.toString())
                            ssmBotView.setOnClickListener { ssmBot.click { } }
                            view.setOnClickListener {
                                mDeviceViewModel.ssmLockLiveData.value = ssmBot
                                mDeviceViewModel.targetModel = CHProductModel.SesameBot1
                                findNavController().navigate(R.id.action_deviceListPG_to_SesameBot2SettingFG)
                            }
                            mDeviceViewModel.ssmosLockDelegates.put(ssmBot, object : CHSesameBotDelegate {
                                override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                    setupBot(ssmBot)
                                }

                                override fun onMechStatusChanged(device: CHSesameBot, status: CHSesameBotMechStatus, intention: CHSesame2Intention) {
                                    ssmBotView.setLock(ssmBot)
                                }
                            })
                            setupBot(ssmBot)

                        }

                        private fun setupBot(ssmBot: CHSesameBot) {
                            customName.text = ssmBot.deviceId.toString()

                            ssmBotView.setLockImage(ssmBot)
                            battery_percent.text = ssmBot.mechStatus?.getBatteryPrecentage().toString() + "%"
                            batteryImg.setImageResource(if (ssmBot.mechStatus?.getBatteryPrecentage() ?: 0 < 50) R.drawable.bt0 else if (ssmBot.mechStatus?.getBatteryPrecentage() ?: 0 < 70) R.drawable.bt50 else R.drawable.bt100)
                            sesame2Status.text = ssmBot.deviceStatus.toString()
                            sesame2Status.visibility = if (ssmBot.deviceStatus.value == CHDeviceLoginStatus.Login || ssmBot.deviceStatus == CHSesame2Status.NoBleSignal) View.GONE else View.VISIBLE
                            shadowStatusTxt.text = ssmBot.deviceShadowStatus.toString()
                            bl_img.setImageResource(if (ssmBot.deviceStatus.value == CHDeviceLoginStatus.Login) R.drawable.bl_green else R.drawable.bl_grey)
                            wifi_img.setImageResource(if (ssmBot.deviceShadowStatus?.value == CHDeviceLoginStatus.Login) R.drawable.wifi_green else R.drawable.wifi_grey)
                        }
                    }
                    else -> return object : RecyclerView.ViewHolder(view), Binder<CHSesame2> {

                        var ssmView: SSMCellView = view.findViewById(R.id.ssmView)
                        var customName: TextView = view.findViewById(R.id.title)
                        var sesame2Status: TextView = view.findViewById(R.id.sub_title)
                        var shadowStatusTxt: TextView = view.findViewById(R.id.sub_title_2)
                        var battery_percent: TextView = view.findViewById(R.id.battery_percent)
                        var battery: ImageView = view.findViewById(R.id.battery)
                        var bl_img: ImageView = view.findViewById(R.id.bl_img)
                        var wifi_img: ImageView = view.findViewById(R.id.wifi_img)

                        @SuppressLint("SetTextI18n")
                        override fun bind(sesame: CHSesame2, pos: Int) {

                            mDeviceViewModel.ssmosLockDelegates.put(sesame, object : CHSesame2Delegate {
                                override fun onMechStatusChanged(device: CHSesame2, status: CHSesame2MechStatus, intention: CHSesame2Intention) {
                                    ssmView.setLock(sesame)
                                }

                                override fun onBleDeviceStatusChanged(device: SesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                                    setupSSMCell(sesame)
                                }
                            })


                            setupSSMCell(sesame)
//
                            ssmView.setOnClickListener {
                                sesame.toggle {
                                    it.onSuccess {
//                                        L.d("hcia", "[UI] toggle S")
                                    }
                                    it.onFailure {
                                        L.d("hcia", "[UI] toggle F :" + it)
                                    }
                                }
                            }



                            view.setOnClickListener {
                                mDeviceViewModel.ssmLockLiveData.value = sesame
                                mDeviceViewModel.targetModel = CHProductModel.SS2
                                findNavController().navigate(R.id.action_deviceListPG_to_mainRoomFG)
                            }
                        }

                        private fun setupSSMCell(sesame: CHSesame2) {
                            customName.text = sesame.deviceId.toString()
                            ssmView.setLock(sesame)
                            ssmView.setLockImage(sesame)
                            bl_img.setImageResource(if (sesame.deviceStatus.value == CHDeviceLoginStatus.Login) R.drawable.bl_green else R.drawable.bl_grey)
                            wifi_img.setImageResource(if (sesame.deviceShadowStatus?.value == CHDeviceLoginStatus.Login) R.drawable.wifi_green else R.drawable.wifi_grey)
                            battery_percent.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE
                            battery_percent.text = sesame.mechStatus?.getBatteryPrecentage().toString() + "%"
                            battery.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE
                            battery.setImageResource(if (sesame.mechStatus?.getBatteryPrecentage() ?: 0 < 50) R.drawable.bt0 else if (sesame.mechStatus?.getBatteryPrecentage() ?: 0 < 70) R.drawable.bt50 else R.drawable.bt100)
                            sesame2Status.text = sesame.deviceStatus.toString()
                            sesame2Status.visibility = if (sesame.deviceStatus.value == CHDeviceLoginStatus.Login || sesame.deviceStatus == CHSesame2Status.NoBleSignal) View.GONE else View.VISIBLE
                            shadowStatusTxt.text = sesame.deviceShadowStatus.toString()
                            shadowStatusTxt.setTextColor(ContextCompat.getColor(view.context, if (sesame.deviceShadowStatus?.value == CHDeviceLoginStatus.Login) R.color.unlock_blue else R.color.lock_red))
                        }
                    }
                }
            }
        }

        swiperefresh.setOnRefreshListener {
            mDeviceViewModel.updateDevices()
            swiperefresh?.isRefreshing = false

        }
        mDeviceViewModel.neeReflesh.observe(viewLifecycleOwner, { isR ->
//            L.d("hcia", "DeviceListFG isR:" + isR)
            swiperefresh?.isRefreshing = isR

            if (!isR) {
                leaderboard_list?.adapter?.notifyDataSetChanged()
            }
        })

    }
}



package co.candyhouse.app.tabs.devices.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.candyhouse.app.tabs.devices.ssm2.*
import co.candyhouse.sesame.open.device.CHSesameBike
import co.candyhouse.sesame.open.device.CHSesameBot
import co.candyhouse.sesame.open.CHDeviceManager
import co.candyhouse.sesame.open.device.*
import co.utils.L
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow


class CHDeviceViewModel : ViewModel(), CHSesame2Delegate, CHWifiModule2Delegate, CHSesameBotDelegate, CHSesameBikeDelegate {
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    var targetModel: CHProductModel? = null

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val myChDevices = MutableStateFlow(ArrayList<CHDevices>())
    var neeReflesh = MutableLiveData<Boolean>()
    val wm2LiveData = MutableLiveData<CHWifiModule2>()
    val ssmLockLiveData = MutableLiveData<CHSesameLocker>()

    var wm2Delegates: MutableMap<CHWifiModule2, CHWifiModule2Delegate> = mutableMapOf()
    var ssmosLockDelegates: MutableMap<CHSesameLocker, CHSesame2StatusDelegate> = mutableMapOf()
    fun targetDevice(): CHDevices = when (targetModel) {
        CHProductModel.WM2 -> wm2LiveData.value!!
        else -> ssmLockLiveData.value!!
    }


    fun updateDevices() {
        L.d("hcia", "👘 同步本地 updateDevices:")
        CHDeviceManager.getCandyDevices {
            it.onFailure {
                L.d("hcia", "👘 同步本地:" + it)
            }
            it.onSuccess {

                myChDevices.value.apply {
//                    L.d("hcia", "👘 刷新 clear:")
                    clear()
                    L.d("hcia", "it.data.size:" + it.data.size)
                    it.data.forEach {
                        L.d("hcia", "it.productModel:" + it.productModel)
                    }
                    myChDevices.value.addAll(it.data)
                    myChDevices.value.sortWith(compareBy({ it.getUIPriority() }, { it.deviceId }))


                    uiScope.launch {
                        L.d("hcia", "通知ＵＩ刷新 !!!!neeReflesh:" + neeReflesh)
                        neeReflesh.postValue(false)
                    }
                }.apply {
                    forEach {
                        if (it is CHSesame2) {
                            it.connect {}

                            it.delegate = this@CHDeviceViewModel
                        }
                        if (it is CHWifiModule2) {
                            it.delegate = this@CHDeviceViewModel
                        }
                        if (it is CHSesameBot) {
                            it.connect {}

                            it.delegate = this@CHDeviceViewModel
//                            L.d("hcia", "設定bot 代理 it.delegate :" + it.delegate)
                        }
                        if (it is CHSesameBike) {
                            it.delegate = this@CHDeviceViewModel
                            it.connect {}

//                            L.d("hcia", "設定bot 代理 it.delegate :" + it.delegate)
                        }

                    }
                }
            }
        }
    }


    override fun onMechStatusChanged(device: CHSesame2, status: CHSesame2MechStatus, intention: CHSesame2Intention) {
        uiScope.launch {
            (ssmosLockDelegates[device] as? CHSesame2Delegate)?.onMechStatusChanged(device, status, intention)
        }
    }

    override fun onBleDeviceStatusChanged(device: CHWifiModule2, status: CHSesame2Status) {

        if (device.deviceStatus == CHSesame2Status.ReceivedBle) {
//            device.connect { }
        }

        uiScope.launch {
            wm2Delegates[device]?.onBleDeviceStatusChanged(device, status)
        }
//        updateService()
    }

    override fun onAPSettingChanged(device: CHWifiModule2, settings: CHWifiModule2MechSettings) {
        uiScope.launch {
            wm2Delegates[device]?.onAPSettingChanged(device, settings)
        }
    }

    override fun onNetWorkStatusChanged(device: CHWifiModule2, settings: CHWifiModule2NetWorkStatus) {
//        L.d("hcia", "總代理 onNetWorkStatusChanged settings:" + settings)
        uiScope.launch {
            wm2Delegates.get(device)?.onNetWorkStatusChanged(device, settings)
        }
    }

    override fun onSSM2KeysChanged(device: CHWifiModule2, ssm2keys: Map<String, String>) {
        uiScope.launch {
            wm2Delegates.get(device)?.onSSM2KeysChanged(device, ssm2keys)
        }
    }

    override fun onOTAProgress(device: CHWifiModule2, percent: Byte) {
        uiScope.launch {
            wm2Delegates.get(device)?.onOTAProgress(device, percent)
        }
    }

    override fun onScanWifiSID(device: CHWifiModule2, ssid: String, rssi: Short) {
        uiScope.launch {
            wm2Delegates.get(device)?.onScanWifiSID(device, ssid, rssi)
        }
    }


    override fun onBleDeviceStatusChanged(device: CHSesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
        uiScope.launch {
            ssmosLockDelegates.get(device)?.onBleDeviceStatusChanged(device, status, shadowStatus)
        }
//        L.d("hcia", "[bot] 總代理 status:" + status + "shadowStatus:" + shadowStatus)
        if (device.deviceStatus == CHSesame2Status.ReceivedBle) {
            device.connect { }
        }


    }


    override fun onMechStatusChanged(device: CHSesameBot, status: CHSesameBotMechStatus, intention: CHSesame2Intention) {
        uiScope.launch { (ssmosLockDelegates.get(device) as? CHSesameBotDelegate)?.onMechStatusChanged(device, status, intention) }
//        L.d("hcia", "[bot] 總代理 intention:" + intention)
    }


}
package co.candyhouse.app.tabs.devices.ssm2.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseDeviceSettingFG
import co.candyhouse.app.tabs.devices.ssm2.*
import co.candyhouse.sesame.open.device.*
import co.utils.L
import kotlinx.android.synthetic.main.fg_setting_main.*

class SSM2SettingFG : BaseDeviceSettingFG(R.layout.fg_setting_main) {

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDeviceModel.ssmLockLiveData.observe(viewLifecycleOwner) { ss2 ->
            ss2 as CHSesame2
            ssmid_txt.text = ss2.deviceId.toString().toUpperCase()
            histag_txt?.text = ss2.getHistoryTag()?.let { String(it) }
        }

        onChangeStatus(mDeviceModel.ssmLockLiveData.value!!.deviceStatus)

        wheelview.apply {

            setItems(getSeconds().toList())
            setInitPosition(0)
            setListener { selected ->
                L.d("hcia", "selected:" + selected)

                val second = secondSettingValue.get(selected)
                (mDeviceModel.ssmLockLiveData.value as CHSesame2).enableAutolock(second) { res ->
                    res.onSuccess {
                        wheelview?.post {
                            autolock_status.text = second.toString()
                            autolock_status.visibility = if (second == 0) View.GONE else View.VISIBLE
                            second_tv?.visibility = if (second == 0) View.GONE else View.VISIBLE
                            wheelview.visibility = View.GONE
                            swiperefresh.isEnabled = true

                        }
                    }
                    res.onFailure {
                        L.d("hcia", "enableAutolock it:" + it)
                    }
                }
            }
        }
        chenge_angle_zone.setOnClickListener { findNavController().navigate(R.id.action_SSM2SettingFG_to_SSM2SetAngleFG) }
    }//end view created

    override fun onChangeStatus(status: CHSesame2Status) {
        if (status.value == CHDeviceLoginStatus.Login) {
            val ss2 = (mDeviceModel.ssmLockLiveData.value as CHSesame2)
            ss2.getAutolockSetting { res ->
                res.onSuccess {
                    autolockSwitch?.apply {
                        post {
                            autolock_status?.text = it.data.toString()
                            autolock_status?.visibility = if (it.data == 0) View.GONE else View.VISIBLE
                            second_tv?.visibility = if (it.data == 0) View.GONE else View.VISIBLE
                            autolockSwitch?.isChecked = it.data != 0
                            setOnCheckedChangeListener { buttonView, isChecked ->
                                L.d("hcia", "isChecked:" + isChecked)
                                wheelview?.visibility = if (isChecked) View.VISIBLE else View.GONE
                                swiperefresh.isEnabled = !isChecked
                                if (!isChecked) {
                                    ss2.disableAutolock { res ->
                                        wheelview?.post {
                                            res.onSuccess {
                                                wheelview.setCurrentPosition(0)
                                                wheelview.setInitPosition(0)
                                                autolock_status?.text = it.data.toString()
                                                autolock_status?.visibility = if (it.data == 0) View.GONE else View.VISIBLE
                                                second_tv?.visibility = if (it.data == 0) View.GONE else View.VISIBLE
                                                wheelview.setItems(getSeconds().toList())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ss2.getVersionTag { res ->
                res.onSuccess {
                    device_version_txt?.post {
                        device_version_txt?.text = it.data
                    }
                }
            }
            L.d("hcia", "status:" + status)
        }
    }
}




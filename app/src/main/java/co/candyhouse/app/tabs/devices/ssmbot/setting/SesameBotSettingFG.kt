package co.candyhouse.app.tabs.devices.ssmbot.setting

import android.os.Bundle
import android.view.View
import android.widget.TextView
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseDeviceSettingFG
import co.candyhouse.sesame.open.device.*
import kotlinx.android.synthetic.main.fg_ssm_bot_setting.*

class SesameBotSettingFG : BaseDeviceSettingFG(R.layout.fg_ssm_bot_setting) {


    override fun onChangeStatus(status: CHSesame2Status) {
        super.onChangeStatus(status)
        if (status.value == CHDeviceLoginStatus.Login) {
            mDeviceModel.ssmLockLiveData.value!!.getVersionTag {
                it.onSuccess {
                    device_version_txt?.post {
                        device_version_txt.text = it.data
                    }
                }
            }
            (mDeviceModel.ssmLockLiveData.value!! as CHSesameBot).mechSetting?.let {
                mode_txt.text = getString(botMode(it).i18nResources())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDeviceModel.ssmLockLiveData.observe(viewLifecycleOwner, { ssb ->
            // Update the UI
            device_uuid_txt.text = ssb?.deviceId.toString()
            ssb.getVersionTag {
                it.onSuccess {
                    device_version_txt?.post {
                        device_version_txt.text = it.data
                    }
                }
            }
            (ssb as CHSesameBot).mechSetting?.let {
                mode_txt.text = getString(botMode(it).i18nResources())
            }
        })

        change_mode_zone.setOnClickListener {
            (mDeviceModel.ssmLockLiveData.value as CHSesameBot).mechSetting?.let { setting ->
//                L.d("hcia", "setting:" + setting)
                (mDeviceModel.ssmLockLiveData.value as CHSesameBot).updateSetting(botMode(setting).changeNextMode(setting)) {
                    mode_txt.post {
                        mode_txt.text = getString(botMode(setting).i18nResources())
                    }
                }
            }
        }

    }//end view created

}


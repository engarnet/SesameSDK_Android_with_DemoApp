package co.candyhouse.app.tabs.devices.ssmbike.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseDeviceSettingFG
import co.utils.L


class SesameBikeSettingFG : BaseDeviceSettingFG(R.layout.fg_ssm_bike_setting) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        L.d("hcia", "車設定頁面 onViewCreated:")


        mDeviceModel.ssmLockLiveData.observe(viewLifecycleOwner, { ssb ->
            getView()?.findViewById<TextView>(R.id.device_uuid_txt)?.text = ssb?.deviceId.toString()
            ssb.getVersionTag {
                it.onSuccess {
                    getView()?.findViewById<TextView>(R.id.device_uuid_txt)?.post {
                        getView()?.findViewById<TextView>(R.id.device_uuid_txt)?.text = it.data
                    }
                }
            }
        })
    }//end view created

}


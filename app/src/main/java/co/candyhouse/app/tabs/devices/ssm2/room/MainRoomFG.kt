package co.candyhouse.app.tabs.devices.ssm2.room

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.app.R
import co.candyhouse.app.base.BaseDeviceFG
import co.candyhouse.sesame.open.CHResultState
import co.candyhouse.sesame.open.device.*
import co.utils.L
import co.utils.clicks
import kotlinx.android.synthetic.main.back_sub.*
import kotlinx.android.synthetic.main.fg_room_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.zakariya.stickyheaders.StickyHeaderLayoutManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainRoomFG : BaseDeviceFG(R.layout.fg_room_main) {
    private var cursor: Long? = null
    private var responseCursor: Long? = null

    private var mHistorys = ArrayList<CHSesame2History>()
    private var mHistoryss = ArrayList<Pair<String, List<CHSesame2History>>>()
    private val mAdapter = SSMHistoryAdapter(mHistoryss)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        right_icon.setOnClickListener { findNavController().navigate(R.id.action_mainRoomFG_to_SSM2SettingFG) }

        mDeviceModel.ssmLockLiveData.observe(viewLifecycleOwner, { ssm2 ->
            // Update the UI
            ssm2 as CHSesame2
            ssmView.setLock(ssm2)
            ssmView.setLockImage(ssm2)
//            ssmView.setOnClickListener { ssm2.toggleProtect {} }
            ssmView.clicks().debounce(200).onEach { ssm2.toggle {} }.launchIn(GlobalScope)
        })
        room_list?.apply {
            layoutManager = StickyHeaderLayoutManager()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // (0：靜止, 1：手動滾動, 2：自動滾動)
                    if (newState == 0) {
                        val layoutManager = (room_list?.layoutManager as StickyHeaderLayoutManager)
                        val firstVisibleItemPosition = layoutManager.getFirstVisibleItemViewHolder(false)?.positionInSection
                        val firstAdapterPosition = layoutManager.getFirstVisibleItemViewHolder(false)?.adapterPosition

                        if (firstAdapterPosition != null) {
                            //  firstVisibleItemPosition==0  top of section ,
                            //  firstAdapterPosition  total count count form top ,
                            if (firstVisibleItemPosition == 0 && firstAdapterPosition < 20) {
                                L.d("bbtig", "firstVisibleItemPosition == 0 && firstAdapterPosition < 20")
                                if (responseCursor != null) {
                                    cursor = responseCursor
                                }
                                refleshHistory()
                            }
                        }
                    }
                }
            })
        }


        refleshHistory()
    }


    private fun refleshHistory() {
//        L.d("hcia", "[UI] refleshHistory:")
        swiperefresh?.post {
            swiperefresh?.isRefreshing = true
        }
        // 要撈幾頁資料
        L.d("bbtig", "Cursor: $cursor")
        (mDeviceModel.ssmLockLiveData.value as CHSesame2).getHistories(cursor) {
            it.onSuccess {
//                L.d("hcia", "UI收到 歷史it:" + it.javaClass.simpleName)
                var sesameHistorys = it.data.first
                responseCursor = it.data.second
                sesameHistorys = sesameHistorys.filter { his ->
                    his is CHSesame2History.WEBLock ||
                            his is CHSesame2History.WEBUnlock ||
                            his is CHSesame2History.AutoLock ||
                            his is CHSesame2History.WM2Unlock ||
                            his is CHSesame2History.WM2Lock ||
                            his is CHSesame2History.ManualLocked ||
                            his is CHSesame2History.ManualUnlocked ||
                            his is CHSesame2History.BLEUnlock ||
                            his is CHSesame2History.BLELock
                }
                if (sesameHistorys.isEmpty()) {
                    return@onSuccess
                }
                room_list?.post {

                    mHistorys.addAll(sesameHistorys)
                    mHistorys = mHistorys.distinctBy { (it.date.getTime()) } as ArrayList<CHSesame2History>
                    mHistorys.sortBy { it.date.getTime() }


                    val mTestGoupHistory = mHistorys.groupBy {
                        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(it.date)
                    }
                    mAdapter.mGroupHistData.clear()
                    mAdapter.mGroupHistData.addAll(mTestGoupHistory.toList())
                    if (room_list?.adapter == null) {
                        room_list?.adapter = mAdapter
                        if (sesameHistorys.size != 0) {
                            room_list?.layoutManager?.scrollToPosition(room_list?.adapter!!.getItemCount() - 1)
                        }
                    }
                    mAdapter.notifyAllSectionsDataSetChanged()
                    if (it is CHResultState.CHResultStateBLE || cursor == null) {
                        L.d("bbtig", "it is CHResultState.CHResultStateBLE || cursor == null")
                        room_list?.layoutManager?.scrollToPosition(room_list?.adapter!!.getItemCount() - 1)
                    }
                }
            }
            it.onFailure {
                // todo kill the hint  if you got!!!
                // 這裡是個workaround
                // 理由:多人連線 sesame2 回 busy:7  notfound:5
                // 策略:延遲網路請求等待隔壁連上的sesame2上傳完畢後拉取
                if (it is NSError) {
//                    L.d("hcia", "🧤[UI] it.code :" + it.code + " " + it)
                    if (it.code == 7 || it.code == 5) {
//                        hispage = 0
//                        refleshHistory()
                        lifecycleScope.launch {
//                        L.d("hcia", "🧤: -->status:" + status)
                            channel.send(true)
                        }
                    }
                }
            }
            swiperefresh?.post {
                swiperefresh?.isRefreshing = false
            }

        }
    }

    private val channel = Channel<Boolean>(1)

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            channel.consumeAsFlow()
                    .debounce(3000)
                    .collect {
//                        L.d("hcia", "🧤: <---status:" + it)
                        cursor = null
                        responseCursor = null
                        refleshHistory()
                    }
        }

        mDeviceModel.ssmosLockDelegates.put(mDeviceModel.ssmLockLiveData.value!!, object : CHSesame2Delegate {
            override fun onBleDeviceStatusChanged(device: CHSesameLocker, status: CHSesame2Status, shadowStatus: CHSesame2ShadowStatus?) {
                if (device.deviceStatus == CHSesame2Status.ReceivedBle) {
                    device.connect {}
                }
                ssmView?.setLockImage(mDeviceModel.ssmLockLiveData.value as CHSesame2)
            }

            override fun onMechStatusChanged(device: CHSesame2, status: CHSesame2MechStatus, intention: CHSesame2Intention) {

                ssmView?.setLock(mDeviceModel.ssmLockLiveData.value as CHSesame2)

                if (device.deviceStatus.value == CHDeviceLoginStatus.UnLogin) {
                    lifecycleScope.launch {
//                        L.d("hcia", "🧤: -->status:" + status)
                        channel.send(true)
                    }
                }
            }
        })
    }

}

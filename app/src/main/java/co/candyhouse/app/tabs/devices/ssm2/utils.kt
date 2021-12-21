package co.candyhouse.app.tabs.devices.ssm2

import co.candyhouse.app.R
import co.candyhouse.sesame.open.device.CHWifiModule2
import co.candyhouse.sesame.open.device.*

fun ssmBikeUIParcer(device: CHSesameBike): Int {

    if (device.deviceShadowStatus == CHSesame2ShadowStatus.LockedWm2) {
        return R.drawable.icon_lock
    }
    if (device.deviceShadowStatus == CHSesame2ShadowStatus.MovedWm2) {
        return R.drawable.icon_unlock
    }
    if (device.deviceShadowStatus == CHSesame2ShadowStatus.UnlockedWm2) {
        return R.drawable.icon_unlock
    }
    return when (device.deviceStatus) {
        CHSesame2Status.DfuMode -> R.drawable.icon_nosignal
        CHSesame2Status.NoBleSignal -> R.drawable.icon_nosignal
        CHSesame2Status.ReceivedBle -> R.drawable.icon_receiveblee
        CHSesame2Status.BleConnecting -> R.drawable.icon_receiveblee
        CHSesame2Status.WaitingGatt -> R.drawable.icon_waitgatt
        CHSesame2Status.BleLogining -> R.drawable.icon_logining
        CHSesame2Status.ReadyToRegister -> R.drawable.icon_nosignal
        CHSesame2Status.Locked -> R.drawable.icon_lock
        CHSesame2Status.Unlocked -> R.drawable.icon_unlock
        CHSesame2Status.NoSettings -> R.drawable.icon_nosetting
        CHSesame2Status.Moved -> R.drawable.icon_unlock
        CHSesame2Status.Reset -> R.drawable.icon_nosignal
        CHSesame2Status.Registering -> R.drawable.icon_logining
        else -> R.drawable.ic_icons_outlined_setting

    }
}

fun ssmBotUIParcer(device: CHSesameBot): Int {

    if (device.deviceShadowStatus == CHSesame2ShadowStatus.UnlockedWm2) {
        return R.drawable.swtich_unlocked
    }
    if (device.deviceShadowStatus == CHSesame2ShadowStatus.LockedWm2) {
        return R.drawable.swtich_locked
    }
    return when (device.deviceStatus) {

        CHSesame2Status.DfuMode -> R.drawable.swtich_no_ble
        CHSesame2Status.NoBleSignal -> R.drawable.swtich_no_ble
        CHSesame2Status.ReceivedBle -> R.drawable.swtich_receive_ble
        CHSesame2Status.BleConnecting -> R.drawable.swtich_receive_ble
        CHSesame2Status.WaitingGatt -> R.drawable.swtich_waitgatt
        CHSesame2Status.BleLogining -> R.drawable.swtich_logining
        CHSesame2Status.ReadyToRegister -> R.drawable.swtich_no_ble
        CHSesame2Status.Locked -> R.drawable.swtich_locked
        CHSesame2Status.Unlocked -> R.drawable.swtich_unlocked
        CHSesame2Status.Reset -> R.drawable.swtich_no_ble
        CHSesame2Status.Registering -> R.drawable.swtich_logining
        else -> R.drawable.ic_icons_outlined_setting

    }
}

fun ssmUIParcer(device: CHSesame2): Int {

    if (device.deviceShadowStatus == CHSesame2ShadowStatus.LockedWm2) {
        return R.drawable.icon_lock
    }
    if (device.deviceShadowStatus == CHSesame2ShadowStatus.MovedWm2) {
        return R.drawable.icon_unlock
    }
    if (device.deviceShadowStatus == CHSesame2ShadowStatus.UnlockedWm2) {
        return R.drawable.icon_unlock
    }
    return when (device.deviceStatus) {
        CHSesame2Status.DfuMode -> R.drawable.icon_nosignal
        CHSesame2Status.NoBleSignal -> R.drawable.icon_nosignal
        CHSesame2Status.ReceivedBle -> R.drawable.icon_receiveblee
        CHSesame2Status.BleConnecting -> R.drawable.icon_receiveblee
        CHSesame2Status.WaitingGatt -> R.drawable.icon_waitgatt
        CHSesame2Status.BleLogining -> R.drawable.icon_logining
        CHSesame2Status.ReadyToRegister -> R.drawable.icon_nosignal
        CHSesame2Status.Locked -> R.drawable.icon_lock
        CHSesame2Status.Unlocked -> R.drawable.icon_unlock
        CHSesame2Status.NoSettings -> R.drawable.icon_nosetting
        CHSesame2Status.Moved -> R.drawable.icon_unlock
        CHSesame2Status.Reset -> R.drawable.icon_nosignal
        CHSesame2Status.Registering -> R.drawable.icon_logining
        CHSesame2Status.WaitingForAuth -> R.drawable.icon_waitgatt
        else -> R.drawable.ic_icons_outlined_setting

    }
}


fun CHDevices.getUIPriority(): Int {//數字越大排越後面
    if (this is CHSesame2) {
        return 0
    }
    if (this is CHSesameBot) {
        return 1
    }
    if (this is CHSesameBike) {
        return 2
    }
    if (this is CHWifiModule2) {
        return 3
    }
    return 0
}


fun CHDevices.getFirZip(): Int {
    return when (productModel) {
        CHProductModel.SS2 -> return R.raw.sesame_221_0_8c080c
        CHProductModel.SS4 -> return R.raw.sesame_421_4_50ce5b
        CHProductModel.SesameBot1 -> return R.raw.ss2sw_212_369eb9
        CHProductModel.BiKeLock -> return R.raw.ss2bike_213_486b77
        CHProductModel.WM2 -> TODO()
    }
}

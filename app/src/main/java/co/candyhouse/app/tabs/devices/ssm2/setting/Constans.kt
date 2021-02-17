package co.candyhouse.app.tabs.devices.ssm2.setting

import co.candyhouse.app.R


val advIntervals = arrayListOf<String>(

        "546.25 ms",
        "760 ms",
        "852.5 ms",
        "1022.5 ms",
        "1285 ms",
        "20.0 ms",
        "152.5 ms",
        "211.25 ms",
        "318.75 ms",
        "417.5 ms"
)
val advintervalSettingValue: Array<Double> = arrayOf(
        546.25.toDouble(),
        760.toDouble(),
        852.5.toDouble(),
        1022.5.toDouble(),
        1285.toDouble(),
        20.0.toDouble(),
        152.5.toDouble(),
        211.25.toDouble(),
        318.75.toDouble(),
        417.5.toDouble()
)
val secondSettingValue = arrayOf(
        0,
        3,
        5,
        7,
        10,
        15,
        30,
        60,
        60 * 2,
        60 * 5,
        60 * 10,
        60 * 15,
        60 * 30,
        60 * 60
)
val dBmsValus: Array<Byte> = arrayOf(
        -4, 0, 3, 4, -40, -20, -16, -12, -8
)
val dBmsSetting: Array<String> = arrayOf(
        "-4 dBm", "0 dBm", "3 dBm", "4 dBm", "-40 dBm", "-20 dBm", "-16 dBm", "-12 dBm", "-8 dBm"
)

fun SSM2SettingFG.getSeconds(): Array<String> {
    val secondSetting = arrayOf(
            getString(R.string.Off),
            getString(R.string.sec3),
            getString(R.string.sec5),
            getString(R.string.sec7),
            getString(R.string.sec10),
            getString(R.string.sec15),
            getString(R.string.sec30),
            getString(R.string.min1),
            getString(R.string.min2),
            getString(R.string.min5),
            getString(R.string.min10),
            getString(R.string.min15),
            getString(R.string.min30),
            getString(R.string.hr1)
    )
    // after the null check, 'this' is autocast to a non-null type, so the toString() below
    // resolves to the member function of the Any class
    return secondSetting
}
package com.quickcleanpro.phonecleaner.domain.model

/**
 * 鐢垫睜鍏呮斁鐢电姸鎬佸揩鐓с€? *
 * Repository 灏嗙郴缁熷箍鎾腑鐨勭姸鎬佺爜杞崲鎴愰〉闈㈠彲鐩存帴浣跨敤鐨勬枃鏈拰鍏呯數鏍囪锛? * 閬垮厤椤甸潰灞傜悊瑙?Android 鐢垫睜鐘舵€佸父閲忋€? */
data class BatteryStatusInfo(
    val statusText: String,
    val isCharging: Boolean,
)

/**
 * 璁惧纭欢涓庡睆骞曚俊鎭揩鐓с€? */
data class DeviceHardwareInfo(
    val model: String,
    val androidVersion: String,
    val screenSize: String,
    val screenDensity: String,
    val multiTouchSupported: Boolean,
    val sensors: DeviceSensorInfo,
    val cpu: DeviceCpuInfo,
)

/**
 * 璁惧浼犳劅鍣ㄦ敮鎸佹儏鍐点€? *
 * 姣忎釜瀛楁瀵瑰簲椤甸潰涓睍绀虹殑涓€椤逛紶鎰熷櫒鑳藉姏锛屼娇鐢ㄥ竷灏斿€间究浜?ViewModel 缁熶竴鏄犲皠鏂囨銆? */
data class DeviceSensorInfo(
    val accelerometer: Boolean,
    val magneticField: Boolean,
    val orientation: Boolean,
    val gyroscope: Boolean,
    val light: Boolean,
    val proximity: Boolean,
    val ambientTemperature: Boolean,
)

/**
 * CPU 鍩虹淇℃伅銆? *
 * 棰戠巼璇诲彇鍙兘鍥犺澶囨潈闄愭垨绯荤粺瑁佸壀澶辫触锛屽け璐ユ椂鐢?data 灞傛彁渚?Unknown 鏂囨銆? */
data class DeviceCpuInfo(
    val hardware: String,
    val model: String,
    val cores: Int,
    val maxFrequency: String,
)

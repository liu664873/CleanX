package com.quickcleanpro.phonecleaner.presentation.common.state

/**
 * 閫氱敤鍔犺浇鐘舵€佹ā鍨嬨€?
 *
 * 鐢ㄤ簬鎶婇〉闈腑鐨勨€滅┖闂层€佸姞杞戒腑銆佹垚鍔熴€佺┖缁撴灉銆侀敊璇€濈姸鎬佺粺涓€琛ㄨ揪锛?
 * 閬垮厤姣忎釜椤甸潰閲嶅缁存姢澶氱粍 Boolean 鎴栦复鏃舵灇涓俱€?
 */
sealed interface LoadableUiState<out T> {

    /** 灏氭湭寮€濮嬪姞杞芥垨绛夊緟鐢ㄦ埛瑙﹀彂銆?*/
    data object Idle : LoadableUiState<Nothing>

    /** 姝ｅ湪鍔犺浇鏁版嵁锛岄€氬父鐢?ViewModel 鍦ㄥ悗鍙板崗绋嬩腑杩涘叆銆?*/
    data object Loading : LoadableUiState<Nothing>

    /** 鍔犺浇鎴愬姛骞舵惡甯﹂〉闈㈤渶瑕佸睍绀虹殑鏁版嵁銆?*/
    data class Success<T>(val data: T) : LoadableUiState<T>

    /** 鍔犺浇鎴愬姛浣嗘病鏈夊彲灞曠ず鍐呭銆?*/
    data class Empty(val message: String = "") : LoadableUiState<Nothing>

    /** 鍔犺浇澶辫触锛屼繚鐣欑敤鎴峰彲璇绘枃妗堝拰鍙€夊紓甯稿師鍥犮€?*/
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : LoadableUiState<Nothing>
}

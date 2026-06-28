package com.pdffox.adv;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.StandardIntegrityManager;
import com.pdffox.adv.adv.AdvCheckManager;
import com.pdffox.adv.util.PreferenceUtil;

import java.security.MessageDigest;

/**
 * Play Integrity token 请求工具。
 *
 * 负责准备 StandardIntegrityTokenProvider、生成 requestHash、请求 token，并交给服务端解析校验。
 */
public class PlayIntegrityHelper {
    private static final String TAG = "PlayIntegrityHelper";
    StandardIntegrityManager.StandardIntegrityTokenProvider integrityTokenProvider;

    /**
     * 发起 Play Integrity 标准请求流程。
     *
     * Google Cloud Project Number 由 SDK 配置提供；token 获取成功后异步发送给服务端。
     */
    public void requestPlayIntegrity() {
        long cloudProjectNumber = Config.INSTANCE.getSdkConfig().getPlayIntegrity().getCloudProjectNumber();
        if (cloudProjectNumber <= 0L) {
            Log.w(TAG, "requestPlayIntegrity: missing cloud project number");
            return;
        }

        Log.e(TAG, "requestPlayIntegrity: " );
        StandardIntegrityManager standardIntegrityManager = IntegrityManagerFactory.createStandard(AdvRuntime.INSTANCE.getApplication());
        StandardIntegrityManager.PrepareIntegrityTokenRequest request = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build();
        standardIntegrityManager.prepareIntegrityToken(request)
                .addOnSuccessListener(tokenProvider -> {
                    integrityTokenProvider = tokenProvider;
                    Log.e(TAG, "requestPlayIntegrity: has requested tokenProvider" );

                    // requestHash 用于把本次客户端请求和服务端解析结果关联起来。
                    String input = System.currentTimeMillis() + "-" + Math.random();
                    String requestHash = generateRequestHash(input);
                    PreferenceUtil.INSTANCE.commitString("requestHash", requestHash);
                    if (requestHash == null) {
                        Log.e(TAG, "requestHash 生成失败");
                        return;
                    }
                    Task<StandardIntegrityManager.StandardIntegrityToken> integrityTokenResponse =
                            integrityTokenProvider.request(
                                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .setRequestHash(requestHash)
                                            .build());
                    integrityTokenResponse
                            .addOnSuccessListener(response -> sendToServer(response.token()))
                            .addOnFailureListener(this::handleError);
                })
                .addOnFailureListener(this::handleError);
    }

    /** 使用 SHA-256 生成 Play Integrity 请求哈希。 */
    private String generateRequestHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "generateRequestHash error", e);
            return null;
        }
    }

    /** 将 token 切到后台线程发送给服务端解析，避免阻塞 Play Integrity 回调线程。 */
    private void sendToServer(String token) {
        Log.e(TAG, "sendToServer: token = " + token);
        // 切换到子线程执行
        new Thread(() -> AdvCheckManager.INSTANCE.checkToken(token)).start();
    }

    /** 统一记录 Play Integrity 请求或 token 获取失败。 */
    private void handleError(Exception exception) {
        Log.e(TAG, "handleError: ", exception);
    }

}

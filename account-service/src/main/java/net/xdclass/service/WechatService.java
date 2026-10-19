package net.xdclass.service;

import net.xdclass.util.JsonData;

public interface WechatService {
    String getAccessToken();
    JsonData generateQrCode();
    JsonData handleEventCallback(String openId,String sceneId);
    JsonData getWechatUserScanResult(String sceneId);
}

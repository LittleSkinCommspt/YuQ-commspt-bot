package top.cubik65536.yuq.pojo;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ConfigType {
    BaiduAIOcrAppId("baiduAIOcrAppId"),
    BaiduAIOcrAppKey("baiduAIOcrAppKey"),
    BaiduAIOcrSecretKey("baiduAIOcrSecretKey"),
    BaiduAIContentCensorAppId("baiduAIContentCensorAppId"),
    BaiduAIContentCensorAppKey("baiduAIContentCensorAppKey"),
    BaiduAIContentCensorSecretKey("baiduAIContentCensorSecretKey"),
    BaiduAISpeechAppId("BaiduAISpeechAppId"),
    BaiduAISpeechAppKey("BaiduAISpeechAppKey"),
    BaiduAISpeechSecretKey("BaiduAISpeechSecretKey"),
    Teambition("teambition"),
    DCloud("dCloud"),
    SauceNao("sauceNao"),
    IdentifyCode("identifyCode"),
    FateAdmCode("fateAdmCode"),
    DdOcrCode("ddOcrCode");


    private final String type;
    ConfigType(String type){
        this.type = type;
    }
}
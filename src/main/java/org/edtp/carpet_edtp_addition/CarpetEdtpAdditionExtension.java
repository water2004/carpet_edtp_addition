package org.edtp.carpet_edtp_addition;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import java.util.HashMap;
import java.util.Map;

import org.edtp.carpet_edtp_addition.util.Translation;

public class CarpetEdtpAdditionExtension implements CarpetExtension {
    
    public static final CarpetEdtpAdditionExtension INSTANCE = new CarpetEdtpAdditionExtension();
    
    @Override
    public void onGameStarted() {
        // 注册设置管理器
        CarpetEdtpAdditionSettings.register();
    }

    @Override
    public String version() {
        return "1.0.0";
    }
    
    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translation.getInstance().getTranslation();
    }
}
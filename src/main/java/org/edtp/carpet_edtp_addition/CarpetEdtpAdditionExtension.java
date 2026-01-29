package org.edtp.carpet_edtp_addition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import net.fabricmc.loader.api.FabricLoader;

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
<<<<<<< HEAD
        return FabricLoader.getInstance()
            .getModContainer("carpet_edtp_addition")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
=======
        return "${mod_version}";
>>>>>>> fc29e08b0282141f2485d46797c0333a75ba60a7
    }
    
    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translation.getInstance().getTranslation(lang);
    }
}
package org.edtp.carpet_edtp_addition;

import net.fabricmc.api.ModInitializer;
import carpet.CarpetServer;

public class Carpet_edtp_addition implements ModInitializer {

    @Override
    public void onInitialize() {
        // 注册 Carpet 扩展
        CarpetServer.manageExtension(CarpetEdtpAdditionExtension.INSTANCE);
    }
}

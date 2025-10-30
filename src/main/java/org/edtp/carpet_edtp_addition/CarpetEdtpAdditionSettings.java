package org.edtp.carpet_edtp_addition;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
import carpet.CarpetServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

public class CarpetEdtpAdditionSettings {
    
    public static final EdtpCarpetRule softObsidian = new EdtpCarpetRule(
        "softObsidian", 
        false, 
        "Set the hardness of obsidian to be the same as end stone"
    );
    
    public static final EdtpCarpetRule unPushableArmorStands = new EdtpCarpetRule(
        "unPushableArmorStands",
        false,
        "Armor stands won't be pushed by attacks, explosions or flowing fluids"
    );
    
    public static final EdtpCarpetRule safeTeleport = new EdtpCarpetRule(
        "safeTeleport",
        false,
        "Prevents teleportation to unsafe locations (void, suffocation)"
    );
    
    public static final EdtpCarpetRule tickCommandForAll = new EdtpCarpetRule(
        "tickCommandForAll",
        false,
        "Allows non-op players to use the /tick command"
    );
    
    public static void register() {
        CarpetServer.settingsManager.addCarpetRule(softObsidian);
        CarpetServer.settingsManager.addCarpetRule(unPushableArmorStands);
        CarpetServer.settingsManager.addCarpetRule(safeTeleport);
        CarpetServer.settingsManager.addCarpetRule(tickCommandForAll);
    }
    
    public static class EdtpCarpetRule implements CarpetRule<Boolean> {
        private final String name;
        private final String description;
        private Boolean value;
        private final Boolean defaultValue;
        
        public EdtpCarpetRule(String name, Boolean defaultValue, String description) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.description = description;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        @Override
        public List<Text> extraInfo() {
            return List.of();
        }
        
        @Override
        public Collection<String> categories() {
            return List.of("SURVIVAL", "EDTP");
        }
        
        @Override
        public Collection<String> suggestions() {
            return List.of("true", "false");
        }
        
        @Override
        public SettingsManager settingsManager() {
            return CarpetServer.settingsManager;
        }
        
        @Override
        public Boolean value() {
            return value;
        }
        
        @Override
        public boolean canBeToggledClientSide() {
            return false;
        }
        
        @Override
        public Class<Boolean> type() {
            return Boolean.class;
        }
        
        @Override
        public Boolean defaultValue() {
            return defaultValue;
        }
        
        @Override
        public boolean strict() {
            return true;
        }
        
        @Override
        public void set(ServerCommandSource source, String value) {
            this.value = Boolean.parseBoolean(value);
            if (source != null) {
                settingsManager().notifyRuleChanged(source, this, value);
            }
        }
        
        @Override
        public void set(ServerCommandSource source, Boolean value) {
            this.value = value;
            if (source != null) {
                settingsManager().notifyRuleChanged(source, this, value.toString());
            }
        }
    }
}
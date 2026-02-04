package org.edtp.carpet_edtp_addition;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
import carpet.CarpetServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
    
    public static final EdtpCarpetRule noFurnaceAsh = new EdtpCarpetRule(
        "noFurnaceAsh",
        false,
        "Items without recipes pass through furnaces instantly, preventing ash waste"
    );
    
    public static final EdtpCarpetRule noPlayerPortals = new EdtpCarpetRule(
        "noPlayerPortals",
        false,
        "Prevents players from using portals"
    );
    
    public static final EdtpCarpetRule strongerBundle = new EdtpCarpetRule(
        "strongerBundle",
        false,
        "Allows shulker boxes to be inserted into bundles (max 8), prevents bundles in shulker boxes"
    );
    
    public static final EdtpCarpetRule toughArmorStands = new EdtpCarpetRule(
        "toughArmorStands",
        false,
        "Armor stands won't take damage from attacks"
    );
    
    public static final EdtpCarpetRule toughSlimeBlocks = new EdtpCarpetRule(
        "toughSlimeBlocks",
        false,
        "Set the hardness of slime blocks and honey blocks to be the same as end stone"
    );

    public static final EdtpCarpetStringRule beesDimCurfew = new EdtpCarpetStringRule(
        "beesDimCurfew",
        "false",
        "In the Nether/End, forces bees to enter hives and prevents them from leaving. Options: false, nether, end, true"
    );

    public static final EdtpCarpetRule resonantWater = new EdtpCarpetRule(
        "resonantWater",
        false,
        "左右逢源 - Water buckets won't be consumed when surrounded by water buckets in the hotbar"
    );

    public static void register() {
        try {
            for (Field field : CarpetEdtpAdditionSettings.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && 
                    Modifier.isStatic(modifiers) && 
                    Modifier.isFinal(modifiers) &&
                    CarpetRule.class.isAssignableFrom(field.getType())) {
                    
                    CarpetRule<?> rule = (CarpetRule<?>) field.get(null);
                    CarpetServer.settingsManager.addCarpetRule(rule);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to register Carpet rules via reflection", e);
        }
    }

    public static boolean isBeesDimCurfewEnabled(World world) {
        if (world == null) {
            return false;
        }
        String value = beesDimCurfew.value();
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("both")) {
            return world.getRegistryKey() == World.NETHER || world.getRegistryKey() == World.END;
        }
        if (normalized.equals("false")) {
            return false;
        }
        if (normalized.equals("nether") || normalized.equals("the_nether")) {
            return world.getRegistryKey() == World.NETHER;
        }
        if (normalized.equals("end") || normalized.equals("the_end")) {
            return world.getRegistryKey() == World.END;
        }
        return false;
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

        @Override
        public String toString() {
            return name;
        }
    }

    public static class EdtpCarpetStringRule implements CarpetRule<String> {
        private final String name;
        private final String description;
        private String value;
        private final String defaultValue;

        public EdtpCarpetStringRule(String name, String defaultValue, String description) {
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
            return List.of("false", "nether", "end", "true");
        }

        @Override
        public SettingsManager settingsManager() {
            return CarpetServer.settingsManager;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean canBeToggledClientSide() {
            return false;
        }

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public boolean strict() {
            return true;
        }

        @Override
        public void set(ServerCommandSource source, String value) {
            this.value = value;
            if (source != null) {
                settingsManager().notifyRuleChanged(source, this, value);
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
package org.edtp.carpet_edtp_addition;

import carpet.CarpetExtension;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.edtp.carpet_edtp_addition.util.Translation;

public class CarpetEdtpAdditionExtension implements CarpetExtension {
    
    public static final CarpetEdtpAdditionExtension INSTANCE = new CarpetEdtpAdditionExtension();
    
    @Override
    public void onGameStarted() {
        // 注册设置管理器
        CarpetEdtpAdditionSettings.register();
        registerCustomTrades();
    }

    private void registerCustomTrades() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> levelMap = VillagerTrades.TRADES.get(VillagerProfession.TOOLSMITH);
        if (levelMap != null) {
            VillagerTrades.ItemListing[] masterTrades = levelMap.get(5);
            if (masterTrades != null) {
                List<VillagerTrades.ItemListing> factories = new ArrayList<>(Arrays.asList(masterTrades));
                factories.add((transaction, entity, random) -> {
                    if (CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value() >= 1) {
                         // SellEnchantedToolFactory(Item item, int basePrice, int maxUses, int experience, float multiplier)
                         return new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_HOE, 13, 3, 30, 0.2f)
                                .getOffer(transaction, entity, random);
                    }
                    return null;
                });
                levelMap.put(5, factories.toArray(new VillagerTrades.ItemListing[0]));
            }
        }
    }

    @Override
    public String version() {
        return FabricLoader.getInstance()
            .getModContainer("carpet_edtp_addition")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }
    
    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translation.getInstance().getTranslation(lang);
    }
}

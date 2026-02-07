package org.edtp.carpet_edtp_addition;

import carpet.CarpetExtension;
import net.fabricmc.loader.api.FabricLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
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
        Int2ObjectMap<TradeOffers.Factory[]> levelMap = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.TOOLSMITH);
        if (levelMap != null) {
            TradeOffers.Factory[] masterTrades = levelMap.get(5);
            if (masterTrades != null) {
                List<TradeOffers.Factory> factories = new ArrayList<>(Arrays.asList(masterTrades));
                factories.add((transaction, entity, random) -> {
                    if (CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value() >= 1) {
                         // SellEnchantedToolFactory(Item item, int basePrice, int maxUses, int experience, float multiplier)
                         return new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_HOE, 13, 3, 30, 0.2f)
                                .create(transaction, entity, random);
                    }
                    return null;
                });
                levelMap.put(5, factories.toArray(new TradeOffers.Factory[0]));
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

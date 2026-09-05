package org.edtp.carpet_edtp_addition.villager;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public final class VillagerTradeModifiers {
    private VillagerTradeModifiers() {
    }

    public static void adjustToolsmithTrades(Villager villager, ServerLevel level) {
        int ruleValue = CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value();
        if (ruleValue <= 0) {
            return;
        }

        VillagerData villagerData = villager.getVillagerData();
        if (!villagerData.profession().is(VillagerProfession.TOOLSMITH)) {
            return;
        }

        MerchantOffers offers = villager.getOffers();
        int maxEnchantLevel = getMaxEnchantLevel(ruleValue);
        boolean hasDiamondHoeOffer = false;
        boolean hasEnchantedDiamondHoe = false;

        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer offer = offers.get(i);
            ItemStack result = offer.getResult();

            if (result.getItem() == Items.DIAMOND_HOE) {
                hasDiamondHoeOffer = true;
                if (ruleValue >= 1 && (!result.isEnchanted() || ruleValue >= 2)) {
                    ItemStack rerolled = createEnchantedTool(level, Items.DIAMOND_HOE, maxEnchantLevel);
                    offers.set(i, copyOffer(offer, rerolled));
                    hasEnchantedDiamondHoe = true;
                    continue;
                }
                hasEnchantedDiamondHoe = result.isEnchanted();
            } else if (ruleValue >= 2 && isUpgradeableToolOffer(result)) {
                ItemStack rerolled = createEnchantedTool(level, result.getItem(), maxEnchantLevel);
                offers.set(i, copyOffer(offer, rerolled));
            }
        }

        if (!hasDiamondHoeOffer && villagerData.level() >= 5 && !hasEnchantedDiamondHoe) {
            offers.add(createOffer(level, Items.DIAMOND_HOE, 13, 3, 30, 0.2F, maxEnchantLevel));
        }
    }

    private static int getMaxEnchantLevel(int ruleValue) {
        if (ruleValue >= 4) {
            return 65;
        }
        if (ruleValue == 3) {
            return 33;
        }
        if (ruleValue == 2) {
            return 25;
        }
        return 19;
    }

    private static boolean isUpgradeableToolOffer(ItemStack result) {
        if (!result.isEnchanted()) {
            return false;
        }

        Item item = result.getItem();
        return item == Items.IRON_AXE
            || item == Items.IRON_PICKAXE
            || item == Items.IRON_SHOVEL
            || item == Items.DIAMOND_AXE
            || item == Items.DIAMOND_PICKAXE
            || item == Items.DIAMOND_SHOVEL
            || item == Items.DIAMOND_HOE;
    }

    @SuppressWarnings("null")
    private static MerchantOffer copyOffer(MerchantOffer original, ItemStack result) {
        return new MerchantOffer(
            original.getItemCostA(),
            original.getItemCostB(),
            result,
            original.getMaxUses(),
            original.getXp(),
            original.getPriceMultiplier()
        );
    }

    @SuppressWarnings("null")
    private static MerchantOffer createOffer(
        ServerLevel level,
        Item item,
        int emeraldCost,
        int maxUses,
        int villagerXp,
        float priceMultiplier,
        int maxEnchantLevel
    ) {
        return new MerchantOffer(
            new ItemCost(Items.EMERALD, emeraldCost),
            Optional.empty(),
            createEnchantedTool(level, item, maxEnchantLevel),
            maxUses,
            villagerXp,
            priceMultiplier
        );
    }

    @SuppressWarnings("null")
    private static ItemStack createEnchantedTool(ServerLevel level, Item item, int maxEnchantLevel) {
        RandomSource random = RandomSource.create();
        int enchantmentLevel = 5 + random.nextInt(maxEnchantLevel - 5 + 1);
        Stream<Holder<Enchantment>> enchantments = level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .map(entry -> (Holder<Enchantment>) entry);
        return EnchantmentHelper.enchantItem(random, new ItemStack(item), enchantmentLevel, enchantments);
    }
}

package org.edtp.carpet_edtp_addition.mixin;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class SellEnchantedToolFactoryMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void adjustToolsmithTrades(ServerLevel world, CallbackInfo ci) {
        int ruleValue = CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value();
        if (ruleValue <= 0) {
            return;
        }

        Villager self = (Villager) (Object) this;
        VillagerData villagerData = self.getVillagerData();
        if (!villagerData.profession().is(VillagerProfession.TOOLSMITH)) {
            return;
        }

        MerchantOffers offers = self.getOffers();
        if (ruleValue >= 2) {
            int maxEnchantLevel = edtp$getMaxEnchantLevel(ruleValue);
            for (int i = 0; i < offers.size(); i++) {
                MerchantOffer offer = offers.get(i);
                if (edtp$isUpgradeableToolOffer(offer.getResult())) {
                    ItemStack rerolled = edtp$createEnchantedTool(world, offer.getResult().getItem(), maxEnchantLevel);
                    offers.set(i, edtp$copyOffer(offer, rerolled));
                }
            }
        }

        if (villagerData.level() >= 5 && !edtp$hasEnchantedDiamondHoe(offers)) {
            int maxEnchantLevel = edtp$getMaxEnchantLevel(ruleValue);
            offers.add(edtp$createOffer(world, Items.DIAMOND_HOE, 13, 3, 30, 0.2F, maxEnchantLevel));
        }
    }

    @Unique
    private static int edtp$getMaxEnchantLevel(int ruleValue) {
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

    @Unique
    private static boolean edtp$hasEnchantedDiamondHoe(MerchantOffers offers) {
        for (MerchantOffer offer : offers) {
            ItemStack result = offer.getResult();
            if (result.getItem() == Items.DIAMOND_HOE && result.isEnchanted()) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean edtp$isUpgradeableToolOffer(ItemStack result) {
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
    @Unique
    private static MerchantOffer edtp$copyOffer(MerchantOffer original, ItemStack result) {
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
    @Unique
    private static MerchantOffer edtp$createOffer(
        ServerLevel world,
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
            edtp$createEnchantedTool(world, item, maxEnchantLevel),
            maxUses,
            villagerXp,
            priceMultiplier
        );
    }

    @SuppressWarnings("null")
    @Unique
    private static ItemStack edtp$createEnchantedTool(ServerLevel world, Item item, int maxEnchantLevel) {
        RandomSource random = RandomSource.create();
        int level = 5 + random.nextInt(maxEnchantLevel - 5 + 1);
        Stream<Holder<Enchantment>> enchantments = world.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .map(entry -> (Holder<Enchantment>) entry);
        return EnchantmentHelper.enchantItem(random, new ItemStack(item), level, enchantments);
    }
}

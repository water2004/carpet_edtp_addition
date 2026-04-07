package org.edtp.carpet_edtp_addition.mixin;

import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

@Mixin(VillagerTrades.EnchantedItemForEmeralds.class)
public class SellEnchantedToolFactoryMixin {

    @Shadow @Final private ItemStack itemStack;
    @Shadow @Final private int baseEmeraldCost;
    @Shadow @Final private int maxUses;
    @Shadow @Final private int villagerXp;
    @Shadow @Final private float priceMultiplier;

    /**
     * @author CarpetEdtpAddition
     * @reason Modify villager max enchant level
     */
    @Overwrite
    public MerchantOffer getOffer(ServerLevel world, Entity entity, RandomSource random) {
        int ruleValue = CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value();
        
        int level = 5 + random.nextInt(15);
        
        if (ruleValue >= 2) {
            int newMax = 25;
            if (ruleValue == 3) newMax = 33;
            if (ruleValue == 4) newMax = 65;
            level = 5 + random.nextInt(newMax - 5 + 1);
        }

        ItemStack itemStack = this.itemStack.copy();
        
        // Use all available enchantments from the registry
        Stream<Holder<Enchantment>> stream = world.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .map(entry -> (Holder<Enchantment>) entry);

        // Apply enchantment
        itemStack = EnchantmentHelper.enchantItem(random, itemStack, level, stream);

        return new MerchantOffer(new ItemCost(Items.EMERALD, this.baseEmeraldCost), Optional.empty(), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
    }
}

package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(TradeOffers.SellEnchantedToolFactory.class)
public class SellEnchantedToolFactoryMixin {

    @Shadow @Final private ItemStack tool;
    @Shadow @Final private int basePrice;
    @Shadow @Final private int maxUses;
    @Shadow @Final private int experience;
    @Shadow @Final private float multiplier;

    /**
     * @author CarpetEdtpAddition
     * @reason Modify villager max enchant level
     */
    @Overwrite
    public TradeOffer create(ServerWorld world, Entity entity, Random random) {
        int ruleValue = CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.value();
        
        int level = 5 + random.nextInt(15);
        
        if (ruleValue >= 2) {
            int newMax = 25;
            if (ruleValue == 3) newMax = 33;
            if (ruleValue == 4) newMax = 65;
            level = 5 + random.nextInt(newMax - 5 + 1);
        }

        ItemStack itemStack = this.tool.copy();
        
        // Use all available enchantments from the registry
        Stream<RegistryEntry<Enchantment>> stream = world.getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT)
            .streamEntries()
            .map(entry -> (RegistryEntry<Enchantment>) entry);

        // Apply enchantment
        itemStack = EnchantmentHelper.enchant(random, itemStack, level, stream);

        return new TradeOffer(new TradedItem(Items.EMERALD, this.basePrice), Optional.empty(), itemStack, this.maxUses, this.experience, this.multiplier);
    }
}

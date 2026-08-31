package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.Blocks;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public class ZombieMixin {
    @ModifyExpressionValue(
        method = "finalizeSpawn",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/zombie/Zombie$ZombieGroupData;canSpawnJockey:Z"
        )
    )
    private boolean preventChickenJockeyOnMagmaBlock(boolean canSpawnJockey) {
        Zombie zombie = (Zombie) (Object) this;
        if (CarpetEdtpAdditionSettings.noChickenJockeysOnMagmaBlocks.value()
                && zombie.level().getBlockState(zombie.blockPosition().below()).is(Blocks.MAGMA_BLOCK)) {
            return false;
        }
        return canSpawnJockey;
    }
}

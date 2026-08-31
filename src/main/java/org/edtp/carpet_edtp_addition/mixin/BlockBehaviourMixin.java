package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @Redirect(
        method = "onExplosionHit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;dropFromExplosion(Lnet/minecraft/world/level/Explosion;)Z")
    )
    private boolean redirectDropFromExplosion(Block block, @NonNull Explosion explosion) {
        if (CarpetEdtpAdditionSettings.tntBreaksWithoutDrops.value()
            && explosion.getDirectSourceEntity() instanceof PrimedTnt) {
            return false;
        }
        return block.dropFromExplosion(explosion);
    }
}

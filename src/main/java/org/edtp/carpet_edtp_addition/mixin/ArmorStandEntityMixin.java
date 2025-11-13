package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(ArmorStandEntity.class)
public class ArmorStandEntityMixin {
    
    /**
     * 当 toughArmorStands 规则开启时,修改条件判断中的常量 5L
     * 将其改为 Long.MIN_VALUE,使 (l - this.lastHitTime > 5L) 始终为 true
     * 这样盔甲架只会播放击中音效,永远不会进入破坏逻辑
     */
    @ModifyConstant(
        method = "damage",
        constant = @Constant(longValue = 5L)
    )
    private long modifyTimeThreshold(long original) {
        if (CarpetEdtpAdditionSettings.toughArmorStands.value()) {
            return Long.MIN_VALUE; // 使条件始终为真
        }
        return original;
    }
}

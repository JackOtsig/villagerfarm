package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.ModGamerules;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @ModifyArg(
            method = "tickMovement()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private GameRule<?> villagerfarm$swapPickupRule(GameRule<?> original) {
        if (((Object) this) instanceof VillagerEntity && original == GameRules.DO_MOB_GRIEFING) {
            return ModGamerules.VILLAGER_FARMING;
        }
        return original;
    }
}

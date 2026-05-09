package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    /**
     * When a villager pickup tick consults {@code mob_griefing}, bypass the
     * check (return true) when {@code features.gamerule_split} is on. Only
     * applies to villagers; other mobs continue to follow the gamerule.
     */
    @Redirect(
            method = "tickMovement()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private Object villagerfarm$bypassPickupRule(GameRules rules, GameRule<?> rule) {
        if (((Object) this) instanceof VillagerEntity
                && rule == GameRules.DO_MOB_GRIEFING
                && VillagerFarmConfig.INSTANCE.features.gamerule_split) {
            return Boolean.TRUE;
        }
        return rules.getValue(rule);
    }
}

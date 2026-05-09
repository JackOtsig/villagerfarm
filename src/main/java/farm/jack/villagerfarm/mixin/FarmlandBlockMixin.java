package farm.jack.villagerfarm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skip the farmland-to-dirt conversion when a farmer-profession villager lands
 * on it. Other villagers and entities still trample as normal. Gated on
 * {@code features.anti_trample}.
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @WrapOperation(method = "onLandedUpon",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void villagerfarm$skipFarmerTrample(Entity entity, BlockState state, World world, BlockPos pos,
                                                Operation<Void> original) {
        if (VillagerFarmConfig.INSTANCE.features.anti_trample
                && entity instanceof VillagerEntity villager
                && villager.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) {
            return;
        }
        original.call(entity, state, world, pos);
    }
}

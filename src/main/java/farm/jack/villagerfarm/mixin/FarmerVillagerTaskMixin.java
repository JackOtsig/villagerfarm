package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.ExtendedCropHarvest;
import farm.jack.villagerfarm.VillagerFarmHelper;
import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.FarmerVillagerTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FarmerVillagerTask.class)
public abstract class FarmerVillagerTaskMixin {

    @Shadow @Nullable private BlockPos currentTarget;
    @Shadow private long nextResponseTime;
    @Shadow @Final private List<BlockPos> targetPositions;
    @Shadow @Nullable private BlockPos chooseRandomTarget(ServerWorld world) { throw new AssertionError(); }

    /**
     * When {@code features.gamerule_split} is on, bypass vanilla's
     * {@code mob_griefing} check inside {@code shouldRun} and always allow the
     * task to start. When off, defer to vanilla.
     */
    @Redirect(
            method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private Object villagerfarm$bypassStartRule(GameRules rules, GameRule<?> rule) {
        if (rule == GameRules.DO_MOB_GRIEFING && VillagerFarmConfig.INSTANCE.features.gamerule_split) {
            return Boolean.TRUE;
        }
        return rules.getValue(rule);
    }

    /**
     * Replace vanilla harvest-then-replant for wheat/beetroot/potato/carrot
     * with our atomic version. When {@code features.atomic_harvest_replant} is
     * off, fall back to vanilla.
     */
    @Redirect(
            method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"))
    private boolean villagerfarm$breakAndReplant(ServerWorld world, BlockPos pos, boolean drop, Entity breaker) {
        if (!VillagerFarmConfig.INSTANCE.features.atomic_harvest_replant) {
            return world.breakBlock(pos, drop, breaker);
        }
        return VillagerFarmHelper.breakAndReplant(world, pos, breaker);
    }

    /** Make sugar cane / nether wart / pumpkin / melon / cocoa selectable as targets. */
    @Inject(method = "isSuitableTarget(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/server/world/ServerWorld;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void villagerfarm$expandTargets(BlockPos pos, ServerWorld world,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!VillagerFarmConfig.INSTANCE.features.extended_crops.enabled) return;
        BlockState state = world.getBlockState(pos);
        if (ExtendedCropHarvest.isHarvestableExtended(state, world, pos)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Vanilla's 3×3×3 scan only fires during a brain re-evaluation. Cane / wart
     * / cocoa / pumpkin / melon setups frequently sit just outside that box, so
     * we extend it by {@code values.search.extended_radius_xyz} (default ±2) for
     * extended-crop positions only.
     */
    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/FarmerVillagerTask;chooseRandomTarget(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/util/math/BlockPos;",
                    shift = At.Shift.BEFORE))
    private void villagerfarm$expandedScan(ServerWorld world, VillagerEntity villager,
                                           CallbackInfoReturnable<Boolean> cir) {
        VillagerFarmConfig cfg = VillagerFarmConfig.INSTANCE;
        if (!cfg.features.extended_crops.enabled) return;
        int radius = Math.max(1, cfg.values.search.extended_radius_xyz);
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        int vx = villager.getBlockX();
        int vy = villager.getBlockY();
        int vz = villager.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) continue;
                    cursor.set(vx + dx, vy + dy, vz + dz);
                    BlockState state = world.getBlockState(cursor);
                    if (ExtendedCropHarvest.isHarvestableExtended(state, world, cursor)) {
                        this.targetPositions.add(cursor.toImmutable());
                    }
                }
            }
        }
    }

    /**
     * Harvest extended crops with a configurable wider reach (default
     * {@code 4.0}, sq=16). Also unstucks vanilla CropBlock targets in
     * {@code [unstuck_min, unstuck_max)} squared distance so the brain doesn't
     * lock onto an unreachable wheat block.
     */
    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At("HEAD"),
            cancellable = true)
    private void villagerfarm$tryExtendedHarvest(ServerWorld world, VillagerEntity villager, long time,
                                                 CallbackInfo ci) {
        BlockPos target = this.currentTarget;
        if (target == null) return;
        if (time <= this.nextResponseTime) return;

        VillagerFarmConfig cfg = VillagerFarmConfig.INSTANCE;
        double distSq = target.getSquaredDistance(villager.getEntityPos());
        BlockState state = world.getBlockState(target);

        if (cfg.features.extended_crops.enabled
                && ExtendedCropHarvest.isHarvestableExtended(state, world, target)) {
            if (distSq >= cfg.values.search.extended_harvest_distance_squared) return;
            if (ExtendedCropHarvest.tryHarvest(world, target, state, villager)) {
                this.targetPositions.remove(target);
                this.currentTarget = chooseRandomTarget(world);
                this.nextResponseTime = time + 20L;
                ci.cancel();
            }
            return;
        }

        if (cfg.features.atomic_harvest_replant
                && distSq >= cfg.values.search.unstuck_min_distance_squared
                && distSq < cfg.values.search.unstuck_max_distance_squared
                && state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
            VillagerFarmHelper.breakAndReplant(world, target, villager);
            this.targetPositions.remove(target);
            this.currentTarget = chooseRandomTarget(world);
            this.nextResponseTime = time + 20L;
            ci.cancel();
        }
    }
}

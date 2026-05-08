package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.ExtendedCropHarvest;
import farm.jack.villagerfarm.ModGamerules;
import farm.jack.villagerfarm.VillagerFarmHelper;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    /** Swap mob_griefing for villager_farming when the task gates its start. */
    @ModifyArg(
            method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private GameRule<?> villagerfarm$swapStartRule(GameRule<?> original) {
        return original == GameRules.DO_MOB_GRIEFING ? ModGamerules.VILLAGER_FARMING : original;
    }

    /** Replace vanilla harvest-then-replant for wheat/beetroot/potato/carrot with our atomic version. */
    @Redirect(
            method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"))
    private boolean villagerfarm$breakAndReplant(ServerWorld world, BlockPos pos, boolean drop, Entity breaker) {
        return VillagerFarmHelper.breakAndReplant(world, pos, breaker);
    }

    /** Make sugar cane / nether wart / pumpkin / melon / cocoa selectable as targets. */
    @Inject(method = "isSuitableTarget(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/server/world/ServerWorld;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void villagerfarm$expandTargets(BlockPos pos, ServerWorld world,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!world.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING)) return;
        BlockState state = world.getBlockState(pos);
        if (ExtendedCropHarvest.isHarvestableExtended(state, world, pos)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Vanilla's 3×3×3 scan only fires during a brain re-evaluation, and only
     * looks at blocks within ±1 of the villager. Cane/wart/cocoa/pumpkin/melon
     * setups frequently sit just outside that box (e.g. cocoa stacked vertically
     * on a log, or a wart farm a couple blocks from the composter). Extend the
     * scan to ±2 for extended-crop positions only — we add the new candidates
     * just before vanilla picks a random target, so the picker considers them.
     */
    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/FarmerVillagerTask;chooseRandomTarget(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/util/math/BlockPos;",
                    shift = At.Shift.BEFORE))
    private void villagerfarm$expandedScan(ServerWorld world, VillagerEntity villager,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!world.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING)) return;
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        int vx = villager.getBlockX();
        int vy = villager.getBlockY();
        int vz = villager.getBlockZ();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
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
     * Handle the harvest for extended crops; vanilla's CropBlock branch will
     * skip them. We use a wider 4.0 reach (vs vanilla's strict 1.0) since the
     * ±2 scan can pick up corner targets at √12 ≈ 3.46. The unstuck-fallback
     * also catches mature CropBlocks the villager can't reach within vanilla's
     * tight 1.0² so the brain doesn't lock onto an unreachable wheat block.
     */
    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At("HEAD"),
            cancellable = true)
    private void villagerfarm$tryExtendedHarvest(ServerWorld world, VillagerEntity villager, long time,
                                                 CallbackInfo ci) {
        BlockPos target = this.currentTarget;
        if (target == null) return;
        if (time <= this.nextResponseTime) return;

        double distSq = target.getSquaredDistance(villager.getEntityPos());
        BlockState state = world.getBlockState(target);
        boolean extendedOn = world.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING);

        if (extendedOn && ExtendedCropHarvest.isHarvestableExtended(state, world, target)) {
            if (distSq >= 16.0) return;
            if (ExtendedCropHarvest.tryHarvest(world, target, state, villager)) {
                this.targetPositions.remove(target);
                this.currentTarget = chooseRandomTarget(world);
                this.nextResponseTime = time + 20L;
                ci.cancel();
            }
            return;
        }

        if (distSq >= 1.0 && distSq < 6.25
                && state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
            VillagerFarmHelper.breakAndReplant(world, target, villager);
            this.targetPositions.remove(target);
            this.currentTarget = chooseRandomTarget(world);
            this.nextResponseTime = time + 20L;
            ci.cancel();
        }
    }
}

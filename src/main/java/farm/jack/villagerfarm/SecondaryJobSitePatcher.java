package farm.jack.villagerfarm;

import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;

import java.util.List;

/**
 * Bridges the gap that prevents farmers from running FarmerVillagerTask near
 * non-farmland crops. Vanilla's brain requires {@code SECONDARY_JOB_SITE} to be
 * populated for shouldRun to even be called — and only farmland blocks count
 * for that. Farmers next to soul-sand wart farms or jungle-log cocoa farms have
 * an empty SECONDARY_JOB_SITE forever, so vanilla never gates them in.
 *
 * <p>Periodically scans around each farmer villager and plants a fake
 * SECONDARY_JOB_SITE pointing at any of our extended-crop substrates so the
 * brain unblocks the task. Search radii and tick interval come from config.
 */
public final class SecondaryJobSitePatcher {
    private SecondaryJobSitePatcher() {}

    public static void install() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            VillagerFarmConfig cfg = VillagerFarmConfig.INSTANCE;
            if (!cfg.features.secondary_job_site_patcher) return;
            int interval = Math.max(1, cfg.values.search.secondary_job_site_tick_interval);
            if (server.getTicks() % interval != 0) return;
            int radiusXz = cfg.values.search.secondary_job_site_radius_xz;
            int radiusY = cfg.values.search.secondary_job_site_radius_y;
            for (ServerWorld world : server.getWorlds()) {
                for (Entity e : world.iterateEntities()) {
                    if (!(e instanceof VillagerEntity v)) continue;
                    if (!v.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) continue;
                    Brain<VillagerEntity> brain = v.getBrain();
                    if (brain.getOptionalRegisteredMemory(MemoryModuleType.SECONDARY_JOB_SITE)
                            .filter(list -> !list.isEmpty()).isPresent()) {
                        continue;
                    }
                    BlockPos found = findExtendedSubstrate(world, v.getBlockPos(), radiusXz, radiusY);
                    if (found != null) {
                        brain.remember(MemoryModuleType.SECONDARY_JOB_SITE,
                                List.of(GlobalPos.create(world.getRegistryKey(), found)));
                    }
                }
            }
        });
    }

    private static BlockPos findExtendedSubstrate(ServerWorld world, BlockPos center, int radiusXz, int radiusY) {
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        for (int dx = -radiusXz; dx <= radiusXz; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusXz; dz <= radiusXz; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState s = world.getBlockState(cursor);
                    if (s.isOf(Blocks.SOUL_SAND)
                            || s.isOf(Blocks.JUNGLE_LOG)
                            || s.isOf(Blocks.SUGAR_CANE)
                            || s.isOf(Blocks.NETHER_WART)
                            || s.isOf(Blocks.COCOA)
                            || s.isOf(Blocks.PUMPKIN_STEM)
                            || s.isOf(Blocks.MELON_STEM)
                            || s.isOf(Blocks.ATTACHED_PUMPKIN_STEM)
                            || s.isOf(Blocks.ATTACHED_MELON_STEM)) {
                        return cursor.toImmutable();
                    }
                }
            }
        }
        return null;
    }
}

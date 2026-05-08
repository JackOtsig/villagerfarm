package farm.jack.villagerfarm;

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
 * <p>Every 40 ticks, this scans ±4 around each farmer villager. If no
 * SECONDARY_JOB_SITE is set and we find one of our extended-crop substrates,
 * we plant a fake one so the brain unblocks the task.
 */
public final class SecondaryJobSitePatcher {
    private SecondaryJobSitePatcher() {}

    public static void install() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 40 != 0) return;
            for (ServerWorld world : server.getWorlds()) {
                if (!world.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING)) continue;
                for (Entity e : world.iterateEntities()) {
                    if (!(e instanceof VillagerEntity v)) continue;
                    if (!v.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) continue;
                    Brain<VillagerEntity> brain = v.getBrain();
                    if (brain.getOptionalRegisteredMemory(MemoryModuleType.SECONDARY_JOB_SITE)
                            .filter(list -> !list.isEmpty()).isPresent()) {
                        continue;  // vanilla already populated it
                    }
                    BlockPos found = findExtendedSubstrate(world, v.getBlockPos());
                    if (found != null) {
                        brain.remember(MemoryModuleType.SECONDARY_JOB_SITE,
                                List.of(GlobalPos.create(world.getRegistryKey(), found)));
                    }
                }
            }
        });
    }

    private static BlockPos findExtendedSubstrate(ServerWorld world, BlockPos center) {
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
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

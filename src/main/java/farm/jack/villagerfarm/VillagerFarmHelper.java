package farm.jack.villagerfarm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public final class VillagerFarmHelper {
    private VillagerFarmHelper() {}

    /**
     * Replaces vanilla's `world.breakBlock(pos, true, breaker)` for a farmer
     * villager with an atomic harvest-and-replant: emit the same loot the vanilla
     * break would have, minus one seed-equivalent (the one going back into the
     * ground), and place the crop's age-0 state in the same tick.
     *
     * Returns true when this short-circuit handled the break; if the target isn't
     * a mature crop we fall back to vanilla `breakBlock`.
     */
    public static boolean breakAndReplant(ServerWorld world, BlockPos pos, Entity breaker) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof CropBlock crop) || !crop.isMature(state)) {
            return world.breakBlock(pos, true, breaker);
        }

        ItemStack seedStack = state.getPickStack(world, pos, false);

        LootWorldContext.Builder lootCtx = new LootWorldContext.Builder(world)
                .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                .add(LootContextParameters.TOOL, ItemStack.EMPTY)
                .addOptional(LootContextParameters.THIS_ENTITY, breaker)
                .addOptional(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(pos));
        List<ItemStack> drops = state.getDroppedStacks(lootCtx);

        if (!seedStack.isEmpty()) {
            for (ItemStack drop : drops) {
                if (drop.isOf(seedStack.getItem())) {
                    drop.decrement(1);
                    break;
                }
            }
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.dropStack(world, pos, drop);
            }
        }

        world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_ALL);

        world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        world.emitGameEvent(breaker, GameEvent.BLOCK_DESTROY, pos);
        return true;
    }
}

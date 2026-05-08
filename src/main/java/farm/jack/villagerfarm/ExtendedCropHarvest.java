package farm.jack.villagerfarm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import origin.jack.blockorigin.api.BlockCause;
import origin.jack.blockorigin.api.BlockOrigin;

import java.util.List;

/**
 * Detection + harvest for the five extended crops villagers don't natively farm:
 * sugar cane, nether wart, pumpkin, melon, cocoa.
 *
 * <p>Pumpkin and melon use blockorigin's {@link BlockCause#RANDOM_TICK_GROW} stamp
 * to distinguish stem-grown fruit (harvestable) from player-placed decorations
 * (skipped). The other three are assumed planted-for-farming whenever they're
 * in a mature state.
 */
public final class ExtendedCropHarvest {
    private ExtendedCropHarvest() {}

    /** True when a villager should select this position as a farm target. */
    public static boolean isHarvestableExtended(BlockState state, ServerWorld world, BlockPos pos) {
        if (state.isOf(Blocks.SUGAR_CANE)) {
            // Target only stalks that have another stalk BELOW them. Walking
            // upward from such a stalk and breaking everything always leaves
            // the bottom stalk in place to regrow.
            return world.getBlockState(pos.down()).isOf(Blocks.SUGAR_CANE);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            return state.get(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;
        }
        if (state.isOf(Blocks.PUMPKIN) || state.isOf(Blocks.MELON)) {
            return BlockOrigin.get(world, pos) == BlockCause.RANDOM_TICK_GROW;
        }
        return false;
    }

    /** Returns true if this method handled the harvest. Caller falls back otherwise. */
    public static boolean tryHarvest(ServerWorld world, BlockPos pos, BlockState state, Entity breaker) {
        if (state.isOf(Blocks.SUGAR_CANE)) {
            harvestSugarCane(world, pos, state, breaker);
            return true;
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            harvestWithReplant(world, pos, state, breaker, Blocks.NETHER_WART.getDefaultState());
            return true;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            BlockState replant = Blocks.COCOA.getDefaultState().with(CocoaBlock.FACING, state.get(CocoaBlock.FACING));
            harvestWithReplant(world, pos, state, breaker, replant);
            return true;
        }
        if (state.isOf(Blocks.PUMPKIN) || state.isOf(Blocks.MELON)) {
            harvestGourd(world, pos, state, breaker);
            return true;
        }
        return false;
    }

    private static void harvestSugarCane(ServerWorld world, BlockPos pos, BlockState state, Entity breaker) {
        BlockPos.Mutable cursor = pos.mutableCopy();
        BlockState top = state;
        while (world.getBlockState(cursor).isOf(Blocks.SUGAR_CANE)) {
            top = world.getBlockState(cursor);
            dropCropLoot(world, cursor.toImmutable(), top, breaker, null);
            world.setBlockState(cursor, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            cursor.move(Direction.UP);
        }
        emitBreakFx(world, pos, top, breaker);
    }

    private static void harvestWithReplant(ServerWorld world, BlockPos pos, BlockState state, Entity breaker,
                                           BlockState replant) {
        ItemStack seedStack = state.getPickStack(world, pos, false);
        dropCropLoot(world, pos, state, breaker, seedStack);
        world.setBlockState(pos, replant, Block.NOTIFY_ALL);
        emitBreakFx(world, pos, state, breaker);
    }

    private static void harvestGourd(ServerWorld world, BlockPos pos, BlockState state, Entity breaker) {
        dropCropLoot(world, pos, state, breaker, null);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        emitBreakFx(world, pos, state, breaker);
    }

    /**
     * Roll the crop's loot table and drop the items, optionally consuming one of
     * {@code consumeOne}'s item to represent the seed left in the ground.
     */
    private static void dropCropLoot(ServerWorld world, BlockPos pos, BlockState state, Entity breaker,
                                     ItemStack consumeOne) {
        LootWorldContext.Builder ctx = new LootWorldContext.Builder(world)
                .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                .add(LootContextParameters.TOOL, ItemStack.EMPTY)
                .addOptional(LootContextParameters.THIS_ENTITY, breaker)
                .addOptional(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(pos));
        List<ItemStack> drops = state.getDroppedStacks(ctx);

        if (consumeOne != null && !consumeOne.isEmpty()) {
            for (ItemStack drop : drops) {
                if (drop.isOf(consumeOne.getItem())) {
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
    }

    private static void emitBreakFx(ServerWorld world, BlockPos pos, BlockState state, Entity breaker) {
        world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        world.emitGameEvent(breaker, GameEvent.BLOCK_DESTROY, pos);
    }
}

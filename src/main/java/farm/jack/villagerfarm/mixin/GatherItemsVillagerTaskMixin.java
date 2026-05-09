package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GatherItemsVillagerTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Custom share path for the new crops. Vanilla's {@code giveHalfOfStack}
 * silently no-ops unless a slot has {@code count > 24} or {@code count >
 * maxCount/2} — too high a bar for low-yield crops villagers eat down to keep
 * their food level. We toss directly via {@link TargetUtil#give} as soon as a
 * stack has at least {@code values.sharing.min_stack_size_to_toss} items, so
 * the new crops actually circulate.
 *
 * <p>Each item type is independently toggleable via
 * {@code features.food_sharing}. Wart still goes only farmer→cleric (when
 * {@code share_nether_wart_to_cleric} is on).
 */
@Mixin(GatherItemsVillagerTask.class)
public abstract class GatherItemsVillagerTaskMixin {

    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At("RETURN"))
    private void villagerfarm$shareNewCrops(ServerWorld world, VillagerEntity self, long time, CallbackInfo ci) {
        VillagerFarmConfig cfg = VillagerFarmConfig.INSTANCE;
        if (!cfg.features.food_sharing.enabled) return;
        if (!self.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) return;

        Optional<VillagerEntity> targetOpt = self.getBrain()
                .getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET)
                .filter(t -> t instanceof VillagerEntity)
                .map(t -> (VillagerEntity) t);
        if (targetOpt.isEmpty()) return;
        VillagerEntity target = targetOpt.get();
        if (self.squaredDistanceTo(target) > cfg.values.sharing.max_share_distance_squared) return;

        int minToToss = Math.max(2, cfg.values.sharing.min_stack_size_to_toss);

        if (cfg.features.food_sharing.share_nether_wart_to_cleric
                && target.getVillagerData().profession().matchesKey(VillagerProfession.CLERIC)
                && tossHalf(self, Items.NETHER_WART, target, minToToss)) {
            return;
        }

        List<Item> pool = new ArrayList<>(4);
        if (cfg.features.food_sharing.share_pumpkin) pool.add(Items.PUMPKIN);
        if (cfg.features.food_sharing.share_melon_slice) pool.add(Items.MELON_SLICE);
        if (cfg.features.food_sharing.share_sugar_cane) pool.add(Items.SUGAR_CANE);
        if (cfg.features.food_sharing.share_cocoa_beans) pool.add(Items.COCOA_BEANS);
        for (Item item : pool) {
            if (tossHalf(self, item, target, minToToss)) return;
        }
    }

    private static boolean tossHalf(VillagerEntity self, Item item, LivingEntity target, int minToToss) {
        SimpleInventory inv = self.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item) && s.getCount() >= minToToss) {
                int n = s.getCount() / 2;
                ItemStack thrown = new ItemStack(item, n);
                s.decrement(n);
                TargetUtil.give(self, thrown, target.getEntityPos());
                return true;
            }
        }
        return false;
    }
}

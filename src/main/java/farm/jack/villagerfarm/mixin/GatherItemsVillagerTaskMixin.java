package farm.jack.villagerfarm.mixin;

import farm.jack.villagerfarm.ModGamerules;
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

import java.util.List;
import java.util.Optional;

/**
 * Custom share path for the new crops. Vanilla's {@code giveHalfOfStack}
 * silently no-ops unless a slot has {@code count > 24} or {@code count >
 * maxCount/2} — too high a bar for low-yield crops villagers eat down to keep
 * their food level. We toss directly via {@link TargetUtil#give} as soon as a
 * stack has at least 2 items so the new crops actually circulate.
 *
 * <p>Pumpkin, melon slices, sugar cane, and cocoa beans are tossed by any
 * farmer to any meeting partner. Nether wart is restricted to farmer→cleric.
 */
@Mixin(GatherItemsVillagerTask.class)
public abstract class GatherItemsVillagerTaskMixin {

    private static final List<Item> SHARE_ANY_TARGET = List.of(
            Items.PUMPKIN, Items.MELON_SLICE, Items.SUGAR_CANE, Items.COCOA_BEANS);

    @Inject(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;J)V",
            at = @At("RETURN"))
    private void villagerfarm$shareNewCrops(ServerWorld world, VillagerEntity self, long time, CallbackInfo ci) {
        if (!world.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING)) return;
        if (!self.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) return;

        Optional<VillagerEntity> targetOpt = self.getBrain()
                .getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET)
                .filter(t -> t instanceof VillagerEntity)
                .map(t -> (VillagerEntity) t);
        if (targetOpt.isEmpty()) return;
        VillagerEntity target = targetOpt.get();
        if (self.squaredDistanceTo(target) > 5.0) return;

        // Wart only goes to clerics.
        if (target.getVillagerData().profession().matchesKey(VillagerProfession.CLERIC)
                && tossHalf(self, Items.NETHER_WART, target)) {
            return;
        }
        // Pumpkin / melon / cane / cocoa go to any meeting villager.
        for (Item item : SHARE_ANY_TARGET) {
            if (tossHalf(self, item, target)) return;
        }
    }

    private static boolean tossHalf(VillagerEntity self, Item item, LivingEntity target) {
        SimpleInventory inv = self.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item) && s.getCount() >= 2) {
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

package farm.jack.villagerfarm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import farm.jack.villagerfarm.PickupEffects;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityLootMixin {

    /**
     * Wrap the villager's pickup hook so we can measure how many of the item
     * actually ended up in the inventory and reward sugar cane / cocoa / wart
     * accordingly.
     */
    @WrapMethod(method = "loot(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/ItemEntity;)V")
    private void villagerfarm$wrapLoot(ServerWorld world, ItemEntity entity, Operation<Void> original) {
        Item item = entity.getStack().getItem();
        int before = entity.getStack().getCount();
        original.call(world, entity);
        int after = entity.isRemoved() ? 0 : entity.getStack().getCount();
        int consumed = before - after;
        if (consumed > 0) {
            PickupEffects.onVillagerPickup((VillagerEntity) (Object) this, item, consumed);
        }
    }
}

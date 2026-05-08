package farm.jack.villagerfarm.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(VillagerEntity.class)
public interface VillagerFoodAccessor {

    @Accessor("ITEM_FOOD_VALUES")
    static Map<Item, Integer> villagerfarm$getItemFoodValues() { throw new AssertionError(); }

    @Accessor("ITEM_FOOD_VALUES")
    @Mutable
    static void villagerfarm$setItemFoodValues(Map<Item, Integer> values) { throw new AssertionError(); }
}

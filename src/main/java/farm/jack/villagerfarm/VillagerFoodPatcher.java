package farm.jack.villagerfarm;

import com.google.common.collect.ImmutableMap;
import farm.jack.villagerfarm.mixin.VillagerFoodAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Map;

/**
 * Replaces vanilla's {@code VillagerEntity.ITEM_FOOD_VALUES} with one that also
 * recognizes pumpkin, melon slices, sugar cane, and cocoa beans as villager food,
 * so the new crops contribute to {@code isReadyToBreed}, {@code canShareFoodForBreeding},
 * and the food-share branch of {@code GatherItemsVillagerTask}.
 */
public final class VillagerFoodPatcher {
    private VillagerFoodPatcher() {}

    public static void install() {
        Map<Item, Integer> existing = VillagerFoodAccessor.villagerfarm$getItemFoodValues();
        ImmutableMap.Builder<Item, Integer> b = ImmutableMap.builder();
        b.putAll(existing);
        b.put(Items.PUMPKIN, 4);
        b.put(Items.MELON_SLICE, 1);
        b.put(Items.SUGAR_CANE, 1);
        b.put(Items.COCOA_BEANS, 1);
        VillagerFoodAccessor.villagerfarm$setItemFoodValues(b.buildKeepingLast());
    }
}

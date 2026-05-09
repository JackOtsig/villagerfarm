package farm.jack.villagerfarm;

import com.google.common.collect.ImmutableMap;
import farm.jack.villagerfarm.config.VillagerFarmConfig;
import farm.jack.villagerfarm.mixin.VillagerFoodAccessor;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Replaces vanilla's {@code VillagerEntity.ITEM_FOOD_VALUES} with one that adds
 * the items configured in {@code values.food_values}. Defaults add pumpkin,
 * melon slice, sugar cane, and cocoa beans so the new crops contribute to
 * breeding readiness and the food-share branch.
 */
public final class VillagerFoodPatcher {
    private VillagerFoodPatcher() {}

    public static void install() {
        Map<Item, Integer> existing = VillagerFoodAccessor.villagerfarm$getItemFoodValues();
        ImmutableMap.Builder<Item, Integer> b = ImmutableMap.builder();
        b.putAll(existing);
        for (var entry : VillagerFarmConfig.INSTANCE.values.food_values.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null) continue;
            Item item = Registries.ITEM.get(id);
            if (item == null) continue;
            b.put(item, entry.getValue());
        }
        VillagerFoodAccessor.villagerfarm$setItemFoodValues(b.buildKeepingLast());
    }
}

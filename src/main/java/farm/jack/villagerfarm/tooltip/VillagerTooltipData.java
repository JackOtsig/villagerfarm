package farm.jack.villagerfarm.tooltip;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes the bits of villager state we want to display in tooltips —
 * activity key + non-empty inventory stacks — as raw NBT. Plain NBT keeps
 * WTHIT and Jade integrations using one shared codec.
 */
public final class VillagerTooltipData {
    public static final String KEY_ACTIVITY = "villagerfarm:activity";
    public static final String KEY_ITEMS = "villagerfarm:items";

    private VillagerTooltipData() {}

    public static void write(NbtCompound out, VillagerEntity villager, RegistryWrapper.WrapperLookup registries) {
        String key = VillagerActivityLabel.pickKey(villager.getBrain().getPossibleActivities());
        out.put(KEY_ACTIVITY, NbtString.of(key));

        SimpleInventory inv = villager.getInventory();
        NbtList items = new NbtList();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            NbtElement encoded = ItemStack.CODEC.encodeStart(registries.getOps(net.minecraft.nbt.NbtOps.INSTANCE), stack)
                    .result()
                    .orElse(null);
            if (encoded != null) items.add(encoded);
        }
        if (!items.isEmpty()) out.put(KEY_ITEMS, items);
    }

    public static String readActivity(NbtCompound in) {
        return in.getString(KEY_ACTIVITY).orElse("idle");
    }

    public static List<ItemStack> readItems(NbtCompound in, RegistryWrapper.WrapperLookup registries) {
        List<ItemStack> result = new ArrayList<>();
        NbtList list = in.getListOrEmpty(KEY_ITEMS);
        for (NbtElement el : list) {
            ItemStack.CODEC.parse(registries.getOps(net.minecraft.nbt.NbtOps.INSTANCE), el)
                    .result()
                    .ifPresent(result::add);
        }
        return result;
    }
}

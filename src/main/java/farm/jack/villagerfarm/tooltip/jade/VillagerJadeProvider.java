package farm.jack.villagerfarm.tooltip.jade;

import farm.jack.villagerfarm.VillagerFarmMod;
import farm.jack.villagerfarm.tooltip.VillagerActivityLabel;
import farm.jack.villagerfarm.tooltip.VillagerTooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.List;

public final class VillagerJadeProvider implements IEntityComponentProvider {
    public static final VillagerJadeProvider INSTANCE = new VillagerJadeProvider();
    private static final Identifier ID = Identifier.of(VillagerFarmMod.MOD_ID, "villager_tooltip");

    private VillagerJadeProvider() {}

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        NbtCompound data = accessor.getServerData();
        if (data == null || data.isEmpty()) return;

        Text activity = VillagerActivityLabel.labelOf(VillagerTooltipData.readActivity(data));
        tooltip.add(activity);

        List<ItemStack> items = VillagerTooltipData.readItems(data, accessor.getLevel().getRegistryManager());
        if (!items.isEmpty()) {
            tooltip.add(items.stream().map(ItemStackElement::of).toList());
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}

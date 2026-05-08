package farm.jack.villagerfarm.tooltip.wthit;

import farm.jack.villagerfarm.tooltip.VillagerActivityLabel;
import farm.jack.villagerfarm.tooltip.VillagerTooltipData;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.component.ItemListComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public final class VillagerWthitProvider implements IEntityComponentProvider {
    public static final VillagerWthitProvider INSTANCE = new VillagerWthitProvider();

    private VillagerWthitProvider() {}

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        var data = accessor.getData().raw();
        Text activity = VillagerActivityLabel.labelOf(VillagerTooltipData.readActivity(data));
        tooltip.addLine(activity);

        List<ItemStack> items = VillagerTooltipData.readItems(data, accessor.getWorld().getRegistryManager());
        if (!items.isEmpty()) {
            tooltip.addLine(new ItemListComponent(items));
        }
    }
}

package farm.jack.villagerfarm.tooltip.wthit;

import farm.jack.villagerfarm.tooltip.VillagerTooltipData;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import net.minecraft.entity.passive.VillagerEntity;

public final class VillagerWthitDataProvider implements IDataProvider<VillagerEntity> {
    public static final VillagerWthitDataProvider INSTANCE = new VillagerWthitDataProvider();

    private VillagerWthitDataProvider() {}

    @Override
    public void appendData(IDataWriter data, IServerAccessor<VillagerEntity> accessor, IPluginConfig config) {
        VillagerTooltipData.write(data.raw(), accessor.getTarget(), accessor.getLevel().getRegistryManager());
    }
}

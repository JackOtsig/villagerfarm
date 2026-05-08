package farm.jack.villagerfarm.tooltip.wthit;

import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.entity.passive.VillagerEntity;

public final class VillagerWthitCommonPlugin implements IWailaCommonPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.entityData(VillagerWthitDataProvider.INSTANCE, VillagerEntity.class);
    }
}

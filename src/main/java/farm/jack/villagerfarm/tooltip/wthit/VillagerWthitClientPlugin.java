package farm.jack.villagerfarm.tooltip.wthit;

import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.entity.passive.VillagerEntity;

public final class VillagerWthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(VillagerWthitProvider.INSTANCE, VillagerEntity.class);
    }
}

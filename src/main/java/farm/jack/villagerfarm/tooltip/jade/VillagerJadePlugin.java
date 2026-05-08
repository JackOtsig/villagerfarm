package farm.jack.villagerfarm.tooltip.jade;

import farm.jack.villagerfarm.VillagerFarmMod;
import net.minecraft.entity.passive.VillagerEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class VillagerJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(VillagerJadeDataProvider.INSTANCE, VillagerEntity.class);
        VillagerFarmMod.LOGGER.info("Villager Farm Jade integration registered (server)");
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(VillagerJadeProvider.INSTANCE, VillagerEntity.class);
        VillagerFarmMod.LOGGER.info("Villager Farm Jade integration registered (client)");
    }
}

package farm.jack.villagerfarm;

import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerFarmMod implements ModInitializer {
    public static final String MOD_ID = "villagerfarm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        VillagerFarmConfig.loadOrCreate();
        VillagerFoodPatcher.install();
        SecondaryJobSitePatcher.install();
        LOGGER.info("Villager Farm initialized");
    }
}

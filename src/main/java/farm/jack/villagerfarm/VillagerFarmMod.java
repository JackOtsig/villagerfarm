package farm.jack.villagerfarm;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerFarmMod implements ModInitializer {
    public static final String MOD_ID = "villagerfarm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModGamerules.init();
        VillagerFoodPatcher.install();
        SecondaryJobSitePatcher.install();
        LOGGER.info("Villager Farm initialized");
    }
}

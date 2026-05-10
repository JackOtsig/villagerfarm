package farm.jack.villagerfarm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import farm.jack.villagerfarm.VillagerFarmMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Config root. Plain Gson POJO — every field is public and initialized to a
 * sensible default, so a missing key in the file simply uses the default. The
 * single static {@link #INSTANCE} is populated by {@link #loadOrCreate()} during
 * mod init and never reassigned afterward (no hot reload).
 *
 * <p>The schema lives in {@code config/villagerfarm.json}. If the file doesn't
 * exist, defaults are written. If parsing fails, defaults are used in memory
 * but the broken file is left alone so the user can fix it.
 */
public final class VillagerFarmConfig {

    public static VillagerFarmConfig INSTANCE = new VillagerFarmConfig();

    public Features features = new Features();
    public Values values = new Values();

    public static final class Features {
        public boolean gamerule_split = true;
        public boolean atomic_harvest_replant = true;
        public boolean anti_trample = true;
        public boolean secondary_job_site_patcher = true;
        public boolean tooltip_integration = true;

        public ExtendedCrops extended_crops = new ExtendedCrops();
        public FoodSharing food_sharing = new FoodSharing();
        public PickupEffectsConfig pickup_effects = new PickupEffectsConfig();
    }

    public static final class ExtendedCrops {
        public boolean enabled = true;
        public boolean sugar_cane = true;
        public boolean nether_wart = true;
        public boolean cocoa = true;
        public boolean pumpkin = true;
        public boolean melon = true;
    }

    public static final class FoodSharing {
        public boolean enabled = true;
        public boolean share_pumpkin = true;
        public boolean share_melon_slice = true;
        public boolean share_sugar_cane = true;
        public boolean share_cocoa_beans = true;
        public boolean share_nether_wart_to_cleric = true;
    }

    public static final class PickupEffectsConfig {
        public StackingEffect sugar_cane = new StackingEffect(true, "minecraft:speed", 200, 0);
        public StackingEffect cocoa_beans = new StackingEffect(true, "minecraft:regeneration", 100, 0);
        public WartEffect nether_wart = new WartEffect();
    }

    public static final class StackingEffect {
        public boolean enabled;
        public String effect;
        public int ticks_per_item;
        public int amplifier;

        public StackingEffect() {}

        public StackingEffect(boolean enabled, String effect, int ticks_per_item, int amplifier) {
            this.enabled = enabled;
            this.effect = effect;
            this.ticks_per_item = ticks_per_item;
            this.amplifier = amplifier;
        }
    }

    public static final class WartEffect {
        public boolean enabled = true;
        public boolean cleric_only = true;
        public int duration_ticks = 600;
        public int amplifier = 0;
        public List<String> allowed_effects = List.of(
                "minecraft:speed",
                "minecraft:haste",
                "minecraft:strength",
                "minecraft:jump_boost",
                "minecraft:regeneration",
                "minecraft:resistance",
                "minecraft:fire_resistance",
                "minecraft:water_breathing",
                "minecraft:absorption",
                "minecraft:health_boost",
                "minecraft:slow_falling");
    }

    public static final class Values {
        public Map<String, Integer> food_values = defaultFoodValues();
        public Search search = new Search();
        public Sharing sharing = new Sharing();

        private static Map<String, Integer> defaultFoodValues() {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("minecraft:pumpkin", 4);
            m.put("minecraft:melon_slice", 1);
            m.put("minecraft:sugar_cane", 1);
            m.put("minecraft:cocoa_beans", 1);
            return m;
        }
    }

    public static final class Search {
        public int extended_radius_xyz = 2;
        public double extended_harvest_distance_squared = 16.0;
        public double unstuck_min_distance_squared = 1.0;
        public double unstuck_max_distance_squared = 6.25;
        public int secondary_job_site_radius_xz = 4;
        public int secondary_job_site_radius_y = 2;
        public int secondary_job_site_tick_interval = 40;
    }

    public static final class Sharing {
        public int min_stack_size_to_toss = 2;
        public double max_share_distance_squared = 5.0;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadOrCreate() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("villagerfarm.json");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                try (Writer w = Files.newBufferedWriter(path)) {
                    GSON.toJson(new VillagerFarmConfig(), w);
                }
                VillagerFarmMod.LOGGER.info("Created default config at {}", path);
            } catch (IOException e) {
                VillagerFarmMod.LOGGER.error("Failed to write default config; using in-memory defaults", e);
            }
            INSTANCE = new VillagerFarmConfig();
            return;
        }
        try (Reader r = Files.newBufferedReader(path)) {
            VillagerFarmConfig parsed = GSON.fromJson(r, VillagerFarmConfig.class);
            INSTANCE = parsed != null ? parsed : new VillagerFarmConfig();
            VillagerFarmMod.LOGGER.info("Loaded config from {}", path);
        } catch (IOException | JsonSyntaxException e) {
            VillagerFarmMod.LOGGER.error("Failed to parse config at {}; using in-memory defaults (file untouched)", path, e);
            INSTANCE = new VillagerFarmConfig();
        }
    }
}

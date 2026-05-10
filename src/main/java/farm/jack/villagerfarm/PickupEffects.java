package farm.jack.villagerfarm;

import farm.jack.villagerfarm.config.VillagerFarmConfig;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

/**
 * Status-effect rewards triggered when a villager picks up one of the new
 * crops. Sugar cane and cocoa beans extend a timer per item consumed; nether
 * wart, when the picker is a cleric (configurable) and the feature is on,
 * applies one random effect from a configured list.
 */
public final class PickupEffects {
    private PickupEffects() {}

    public static void onVillagerPickup(VillagerEntity villager, Item item, int consumed) {
        if (consumed <= 0) return;
        VillagerFarmConfig cfg = VillagerFarmConfig.INSTANCE;

        if (item == Items.SUGAR_CANE) {
            applyStackingEffect(villager, cfg.features.pickup_effects.sugar_cane, consumed);
            return;
        }
        if (item == Items.COCOA_BEANS) {
            applyStackingEffect(villager, cfg.features.pickup_effects.cocoa_beans, consumed);
            return;
        }
        if (item == Items.NETHER_WART) {
            VillagerFarmConfig.WartEffect w = cfg.features.pickup_effects.nether_wart;
            if (!w.enabled) return;
            if (w.cleric_only && !villager.getVillagerData().profession().matchesKey(VillagerProfession.CLERIC)) return;
            List<RegistryEntry<StatusEffect>> pool = resolvePool(w.allowed_effects);
            if (pool.isEmpty()) return;
            RegistryEntry<StatusEffect> rolled = pool.get(villager.getRandom().nextInt(pool.size()));
            extend(villager, rolled, w.duration_ticks, w.amplifier);
        }
    }

    private static void applyStackingEffect(VillagerEntity villager, VillagerFarmConfig.StackingEffect cfg, int consumed) {
        if (!cfg.enabled) return;
        Identifier id = Identifier.tryParse(cfg.effect);
        if (id == null) return;
        RegistryEntry<StatusEffect> effect = Registries.STATUS_EFFECT.getEntry(id).orElse(null);
        if (effect == null) return;
        extend(villager, effect, consumed * cfg.ticks_per_item, cfg.amplifier);
    }

    private static void extend(VillagerEntity villager, RegistryEntry<StatusEffect> effect, int addTicks, int amplifier) {
        StatusEffectInstance current = villager.getStatusEffect(effect);
        int total = (current != null && current.getAmplifier() == amplifier) ? current.getDuration() + addTicks : addTicks;
        villager.addStatusEffect(new StatusEffectInstance(effect, total, amplifier, false, true, true));
    }

    private static List<RegistryEntry<StatusEffect>> resolvePool(List<String> ids) {
        List<RegistryEntry<StatusEffect>> out = new ArrayList<>(ids.size());
        for (String s : ids) {
            Identifier id = Identifier.tryParse(s);
            if (id == null) continue;
            Registries.STATUS_EFFECT.getEntry(id).ifPresent(out::add);
        }
        return out;
    }
}

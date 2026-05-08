package farm.jack.villagerfarm;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

import java.util.List;

/**
 * Status-effect rewards triggered when a villager picks up one of the new
 * crops. Sugar cane and cocoa beans extend a timer per item consumed; nether
 * wart, when the picker is a cleric and the gamerule is on, applies one
 * random effect from a curated villager-safe list.
 */
public final class PickupEffects {
    private PickupEffects() {}

    private static final int CANE_TICKS_PER_ITEM = 200;     // 10s
    private static final int COCOA_TICKS_PER_ITEM = 100;    // 5s
    private static final int WART_EFFECT_TICKS = 600;       // 30s

    private static final List<RegistryEntry<StatusEffect>> WART_POOL = List.of(
            StatusEffects.SPEED,
            StatusEffects.HASTE,
            StatusEffects.STRENGTH,
            StatusEffects.JUMP_BOOST,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.WATER_BREATHING,
            StatusEffects.ABSORPTION,
            StatusEffects.HEALTH_BOOST,
            StatusEffects.SLOW_FALLING
    );

    public static void onVillagerPickup(VillagerEntity villager, Item item, int consumed) {
        if (consumed <= 0) return;

        if (item == Items.SUGAR_CANE) {
            extend(villager, StatusEffects.SPEED, consumed * CANE_TICKS_PER_ITEM);
            return;
        }
        if (item == Items.COCOA_BEANS) {
            extend(villager, StatusEffects.REGENERATION, consumed * COCOA_TICKS_PER_ITEM);
            return;
        }
        if (item == Items.NETHER_WART) {
            ServerWorld sw = (ServerWorld) villager.getEntityWorld();
            if (!sw.getGameRules().getValue(ModGamerules.VILLAGER_EXTENDED_FARMING)) return;
            if (!villager.getVillagerData().profession().matchesKey(VillagerProfession.CLERIC)) return;
            RegistryEntry<StatusEffect> rolled = WART_POOL.get(villager.getRandom().nextInt(WART_POOL.size()));
            extend(villager, rolled, WART_EFFECT_TICKS);
        }
    }

    /** Extend (or set) the level-I duration of {@code effect} by {@code addTicks}. */
    private static void extend(VillagerEntity villager, RegistryEntry<StatusEffect> effect, int addTicks) {
        StatusEffectInstance current = villager.getStatusEffect(effect);
        int total = (current != null && current.getAmplifier() == 0) ? current.getDuration() + addTicks : addTicks;
        villager.addStatusEffect(new StatusEffectInstance(effect, total, 0, false, true, true));
    }
}

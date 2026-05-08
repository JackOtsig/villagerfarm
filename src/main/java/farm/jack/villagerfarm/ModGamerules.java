package farm.jack.villagerfarm;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public final class ModGamerules {
    public static final GameRule<Boolean> VILLAGER_FARMING =
            GameRuleBuilder.forBoolean(true)
                    .category(GameRuleCategory.MOBS)
                    .buildAndRegister(Identifier.of(VillagerFarmMod.MOD_ID, "villager_farming"));

    public static final GameRule<Boolean> VILLAGER_EXTENDED_FARMING =
            GameRuleBuilder.forBoolean(true)
                    .category(GameRuleCategory.MOBS)
                    .buildAndRegister(Identifier.of(VillagerFarmMod.MOD_ID, "villager_extended_farming"));

    private ModGamerules() {}

    public static void init() {}
}

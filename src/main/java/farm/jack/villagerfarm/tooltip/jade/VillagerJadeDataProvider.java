package farm.jack.villagerfarm.tooltip.jade;

import farm.jack.villagerfarm.VillagerFarmMod;
import farm.jack.villagerfarm.tooltip.VillagerTooltipData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public final class VillagerJadeDataProvider implements IServerDataProvider<EntityAccessor> {
    public static final VillagerJadeDataProvider INSTANCE = new VillagerJadeDataProvider();
    private static final Identifier ID = Identifier.of(VillagerFarmMod.MOD_ID, "villager_data");

    private VillagerJadeDataProvider() {}

    @Override
    public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof VillagerEntity villager)) return;
        VillagerTooltipData.write(tag, villager, accessor.getLevel().getRegistryManager());
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}

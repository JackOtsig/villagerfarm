package farm.jack.villagerfarm.tooltip;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;

/**
 * Maps a villager brain's possible-activities set to one human-friendly word.
 * Picks the highest-priority interesting activity, falling back to IDLE.
 */
public final class VillagerActivityLabel {
    private VillagerActivityLabel() {}

    private static final List<Activity> PRIORITY = List.of(
            Activity.PANIC,
            Activity.RAID,
            Activity.PRE_RAID,
            Activity.HIDE,
            Activity.MEET,
            Activity.WORK,
            Activity.REST,
            Activity.PLAY,
            Activity.IDLE);

    /** Returns a translation-key suffix like "work" / "meet" / "panic". */
    public static String pickKey(Set<Activity> activities) {
        if (activities == null || activities.isEmpty()) return "idle";
        for (Activity a : PRIORITY) {
            if (activities.contains(a)) return keyFor(a);
        }
        return "idle";
    }

    public static Text labelOf(String key) {
        return Text.translatable("villagerfarm.tooltip.activity." + key);
    }

    private static String keyFor(Activity a) {
        if (a == Activity.WORK) return "work";
        if (a == Activity.REST) return "rest";
        if (a == Activity.MEET) return "meet";
        if (a == Activity.PLAY) return "play";
        if (a == Activity.PANIC) return "panic";
        if (a == Activity.RAID || a == Activity.PRE_RAID) return "raid";
        if (a == Activity.HIDE) return "hide";
        return "idle";
    }
}

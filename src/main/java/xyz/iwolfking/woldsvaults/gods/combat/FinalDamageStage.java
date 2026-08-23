package xyz.iwolfking.woldsvaults.gods.combat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The single final damage stage. One {@link LivingDamageEvent} handler at
 * {@link EventPriority#LOWEST} owns the last word on a hit; everything that would otherwise fight
 * over event priority registers an ordered sub-stage here instead.
 *
 * <p>Sub-stages run in ascending {@code order}, ties broken by id, so the outcome does not depend
 * on registration order or class-loading order. Each receives the running amount and returns the
 * amount the next sub-stage sees; the handler writes the result back once. Registering damage
 * reducers, floors and caps here rather than as their own listeners is what keeps them from
 * racing each other the way Second Chance races the base mod's reducers.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class FinalDamageStage {
    public static final int ORDER_SPLIT = -100;
    public static final int ORDER_REDUCTION = 0;
    public static final int ORDER_CAP = 100;
    public static final int ORDER_FLOOR = 200;

    private static final Map<ResourceLocation, Entry> SUB_STAGES = new ConcurrentHashMap<>();
    private static volatile List<Entry> ordered = List.of();

    private FinalDamageStage() {
    }

    /**
     * Registers or replaces a sub-stage. Lower {@code order} runs earlier; use
     * {@link #ORDER_SPLIT}, {@link #ORDER_REDUCTION}, {@link #ORDER_CAP} and {@link #ORDER_FLOOR}
     * as anchors, so that anything moving damage to another entity does it before the defender's
     * own mitigation, reductions land before caps, and floors always have the last word.
     */
    public static void register(ResourceLocation id, int order, SubStage stage) {
        if (SUB_STAGES.put(id, new Entry(id, order, stage)) != null) {
            WoldsVaults.LOGGER.warn("Final damage sub-stage {} was registered twice; the later registration wins.", id);
        }
        reorder();
    }

    public static boolean unregister(ResourceLocation id) {
        if (SUB_STAGES.remove(id) == null) {
            return false;
        }
        reorder();
        return true;
    }

    public static List<ResourceLocation> listSubStages() {
        List<ResourceLocation> ids = new ArrayList<>();
        ordered.forEach(entry -> ids.add(entry.id()));
        return ids;
    }

    private static void reorder() {
        List<Entry> sorted = new ArrayList<>(SUB_STAGES.values());
        sorted.sort(Comparator.comparingInt(Entry::order).thenComparing(Entry::id));
        ordered = List.copyOf(sorted);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void runFinalStage(LivingDamageEvent event) {
        List<Entry> stages = ordered;
        if (stages.isEmpty() || event.isCanceled()) {
            return;
        }
        float amount = event.getAmount();
        float result = amount;
        for (Entry entry : stages) {
            try {
                result = entry.stage().apply(event, result);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("Final damage sub-stage {} threw; its contribution was skipped.", entry.id(), e);
            }
        }
        if (result != amount) {
            event.setAmount(Math.max(result, 0.0F));
        }
    }

    /** One ordered step of the final damage stage. */
    @FunctionalInterface
    public interface SubStage {
        float apply(LivingDamageEvent event, float amount);
    }

    private record Entry(ResourceLocation id, int order, SubStage stage) {
    }
}

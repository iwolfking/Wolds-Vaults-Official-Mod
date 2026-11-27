package xyz.iwolfking.woldsvaults.api.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Tracks which ability cast is running. A cast pushes its {@link CastTag} for its own call, entities and block entities
 * created under the tag push it again while they tick, and scheduled tasks capture it, so every hit a cast causes can
 * find the cast it came from. Each pushed tag records its {@link CastCarrier}, so a spell's glyphs reach only the hits
 * that carrier delivers.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class CastContext {
    private static final ThreadLocal<Deque<Frame>> STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<Entity, CastTag> ENTITY_TAGS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<BlockEntity, CastTag> BLOCK_ENTITY_TAGS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Set<String> UNTAGGED_WARNED = ConcurrentHashMap.newKeySet();

    private CastContext() {
    }

    private record Frame(CastTag tag, CastCarrier carrier) {
    }

    @Nullable
    public static CastTag current() {
        Frame frame = STACK.get().peek();
        return frame == null ? null : frame.tag();
    }

    /** What delivers the current cast's effects right now; null outside any cast. */
    @Nullable
    public static CastCarrier currentCarrier() {
        Frame frame = STACK.get().peek();
        return frame == null ? null : frame.carrier();
    }

    public static void push(CastTag tag) {
        push(tag, CastCarrier.CAST);
    }

    public static void push(CastTag tag, CastCarrier carrier) {
        STACK.get().push(new Frame(tag, carrier));
    }

    public static void pop(CastTag tag) {
        Deque<Frame> stack = STACK.get();
        if (stack.peek() == null || stack.peek().tag() != tag) {
            WoldLogger.error("Cast context out of order: popping cast {} but the top is {}; clearing the stack.",
                    tag.castId(), stack.peek() == null ? "empty" : stack.peek().tag().castId());
            stack.clear();
            return;
        }
        stack.pop();
    }

    public static <T> T run(@Nullable CastTag tag, Supplier<T> action) {
        return run(tag, CastCarrier.CAST, action);
    }

    /**
     * Runs {@code action} with {@code tag} as the current cast, delivered by {@code carrier}, or unchanged when the tag
     * is null.
     */
    public static <T> T run(@Nullable CastTag tag, CastCarrier carrier, Supplier<T> action) {
        if (tag == null) {
            return action.get();
        }
        push(tag, carrier);
        try {
            return action.get();
        } finally {
            pop(tag);
        }
    }

    public static void runTask(@Nullable CastTag tag, Runnable action) {
        runTask(tag, CastCarrier.CAST, action);
    }

    public static void runTask(@Nullable CastTag tag, CastCarrier carrier, Runnable action) {
        run(tag, carrier, () -> {
            action.run();
            return null;
        });
    }

    /** Wraps a task so it runs under the cast that scheduled it. Without a current cast it is returned as is. */
    public static Runnable capture(Runnable task) {
        CastTag tag = current();
        if (tag == null) {
            return task;
        }
        return () -> runTask(tag, CastCarrier.SCHEDULED, task);
    }

    @Nullable
    public static CastTag tagOf(@Nullable Entity entity) {
        return entity == null ? null : ENTITY_TAGS.get(entity);
    }

    public static void tag(Entity entity, CastTag tag) {
        ENTITY_TAGS.put(entity, tag);
    }

    @Nullable
    public static CastTag tagOf(@Nullable BlockEntity blockEntity) {
        return blockEntity == null ? null : BLOCK_ENTITY_TAGS.get(blockEntity);
    }

    /** Tags a server-side block entity created during a cast with that cast; returns the tag, or null outside a cast. */
    @Nullable
    public static CastTag tagIfCasting(BlockEntity blockEntity) {
        CastTag tag = current();
        if (tag != null && blockEntity != null && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            BLOCK_ENTITY_TAGS.put(blockEntity, tag);
            return tag;
        }
        return null;
    }

    public static void tag(BlockEntity blockEntity, CastTag tag) {
        BLOCK_ENTITY_TAGS.put(blockEntity, tag);
    }

    /**
     * The cast a hit belongs to: the running cast when {@code player} owns it, otherwise the cast that created the
     * hit's direct entity. Null when the hit came from no tracked cast.
     */
    @Nullable
    public static CastTag forHit(ServerPlayer player, DamageSource source) {
        CastTag tag = current();
        if (tag != null && tag.isOwnedBy(player)) {
            return tag;
        }
        CastTag entityTag = tagOf(source.getDirectEntity());
        if (entityTag != null && entityTag.isOwnedBy(player)) {
            return entityTag;
        }
        return null;
    }

    /** Logs once per direct-entity type when an ability hit that should carry a cast has none. */
    public static void warnUntagged(DamageSource source) {
        Entity direct = source.getDirectEntity();
        String key = direct == null ? source.getMsgId() : direct.getType().toString();
        if (UNTAGGED_WARNED.add(key)) {
            WoldLogger.error("Ability hit from {} carried no cast tag; per-cast effects (Power Dump, Lucky Reset) fall back to none for it.", key);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }
        CastTag tag = current();
        if (tag != null && !ENTITY_TAGS.containsKey(event.getEntity())) {
            ENTITY_TAGS.put(event.getEntity(), tag);
        }
    }
}

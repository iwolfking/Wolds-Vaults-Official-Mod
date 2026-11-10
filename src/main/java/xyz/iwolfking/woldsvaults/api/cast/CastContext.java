package xyz.iwolfking.woldsvaults.api.cast;

import iskallia.vault.block.entity.LandmineBlockEntity;
import iskallia.vault.block.entity.TotemTileEntity;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.cast.DeferredGlyphs;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;

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
 * Tracks which ability cast is running. A cast pushes its {@link CastTag} for its own call; entities
 * and block entities created under a tag keep it and push it again while they tick, and scheduled
 * tasks capture it, so every hit a cast causes can find the cast it came from. Each pushed tag records
 * what carries it right now (the cast itself, a scheduled task, an entity's or block entity's tick, an
 * ability's own later window), so a spell's glyphs reach only the hits that carrier delivers. A totem or
 * landmine placed by a spell step also keeps the step's glyphs in its saved data and gets them back after
 * its chunk reloads.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class CastContext {
    private static final String GLYPHS_KEY = "woldsvaults:spell_glyphs";
    private static final ThreadLocal<Deque<Frame>> STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<Entity, CastTag> ENTITY_TAGS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<BlockEntity, CastTag> BLOCK_ENTITY_TAGS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Set<String> UNTAGGED_WARNED = ConcurrentHashMap.newKeySet();

    private CastContext() {
    }

    private record Frame(CastTag tag, SpellHits.Carrier carrier) {
    }

    @Nullable
    public static CastTag current() {
        Frame frame = STACK.get().peek();
        return frame == null ? null : frame.tag();
    }

    /** What delivers the current cast's effects right now; null outside any cast. */
    @Nullable
    public static SpellHits.Carrier currentCarrier() {
        Frame frame = STACK.get().peek();
        return frame == null ? null : frame.carrier();
    }

    public static void push(CastTag tag) {
        push(tag, SpellHits.Carrier.CAST);
    }

    public static void push(CastTag tag, SpellHits.Carrier carrier) {
        STACK.get().push(new Frame(tag, carrier));
    }

    public static void pop(CastTag tag) {
        Deque<Frame> stack = STACK.get();
        if (stack.peek() == null || stack.peek().tag() != tag) {
            WoldsVaults.LOGGER.error("Cast context out of order: popping cast {} but the top is {}; clearing the stack.",
                    tag.castId(), stack.peek() == null ? "empty" : stack.peek().tag().castId());
            stack.clear();
            return;
        }
        stack.pop();
    }

    /** Runs {@code action} with {@code tag} as the current cast. A null tag runs it unchanged. */
    public static <T> T run(@Nullable CastTag tag, Supplier<T> action) {
        return run(tag, SpellHits.Carrier.CAST, action);
    }

    /** Runs {@code action} with {@code tag} as the current cast, delivered by {@code carrier}. A null tag runs it unchanged. */
    public static <T> T run(@Nullable CastTag tag, SpellHits.Carrier carrier, Supplier<T> action) {
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
        runTask(tag, SpellHits.Carrier.CAST, action);
    }

    public static void runTask(@Nullable CastTag tag, SpellHits.Carrier carrier, Runnable action) {
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
        return () -> runTask(tag, SpellHits.Carrier.SCHEDULED, task);
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

    /** Gives a block entity created during a cast that cast's tag, and keeps a spell step's glyphs in its saved data. */
    public static void tagIfCasting(BlockEntity blockEntity) {
        CastTag tag = current();
        if (tag != null && blockEntity != null && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            BLOCK_ENTITY_TAGS.put(blockEntity, tag);
            if (tag.glyphs() != null && keepsGlyphs(blockEntity)) {
                blockEntity.getTileData().put(GLYPHS_KEY, tag.glyphs().save());
            }
        }
    }

    /**
     * The cast tag of a ticking block entity. A totem or landmine a spell step placed whose tag was lost with its chunk
     * gets a tag back from the glyphs in its saved data once its owner is online; that tag never resolves Power Dump.
     */
    @Nullable
    public static CastTag tagOrRestore(BlockEntity blockEntity) {
        CastTag tag = tagOf(blockEntity);
        if (tag != null || !keepsGlyphs(blockEntity) || !(blockEntity.getLevel() instanceof ServerLevel level)) {
            return tag;
        }
        CompoundTag data = blockEntity.getTileData();
        if (!data.contains(GLYPHS_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        DeferredGlyphs glyphs = DeferredGlyphs.load(data.getCompound(GLYPHS_KEY));
        if (glyphs == null) {
            data.remove(GLYPHS_KEY);
            return null;
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(glyphs.owner());
        if (owner == null) {
            return null;
        }
        Skill skill = PlayerAbilitiesData.get(level).getAbilities(owner).getForId(glyphs.spec())
                .filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast).map(TieredSkill::getChild).orElse(null);
        if (skill == null) {
            WoldsVaults.LOGGER.error("Deferred glyphs: {} at {} was placed by {} but {} has no such ability now; the glyphs are dropped.",
                    blockEntity.getClass().getSimpleName(), blockEntity.getBlockPos(), glyphs.spec(), owner.getScoreboardName());
            data.remove(GLYPHS_KEY);
            return null;
        }
        CastTag restored = new CastTag(owner, skill);
        restored.resolvePowerDump(false, 1.0F);
        restored.setGlyphs(glyphs);
        BLOCK_ENTITY_TAGS.put(blockEntity, restored);
        return restored;
    }

    private static boolean keepsGlyphs(BlockEntity blockEntity) {
        return blockEntity instanceof TotemTileEntity || blockEntity instanceof LandmineBlockEntity;
    }

    /**
     * The cast a hit belongs to: the running cast when {@code player} owns it, otherwise the cast that
     * created the hit's direct entity. Null when the hit came from no tracked cast.
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
            WoldsVaults.LOGGER.warn("Ability hit from {} carried no cast tag; per-cast effects (Power Dump, Lucky Reset) fall back to none for it.", key);
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

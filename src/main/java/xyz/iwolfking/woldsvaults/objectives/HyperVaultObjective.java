package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.basic.EnumAdapter;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.adapter.vault.DirectAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.ElixirObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.elixir.ChestElixirTask;
import iskallia.vault.core.vault.objective.elixir.CoinStacksElixirTask;
import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.vault.objective.elixir.MobElixirTask;
import iskallia.vault.core.vault.objective.elixir.OreElixirTask;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.vault.overlay.VaultOverlay;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperBossManager;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperCycleManager;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperEscalationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Endlessly-cycling objective: clear a batch of mini-objectives, arm and kill an escalating
 * hyperboss, collect the escalation rewards, repeat. The vault has no exit portal; the only
 * way out with a completion is the 30s exit pillar spawned after each boss kill.
 */
public class HyperVaultObjective extends Objective {
    // Tuning constants. Boss health/damage are fractions of base (0.5 = +50%), see BossRuneModifiers.
    public static final float HYPER_STAT_FACTOR = 1.75F;
    public static final float SPEED_PER_STACK = 0.20F;
    public static final int CHAOS_PER_KILL = 25;
    public static final int CHAOS_CAP = 350;
    public static final int WAVE_PERIOD_TICKS = 20 * 20;
    public static final float[] HEALTH_GATES = {0.8F, 0.6F, 0.4F, 0.2F};
    public static final int WAVE_MOB_MIN = 2;
    public static final int WAVE_MOB_MAX = 4;
    public static final int EXIT_PILLAR_TICKS = 20 * 30;
    public static final int FIGHT_ADD_PERIOD_TICKS = 20 * 6;
    public static final int AMBIENT_PERIOD_TICKS = 20 * 120;
    public static final int BASE_RUNE_TIER = 3;
    public static final int RUNE_TIER_CAP = 10;
    // Boss escalation = base × HYPER_STAT_FACTOR^cycle (the same exponential everything in the
    // vault rides) PLUS a boss-exclusive linear bonus per cycle. Fractions of base (50.0 = +5000%).
    public static final double BOSS_HEALTH_PERCENT = 50.0;
    public static final double BOSS_DAMAGE_PERCENT = 50.0;
    public static final double BOSS_STAT_INCREMENT = 15.0;
    // Score thresholds that add one reward-injection tier marker per hyperboss kill
    // (non-exclusive: a 500k boss adds all three).
    public static final int SCORE_RARE = 75_000;
    public static final int SCORE_EPIC = 150_000;
    public static final int SCORE_OMEGA = 500_000;
    // A kill whose boss scored at least this awards a second super crate tier.
    public static final int SCORE_DOUBLE_SUPER = 1_000_000;
    // Total mob movement speed is capped at base × this (+200%): stacked speed modifiers made
    // mobs unhittable past ~cycle 4. Applied one tick after spawn so every modifier's own
    // ENTITY_SPAWN hook has run first, whatever order the modifiers were added in.
    public static final double SPEED_CAP_FACTOR = 3.0;
    private static final UUID SPEED_CLAMP_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_speed_clamp".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    // Haste is in rune "haste points" (vanilla rune roll is 0-3), not percent. Tune in testing.
    public static final int BOSS_ABILITY_HASTE = 6;
    public static final int OBELISK_MIN = 2;
    public static final int OBELISK_MAX = 4;
    public static final float BRUTAL_OBELISK_PROBABILITY = 0.6F;
    // Elixir target = base × (MULTIPLIER + INCREMENT × cycle): 75% of a normal vault's
    // requirement at cycle 0, +25% of that normal base per completed cycle.
    public static final float ELIXIR_TARGET_MULTIPLIER = 0.75F;
    public static final float ELIXIR_TARGET_INCREMENT = 0.25F;
    // Entity tag on everything the boss fight spawns (adds + waves) so the per-kill cleanup
    // can find and discard the leftover escort.
    public static final String FIGHT_SPAWN_TAG = "hyper_fight_spawn";
    // Stack caps for hyper-added modifiers (counts crystal-applied stacks too). Electric mob
    // spam is annoying enough that one stack is plenty; Wounded (-5 hearts) is the one
    // player max-health drain left in the pools and must never zero a health pool.
    private static final java.util.Map<ResourceLocation, Integer> STACK_CAPS = java.util.Map.of(
            ResourceLocation.parse("the_vault:electric"), 1,
            ResourceLocation.parse("the_vault:wounded"), 4);

    /** True when adding this modifier would exceed its hyper stack cap; logs the skip. */
    public static boolean isStackCapped(Vault vault, VaultModifier<?> modifier) {
        Integer cap = STACK_CAPS.get(modifier.getId());
        if (cap == null || xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils.getCountOfModifiers(vault, modifier.getId()) < cap) {
            return false;
        }
        WoldsVaults.LOGGER.info("Skipped rolling another {} — capped at {} stack(s) in Hyper vaults.", modifier.getId(), cap);
        return true;
    }
    // Hyper's chaos pools (pack config; concealed-chaos / negative-event copies minus the banned
    // modifiers). hyper_mixed rolls 25 per pull for the dumps; hyper_all_bad rolls 1 for
    // brutal-boss kills; hyper_bad_timer_events rolls 1 for the 2-minute ambient ticks.
    public static final ResourceLocation CHAOS_POOL_MIXED = WoldsVaults.id("hyper_mixed");
    public static final ResourceLocation CHAOS_POOL_ALL_BAD = WoldsVaults.id("hyper_all_bad");
    public static final ResourceLocation CHAOS_POOL_TIMER_EVENTS = WoldsVaults.id("hyper_bad_timer_events");
    public static final ResourceLocation BINGO_POOL = WoldsVaults.id("hyper");

    public enum Phase {
        ROLLING, MINIS, ARMED, FIGHT, REWARD
    }

    /** Mini-objective set. Every batch is ELIXIR + one of BINGO/SCAVENGER + the forced BRUTAL. */
    public enum HyperMini {
        BINGO, SCAVENGER, ELIXIR, BRUTAL
    }

    public static final SupplierKey<Objective> KEY = SupplierKey.of("hyper", Objective.class).with(Version.v1_31, HyperVaultObjective::new);
    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Phase> PHASE = FieldKey.of("phase", Phase.class).with(Version.v1_31, Adapters.ofEnum(Phase.class, EnumAdapter.Mode.ORDINAL), DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CYCLE = FieldKey.of("cycle", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CHAOS_COUNT = FieldKey.of("chaos_count", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> BATCH_MASK = FieldKey.of("batch_mask", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    // Minis live here, NOT in CHILDREN: the base class recurses into CHILDREN unconditionally and
    // external systems discover objectives through it. CHILDREN keeps the real successor (crate).
    public static final FieldKey<ObjList> MINIS = FieldKey.of("minis", ObjList.class).with(Version.v1_31, CompoundAdapter.of(ObjList::new), DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<RuneBossFights> FIGHTS = FieldKey.of("fights", RuneBossFights.class).with(Version.v1_31, new DirectAdapter<RuneBossFights>((value, buffer, context) -> value.writeBits(buffer, context), (buffer, context) -> {
        RuneBossFights fights = new RuneBossFights();
        fights.readBits(buffer, context);
        return Optional.of(fights);
    }), DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<BlockPos> PILLAR_POS = FieldKey.of("pillar_pos", BlockPos.class).with(Version.v1_31, Adapters.BLOCK_POS, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<BlockPos> EXIT_POS = FieldKey.of("exit_pos", BlockPos.class).with(Version.v1_31, Adapters.BLOCK_POS, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> EXIT_TICKS = FieldKey.of("exit_ticks", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> ELIXIR_PROGRESS = FieldKey.of("elixir_progress", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> ELIXIR_TARGET = FieldKey.of("elixir_target", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<ElixirTask.List> ELIXIR_TASKS = FieldKey.of("elixir_tasks", ElixirTask.List.class).with(Version.v1_31, CompoundAdapter.of(ElixirTask.List::new), DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> WAVE_TICK = FieldKey.of("wave_tick", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> AMBIENT_TICK = FieldKey.of("ambient_tick", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> GATE_MASK = FieldKey.of("gate_mask", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all()).register(FIELDS);
    public static final FieldKey<UUID> BOSS_ID = FieldKey.of("boss_id", UUID.class).with(Version.v1_31, Adapters.UUID, DISK.all()).register(FIELDS);
    // The pillar tile's saved state: the fight consumes the pillar block when the boss summons,
    // so each reward phase re-places it from this snapshot for the next cycle's arming.
    public static final FieldKey<CompoundTag> PILLAR_NBT = FieldKey.of("pillar_nbt", CompoundTag.class).with(Version.v1_31, Adapters.COMPOUND_NBT, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> SCORE = FieldKey.of("score", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> ZONE_ID = FieldKey.of("zone_id", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);
    // Settable ("+X%") vault-modifier values live only in shared registry instances that reset on
    // every config reload, so a mid-vault relog silently zeroes them. Snapshot on first init,
    // re-apply on every later init. {modifier id -> value}
    public static final FieldKey<CompoundTag> SETTABLE_VALUES = FieldKey.of("settable_values", CompoundTag.class).with(Version.v1_31, Adapters.COMPOUND_NBT, DISK.all()).register(FIELDS);

    private static final int PILLAR_PIN_PERIOD_TICKS = 100;

    // Managers are pure behavior, rebuilt in initServer; every durable value lives in a FieldKey.
    private HyperCycleManager cycleManager;
    private HyperBossManager bossManager;
    private HyperEscalationManager escalationManager;
    // Mobs spawned last tick, awaiting the speed cap on the next objective tick. Transient:
    // anything in flight across a reload misses the clamp (a one-tick window, accepted).
    private final List<Mob> speedClampQueue = new ArrayList<>();
    private int speedClampCount = 0;

    protected HyperVaultObjective() {
        this.set(PHASE, Phase.ROLLING);
        this.set(CYCLE, 0);
        this.set(CHAOS_COUNT, 0);
        this.set(BATCH_MASK, 0);
        this.set(MINIS, new ObjList());
        this.set(FIGHTS, new RuneBossFights());
        this.set(EXIT_TICKS, 0);
        this.set(ELIXIR_PROGRESS, 0);
        this.set(ELIXIR_TARGET, 0);
        this.set(ELIXIR_TASKS, new ElixirTask.List());
        this.set(WAVE_TICK, 0);
        this.set(AMBIENT_TICK, AMBIENT_PERIOD_TICKS);
        this.set(GATE_MASK, 0);
    }

    public static HyperVaultObjective of() {
        return new HyperVaultObjective();
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    public static Optional<HyperVaultObjective> get(Vault vault) {
        return vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).stream().findFirst();
    }

    /** HYPER stack count, read by {@link xyz.iwolfking.woldsvaults.modifiers.vault.HyperStatModifier} on mob spawn. */
    public static int getCycleCount(Vault vault) {
        return get(vault).map(objective -> objective.getOr(CYCLE, 0)).orElse(0);
    }

    /**
     * Reserves up to {@code requested} points of the 175 chaos budget (chaos dumps, ambient
     * negative events and brutal-kill modifiers all draw from it). Returns the granted amount.
     */
    public static int consumeChaosBudget(Vault vault, int requested) {
        return get(vault).map(objective -> {
            int used = objective.getOr(CHAOS_COUNT, 0);
            int granted = Math.max(0, Math.min(requested, CHAOS_CAP - used));
            if (granted > 0) {
                objective.set(CHAOS_COUNT, used + granted);
            }
            return granted;
        }).orElse(0);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        this.cycleManager = new HyperCycleManager(vault, world, this);
        this.escalationManager = new HyperEscalationManager(vault, world, this, this.cycleManager);
        this.bossManager = new HyperBossManager(vault, world, this, this.escalationManager);

        // A reload during the reward window would otherwise leave the arena gates frozen shut
        // (the door animation is transient); replaying the 5s opening is harmless.
        if (this.getOr(PHASE, Phase.ROLLING) == Phase.REWARD) {
            this.escalationManager.restartDoorAnimation();
        }

        // Belt-and-braces vs. layer 1 in VaultMapItem.applyCrystalRecipe: Cull would let everything
        // (including the hyperboss) spawn at 1 hp. There is no public modifier-removal API, so if it
        // slipped through anyway, all we can do is shout.
        if (vault.get(Vault.MODIFIERS).hasModifier(WoldsVaults.id("cull"))) {
            WoldsVaults.LOGGER.error("Cull modifier is active on a Hyper vault and cannot be removed at runtime! "
                    + "The map-application guard should have stripped it; the vault will be trivialized.");
        }

        restoreOrSnapshotSettableValues(vault);

        // Boss room adjacent to spawn, exactly like RuneBossObjective but never in random rooms.
        CommonEvents.LAYOUT_TEMPLATE_GENERATION.register(this, data -> {
            if (data.getVault() != vault || data.getPieceType() != VaultLayout.PieceType.ROOM) {
                return;
            }
            Direction facing = vault.get(Vault.WORLD).get(WorldManager.FACING);
            RegionPos back = data.getRegion().add(facing, -(data.getLayout().get(ClassicInfiniteLayout.TUNNEL_SPAN) + 1));
            if (back.getX() == 0 && back.getZ() == 0 && data.getTemplate() == null) {
                TemplatePoolKey key = VaultRegistry.TEMPLATE_POOL.getKey(VaultMod.id("vault/rooms/special/boss"));
                if (key == null) {
                    WoldsVaults.LOGGER.error("Boss room template pool 'vault/rooms/special/boss' is missing; the Hyper vault has no arena!");
                    return;
                }
                data.setTemplate(data.getLayout().getRoom(key.get(vault.get(Vault.VERSION)), vault, vault.get(Vault.VERSION), data.getRegion(), data.getRandom(), data.getSettings()));
            }
        });

        // Pillar shows no rune requirement; arming is the shift-click below, never rune items.
        // Also our earliest sight of the pillar — remember where it is for waves/exit/pinning.
        CommonEvents.RUNE_BOSS_GENERATE_RUNES.register(this, data -> {
            if (data.getLevel() == world && data.getTile() instanceof BossRunePillarTileEntity pillar) {
                data.setResult(0);
                this.set(PILLAR_POS, pillar.getBlockPos());
            }
        });

        // Podium: right-click with an empty main hand while the batch is complete arms the
        // fight. Deliberately NOT sneak-gated: BLOCK_USE fires from inside BlockBehaviour.use,
        // and vanilla skips use() entirely for a sneaking player holding an item in EITHER
        // hand — a shift-click with a totem in the off-hand never reaches this handler at all.
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.RUNE_PILLAR).register(this, data -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                return;
            }
            if (this.getOr(PHASE, Phase.ROLLING) != Phase.ARMED) {
                return;
            }
            boolean mainHandEmpty = data.getPlayer().getMainHandItem().isEmpty();
            WoldsVaults.LOGGER.info("Podium clicked while armed at {}: mainHandEmpty={}, sneaking={}.",
                    data.getPos(), mainHandEmpty, data.getPlayer().isShiftKeyDown());
            if (!mainHandEmpty) {
                return;
            }
            try {
                this.bossManager.armAndStartFight(data.getPos());
            } catch (Exception e) {
                // The VH event bus catches handler exceptions and printStackTraces them to raw
                // stderr, which the launcher does not put in the log — without this catch a
                // failure here is completely invisible.
                WoldsVaults.LOGGER.error("Arming the hyperboss fight failed!", e);
                return;
            }
            data.setResult(InteractionResult.SUCCESS);
        });

        // Exit pillar (a temporary obelisk placed by the escalation manager after each kill).
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.OBELISK).register(this, data -> {
            if (data.getHand() != InteractionHand.MAIN_HAND || this.getOr(PHASE, Phase.ROLLING) != Phase.REWARD) {
                return;
            }
            BlockPos exit = this.getOr(EXIT_POS, null);
            if (exit == null) {
                return;
            }
            BlockPos clicked = data.getPos();
            if (!clicked.equals(exit) && !clicked.equals(exit.above())) {
                return;
            }
            Listener listener = vault.get(Vault.LISTENERS).get(data.getPlayer().getUUID());
            if (!(listener instanceof Runner runner)) {
                return;
            }
            data.setResult(InteractionResult.SUCCESS);
            try {
                vault.ifPresent(Vault.STATS, stats -> stats.get(listener.getId()).set(StatCollector.COMPLETION, Completion.COMPLETED));
                // The completion crate (CHILDREN) only awards from its own tick paths, which this
                // objective otherwise never runs; one child tick with COMPLETION set does the award.
                HyperVaultObjective.super.tickListener(world, vault, runner);
                broadcast(vault, data.getPlayer().getDisplayName().getString() + " escaped the HYPER Vault!", ChatFormatting.AQUA);
                if (vault.get(Vault.LISTENERS).getAll().size() <= 1) {
                    // Last player out: the vault is about to tear down.
                    purgeHostileEntities(world);
                }
                vault.get(Vault.LISTENERS).remove(world, vault, runner);
            } catch (Exception e) {
                // See the podium handler: the VH event bus would swallow this silently.
                WoldsVaults.LOGGER.error("Hyper exit-pillar completion failed!", e);
            }
        });

        // Track the hyperboss entity so health gates can be evaluated without touching
        // RuneBossFight's private health fields.
        // The score is NOT captured here: the summon adds the boss to the world before its
        // traits apply, so this event still sees base stats. HyperBossManager reads it mid-fight.
        CommonEvents.ENTITY_SPAWN.register(this, event -> {
            if (event.getEntity().level == world && event.getEntity() instanceof VaultBossEntity boss
                    && this.getOr(PHASE, Phase.ROLLING) == Phase.FIGHT) {
                this.set(BOSS_ID, boss.getUUID());
            }
        });

        // Queue every spawned mob for the +200% movement-speed cap, applied next objective tick
        // (after the whole spawn event, so every modifier's spawn hook has run regardless of the
        // order chaos dumps registered them). The hyperboss is excluded here: its modifiers only
        // arrive at first live tick (applyBossStats), which applies the same clamp itself.
        CommonEvents.ENTITY_SPAWN.register(this, event -> {
            if (event.getEntity().level == world && event.getEntity() instanceof Mob mob
                    && !(mob instanceof VaultBossEntity)) {
                this.speedClampQueue.add(mob);
            }
        });

        // The shared elixir bar fills from the same four sources the elixir tasks use.
        CommonEvents.CHEST_LOOT_GENERATION.post().register(this, data -> {
            if (data.getPlayer().level != world || !elixirActive()) {
                return;
            }
            Block block = data.getState().getBlock();
            if (!(block instanceof VaultChestBlock chest) || block instanceof VaultBarrelBlock) {
                return;
            }
            forEachElixirTask(ChestElixirTask.class, task -> {
                if (task.get(ChestElixirTask.TYPE) == chest.getType()) {
                    addElixir(vault, task.getOr(ElixirTask.ELIXIR, 0));
                }
            });
        });
        CommonEvents.ENTITY_DROPS.register(this, event -> {
            if (event.getEntityLiving().level != world || !elixirActive()) {
                return;
            }
            Entity killer = event.getSource().getEntity();
            if (killer instanceof EternalEntity eternal) {
                killer = eternal.getOwner().right().orElse(null);
            }
            if (!(killer instanceof Player)) {
                return;
            }
            forEachElixirTask(MobElixirTask.class, task -> {
                if (ModConfigs.ELIXIR.isEntityInGroup(event.getEntity(), task.get(MobElixirTask.GROUP))) {
                    addElixir(vault, task.getOr(ElixirTask.ELIXIR, 0));
                }
            });
        });
        CommonEvents.PLAYER_MINE.register(this, data -> {
            if (data.getPlayer().level != world || !elixirActive()) {
                return;
            }
            if (!(data.getState().getBlock() instanceof VaultOreBlock) || !data.getState().getValue(VaultOreBlock.GENERATED)) {
                return;
            }
            forEachElixirTask(OreElixirTask.class, task -> addElixir(vault, task.getOr(ElixirTask.ELIXIR, 0)));
        });
        CommonEvents.COIN_STACK_LOOT_GENERATION.post().register(this, data -> {
            if (data.getPlayer().level != world || !elixirActive()) {
                return;
            }
            forEachElixirTask(CoinStacksElixirTask.class, task -> addElixir(vault, task.getOr(ElixirTask.ELIXIR, 0)));
        });

        this.get(FIGHTS).onAttach(world, vault);
        this.get(MINIS).forEach(mini -> mini.initServer(world, vault));
        super.initServer(world, vault);
    }

    /**
     * Discards every hostile and projectile before the vault closes behind the last player.
     * A straggler explosion resolving in an unloading chunk deadlocks the whole server:
     * VaultFireball.explode kills an entity whose death-pose resize sync-loads its chunk from
     * the vault's tick thread, and that join can never complete once players are gone.
     */
    private static void purgeHostileEntities(VirtualWorld world) {
        List<Entity> doomed = new ArrayList<>();
        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof Enemy || entity instanceof Projectile) {
                doomed.add(entity);
            }
        }
        doomed.forEach(Entity::discard);
        WoldsVaults.LOGGER.info("Hyper teardown: discarded {} hostile/projectile entities before vault close.", doomed.size());
    }

    /** Applies the speed cap to last tick's spawns (see the ENTITY_SPAWN registration). */
    private void drainSpeedClampQueue() {
        if (this.speedClampQueue.isEmpty()) {
            return;
        }
        for (Mob mob : this.speedClampQueue) {
            if (mob.isAlive() && clampMovementSpeed(mob)) {
                this.speedClampCount++;
                if (this.speedClampCount == 1 || this.speedClampCount % 200 == 0) {
                    WoldsVaults.LOGGER.info("Capped mob movement speed at +{}% ({} capped so far; latest: {}).",
                            Math.round((SPEED_CAP_FACTOR - 1.0) * 100.0), this.speedClampCount,
                            mob.getType().getRegistryName());
                }
            }
        }
        this.speedClampQueue.clear();
    }

    /**
     * Caps the entity's final movement speed at base × {@link #SPEED_CAP_FACTOR} with a single
     * corrective MULTIPLY_TOTAL modifier, so later potion-style speed effects still work but the
     * permanent modifier stacking cannot push past the cap. Idempotent; true if a cap was added.
     */
    public static boolean clampMovementSpeed(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null || speed.getModifier(SPEED_CLAMP_UUID) != null) {
            return false;
        }
        double base = speed.getBaseValue();
        double value = speed.getValue();
        if (base <= 0.0 || value <= base * SPEED_CAP_FACTOR) {
            return false;
        }
        speed.addPermanentModifier(new AttributeModifier(SPEED_CLAMP_UUID, "hyper_speed_clamp",
                base * SPEED_CAP_FACTOR / value - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return true;
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        this.get(FIGHTS).onTick(world, vault);
        this.get(MINIS).forEach(mini -> mini.tickServer(world, vault));
        drainSpeedClampQueue();
        this.cycleManager.tick();
        this.bossManager.tick();
        this.escalationManager.tick();
        if (world.getGameTime() % PILLAR_PIN_PERIOD_TICKS == 0L) {
            pinPillarRuneDisplay(world);
        }
        // Deliberately no super.tickServer(): CHILDREN (the crate) must never tick as a successor;
        // it awards through its own LISTENER_LEAVE hook when a player exits with COMPLETED stats.
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }
        // Never call super here: per-listener completion happens through the exit pillar.
    }

    @Override
    public void releaseServer() {
        this.get(MINIS).forEach(Objective::releaseServer);
        this.get(FIGHTS).onDetach();
        super.releaseServer();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initClient(Vault vault) {
        this.get(MINIS).forEach(mini -> mini.initClient(vault));
        super.initClient(vault);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void releaseClient() {
        this.get(MINIS).forEach(Objective::releaseClient);
        super.releaseClient();
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        return true;
    }

    /**
     * First init: capture every settable modifier's live value (the crystal that configured this
     * vault was deserialized moments ago, so the shared instances are correct right now).
     * Later inits (relogs, restarts): push the captured values back into the shared instances,
     * which config reloads reset to 0.
     */
    private void restoreOrSnapshotSettableValues(Vault vault) {
        if (!this.has(SETTABLE_VALUES)) {
            snapshotSettableValues(vault);
            return;
        }
        CompoundTag saved = this.get(SETTABLE_VALUES);
        int restored = 0;
        for (Modifiers.Entry entry : vault.get(Vault.MODIFIERS).getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            if (modifier instanceof SettableValueVaultModifier<?> settable) {
                String id = modifier.getId().toString();
                if (saved.contains(id)) {
                    settable.properties().setValue(saved.getFloat(id));
                    restored++;
                }
            }
        }
        if (restored > 0) {
            WoldsVaults.LOGGER.info("Restored {} settable vault-modifier value(s) on Hyper vault load.", restored);
        }
    }

    /** Re-run after anything changes a settable value (e.g. the crate-quantity escalation). */
    public void snapshotSettableValues(Vault vault) {
        CompoundTag tag = new CompoundTag();
        for (Modifiers.Entry entry : vault.get(Vault.MODIFIERS).getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            if (modifier instanceof SettableValueVaultModifier<?> settable) {
                tag.putFloat(modifier.getId().toString(), settable.properties().getValue());
            }
        }
        this.set(SETTABLE_VALUES, tag);
    }

    /**
     * The pillar re-derives its displayed rune requirement in ways we cannot intercept after
     * chunk reloads (it has no setter and a 3-rune fallback), so it is periodically pinned to 0.
     */
    private void pinPillarRuneDisplay(VirtualWorld world) {
        BlockPos pos = this.getOr(PILLAR_POS, null);
        if (pos == null || !world.isLoaded(pos)) {
            return;
        }
        if (world.getBlockEntity(pos) instanceof BossRunePillarTileEntity pillar && pillar.getRuneMinimum() != 0) {
            ((BossRunePillarAccessor) pillar).setRuneMinimum(0);
            pillar.sendUpdates();
        }
    }

    public boolean isMiniInBatch(HyperMini mini) {
        return (this.getOr(BATCH_MASK, 0) & (1 << mini.ordinal())) != 0;
    }

    public boolean elixirActive() {
        return isMiniInBatch(HyperMini.ELIXIR) && this.getOr(ELIXIR_TARGET, 0) > 0;
    }

    private <T extends ElixirTask> void forEachElixirTask(Class<T> type, java.util.function.Consumer<T> action) {
        for (ElixirTask task : this.get(ELIXIR_TASKS)) {
            if (type.isInstance(task)) {
                action.accept(type.cast(task));
            }
        }
    }

    private void addElixir(Vault vault, int amount) {
        if (amount <= 0) {
            return;
        }
        int target = this.getOr(ELIXIR_TARGET, 0);
        int before = this.getOr(ELIXIR_PROGRESS, 0);
        if (before >= target) {
            return;
        }
        int after = Math.min(target, before + amount);
        this.set(ELIXIR_PROGRESS, after);
        if (after >= target) {
            broadcast(vault, "The shared elixir bar is full!", ChatFormatting.LIGHT_PURPLE);
        }
    }

    public static void broadcast(Vault vault, String message, ChatFormatting color) {
        for (Listener listener : ((Listeners) vault.get(Vault.LISTENERS)).getAll()) {
            listener.getPlayer().ifPresent(player ->
                    player.displayClientMessage(new TextComponent(message).withStyle(color), false));
        }
    }

    public <T extends Objective> Optional<T> findMini(Class<T> type) {
        for (Objective mini : this.get(MINIS)) {
            if (type.isInstance(mini)) {
                return Optional.of(type.cast(mini));
            }
        }
        return Optional.empty();
    }

    // --- HUD ---------------------------------------------------------------------------------
    // Incoming pose: x = the objective module's horizontal center, y = its top. One compact
    // horizontal row (elixir bar + obelisk icons); the bingo/collector card only renders while
    // the mod's own "open bingo" key is held.

    private static final float HUD_TOP_MARGIN = 8.0F;
    private static final float HUD_SCALE = 0.75F;
    private static final float ELIXIR_CENTER_X = -120.0F;
    private static final float OBELISK_CENTER_X = 90.0F;
    private static final float CARD_TOP_OFFSET = 46.0F;

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        Font font = Minecraft.getInstance().font;
        switch (this.getOr(PHASE, Phase.ROLLING)) {
            case MINIS -> renderMinisRow(vault, matrixStack, window, partialTicks, player, font);
            case ARMED -> drawCentered(font, matrixStack, new TextComponent("Click the boss podium with an empty hand!").withStyle(ChatFormatting.RED), HUD_TOP_MARGIN);
            case FIGHT -> renderBossBar(matrixStack, window, partialTicks);
            case REWARD -> {
                drawCentered(font, matrixStack, new TextComponent("Exit pillar: " + (this.getOr(EXIT_TICKS, 0) / 20) + "s").withStyle(ChatFormatting.AQUA), HUD_TOP_MARGIN);
                drawCentered(font, matrixStack, new TextComponent("Vault Score: " + this.getOr(SCORE, 0)).withStyle(ChatFormatting.GOLD), HUD_TOP_MARGIN + 11.0F);
            }
            case ROLLING -> {
            }
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderMinisRow(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player, Font font) {
        matrixStack.pushPose();
        matrixStack.translate(0.0F, HUD_TOP_MARGIN, 0.0F);
        matrixStack.scale(HUD_SCALE, HUD_SCALE, 1.0F);
        renderElixirBar(matrixStack, font);
        renderObeliskRow(matrixStack, font);
        renderCardLabel(matrixStack, font);
        matrixStack.popPose();

        // Held key = show the card, exactly like the mod's expanded-bingo affordance.
        if (Minecraft.getInstance().screen == null && ModKeybinds.openBingo.isDown()) {
            for (Objective mini : this.get(MINIS)) {
                if (mini instanceof BingoObjective || mini instanceof ScavengerBingoObjective) {
                    matrixStack.pushPose();
                    matrixStack.translate(0.0F, CARD_TOP_OFFSET, 0.0F);
                    mini.render(vault, matrixStack, window, partialTicks, player);
                    matrixStack.popPose();
                    break;
                }
            }
        }
    }

    /** Replica of ElixirObjective's bar (its render only works with per-player goal maps). */
    @OnlyIn(Dist.CLIENT)
    private void renderElixirBar(PoseStack matrixStack, Font font) {
        if (!isMiniInBatch(HyperMini.ELIXIR)) {
            return;
        }
        int current = this.getOr(ELIXIR_PROGRESS, 0);
        int target = Math.max(1, this.getOr(ELIXIR_TARGET, 1));
        float progress = Math.min(1.0F, (float) current / (float) target);

        matrixStack.pushPose();
        matrixStack.translate(ELIXIR_CENTER_X - 100.0F, 0.0F, 0.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int previousTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, ElixirObjective.HUD);
        GuiComponent.blit(matrixStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 50);
        GuiComponent.blit(matrixStack, 0, 8, 0.0F, 28.0F, 15 + (int) (130.0F * progress), 10, 200, 50);
        RenderSystem.setShaderTexture(0, previousTexture);
        Component text = new TextComponent(current + "/" + target).withStyle(current >= target ? ChatFormatting.GREEN : ChatFormatting.WHITE);
        font.drawShadow(matrixStack, text, 100.0F - font.width(text) / 2.0F, 28.0F, 0xFFFFFF);
        matrixStack.popPose();
    }

    /** Replica of ObeliskObjective's wave icons (its render hard-anchors to the screen center). */
    @OnlyIn(Dist.CLIENT)
    private void renderObeliskRow(PoseStack matrixStack, Font font) {
        Optional<BrutalBossesObjective> brutal = findMini(BrutalBossesObjective.class);
        if (brutal.isEmpty()) {
            return;
        }
        ObeliskObjective.Wave[] waves = brutal.get().get(ObeliskObjective.WAVES);
        int slotWidth = 22;
        int gapWidth = 7;
        int totalWidth = waves.length * slotWidth + (waves.length - 1) * gapWidth;
        float x = OBELISK_CENTER_X - totalWidth / 2.0F;

        for (ObeliskObjective.Wave wave : waves) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int previousTexture = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, VaultOverlay.VAULT_HUD);
            GuiComponent.blit(matrixStack, (int) x + 5, 0, wave.isActive() ? 77.0F : 64.0F, 84.0F, 12, 22, 256, 256);
            RenderSystem.setShaderTexture(0, previousTexture);

            TextComponent count = new TextComponent(wave.get(ObeliskObjective.Wave.COUNT) + "/" + wave.get(ObeliskObjective.Wave.TARGET));
            ChatFormatting color = wave.isCompleted() ? ChatFormatting.GREEN : (wave.isActive() ? ChatFormatting.RED : ChatFormatting.GRAY);
            font.drawShadow(matrixStack, count.withStyle(color), x + slotWidth / 2.0F - font.width(count) / 2.0F, 24.0F, 0xFFFFFF);
            x += slotWidth + gapWidth;
        }
    }

    /** The rune-boss health bar, drawn by the synced fight itself (health, shield, wave blast). */
    @OnlyIn(Dist.CLIENT)
    private void renderBossBar(PoseStack matrixStack, Window window, float partialTicks) {
        List<RuneBossFight> fights = this.get(FIGHTS).getFights();
        for (int i = fights.size() - 1; i >= 0; i--) {
            RuneBossFight fight = fights.get(i);
            if (!fight.isCompleted()) {
                matrixStack.pushPose();
                matrixStack.translate(0.0F, HUD_TOP_MARGIN, 0.0F);
                fight.render(matrixStack, window, partialTicks);
                matrixStack.popPose();
                return;
            }
        }
    }

    /** One short line under the obelisk icons so the card objective is visible without its grid. */
    @OnlyIn(Dist.CLIENT)
    private void renderCardLabel(PoseStack matrixStack, Font font) {
        String name;
        boolean done;
        if (isMiniInBatch(HyperMini.BINGO)) {
            name = "Bingo";
            done = findMini(BingoObjective.class).map(bingo -> bingo.getBingos() > 0).orElse(false);
        } else if (isMiniInBatch(HyperMini.SCAVENGER)) {
            name = "Collector";
            done = findMini(ScavengerBingoObjective.class).map(scav -> scav.getCompletedBingos() > 0).orElse(false);
        } else {
            return;
        }
        Component label = done
                ? new TextComponent(name + " ✔").withStyle(ChatFormatting.GREEN)
                : new TextComponent(name + " — hold ").append(ModKeybinds.openBingo.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW);
        font.drawShadow(matrixStack, label, OBELISK_CENTER_X - font.width(label) / 2.0F, 36.0F, 0xFFFFFF);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawCentered(Font font, PoseStack matrixStack, Component text, float y) {
        font.drawShadow(matrixStack, text, -font.width(text) / 2.0F, y, 0xFFFFFF);
    }
}

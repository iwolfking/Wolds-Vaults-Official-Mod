package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
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
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.ChaosObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.elixir.ChestElixirTask;
import iskallia.vault.core.vault.objective.elixir.CoinStacksElixirTask;
import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.vault.objective.elixir.MobElixirTask;
import iskallia.vault.core.vault.objective.elixir.OreElixirTask;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperBossManager;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperCycleManager;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperEscalationManager;

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
    public static final int CHAOS_CAP = 175;
    public static final int MINIS_PER_CYCLE = 2;
    public static final int WAVE_PERIOD_TICKS = 20 * 20;
    public static final float[] HEALTH_GATES = {0.8F, 0.6F, 0.4F, 0.2F};
    public static final int WAVE_MOB_MIN = 2;
    public static final int WAVE_MOB_MAX = 4;
    public static final int EXIT_PILLAR_TICKS = 20 * 30;
    public static final int AMBIENT_PERIOD_TICKS = 20 * 120;
    public static final int BASE_RUNE_TIER = 3;
    public static final int RUNE_TIER_CAP = 10;
    public static final double BOSS_HEALTH_PERCENT = 50.0;
    public static final double BOSS_DAMAGE_PERCENT = 10.0;
    // Haste is in rune "haste points" (vanilla rune roll is 0-3), not percent. Tune in testing.
    public static final int BOSS_ABILITY_HASTE = 6;
    public static final int TREASURE_DOOR_TARGET = 6;
    public static final int CHEST_TARGET_EACH = 30;
    public static final int OBELISK_MIN = 2;
    public static final int OBELISK_MAX = 4;
    public static final float BRUTAL_OBELISK_PROBABILITY = 0.6F;
    public static final float ELIXIR_TARGET_MULTIPLIER = 0.5F;
    public static final ResourceLocation CHAOS_POOL = WoldsVaults.id("hyper_chaos");
    public static final ResourceLocation BINGO_POOL = WoldsVaults.id("hyper");

    public enum Phase {
        ROLLING, MINIS, ARMED, FIGHT, REWARD
    }

    /** Mini-objective pool. BRUTAL is forced every cycle; two of the others are rolled. */
    public enum HyperMini {
        BINGO, SCAVENGER, ELIXIR, CHAOS, TREASURE_DOORS, CHEST_COLLECT, BRUTAL
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
    public static final FieldKey<Integer> CHESTS_WOODEN = FieldKey.of("chests_wooden", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CHESTS_GILDED = FieldKey.of("chests_gilded", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CHESTS_ORNATE = FieldKey.of("chests_ornate", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CHESTS_LIVING = FieldKey.of("chests_living", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> TREASURE_DOORS = FieldKey.of("treasure_doors", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> WAVE_TICK = FieldKey.of("wave_tick", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> AMBIENT_TICK = FieldKey.of("ambient_tick", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);
    public static final FieldKey<Integer> GATE_MASK = FieldKey.of("gate_mask", Integer.class).with(Version.v1_31, Adapters.INT_SEGMENTED_3, DISK.all()).register(FIELDS);
    public static final FieldKey<UUID> BOSS_ID = FieldKey.of("boss_id", UUID.class).with(Version.v1_31, Adapters.UUID, DISK.all()).register(FIELDS);

    // Managers are pure behavior, rebuilt in initServer; every durable value lives in a FieldKey.
    private HyperCycleManager cycleManager;
    private HyperBossManager bossManager;
    private HyperEscalationManager escalationManager;

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
        this.set(CHESTS_WOODEN, 0);
        this.set(CHESTS_GILDED, 0);
        this.set(CHESTS_ORNATE, 0);
        this.set(CHESTS_LIVING, 0);
        this.set(TREASURE_DOORS, 0);
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

        // Belt-and-braces vs. layer 1 in VaultMapItem.applyCrystalRecipe: Cull would let everything
        // (including the hyperboss) spawn at 1 hp. There is no public modifier-removal API, so if it
        // slipped through anyway, all we can do is shout.
        if (vault.get(Vault.MODIFIERS).hasModifier(WoldsVaults.id("cull"))) {
            WoldsVaults.LOGGER.error("Cull modifier is active on a Hyper vault and cannot be removed at runtime! "
                    + "The map-application guard should have stripped it; the vault will be trivialized.");
        }

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
        CommonEvents.RUNE_BOSS_GENERATE_RUNES.register(this, data -> {
            if (data.getLevel() == world && data.getTile() instanceof BossRunePillarTileEntity) {
                data.setResult(0);
            }
        });

        // Podium: shift + empty main hand while the batch is complete arms and starts the fight.
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.RUNE_PILLAR).register(this, data -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                return;
            }
            if (this.getOr(PHASE, Phase.ROLLING) != Phase.ARMED) {
                return;
            }
            if (!data.getPlayer().isShiftKeyDown() || !data.getPlayer().getMainHandItem().isEmpty()) {
                return;
            }
            this.bossManager.armAndStartFight(data.getPos());
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
            vault.ifPresent(Vault.STATS, stats -> stats.get(listener.getId()).set(StatCollector.COMPLETION, Completion.COMPLETED));
            broadcast(vault, data.getPlayer().getDisplayName().getString() + " escaped the HYPER Vault!", ChatFormatting.AQUA);
            vault.get(Vault.LISTENERS).remove(world, vault, runner);
        });

        // Track the hyperboss entity so health gates can be evaluated without touching
        // RuneBossFight's private health fields.
        CommonEvents.ENTITY_SPAWN.register(this, event -> {
            if (event.getEntity().level == world && event.getEntity() instanceof VaultBossEntity boss
                    && this.getOr(PHASE, Phase.ROLLING) == Phase.FIGHT) {
                this.set(BOSS_ID, boss.getUUID());
            }
        });

        // One handler feeds both the chest-collect counters and the shared elixir bar.
        CommonEvents.CHEST_LOOT_GENERATION.post().register(this, data -> {
            if (data.getPlayer().level != world) {
                return;
            }
            Block block = data.getState().getBlock();
            if (!(block instanceof VaultChestBlock chest) || block instanceof VaultBarrelBlock) {
                return;
            }
            countChest(chest.getType());
            if (elixirActive()) {
                forEachElixirTask(ChestElixirTask.class, task -> {
                    if (task.get(ChestElixirTask.TYPE) == chest.getType()) {
                        addElixir(vault, task.getOr(ElixirTask.ELIXIR, 0));
                    }
                });
            }
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
        CommonEvents.TREASURE_ROOM_OPEN.register(this, data -> {
            if (data.getLevel() == world && isMiniInBatch(HyperMini.TREASURE_DOORS)) {
                this.modify(TREASURE_DOORS, doors -> doors + 1);
            }
        });

        this.get(FIGHTS).onAttach(world, vault);
        this.get(MINIS).forEach(mini -> mini.initServer(world, vault));
        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        this.get(FIGHTS).onTick(world, vault);
        this.get(MINIS).forEach(mini -> mini.tickServer(world, vault));
        this.cycleManager.tick();
        this.bossManager.tick();
        this.escalationManager.tick();
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

    private void countChest(VaultChestType type) {
        if (!isMiniInBatch(HyperMini.CHEST_COLLECT)) {
            return;
        }
        switch (type) {
            case WOODEN -> this.modify(CHESTS_WOODEN, count -> count + 1);
            case GILDED -> this.modify(CHESTS_GILDED, count -> count + 1);
            case ORNATE -> this.modify(CHESTS_ORNATE, count -> count + 1);
            case LIVING -> this.modify(CHESTS_LIVING, count -> count + 1);
            default -> {
            }
        }
    }

    public static void broadcast(Vault vault, String message, ChatFormatting color) {
        for (Listener listener : ((Listeners) vault.get(Vault.LISTENERS)).getAll()) {
            listener.getPlayer().ifPresent(player ->
                    player.displayClientMessage(new TextComponent(message).withStyle(color), false));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        Font font = Minecraft.getInstance().font;
        Phase phase = this.getOr(PHASE, Phase.ROLLING);
        int cycle = this.getOr(CYCLE, 0);
        float y = 0.0F;

        drawCentered(font, matrixStack, new TextComponent("HYPER ×" + cycle).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), y);
        y += 11.0F;

        switch (phase) {
            case ROLLING -> drawCentered(font, matrixStack, new TextComponent("Rolling objectives...").withStyle(ChatFormatting.GRAY), y);
            case MINIS -> y = renderMinis(vault, matrixStack, window, partialTicks, player, font, y);
            case ARMED -> drawCentered(font, matrixStack, new TextComponent("Shift-click the boss podium!").withStyle(ChatFormatting.RED), y);
            case FIGHT -> drawCentered(font, matrixStack, new TextComponent("Slay the Hyperboss!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), y);
            case REWARD -> drawCentered(font, matrixStack, new TextComponent("Exit pillar: " + (this.getOr(EXIT_TICKS, 0) / 20) + "s").withStyle(ChatFormatting.AQUA), y);
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private float renderMinis(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player, Font font, float y) {
        for (HyperMini mini : HyperMini.values()) {
            if (!isMiniInBatch(mini)) {
                continue;
            }
            Component line = switch (mini) {
                case ELIXIR -> progressLine("Elixir", this.getOr(ELIXIR_PROGRESS, 0), this.getOr(ELIXIR_TARGET, 0));
                case TREASURE_DOORS -> progressLine("Treasure Doors", this.getOr(TREASURE_DOORS, 0), TREASURE_DOOR_TARGET);
                case CHEST_COLLECT -> new TextComponent("Chests  W " + this.getOr(CHESTS_WOODEN, 0)
                        + "  G " + this.getOr(CHESTS_GILDED, 0)
                        + "  O " + this.getOr(CHESTS_ORNATE, 0)
                        + "  L " + this.getOr(CHESTS_LIVING, 0)
                        + "  (each /" + CHEST_TARGET_EACH + ")").withStyle(ChatFormatting.YELLOW);
                case BINGO -> statusLine("Bingo line", findMini(BingoObjective.class).map(b -> b.getBingos() > 0).orElse(false));
                case SCAVENGER -> statusLine("Collector line", findMini(ScavengerBingoObjective.class).map(s -> s.getCompletedBingos() > 0).orElse(false));
                case CHAOS -> statusLine("Chaos objectives", findMini(ChaosObjective.class).map(ChaosObjective::isCompleted).orElse(false));
                case BRUTAL -> statusLine("Brutal pillars", findMini(BrutalBossesObjective.class).map(BrutalBossesObjective::isCompleted).orElse(false));
            };
            drawCentered(font, matrixStack, line, y);
            y += 10.0F;
        }
        // Card minis draw their own HUD below our text block.
        float cardOffset = y + 6.0F;
        for (Objective mini : this.get(MINIS)) {
            if (mini instanceof BingoObjective || mini instanceof ScavengerBingoObjective) {
                matrixStack.pushPose();
                matrixStack.translate(0.0F, cardOffset, 0.0F);
                mini.render(vault, matrixStack, window, partialTicks, player);
                matrixStack.popPose();
                cardOffset += 60.0F;
            }
        }
        return y;
    }

    @OnlyIn(Dist.CLIENT)
    private static Component progressLine(String name, int current, int target) {
        ChatFormatting color = current >= target ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        return new TextComponent(name + ": " + current + "/" + target).withStyle(color);
    }

    @OnlyIn(Dist.CLIENT)
    private static Component statusLine(String name, boolean done) {
        return new TextComponent(name + (done ? " ✔" : " ✘")).withStyle(done ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawCentered(Font font, PoseStack matrixStack, Component text, float y) {
        font.drawShadow(matrixStack, text, -font.width(text) / 2.0F, y, 0xFFFFFF);
    }

    public <T extends Objective> Optional<T> findMini(Class<T> type) {
        for (Objective mini : this.get(MINIS)) {
            if (type.isInstance(mini)) {
                return Optional.of(type.cast(mini));
            }
        }
        return Optional.empty();
    }
}

package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.DiscoveredRoomStat;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.event.common.DiscoverVaultRoomEvent;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.VaultGenerator;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.storage.BlockCuboid;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZone;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.init.ModOptions;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellFootprint;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.entities.extraction.RigTarget;
import xyz.iwolfking.woldsvaults.entities.extraction.ThumperEntity;
import xyz.iwolfking.woldsvaults.blocks.ExtractionCacheBlock;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.objectives.extraction.RoomGrid;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.extraction.jobs.ExtractionPayouts;
import xyz.iwolfking.woldsvaults.extraction.jobs.GreedCardUpgrades;
import iskallia.vault.world.data.PlayerGreedTreeData;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.objectives.extraction.ExtractionSite;
import xyz.iwolfking.woldsvaults.objectives.extraction.ExtractionSpawnManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ExtractionObjective extends Objective {
    public static final ResourceLocation HUD = WoldsVaults.id("textures/gui/extraction/hud.png");
    private static final int PAD = 2;
    private static final java.util.Map<String, int[][]> PIP_UVS = java.util.Map.of(
            "velara", new int[][]{{169, 138}, {186, 138}, {203, 138}},
            "tenos", new int[][]{{220, 138}, {237, 138}, {0, 155}},
            "wendarr", new int[][]{{17, 155}, {34, 155}, {51, 155}},
            "idona", new int[][]{{68, 155}, {85, 155}, {102, 155}});
    private static final Set<String> UNKNOWN_PIP_GODS = new HashSet<>();
    private static final TicketType<ChunkPos> ROOM_TICKET = TicketType.create("woldsvaults_extraction_room", Comparator.comparingLong(ChunkPos::toLong));
    private static final int ROOM_TICKET_DISTANCE = 2;
    private static final int ROOM_LOAD_CHECK_TICKS = 20;
    private static final ResourceLocation ROOM_POOL = WoldsVaults.id("vault/rooms/special/extraction_3x3");
    private static final BlockPos[] CAVE_ANCHORS = new BlockPos[]{
            new BlockPos(5, 20, 5), new BlockPos(41, 20, 5), new BlockPos(5, 20, 41), new BlockPos(41, 20, 41)
    };
    private static final Vec3[] ROOM_3X3_PEDESTALS = new Vec3[]{
            new Vec3(70.5D, 24.0D, 32.5D), new Vec3(36.5D, 24.0D, 104.5D), new Vec3(104.5D, 24.0D, 104.5D)
    };
    private static final BlockPos[] ROOM_3X3_CAVES = new BlockPos[]{
            new BlockPos(48, 20, 7), new BlockPos(70, 20, 6), new BlockPos(92, 20, 7), new BlockPos(7, 20, 48),
            new BlockPos(6, 20, 70), new BlockPos(7, 20, 92), new BlockPos(133, 20, 48), new BlockPos(134, 20, 70),
            new BlockPos(133, 20, 92), new BlockPos(48, 20, 133), new BlockPos(70, 20, 134), new BlockPos(92, 20, 133)
    };
    private static final int[][] ROOM_3X3_CAVE_HOMES = new int[][]{
            {0}, {0}, {0}, {0}, {1}, {1}, {0}, {2}, {2}, {1}, {1, 2}, {2}
    };
    public static final int MAIN_PAD = 0;
    private static final int THUMPER_DUST = 0xA64DFF;

    public static final SupplierKey<Objective> KEY = SupplierKey.of("extraction", Objective.class).with(Version.latest(), ExtractionObjective::new);
    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());
    public static final FieldKey<ExtractionSite[]> SITES = FieldKey.of("sites", ExtractionSite[].class)
            .with(Version.latest(), Adapters.ofArray(ExtractionSite[]::new, CompoundAdapter.of(ExtractionSite::new)), DISK.all().or(CLIENT.all()))
            .register(FIELDS);
    public static final FieldKey<Boolean> COMPLETED = FieldKey.of("completed", Boolean.class)
            .with(Version.latest(), Adapters.BOOLEAN, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CELL_X = FieldKey.of("cell_x", Integer.class)
            .with(Version.latest(), Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> CELL_Z = FieldKey.of("cell_z", Integer.class)
            .with(Version.latest(), Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Boolean> ZONES_ADDED = FieldKey.of("zones_added", Boolean.class)
            .with(Version.latest(), Adapters.BOOLEAN, DISK.all()).register(FIELDS);

    private ExtractionSpawnManager spawnManager;
    private final Set<UUID> discoveredPlayers = new HashSet<>();
    private final Set<Integer> sparkleFallbackLogged = new HashSet<>();
    private final Set<Integer> roomFallbackLogged = new HashSet<>();
    private Vault vault;
    private VirtualWorld world;
    private final Map<Integer, List<ChunkPos>> loadedRooms = new HashMap<>();
    private final Map<Integer, RoomGrid> grids = new HashMap<>();
    private final Map<Integer, Integer> cacheTimers = new HashMap<>();
    private final Set<Integer> cacheEmptyLogged = new HashSet<>();
    private final Map<Long, Integer> missingThumperTicks = new HashMap<>();

    protected ExtractionObjective() {
        this.set(SITES, new ExtractionSite[0]);
        this.set(COMPLETED, false);
        this.set(CELL_X, 47);
        this.set(CELL_Z, 47);
        this.set(ZONES_ADDED, false);
    }

    public static ExtractionObjective of() {
        return new ExtractionObjective();
    }

    public static Optional<ExtractionObjective> get(Vault vault) {
        List<ExtractionObjective> all = vault.get(Vault.OBJECTIVES).getAll(ExtractionObjective.class);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        this.vault = vault;
        this.world = world;
        guarded("site selection", () -> {
            if (this.get(SITES).length == 0) {
                this.pickSites(vault);
            }
        });
        guarded("site rooms", () -> this.registerSiteRooms(vault));

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            if (data.getVault() == vault) {
                data.setProbability(0.0D);
            }
        });

        guarded("site zones", () -> this.addZones(world));

        CommonEvents.CHEST_LOOT_GENERATION.post().register(this, data -> guarded("cell chest drop", () -> {
            if (data.getPlayer().level != world || !this.anySiteDrilling(world)) {
                return;
            }
            if (data.getRandom().nextDouble() < ModConfigs.EXTRACTION_OBJECTIVE.CELLS.chestDropChance) {
                data.getLoot().add(createCellStack(vault));
            }
        }));

        CommonEvents.ENTITY_DROPS.register(this, event -> guarded("cell mob drop", () -> {
            Entity entity = event.getEntity();
            if (entity.getLevel() != world || !this.anySiteDrilling(world)) {
                return;
            }
            double chance;
            ExtractionObjectiveConfig.CellsEntry cells = ModConfigs.EXTRACTION_OBJECTIVE.CELLS;
            if (entity.getTags().contains(ExtractionSpawnManager.TAG_CARRIER)
                    || (entity.getTags().contains(ExtractionSpawnManager.TAG_RAM) && ModConfigs.EXTRACTION_OBJECTIVE.WAVES.ram.cellDrop)) {
                chance = 1.0D;
            } else if (entity.getTags().contains(ExtractionSpawnManager.TAG_BOSS)) {
                chance = cells.bossMobDropChance;
            } else if (entity.getTags().contains(ExtractionSpawnManager.TAG_ELITE)) {
                chance = cells.eliteMobDropChance;
            } else if (entity.getTags().contains(ExtractionSpawnManager.TAG_ASSASSIN)) {
                chance = cells.assassinMobDropChance;
            } else if (entity.getTags().contains(ExtractionSpawnManager.TAG_HORDE)) {
                chance = cells.hordeMobDropChance;
            } else {
                chance = cells.otherMobDropChance;
            }
            if (world.getRandom().nextDouble() < chance) {
                event.getDrops().add(new ItemEntity(entity.getLevel(), entity.getX(), entity.getY(), entity.getZ(), createCellStack(vault)));
            }
        }));

        CommonEvents.LISTENER_LEAVE.register(this, data -> guarded("return infuser cards", () -> {
            if (data.getVault() != vault) {
                return;
            }
            data.getListener().getPlayer().ifPresent(player -> this.returnCardsOnLeave(world, player));
        }));

        this.spawnManager = new ExtractionSpawnManager(vault, world, this);
        super.initServer(world, vault);
    }

    /**
     * A placer leaving the vault takes their cards with them: upgraded if their Greed Infuser finished, as inserted
     * otherwise, in which case the unfinished job is cancelled and the rig removed.
     */
    private void returnCardsOnLeave(VirtualWorld world, ServerPlayer player) {
        for (ExtractionSite site : this.get(SITES)) {
            ExtractorEntity extractor = this.getLiveExtractor(world, site);
            if (extractor == null || extractor.getCards().isEmpty() || !extractor.getOwner().map(player.getUUID()::equals).orElse(false)) {
                continue;
            }
            boolean finished = extractor.isJobDone();
            int count = extractor.giveCardsTo(player);
            player.displayClientMessage(new TextComponent(String.format("%d greed card%s returned from your Greed Infuser%s", count, count == 1 ? "" : "s",
                    finished ? "." : " - its unfinished job was cancelled.")).withStyle(ChatFormatting.YELLOW), false);
            if (!finished) {
                if (site.isArmed()) {
                    site.set(ExtractionSite.STATE, site.get(ExtractionSite.PROGRESS) > 0.0F ? ExtractionSite.STATE_DRILLING : ExtractionSite.STATE_UNTOUCHED);
                }
                site.set(ExtractionSite.OCCUPIED, false);
                extractor.discard();
                this.markDirty(SITES);
            }
        }
    }

    /** Switches the armed site holding this rig to drilling; false (and logged) when the rig is not an armed site's occupant. */
    public boolean startArmedExtractor(ExtractorEntity extractor) {
        for (ExtractionSite site : this.get(SITES)) {
            if (site.has(ExtractionSite.ENTITY) && extractor.getUUID().equals(site.get(ExtractionSite.ENTITY))) {
                if (!site.isArmed()) {
                    WoldsVaults.LOGGER.error("Extractor {} asked to start but its site is in state {}, not armed.", extractor.getUUID(), site.get(ExtractionSite.STATE));
                    return false;
                }
                site.set(ExtractionSite.STATE, ExtractionSite.STATE_DRILLING);
                this.markDirty(SITES);
                return true;
            }
        }
        WoldsVaults.LOGGER.error("Armed extractor {} asked to start but no extraction site is keyed to it.", extractor.getUUID());
        return false;
    }

    /** A never-started rig was taken back: the site returns to the state it had before it was placed. */
    public void onArmedExtractorCollected(ExtractorEntity extractor) {
        for (ExtractionSite site : this.get(SITES)) {
            if (site.has(ExtractionSite.ENTITY) && extractor.getUUID().equals(site.get(ExtractionSite.ENTITY))) {
                site.set(ExtractionSite.STATE, site.get(ExtractionSite.PROGRESS) > 0.0F ? ExtractionSite.STATE_DRILLING : ExtractionSite.STATE_UNTOUCHED);
                site.set(ExtractionSite.OCCUPIED, false);
                this.markDirty(SITES);
                return;
            }
        }
        WoldsVaults.LOGGER.error("Armed extractor {} was collected but no extraction site was keyed to it.", extractor.getUUID());
    }

    private static ItemStack createCellStack(Vault vault) {
        ItemStack stack = new ItemStack(ModItems.POWER_CELL);
        stack.getOrCreateTag().putString("VaultId", vault.get(Vault.ID).toString());
        return stack;
    }

    /**
     * Seeds the site layout from the vault seed: multi-cell site rooms whose footprint fits the layout, centred on the
     * 4-9 room ring around the start, with pairwise room separation of at least the configured minimum and never
     * overlapping, each with a distinct god colour. Falls back (with an error log) to relaxed rings if the rolled layout
     * cannot satisfy the ring.
     */
    private void pickSites(Vault vault) {
        ExtractionObjectiveConfig.SitesEntry cfg = ModConfigs.EXTRACTION_OBJECTIVE.SITES;
        VaultGenerator generator = vault.get(Vault.WORLD).get(WorldManager.GENERATOR);
        if (!(generator instanceof GridGenerator gridGenerator)) {
            WoldsVaults.LOGGER.error("Extraction objective needs a GridGenerator vault but got {}; no sites can be placed!",
                    generator == null ? "null" : generator.getClass().getName());
            return;
        }
        Object layoutObj = gridGenerator.get(GridGenerator.LAYOUT);
        if (!(layoutObj instanceof VaultGridLayout layout)) {
            WoldsVaults.LOGGER.error("Extraction objective needs a VaultGridLayout but got {}; no sites can be placed!",
                    layoutObj == null ? "null" : layoutObj.getClass().getName());
            return;
        }
        int cellX = gridGenerator.get(GridGenerator.CELL_X);
        int cellZ = gridGenerator.get(GridGenerator.CELL_Z);
        this.set(CELL_X, cellX);
        this.set(CELL_Z, cellZ);
        int span = ModConfigs.MULTI_CELL_ROOMS.spanOfPool(ROOM_POOL);
        if (span <= 1) {
            WoldsVaults.LOGGER.error("Extraction room pool {} is not listed in the multi-cell room table; sites fall back to single-cell rooms.", ROOM_POOL);
            span = 1;
        }
        int radius = Math.max(2, VaultUtils.getVaultRadius(layout, vault));
        List<int[]> ringCandidates = new ArrayList<>();
        List<int[]> anyCandidates = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                RegionPos region = RegionPos.of(x, z, cellX, cellZ);
                if (x == 0 && z == 0) {
                    continue;
                }
                if (span > 1 ? !MultiCellRooms.fits(vault, layout, x, z, span) : layout.getType(vault, region) != VaultLayout.PieceType.ROOM) {
                    continue;
                }
                int centreX = x + span / 2;
                int centreZ = z + span / 2;
                int roomDistance = Math.max(Math.abs(centreX), Math.abs(centreZ)) / 2;
                anyCandidates.add(new int[]{x, z});
                if (roomDistance >= cfg.ringMinRooms && roomDistance <= cfg.ringMaxRooms) {
                    ringCandidates.add(new int[]{x, z});
                }
            }
        }
        int noOverlap = span > 1 ? span + 1 : 0;
        int separation = Math.max(cfg.minSeparationRooms * 2, noOverlap);
        Random random = new Random(vault.get(Vault.SEED) ^ 0x455854524143L);
        List<int[]> chosen = choose(ringCandidates, cfg.count, separation, random);
        if (chosen.size() < cfg.count) {
            WoldsVaults.LOGGER.error("Extraction site ring {}..{} only fit {} of {} sites in this layout; relaxing to the whole vault.",
                    cfg.ringMinRooms, cfg.ringMaxRooms, chosen.size(), cfg.count);
            chosen = choose(anyCandidates, cfg.count, separation, random);
        }
        if (chosen.size() < cfg.count) {
            WoldsVaults.LOGGER.error("Extraction site separation {} rooms could not be satisfied either; placing {} sites that only avoid overlapping.",
                    cfg.minSeparationRooms, cfg.count);
            chosen = choose(anyCandidates, cfg.count, noOverlap, random);
        }
        List<VaultGod> gods = new ArrayList<>(Arrays.asList(VaultGod.values()));
        Collections.shuffle(gods, random);
        ExtractionObjectiveConfig.JobEntry defaultJob = ModConfigs.EXTRACTION_OBJECTIVE.getJob(ModConfigs.EXTRACTION_OBJECTIVE.DEFAULT_EXTRACTOR.job);
        int requiredTicks = defaultJob == null ? 2400 : defaultJob.baseDurationTicks;
        ExtractionSite[] sites = new ExtractionSite[chosen.size()];
        for (int i = 0; i < chosen.size(); i++) {
            String god = gods.get(i % gods.size()).getName().toLowerCase(java.util.Locale.ROOT);
            sites[i] = new ExtractionSite(chosen.get(i)[0], chosen.get(i)[1], span, god, requiredTicks);
        }
        this.set(SITES, sites);
    }

    /**
     * Registers every multi-cell site room with its god palette. Runs on every vault initialisation, before any chunk
     * generates, so a restarted server rebuilds the same rooms; a room that no longer fits logs an error.
     */
    private void registerSiteRooms(Vault vault) {
        VaultGenerator generator = vault.get(Vault.WORLD).get(WorldManager.GENERATOR);
        if (!(generator instanceof GridGenerator gridGenerator) || !(gridGenerator.get(GridGenerator.LAYOUT) instanceof VaultGridLayout layout)) {
            WoldsVaults.LOGGER.error("Extraction site rooms need a grid vault layout; none were registered.");
            return;
        }
        for (ExtractionSite site : this.get(SITES)) {
            if (site.getSpan() <= 1) {
                WoldsVaults.LOGGER.error("Extraction site at ({}, {}) was saved as a single-cell site; it keeps no room of its own.",
                        site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z));
                continue;
            }
            MultiCellRooms.force(vault, layout, site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z), site.getSpan(), ROOM_POOL,
                    settings -> this.applyGodPalette(settings, site), "extraction site");
        }
    }

    private MultiCellFootprint siteFootprint(ExtractionSite site, int siteIndex) {
        if (site.getSpan() <= 1) {
            return null;
        }
        MultiCellFootprint footprint = this.vault == null ? null
                : MultiCellRooms.at(this.vault, site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z));
        if (footprint == null && this.roomFallbackLogged.add(siteIndex)) {
            WoldsVaults.LOGGER.error("Extraction site {} has no registered multi-cell room; using single-cell site geometry instead.", siteIndex);
        }
        return footprint;
    }

    public int siteIndex(ExtractionSite site) {
        return this.indexOf(site);
    }

    private int indexOf(ExtractionSite site) {
        ExtractionSite[] sites = this.get(SITES);
        for (int i = 0; i < sites.length; i++) {
            if (sites[i] == site) {
                return i;
            }
        }
        return -1;
    }

    private static List<int[]> choose(List<int[]> candidates, int count, int minCellSeparation, Random random) {
        List<int[]> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);
        List<int[]> chosen = new ArrayList<>();
        for (int[] candidate : shuffled) {
            boolean farEnough = true;
            for (int[] existing : chosen) {
                int cheb = Math.max(Math.abs(candidate[0] - existing[0]), Math.abs(candidate[1] - existing[1]));
                if (cheb < minCellSeparation) {
                    farEnough = false;
                    break;
                }
            }
            if (farEnough) {
                chosen.add(candidate);
                if (chosen.size() >= count) {
                    break;
                }
            }
        }
        return chosen;
    }

    private void applyGodPalette(PlacementSettings settings, ExtractionSite site) {
        ResourceLocation paletteId = WoldsVaults.id("extraction/" + site.get(ExtractionSite.GOD));
        PaletteKey palette = VaultRegistry.PALETTE.getKey(paletteId);
        if (palette == null) {
            WoldsVaults.LOGGER.error("Extraction palette '{}' is missing; the site room will keep its authored stand-in blocks!", paletteId);
            return;
        }
        settings.addProcessor(palette.get(Version.latest()));
    }

    private void addZones(VirtualWorld world) {
        if (this.get(ZONES_ADDED)) {
            return;
        }
        for (ExtractionSite site : this.get(SITES)) {
            AABB bounds = this.siteRoomBounds(site);
            BlockPos origin = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
            BlockCuboid cuboid = BlockCuboid.of(origin, new BlockPos(bounds.maxX - 1, bounds.maxY - 1, bounds.maxZ - 1));
            WorldZonesData.get(world.getServer()).getOrCreate(world.dimension()).add(new WorldZone().add(cuboid).setModify(false));
        }
        this.set(ZONES_ADDED, true);
    }

    public BlockPos siteRoomOrigin(ExtractionSite site) {
        int cellX = this.get(CELL_X);
        int cellZ = this.get(CELL_Z);
        RegionPos region = RegionPos.of(site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z), cellX, cellZ);
        return region.toBlockPos().above(cellX > 47 ? 0 : 9);
    }

    public AABB siteRoomBounds(ExtractionSite site) {
        BlockPos origin = this.siteRoomOrigin(site);
        int span = this.siteFootprint(site, this.indexOf(site)) == null ? 1 : site.getSpan();
        return new AABB(origin, origin.offset(47 * span, 47, 47 * span));
    }

    /** World centres of every extraction pedestal in the site's room, in the room template's authored order. */
    public List<Vec3> sitePedestals(ExtractionSite site) {
        MultiCellFootprint footprint = this.siteFootprint(site, this.indexOf(site));
        if (footprint == null) {
            BlockPos origin = this.siteRoomOrigin(site);
            return List.of(new Vec3(origin.getX() + 23.5D, origin.getY() + 21.0D, origin.getZ() + 23.5D));
        }
        List<Vec3> pedestals = new ArrayList<>(ROOM_3X3_PEDESTALS.length);
        for (Vec3 local : ROOM_3X3_PEDESTALS) {
            pedestals.add(footprint.toWorldCentre(local, this.get(CELL_X), this.get(CELL_Z)));
        }
        return pedestals;
    }

    /** The pedestal the site's extractor uses: the one it was placed on, else the first. */
    public Vec3 sitePedestalCenter(ExtractionSite site) {
        List<Vec3> pedestals = this.sitePedestals(site);
        int point = site.getPoint();
        return pedestals.get(point >= 0 && point < pedestals.size() ? point : 0);
    }

    /** World positions of every spawn cave mouth in the site's room, in the authored order (S1..S12 for 3x3 rooms). */
    public List<BlockPos> caveAnchors(ExtractionSite site) {
        MultiCellFootprint footprint = this.siteFootprint(site, this.indexOf(site));
        List<BlockPos> out = new ArrayList<>();
        if (footprint == null) {
            BlockPos origin = this.siteRoomOrigin(site);
            for (BlockPos local : CAVE_ANCHORS) {
                out.add(origin.offset(local.getX(), local.getY(), local.getZ()));
            }
            return out;
        }
        for (BlockPos local : ROOM_3X3_CAVES) {
            out.add(footprint.toWorld(local, this.get(CELL_X), this.get(CELL_Z)));
        }
        return out;
    }

    /** The pads a cave faces as authored in the room template; only used until the room grid is built. */
    public int[] authoredCaveHomes(int cave) {
        return cave >= 0 && cave < ROOM_3X3_CAVE_HOMES.length ? ROOM_3X3_CAVE_HOMES[cave] : new int[]{MAIN_PAD};
    }

    /** True when the site's room has thumper pads (a 3x3 room): thumpers must stand before the main rig goes down. */
    public boolean isStaged(ExtractionSite site) {
        return this.sitePedestals(site).size() > ExtractionSite.THUMPER_SLOTS;
    }

    public RoomGrid gridFor(int siteIndex) {
        return this.grids.get(siteIndex);
    }

    public float siteProgressFraction(ExtractionSite site) {
        return clamp01(site.get(ExtractionSite.PROGRESS) / Math.max(1.0F, site.get(ExtractionSite.REQUIRED)));
    }

    public ThumperEntity getThumper(VirtualWorld world, ExtractionSite site, int slot) {
        if (!site.has(ExtractionSite.THUMPER_ENTITY[slot])) {
            return null;
        }
        Entity entity = world.getEntity(site.get(ExtractionSite.THUMPER_ENTITY[slot]));
        return entity instanceof ThumperEntity thumper && thumper.isAlive() ? thumper : null;
    }

    /** The rig on the given pad if wave mobs should attack it right now, else null. */
    public LivingEntity rigAt(VirtualWorld world, ExtractionSite site, int pad) {
        if (pad == MAIN_PAD) {
            ExtractorEntity extractor = this.getLiveExtractor(world, site);
            return extractor != null && site.isDrilling() && extractor.isRigTargetable() ? extractor : null;
        }
        int slot = pad - 1;
        if (slot < 0 || slot >= ExtractionSite.THUMPER_SLOTS) {
            return null;
        }
        ThumperEntity thumper = this.getThumper(world, site, slot);
        return thumper != null && thumper.isRigTargetable() ? thumper : null;
    }

    /** Every rig of the site wave mobs should attack right now. */
    public List<LivingEntity> liveRigs(VirtualWorld world, ExtractionSite site) {
        List<LivingEntity> out = new ArrayList<>();
        int pads = this.isStaged(site) ? 1 + ExtractionSite.THUMPER_SLOTS : 1;
        for (int pad = 0; pad < pads; pad++) {
            LivingEntity rig = this.rigAt(world, site, pad);
            if (rig != null) {
                out.add(rig);
            }
        }
        return out;
    }

    public ExtractorEntity getLiveExtractor(VirtualWorld world, ExtractionSite site) {
        if (!site.has(ExtractionSite.ENTITY)) {
            return null;
        }
        Entity entity = world.getEntity(site.get(ExtractionSite.ENTITY));
        return entity instanceof ExtractorEntity extractor && extractor.isAlive() ? extractor : null;
    }

    public List<ExtractionSite> getDrillingSitesWithLiveExtractor(VirtualWorld world) {
        List<ExtractionSite> result = new ArrayList<>();
        for (ExtractionSite site : this.get(SITES)) {
            if (!site.isDrilling()) {
                continue;
            }
            ExtractorEntity extractor = this.getLiveExtractor(world, site);
            if (extractor != null && !extractor.isJobDone()) {
                result.add(site);
            }
        }
        return result;
    }

    public boolean anySiteDrilling(VirtualWorld world) {
        return !this.getDrillingSitesWithLiveExtractor(world).isEmpty();
    }

    /** Deploys the given armed extractor item at whichever site pedestal the clicked block belongs to. */
    public InteractionResult tryDeploy(Vault vault, Player player, ItemStack stack, BlockPos clickedPos) {
        if (!(player.level instanceof VirtualWorld world)) {
            WoldsVaults.LOGGER.error("Extractor deployment attempted outside a vault world by {}.", player.getName().getString());
            return InteractionResult.FAIL;
        }
        ExtractionSite target = null;
        int targetIndex = -1;
        int targetPoint = -1;
        ExtractionSite[] sites = this.get(SITES);
        search:
        for (int i = 0; i < sites.length; i++) {
            List<Vec3> pedestals = this.sitePedestals(sites[i]);
            for (int point = 0; point < pedestals.size(); point++) {
                if (pedestals.get(point).distanceTo(Vec3.atCenterOf(clickedPos)) <= 3.5D) {
                    target = sites[i];
                    targetIndex = i;
                    targetPoint = point;
                    break search;
                }
            }
        }
        if (target == null) {
            player.displayClientMessage(new TextComponent("Place the extractor on the crystal pedestal at a drill site.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (target.isComplete()) {
            player.displayClientMessage(new TextComponent("This site has already been fully extracted.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (this.getLiveExtractor(world, target) != null) {
            player.displayClientMessage(new TextComponent("An extractor is already drilling at this site.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        String jobId = ExtractorItem.getData(stack).getString("Job");
        ExtractionObjectiveConfig.JobEntry job = ModConfigs.EXTRACTION_OBJECTIVE.getJob(jobId);
        if (job == null) {
            WoldsVaults.LOGGER.error("Extractor item carries unknown job '{}'; it cannot be deployed.", jobId);
            player.displayClientMessage(new TextComponent("This extractor's drill head is not configured (job '" + jobId + "').").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        int power = ExtractorItem.getData(stack).getInt("Power");
        if (power < job.minPower) {
            player.displayClientMessage(new TextComponent("This extractor lacks the power for its job (needs " + job.minPower + ").").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        int rank = GreedMedallionVaultState.get(vault).map(GreedMedallionTier::getRankIndex).orElse(0);
        if (rank < job.minRank) {
            String needed = GreedMedallionTier.byRankIndex(job.minRank).map(tier -> tier.getBand() + " " + tier.getTierInBand()).orElse("rank " + job.minRank);
            player.displayClientMessage(new TextComponent("This job needs a " + needed + " greed medallion or better on the crystal.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        boolean staged = this.isStaged(target);
        if (staged && targetPoint != MAIN_PAD) {
            return this.placeThumper(world, player, stack, target, targetIndex, targetPoint - 1);
        }
        if (staged) {
            UUID rigId = ExtractorItem.getRigId(stack);
            if (!target.has(ExtractionSite.RIG_KEY)) {
                player.displayClientMessage(new TextComponent("Place both thumpers on the purple pads first (0/2).").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!target.get(ExtractionSite.RIG_KEY).equals(rigId)) {
                player.displayClientMessage(new TextComponent("This room's thumpers belong to another extractor.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (target.thumpersPlaced() < ExtractionSite.THUMPER_SLOTS) {
                player.displayClientMessage(new TextComponent(String.format("Place both thumpers first (%d/%d).", target.thumpersPlaced(), ExtractionSite.THUMPER_SLOTS)).withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }
        String cardTarget = "";
        int cardSlots = 0;
        if (job.requiresArming) {
            if ("cards".equalsIgnoreCase(job.payout.type)) {
                int greedTier = player.getServer() == null ? 0 : PlayerGreedTreeData.get(player.getServer()).getGreedTier(player.getUUID());
                String placerClass = GreedCardUpgrades.classForGreedTier(greedTier);
                if (placerClass == null) {
                    player.displayClientMessage(new TextComponent("Your greed rank does not unlock any greed cards yet, so the Greed Infuser has nothing to upgrade to.").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
                cardTarget = placerClass;
                cardSlots = ModConfigs.EXTRACTION_OBJECTIVE.GREED_INFUSER.slotsFor(power, job.minPower);
            } else {
                WoldsVaults.LOGGER.error("Job '{}' requires arming but payout type '{}' has no insert screen; the rig is placed armed with nothing to insert.", jobId, job.payout.type);
            }
        }
        ExtractorEntity extractor = ModEntities.EXTRACTOR.create(world);
        if (extractor == null) {
            WoldsVaults.LOGGER.error("Extractor entity could not be created; deployment aborted.");
            return InteractionResult.FAIL;
        }
        target.set(ExtractionSite.POINT, targetPoint);
        Vec3 pedestal = this.sitePedestalCenter(target);
        extractor.initFromItem(stack, player, targetIndex, ModConfigs.EXTRACTION_OBJECTIVE.DEPLOYMENT.initialEnergyFraction);
        extractor.moveTo(pedestal.x, pedestal.y - 2.0D, pedestal.z, 0.0F, 0.0F);
        if (job.requiresArming) {
            extractor.arm(cardSlots, cardTarget);
        }
        world.addWithUUID(extractor);
        target.set(ExtractionSite.ENTITY, extractor.getUUID());
        target.set(ExtractionSite.STATE, job.requiresArming ? ExtractionSite.STATE_ARMED : ExtractionSite.STATE_DRILLING);
        target.set(ExtractionSite.OCCUPIED, true);
        target.set(ExtractionSite.REQUIRED, job.durationTicks(power, ExtractorItem.getData(stack).getInt("Speed")));
        this.markDirty(SITES);
        ExtractorItem.markDeployed(stack, vault.get(Vault.ID), extractor.getUUID());
        if (job.requiresArming) {
            world.playSound(null, new BlockPos(pedestal.x, pedestal.y, pedestal.z), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6F, 1.2F);
            player.displayClientMessage(new TextComponent(String.format("Greed Infuser placed - right-click it to insert up to %d greed cards, then press Start.", cardSlots))
                    .withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.CONSUME;
        }
        world.playSound(null, new BlockPos(pedestal.x, pedestal.y, pedestal.z), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        player.displayClientMessage(new TextComponent("Extractor deployed - defend it while it drills!").withStyle(ChatFormatting.GOLD), true);
        return InteractionResult.CONSUME;
    }

    public void onExtractorDestroyed(ExtractorEntity extractor) {
        this.vacate(extractor);
        this.siteOf(extractor).ifPresent(site -> this.clearCaches(this.world, site));
        broadcast(this.vaultOf(extractor), new TextComponent("An extractor was destroyed! Its drill site holds its progress.").withStyle(ChatFormatting.RED));
    }

    public void onExtractorCollected(ExtractorEntity extractor) {
        this.vacate(extractor);
        this.siteOf(extractor).ifPresent(site -> {
            this.clearCaches(this.world, site);
            if (site.isComplete()) {
                for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
                    this.removeThumper(this.world, site, slot);
                }
                site.remove(ExtractionSite.RIG_KEY);
                this.markDirty(SITES);
            }
        });
    }

    private Optional<ExtractionSite> siteOf(ExtractorEntity extractor) {
        for (ExtractionSite site : this.get(SITES)) {
            if (site.has(ExtractionSite.ENTITY) && extractor.getUUID().equals(site.get(ExtractionSite.ENTITY))) {
                return Optional.of(site);
            }
        }
        return Optional.empty();
    }

    /** Clears the site's occupant flag so the placed HUD stops showing a machine that is no longer there. */
    private void vacate(ExtractorEntity extractor) {
        for (ExtractionSite site : this.get(SITES)) {
            if (site.has(ExtractionSite.ENTITY) && extractor.getUUID().equals(site.get(ExtractionSite.ENTITY))) {
                site.set(ExtractionSite.OCCUPIED, false);
                this.markDirty(SITES);
                return;
            }
        }
        WoldsVaults.LOGGER.error("Extractor {} left the vault but no extraction site was keyed to it; the placed HUD may linger.", extractor.getUUID());
    }

    /** A player broke a cache chest in one of this objective's site rooms: rolls its power cell and forgets the spot. */
    public void onCacheBroken(VirtualWorld world, BlockPos pos, Player player) {
        long key = pos.asLong();
        for (ExtractionSite site : this.get(SITES)) {
            long[] chests = site.has(ExtractionSite.CACHE_CHESTS) ? site.get(ExtractionSite.CACHE_CHESTS) : new long[0];
            for (int i = 0; i < chests.length; i++) {
                if (chests[i] != key) {
                    continue;
                }
                long[] kept = new long[chests.length - 1];
                System.arraycopy(chests, 0, kept, 0, i);
                System.arraycopy(chests, i + 1, kept, i, chests.length - i - 1);
                site.set(ExtractionSite.CACHE_CHESTS, kept);
                this.markDirty(SITES);
                if (world.getRandom().nextDouble() < ModConfigs.EXTRACTION_OBJECTIVE.CACHE.cellChance) {
                    Block.popResource(world, pos, createCellStack(this.vault));
                }
                return;
            }
        }
        WoldsVaults.LOGGER.error("A cache chest at {} was broken but no extraction site tracks it; no power cell was rolled.", pos);
    }

    private InteractionResult placeThumper(VirtualWorld world, Player player, ItemStack stack, ExtractionSite site, int siteIndex, int slot) {
        UUID itemKey = ExtractorItem.getRigId(stack);
        if (site.has(ExtractionSite.RIG_KEY)) {
            if (!site.get(ExtractionSite.RIG_KEY).equals(itemKey)) {
                player.displayClientMessage(new TextComponent("This room's thumpers belong to another extractor. Sneak-use a thumper with an empty hand to pack them up.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        } else {
            ExtractionSite[] sites = this.get(SITES);
            for (int i = 0; i < sites.length && itemKey != null; i++) {
                if (i != siteIndex && !sites[i].isComplete() && sites[i].has(ExtractionSite.RIG_KEY) && sites[i].get(ExtractionSite.RIG_KEY).equals(itemKey)) {
                    player.displayClientMessage(new TextComponent("This extractor is committed to another drill site in this vault.").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
            }
            itemKey = ExtractorItem.ensureRigId(stack);
            site.set(ExtractionSite.RIG_KEY, itemKey);
        }
        int state = site.getThumperState(slot);
        if (state == ExtractionSite.THUMPER_STANDING) {
            player.displayClientMessage(new TextComponent(String.format("A thumper already stands here (%d/%d placed).", site.thumpersPlaced(), ExtractionSite.THUMPER_SLOTS))
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }
        if (state == ExtractionSite.THUMPER_WRECKED) {
            if (this.getLiveExtractor(world, site) != null) {
                player.displayClientMessage(new TextComponent("A wrecked thumper can only be rebuilt while no extractor stands on the main pad.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            this.removeThumper(world, site, slot);
        }
        ThumperEntity thumper = ModEntities.THUMPER.create(world);
        if (thumper == null) {
            WoldsVaults.LOGGER.error("Thumper entity could not be created; placement aborted.");
            return InteractionResult.FAIL;
        }
        Vec3 pad = this.sitePedestals(site).get(slot + 1);
        double top = this.pedestalTop(world, new BlockPos(pad), pad.y, siteIndex * 8 + slot + 1);
        thumper.initFromItem(stack, ModConfigs.EXTRACTION_OBJECTIVE.THUMPER, siteIndex, slot + 1, itemKey);
        thumper.moveTo(pad.x, top, pad.z, 0.0F, 0.0F);
        world.addWithUUID(thumper);
        site.set(ExtractionSite.THUMPER_ENTITY[slot], thumper.getUUID());
        site.set(ExtractionSite.THUMPER_STATE[slot], ExtractionSite.THUMPER_STANDING);
        this.mirrorThumper(site, slot, thumper);
        this.markDirty(SITES);
        world.playSound(null, new BlockPos(pad), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.7F, 0.7F);
        int placed = site.thumpersPlaced();
        player.displayClientMessage(new TextComponent(placed < ExtractionSite.THUMPER_SLOTS
                ? String.format("Thumper placed (%d/%d).", placed, ExtractionSite.THUMPER_SLOTS)
                : "Both thumpers placed - put the extractor on the main pad to start drilling.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return InteractionResult.CONSUME;
    }

    /** Right-click on a thumper: deposit cells into the main rig, pack thumpers up (sneaking), rebuild a wreck, or show its status. */
    public void interactThumper(ThumperEntity thumper, Player player, ItemStack held) {
        ExtractionSite[] sites = this.get(SITES);
        int index = thumper.getSiteIndex();
        int slot = thumper.getPadIndex() - 1;
        if (index < 0 || index >= sites.length || slot < 0 || slot >= ExtractionSite.THUMPER_SLOTS || this.world == null) {
            WoldsVaults.LOGGER.error("Thumper {} claims site {} pad {}, which does not exist.", thumper.getUUID(), index, thumper.getPadIndex());
            return;
        }
        ExtractionSite site = sites[index];
        ExtractorEntity main = this.getLiveExtractor(this.world, site);
        boolean keyed = held.getItem() instanceof ExtractorItem && thumper.getRigKey() != null && thumper.getRigKey().equals(ExtractorItem.getRigId(held));
        if (player.isShiftKeyDown()) {
            if (main != null) {
                player.displayClientMessage(new TextComponent("Collect the extractor before packing up its thumpers.").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (held.isEmpty()) {
                for (int s = 0; s < ExtractionSite.THUMPER_SLOTS; s++) {
                    this.removeThumper(this.world, site, s);
                }
                site.remove(ExtractionSite.RIG_KEY);
                this.markDirty(SITES);
                player.displayClientMessage(new TextComponent("Thumpers packed up - this room is free for any extractor.").withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            if (keyed) {
                this.removeThumper(this.world, site, slot);
                if (site.thumpersPlaced() == 0) {
                    site.remove(ExtractionSite.RIG_KEY);
                }
                this.markDirty(SITES);
                player.displayClientMessage(new TextComponent(String.format("Thumper packed up (%d/%d left).", site.thumpersPlaced(), ExtractionSite.THUMPER_SLOTS))
                        .withStyle(ChatFormatting.YELLOW), true);
                return;
            }
            player.displayClientMessage(new TextComponent("Sneak-use with an empty hand to pack up this room's thumpers.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (keyed && thumper.isWrecked() && main == null) {
            this.placeThumper(this.world, player, held, site, index, slot);
            return;
        }
        boolean hasCells = player.getInventory().contains(new ItemStack(ModItems.POWER_CELL));
        if (main != null && !main.isJobDone()) {
            int deposited = main.depositCellsFrom(player);
            if (deposited > 0) {
                player.displayClientMessage(new TextComponent(String.format("Deposited %d power cell%s into the extractor (+%,.0f energy).",
                        deposited, deposited == 1 ? "" : "s", deposited * main.getEnergyPerCell())).withStyle(ChatFormatting.YELLOW), true);
                this.world.playSound(null, thumper.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 0.6F);
                return;
            }
        } else if (hasCells) {
            player.displayClientMessage(new TextComponent("Place the extractor on the main pad first.").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (thumper.isWrecked()) {
            player.displayClientMessage(new TextComponent(main == null
                    ? "This thumper is wrecked - right-click it with its extractor to rebuild it."
                    : "This thumper is wrecked.").withStyle(ChatFormatting.RED), true);
            return;
        }
        player.displayClientMessage(new TextComponent(String.format("Thumper %d: hull %,.0f/%,.0f, shield %,.0f/%,.0f%s", slot + 1,
                thumper.getHealth(), thumper.getMaxHealth(), thumper.getShield(), thumper.getMaxShield(),
                thumper.isActive() ? "" : " (inert until the extractor drills)")).withStyle(ChatFormatting.GRAY), true);
    }

    /** A thumper's hull ran out: the slot turns into a wreck, the job slows and the main rig pays a share of its destruction loss. */
    public void onThumperWrecked(ThumperEntity thumper) {
        ExtractionSite[] sites = this.get(SITES);
        int index = thumper.getSiteIndex();
        int slot = thumper.getPadIndex() - 1;
        if (index < 0 || index >= sites.length || slot < 0 || slot >= ExtractionSite.THUMPER_SLOTS || this.world == null) {
            WoldsVaults.LOGGER.error("Wrecked thumper {} claims site {} pad {}, which does not exist; no penalty was charged.", thumper.getUUID(), index, thumper.getPadIndex());
            return;
        }
        ExtractionSite site = sites[index];
        site.set(ExtractionSite.THUMPER_STATE[slot], ExtractionSite.THUMPER_WRECKED);
        this.mirrorThumper(site, slot, thumper);
        this.markDirty(SITES);
        ExtractionObjectiveConfig.ThumperEntry cfg = ModConfigs.EXTRACTION_OBJECTIVE.THUMPER;
        ExtractorEntity main = this.getLiveExtractor(this.world, site);
        if (main == null) {
            WoldsVaults.LOGGER.error("Thumper {} of site {} was wrecked with no live main rig; its durability penalty was not charged.", thumper.getUUID(), index);
        } else {
            float loss = main.chargeDestructionPenalty(cfg.destructionPenaltyFraction);
            WoldsVaults.LOGGER.info("Thumper {} of site {} wrecked; main rig loses {} durability.", slot + 1, index, String.format("%.1f", loss));
        }
        float rate = Math.max(0.0F, 1.0F - cfg.progressLossPerThumper * site.thumpersWrecked());
        broadcast(this.vault, new TextComponent(String.format("A thumper was destroyed! Extraction slowed to %d%%.", Math.round(rate * 100.0F))).withStyle(ChatFormatting.RED));
    }

    private void removeThumper(VirtualWorld world, ExtractionSite site, int slot) {
        if (world != null && site.has(ExtractionSite.THUMPER_ENTITY[slot])) {
            Entity entity = world.getEntity(site.get(ExtractionSite.THUMPER_ENTITY[slot]));
            if (entity != null) {
                entity.discard();
            }
        }
        site.remove(ExtractionSite.THUMPER_ENTITY[slot]);
        site.set(ExtractionSite.THUMPER_STATE[slot], ExtractionSite.THUMPER_NONE);
        site.set(ExtractionSite.THUMPER_HP[slot], 0.0F);
        site.set(ExtractionSite.THUMPER_MAX_HP[slot], 0.0F);
        site.set(ExtractionSite.THUMPER_SHIELD[slot], 0.0F);
        site.set(ExtractionSite.THUMPER_MAX_SHIELD[slot], 0.0F);
        this.markDirty(SITES);
    }

    private void mirrorThumper(ExtractionSite site, int slot, ThumperEntity thumper) {
        site.set(ExtractionSite.THUMPER_HP[slot], thumper.isWrecked() ? 0.0F : thumper.getHealth());
        site.set(ExtractionSite.THUMPER_MAX_HP[slot], thumper.getMaxHealth());
        site.set(ExtractionSite.THUMPER_SHIELD[slot], thumper.isWrecked() ? 0.0F : thumper.getShield());
        site.set(ExtractionSite.THUMPER_MAX_SHIELD[slot], thumper.getMaxShield());
    }

    /** Per tick: builds room grids for staging or drilling rooms, switches thumpers on while the main rig drills, mirrors them for the HUD and runs the cache spawner. */
    private void tickRooms(VirtualWorld world) {
        ExtractionSite[] sites = this.get(SITES);
        ExtractionObjectiveConfig.CacheEntry cache = ModConfigs.EXTRACTION_OBJECTIVE.CACHE;
        boolean dirty = false;
        for (int i = 0; i < sites.length; i++) {
            ExtractionSite site = sites[i];
            if (!this.isStaged(site)) {
                continue;
            }
            ExtractorEntity main = this.getLiveExtractor(world, site);
            boolean drilling = site.isDrilling() && main != null && !main.isJobDone() && !main.isArmed();
            if (!site.isComplete() && (site.thumpersPlaced() > 0 || site.isOccupied())) {
                RoomGrid grid = this.ensureGrid(site, i);
                if (!grid.isReady() && !grid.hasFailed()) {
                    if (drilling) {
                        grid.buildNow(world, cache.mobMargin, cache.pedestalClearance);
                    } else {
                        grid.tickBuild(world, cache.mobMargin, cache.pedestalClearance);
                    }
                }
            }
            for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
                int state = site.getThumperState(slot);
                if (state == ExtractionSite.THUMPER_NONE) {
                    continue;
                }
                ThumperEntity thumper = this.getThumper(world, site, slot);
                long missingKey = ((long) i << 8) | slot;
                if (thumper == null) {
                    Vec3 pad = this.sitePedestals(site).get(slot + 1);
                    if (world.isLoaded(new BlockPos(pad)) && this.missingThumperTicks.merge(missingKey, 1, Integer::sum) >= 200) {
                        WoldsVaults.LOGGER.error("Thumper slot {} of site {} has had no entity for 10 s with its pad loaded; clearing the slot.", slot + 1, i);
                        this.missingThumperTicks.remove(missingKey);
                        this.removeThumper(world, site, slot);
                    }
                    continue;
                }
                this.missingThumperTicks.remove(missingKey);
                thumper.setActive(drilling && !thumper.isWrecked());
                if (world.getGameTime() % 4L == 0L) {
                    this.mirrorThumper(site, slot, thumper);
                    dirty = true;
                }
            }
            if (drilling) {
                this.tickCaches(world, site, i, cache);
            } else {
                this.cacheTimers.remove(i);
            }
        }
        if (dirty) {
            this.markDirty(SITES);
        }
    }

    private RoomGrid ensureGrid(ExtractionSite site, int index) {
        RoomGrid grid = this.grids.get(index);
        if (grid == null) {
            int size = 47 * site.getSpan();
            grid = new RoomGrid(index, this.siteRoomOrigin(site), size, 47, size, this.sitePedestals(site), this.caveAnchors(site));
            this.grids.put(index, grid);
            WoldsVaults.LOGGER.info("Extraction site {} is reading its {}x47x{} room for lane pathing.", index, size, size);
        }
        return grid;
    }

    private void tickCaches(VirtualWorld world, ExtractionSite site, int index, ExtractionObjectiveConfig.CacheEntry cfg) {
        RoomGrid grid = this.grids.get(index);
        if (grid == null || !grid.isReady()) {
            return;
        }
        int timer = this.cacheTimers.merge(index, 1, Integer::sum);
        if (timer < cfg.intervalTicks) {
            return;
        }
        this.cacheTimers.put(index, 0);
        if (grid.chestSpotCount() == 0) {
            if (this.cacheEmptyLogged.add(index)) {
                WoldsVaults.LOGGER.error("Extraction site {} room has no cache chest spots; no caches will spawn there.", index);
            }
            return;
        }
        List<BlockPos> standing = new ArrayList<>();
        for (long packed : site.has(ExtractionSite.CACHE_CHESTS) ? site.get(ExtractionSite.CACHE_CHESTS) : new long[0]) {
            BlockPos pos = BlockPos.of(packed);
            if (world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE)) {
                standing.add(pos);
            }
        }
        int room = cfg.cap - standing.size();
        if (room <= 0) {
            this.storeCaches(site, standing);
            return;
        }
        Random random = world.getRandom();
        int wanted = Math.min(room, cfg.batchMin + random.nextInt(Math.max(1, cfg.batchMax - cfg.batchMin + 1)));
        List<ServerPlayer> players = new ArrayList<>();
        for (Runner runner : this.vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().filter(p -> p.level == world).ifPresent(players::add);
        }
        int placed = 0;
        if (cfg.cluster) {
            BlockPos centre = null;
            for (int attempt = 0; attempt < 16 && centre == null; attempt++) {
                BlockPos candidate = grid.randomChestSpot(random);
                if (candidate != null && farFrom(candidate, players, cfg.playerClearance) && farFromAll(candidate, standing, cfg.clusterSpacing)) {
                    centre = candidate;
                }
            }
            if (centre == null) {
                WoldsVaults.LOGGER.error("Extraction site {} found no cache pile centre clear of players and other piles in 16 tries; this batch is skipped.", index);
            } else {
                List<BlockPos> spots = grid.chestSpotsNear(centre, cfg.clusterRadius);
                Collections.shuffle(spots, random);
                for (BlockPos spot : spots) {
                    if (placed >= wanted) {
                        break;
                    }
                    if (farFromAll(spot, standing, cfg.minSpacing) && this.placeCache(world, spot, random)) {
                        standing.add(spot);
                        placed++;
                    }
                }
            }
        } else {
            for (int i = 0; i < wanted; i++) {
                for (int attempt = 0; attempt < 8; attempt++) {
                    BlockPos spot = grid.randomChestSpot(random);
                    if (spot != null && farFrom(spot, players, cfg.playerClearance) && farFromAll(spot, standing, cfg.minSpacing) && this.placeCache(world, spot, random)) {
                        standing.add(spot);
                        placed++;
                        break;
                    }
                }
            }
        }
        if (placed < wanted) {
            WoldsVaults.LOGGER.debug("Extraction site {} placed {} of {} cache chests this batch.", index, placed, wanted);
        }
        this.storeCaches(site, standing);
    }

    private void storeCaches(ExtractionSite site, List<BlockPos> standing) {
        long[] packed = new long[standing.size()];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = standing.get(i).asLong();
        }
        site.set(ExtractionSite.CACHE_CHESTS, packed);
        this.markDirty(SITES);
    }

    private static boolean farFrom(BlockPos pos, List<ServerPlayer> players, int clearance) {
        for (ServerPlayer player : players) {
            if (player.blockPosition().distSqr(pos) < (double) clearance * clearance) {
                return false;
            }
        }
        return true;
    }

    private static boolean farFromAll(BlockPos pos, List<BlockPos> others, int spacing) {
        for (BlockPos other : others) {
            if (Math.max(Math.abs(other.getX() - pos.getX()), Math.abs(other.getZ() - pos.getZ())) < spacing && Math.abs(other.getY() - pos.getY()) < spacing + 2) {
                return false;
            }
        }
        return true;
    }

    /** Places one cache chest inside the room's no-modify zone by allowing that one position for the duration of the call. */
    private boolean placeCache(VirtualWorld world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }
        List<WorldZone> zones = WorldZonesData.get(world.getServer()).getOrCreate(world.dimension()).get(pos);
        for (WorldZone zone : zones) {
            zone.addAllowedPosition(pos);
        }
        try {
            world.setBlock(pos, ModBlocks.EXTRACTION_CACHE.defaultBlockState()
                    .setValue(ExtractionCacheBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random)), 3);
        } finally {
            for (WorldZone zone : zones) {
                if (zone.getAllowedPositions() != null) {
                    zone.getAllowedPositions().remove(pos.asLong());
                }
            }
        }
        boolean ok = world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE);
        if (!ok) {
            WoldsVaults.LOGGER.error("Cache chest placement at {} was refused by the world (zone or mixin); skipping that spot.", pos);
        }
        return ok;
    }

    /** Removes every standing cache chest of the site: unmined chests vanish when its fight ends. */
    private void clearCaches(VirtualWorld world, ExtractionSite site) {
        if (world == null || !site.has(ExtractionSite.CACHE_CHESTS)) {
            return;
        }
        int removed = 0;
        for (long packed : site.get(ExtractionSite.CACHE_CHESTS)) {
            BlockPos pos = BlockPos.of(packed);
            if (world.isLoaded(pos) && world.getBlockState(pos).is(ModBlocks.EXTRACTION_CACHE)) {
                world.removeBlock(pos, false);
                removed++;
            }
        }
        site.set(ExtractionSite.CACHE_CHESTS, new long[0]);
        this.markDirty(SITES);
        if (removed > 0) {
            WoldsVaults.LOGGER.info("Extraction site {} fight ended; removed {} unmined cache chests.", this.indexOf(site), removed);
        }
    }


    private Vault vaultOf(ExtractorEntity extractor) {
        return VaultUtils.getVault(extractor.level).orElse(null);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        if (world.getGameTime() % ROOM_LOAD_CHECK_TICKS == 0L) {
            guarded("site room loading", () -> this.updateRoomLoading(world));
        }
        if (vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            return;
        }
        guarded("room grids and thumpers", () -> this.tickRooms(world));
        if (this.spawnManager != null) {
            this.spawnManager.tick();
        }
        if (world.getGameTime() % 3L == 0L) {
            guarded("pedestal sparkle", () -> this.sparklePedestals(world, vault));
        }
        if (this.get(COMPLETED)) {
            super.tickServer(world, vault);
            return;
        }
        ExtractionSite[] sites = this.get(SITES);
        boolean ticked = false;
        for (ExtractionSite site : sites) {
            if (site.isDrilling()) {
                this.tickSite(world, vault, site);
                ticked = true;
            }
        }
        if (ticked) {
            this.markDirty(SITES);
        }
    }

    @Override
    public void releaseServer() {
        if (this.world != null) {
            for (Integer siteIndex : new ArrayList<>(this.loadedRooms.keySet())) {
                this.unloadRoom(this.world, siteIndex);
            }
        }
        super.releaseServer();
    }

    /**
     * Keeps a site room's chunks entity-ticking while an extractor stands in it or its wave mobs are still alive, so
     * mobs keep attacking rigs anywhere in the room wherever the players are. Chunk tickets are not saved; they are
     * re-added from the site state after a restart and dropped when the room goes quiet or the vault unloads.
     */
    private void updateRoomLoading(VirtualWorld world) {
        ExtractionSite[] sites = this.get(SITES);
        for (int i = 0; i < sites.length; i++) {
            boolean loaded = this.loadedRooms.containsKey(i);
            boolean staging = !sites[i].isComplete() && sites[i].thumpersPlaced() > 0;
            boolean active = sites[i].isOccupied() || staging || (loaded && this.hasLiveWaveMobs(world, sites[i]));
            if (active && !loaded) {
                this.loadRoom(world, i, sites[i]);
            } else if (!active && loaded) {
                this.unloadRoom(world, i);
            }
        }
    }

    private boolean hasLiveWaveMobs(VirtualWorld world, ExtractionSite site) {
        return !world.getEntitiesOfClass(Mob.class, this.siteRoomBounds(site),
                mob -> mob.isAlive() && mob.getTags().contains(ExtractionSpawnManager.TAG_WAVE)).isEmpty();
    }

    private void loadRoom(VirtualWorld world, int siteIndex, ExtractionSite site) {
        AABB bounds = this.siteRoomBounds(site);
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = ((int) Math.floor(bounds.minX)) >> 4; cx <= ((int) Math.ceil(bounds.maxX) - 1) >> 4; cx++) {
            for (int cz = ((int) Math.floor(bounds.minZ)) >> 4; cz <= ((int) Math.ceil(bounds.maxZ) - 1) >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                world.getChunkSource().addRegionTicket(ROOM_TICKET, chunk, ROOM_TICKET_DISTANCE, chunk);
                chunks.add(chunk);
            }
        }
        this.loadedRooms.put(siteIndex, chunks);
        WoldsVaults.LOGGER.info("Extraction site {} is active; holding its {} room chunks loaded.", siteIndex, chunks.size());
    }

    private void unloadRoom(VirtualWorld world, int siteIndex) {
        List<ChunkPos> chunks = this.loadedRooms.remove(siteIndex);
        if (chunks == null) {
            return;
        }
        for (ChunkPos chunk : chunks) {
            world.getChunkSource().removeRegionTicket(ROOM_TICKET, chunk, ROOM_TICKET_DISTANCE, chunk);
        }
        WoldsVaults.LOGGER.info("Extraction site {} went quiet; released its {} room chunks.", siteIndex, chunks.size());
    }

    /**
     * Marks every pad that still wants a rig with sparkles visible from anywhere in its room: the main pad gets a
     * dense, vibrant column in its god's colour, thumper pads a light purple shimmer.
     */
    private void sparklePedestals(VirtualWorld world, Vault vault) {
        ExtractionSite[] sites = this.get(SITES);
        List<ServerPlayer> players = new ArrayList<>();
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(players::add);
        }
        if (players.isEmpty()) {
            return;
        }
        boolean twinkle = world.getGameTime() % 9L == 0L;
        DustParticleOptions thumperDust = new DustParticleOptions(rgb(THUMPER_DUST), 0.8F);
        for (int i = 0; i < sites.length; i++) {
            ExtractionSite site = sites[i];
            if (site.isComplete()) {
                continue;
            }
            AABB visibleFrom = this.siteRoomBounds(site).inflate(8.0D);
            List<ServerPlayer> watching = new ArrayList<>();
            for (ServerPlayer player : players) {
                if (player.level == world && visibleFrom.contains(player.position())) {
                    watching.add(player);
                }
            }
            if (watching.isEmpty()) {
                continue;
            }
            List<Vec3> pedestals = this.sitePedestals(site);
            boolean staged = pedestals.size() > ExtractionSite.THUMPER_SLOTS;
            boolean mainLive = this.getLiveExtractor(world, site) != null;
            VaultGod god = VaultGod.fromName(site.get(ExtractionSite.GOD));
            if (god == null && this.sparkleFallbackLogged.add(1000 + i)) {
                WoldsVaults.LOGGER.error("Extraction site {} names god '{}', which does not exist; its main pad sparkles white.", i, site.get(ExtractionSite.GOD));
            }
            Vector3f godColour = rgb(god == null ? 0xFFFFFF : god.getColor());
            DustColorTransitionOptions mainDust = new DustColorTransitionOptions(godColour, rgb(0xFFFFFF), 1.6F);
            DustParticleOptions burst = new DustParticleOptions(godColour, 2.0F);
            for (int point = 0; point < pedestals.size(); point++) {
                boolean thumperPad = staged && point != MAIN_PAD;
                boolean wants;
                if (thumperPad) {
                    int state = site.getThumperState(point - 1);
                    wants = state == ExtractionSite.THUMPER_NONE || (state == ExtractionSite.THUMPER_WRECKED && !mainLive);
                } else {
                    wants = !mainLive;
                }
                if (!wants) {
                    continue;
                }
                Vec3 center = pedestals.get(point);
                BlockPos probe = new BlockPos(center);
                if (!world.isLoaded(probe)) {
                    continue;
                }
                double top = this.pedestalTop(world, probe, center.y, i * 8 + point);
                for (ServerPlayer player : watching) {
                    if (thumperPad) {
                        world.sendParticles(player, thumperDust, true, center.x, top + 0.4D, center.z, 1, 0.3D, 0.3D, 0.3D, 0.0D);
                        if (twinkle) {
                            world.sendParticles(player, ParticleTypes.WITCH, true, center.x, top + 0.6D, center.z, 1, 0.3D, 0.3D, 0.3D, 0.0D);
                        }
                    } else {
                        world.sendParticles(player, mainDust, true, center.x, top + 0.5D, center.z, 4, 0.35D, 0.5D, 0.35D, 0.0D);
                        world.sendParticles(player, ParticleTypes.END_ROD, true, center.x, top + 0.3D, center.z, 2, 0.25D, 0.2D, 0.25D, 0.01D);
                        if (twinkle) {
                            world.sendParticles(player, burst, true, center.x, top + 1.2D, center.z, 6, 0.4D, 0.75D, 0.4D, 0.0D);
                        }
                    }
                }
            }
        }
    }

    private static Vector3f rgb(int colour) {
        return new Vector3f(((colour >> 16) & 0xFF) / 255.0F, ((colour >> 8) & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F);
    }

    private double pedestalTop(VirtualWorld world, BlockPos probe, double nominalY, int siteIndex) {
        for (int i = 0; i <= 3; i++) {
            BlockPos pos = probe.below(i);
            if (!world.getBlockState(pos).isAir()) {
                return pos.getY() + 1.0D;
            }
        }
        if (this.sparkleFallbackLogged.add(siteIndex)) {
            WoldsVaults.LOGGER.error("Extraction site {} has no solid pedestal block within 3 below {}; sparkling at the nominal pedestal height instead.", siteIndex, probe);
        }
        return nominalY;
    }

    private void tickSite(VirtualWorld world, Vault vault, ExtractionSite site) {
        ExtractorEntity extractor = this.getLiveExtractor(world, site);
        if (extractor == null) {
            return;
        }
        this.mirror(site, extractor);
        if (extractor.isJobDone()) {
            return;
        }
        ExtractionObjectiveConfig.JobEntry job = ModConfigs.EXTRACTION_OBJECTIVE.getJob(extractor.getJobId());
        if (job == null) {
            WoldsVaults.LOGGER.error("Deployed extractor at site ({}, {}) has unknown job '{}'; it cannot progress.",
                    site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z), extractor.getJobId());
            return;
        }
        if (extractor.getEnergy() <= 0.0F) {
            extractor.setDrillingAnim(false);
            return;
        }
        extractor.setDrillingAnim(true);
        float rate = this.isStaged(site)
                ? Math.max(0.0F, 1.0F - ModConfigs.EXTRACTION_OBJECTIVE.THUMPER.progressLossPerThumper * site.thumpersWrecked())
                : 1.0F;
        site.modify(ExtractionSite.PROGRESS, progress -> progress + rate);
        for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
            ThumperEntity thumper = this.getThumper(world, site, slot);
            if (thumper != null) {
                thumper.tickRegen();
            }
        }
        extractor.setEnergy(extractor.getEnergy() - extractor.getEnergyCostPerSecond() / 20.0F);
        extractor.drainDurability(extractor.getWearPerTick());
        extractor.drainAttachmentDurability(ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY.attachmentDrainPerSecondMining / 20.0F);
        extractor.tickShieldRegen();
        extractor.tickHealthRegen();
        if (site.get(ExtractionSite.PROGRESS) >= site.get(ExtractionSite.REQUIRED)) {
            this.completeSite(world, vault, site, extractor, job);
        }
    }

    private void mirror(ExtractionSite site, ExtractorEntity extractor) {
        site.set(ExtractionSite.HP, extractor.getHealth());
        site.set(ExtractionSite.MAX_HP, extractor.getMaxHealth());
        site.set(ExtractionSite.SHIELD, extractor.getShield());
        site.set(ExtractionSite.MAX_SHIELD, extractor.getMaxShield());
        site.set(ExtractionSite.ENERGY, extractor.getEnergy());
        site.set(ExtractionSite.MAX_ENERGY, extractor.getEnergyCapacity());
    }

    private void completeSite(VirtualWorld world, Vault vault, ExtractionSite site, ExtractorEntity extractor, ExtractionObjectiveConfig.JobEntry job) {
        NonNullList<ItemStack> payout = ExtractionPayouts.roll(extractor.getJobId(), job, extractor, vault, extractor.blockPosition(), world.getRandom());
        extractor.completeJob(payout);
        site.set(ExtractionSite.STATE, ExtractionSite.STATE_COMPLETE);
        this.clearCaches(world, site);
        site.set(ExtractionSite.PROGRESS, (float) (int) site.get(ExtractionSite.REQUIRED));
        this.mirror(site, extractor);
        int complete = 0;
        for (ExtractionSite s : this.get(SITES)) {
            if (s.isComplete()) {
                complete++;
            }
        }
        broadcast(vault, new TextComponent(String.format("An extraction site finished drilling! (%d/%d)", complete, this.get(SITES).length)).withStyle(ChatFormatting.GOLD));
        if (complete >= this.get(SITES).length) {
            this.set(COMPLETED, true);
            broadcast(vault, new TextComponent("All extraction sites are complete!").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }
        if (listener instanceof Runner && listener.getPlayer().isPresent() && this.discoveredPlayers.add(listener.getPlayer().get().getUUID())) {
            guarded("site pre-discovery", () -> this.discoverSitesFor(listener.getPlayer().get(), vault));
        }
        if (listener instanceof Runner && this.get(COMPLETED)) {
            super.tickListener(world, vault, listener);
        }
    }

    /** Marks the three site rooms as discovered on this player's vault map, purple background and all. */
    private void discoverSitesFor(ServerPlayer player, Vault vault) {
        VaultGenerator generator = vault.get(Vault.WORLD).get(WorldManager.GENERATOR);
        if (!(generator instanceof GridGenerator gridGenerator)) {
            return;
        }
        Object layoutObj = gridGenerator.get(GridGenerator.LAYOUT);
        if (!(layoutObj instanceof VaultGridLayout layout)) {
            return;
        }
        StatCollector stats = vault.get(Vault.STATS).get(player.getUUID());
        DiscoveredRoomStat rooms = stats.get(StatCollector.ROOMS_DISCOVERED);
        for (ExtractionSite site : this.get(SITES)) {
            RegionPos region = RegionPos.of(site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z), this.get(CELL_X), this.get(CELL_Z));
            Optional<DiscoveredRoomStat.RoomDiscovery> discovery = DiscoveredRoomStat.resolveRoom(vault, gridGenerator, layout, region);
            if (discovery.isEmpty()) {
                WoldsVaults.LOGGER.error("Extraction site room at region ({}, {}) could not be resolved for map pre-discovery.",
                        site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z));
                continue;
            }
            DiscoveredRoomStat.recordDiscovery(player, vault, rooms, stats.getDetailedRoomsDiscovered(), discovery.get(), DiscoverVaultRoomEvent.Context.SYSTEM);
        }
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if (!this.get(COMPLETED)) {
            return objective == this;
        }
        for (Objective child : this.get(CHILDREN)) {
            if (child.isActive(world, vault, objective)) {
                return true;
            }
        }
        return false;
    }

    private static void broadcast(Vault vault, net.minecraft.network.chat.Component message) {
        if (vault == null) {
            return;
        }
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(player -> player.displayClientMessage(message, false));
        }
    }

    private static void guarded(String what, Runnable body) {
        try {
            body.run();
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Extraction handler '{}' failed!", what, e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        if (this.get(COMPLETED)) {
            boolean rendered = false;
            for (Objective child : this.get(CHILDREN)) {
                rendered |= child.render(vault, matrixStack, window, partialTicks, player);
            }
            if (rendered) {
                return true;
            }
        }
        ExtractionSite[] sites = this.get(SITES);
        if (sites.length == 0) {
            return false;
        }
        ExtractionSite nearby = this.findNearbyDrillingSite(sites, player);
        boolean showNumbers = ModOptions.ELIXIR_ORB_NUMBERS.getValue();
        if (nearby != null) {
            this.renderPlacedHud(matrixStack, nearby, sites, showNumbers, player, partialTicks);
        } else {
            this.renderIdleHud(matrixStack, sites);
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private ExtractionSite findNearbyDrillingSite(ExtractionSite[] sites, Player player) {
        int cellX = Math.max(1, this.get(CELL_X));
        int cellZ = Math.max(1, this.get(CELL_Z));
        RegionPos playerRegion = RegionPos.ofBlockPos(player.blockPosition(), cellX, cellZ);
        for (ExtractionSite site : sites) {
            if (!site.isDrilling() || !site.isOccupied()) {
                continue;
            }
            int half = site.getSpan() / 2;
            int dx = Math.abs(playerRegion.getX() - (site.get(ExtractionSite.REGION_X) + half));
            int dz = Math.abs(playerRegion.getZ() - (site.get(ExtractionSite.REGION_Z) + half));
            if (dx <= 2 + half && dz <= 2 + half) {
                return site;
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderPlacedHud(PoseStack matrixStack, ExtractionSite site, ExtractionSite[] sites, boolean showNumbers, Player player, float partialTicks) {
        float progress = clamp01(site.get(ExtractionSite.PROGRESS) / Math.max(1.0F, site.get(ExtractionSite.REQUIRED)));
        float hpFrac = frac(site.get(ExtractionSite.HP), site.get(ExtractionSite.MAX_HP));
        float shieldFrac = frac(site.get(ExtractionSite.SHIELD), site.get(ExtractionSite.MAX_SHIELD));
        float energyFrac = frac(site.get(ExtractionSite.ENERGY), site.get(ExtractionSite.MAX_ENERGY));
        long gameTime = player.level.getGameTime();
        float t = (gameTime + partialTicks) * 0.06F;

        matrixStack.pushPose();
        matrixStack.translate(-115.0D, 0.0D, 0.0D);
        bindHud();
        GuiComponent.blit(matrixStack, 11, 0, 0, 185, 176, 6, 256, 256);
        GuiComponent.blit(matrixStack, 195, 0, 0, 0, 35, 48, 256, 256);
        this.blitBar(matrixStack, 9, 4, 0, 49, 0, 68, 181, 68, 180, 18, 176, progress);
        this.blitBubbles(matrixStack, 11, 6, 14, 176, Math.round(176.0F * progress), 6, 5, t);
        this.blitBar(matrixStack, 22, 19, 0, 87, 0, 104, 131, 104, 130, 16, 126, hpFrac);
        this.blitBubbles(matrixStack, 24, 21, 12, 126, Math.round(126.0F * hpFrac), 5, 13, t + 2.1F);
        this.blitBar(matrixStack, 22, 31, 0, 121, 0, 138, 131, 138, 130, 16, 126, shieldFrac);
        this.blitBubbles(matrixStack, 24, 33, 12, 126, Math.round(126.0F * shieldFrac), 5, 13, t + 4.3F);
        GuiComponent.blit(matrixStack, 0, 2, 186, 0, 22, 22, 256, 256);
        GuiComponent.blit(matrixStack, 176, 2, 209, 0, 22, 22, 256, 256);
        GuiComponent.blit(matrixStack, 9, 19, 135, 138, 16, 16, 256, 256);
        GuiComponent.blit(matrixStack, 9, 31, 152, 138, 16, 16, 256, 256);
        for (int i = 0; i < sites.length - 1; i++) {
            GuiComponent.blit(matrixStack, 159 + 15 * i, 26, 119, 155, 15, 14, 256, 256);
        }
        for (int i = 0; i < sites.length; i++) {
            this.blitPip(matrixStack, 151 + 15 * i, 25, sites[i]);
        }
        GuiComponent.blit(matrixStack, 198, 2, 36, 0, 29, 44, 256, 256);
        int lit = Math.round(40.0F * energyFrac);
        if (lit >= 1) {
            int frame = (int) ((gameTime / 3L) % 4L);
            GuiComponent.blit(matrixStack, 198, 4 + 40 - lit, 66 + 30 * frame, PAD + 40 - lit, 25 + 2 * PAD, lit + PAD, 256, 256);
        }
        if (this.isStagedClient(site)) {
            for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
                this.renderThumperBar(matrixStack, site, slot, THUMPER_BAR_X[slot], THUMPER_BAR_Y);
            }
        }
        matrixStack.popPose();

        if (showNumbers) {
            Font font = Minecraft.getInstance().font;
            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            matrixStack.pushPose();
            matrixStack.translate(-115.0D, 0.0D, 100.0D);
            matrixStack.scale(0.7F, 0.7F, 0.7F);
            drawCentered(font, buffer, matrixStack, String.format("%.2f%%", progress * 100.0F), 99.0F / 0.7F, (13.0F - 3.5F) / 0.7F, 0xFFF6E4);
            drawCentered(font, buffer, matrixStack, String.format("%,.0f/%,.0f", site.get(ExtractionSite.HP), site.get(ExtractionSite.MAX_HP)), 80.5F / 0.7F, (27.0F - 3.5F) / 0.7F, 0xE2FFEA);
            drawCentered(font, buffer, matrixStack, String.format("%,.0f/%,.0f", site.get(ExtractionSite.SHIELD), site.get(ExtractionSite.MAX_SHIELD)), 80.5F / 0.7F, (39.0F - 3.5F) / 0.7F, 0xE2F4FF);
            buffer.endBatch();
            matrixStack.popPose();
            if (this.isStagedClient(site)) {
                matrixStack.pushPose();
                matrixStack.translate(-115.0D, 0.0D, 100.0D);
                matrixStack.scale(0.5F, 0.5F, 0.5F);
                for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
                    int state = site.getThumperState(slot);
                    String label = state == ExtractionSite.THUMPER_STANDING
                            ? String.format("%,.0f/%,.0f", site.get(ExtractionSite.THUMPER_HP[slot]), site.get(ExtractionSite.THUMPER_MAX_HP[slot]))
                            : state == ExtractionSite.THUMPER_WRECKED ? "WRECKED" : "EMPTY";
                    float cx = THUMPER_BAR_X[slot] + THUMPER_BAR_W / 2.0F;
                    drawCentered(font, buffer, matrixStack, label, cx / 0.5F, (THUMPER_BAR_Y + 3.0F) / 0.5F,
                            state == ExtractionSite.THUMPER_WRECKED ? 0xFF8A80 : 0xE2FFEA);
                }
                buffer.endBatch();
                matrixStack.popPose();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderIdleHud(PoseStack matrixStack, ExtractionSite[] sites) {
        int width = 16 + 15 * (sites.length - 1);
        matrixStack.pushPose();
        matrixStack.translate(-width / 2.0D, 0.0D, 0.0D);
        bindHud();
        for (int i = 0; i < sites.length - 1; i++) {
            GuiComponent.blit(matrixStack, 8 + 15 * i, 1, 119, 155, 15, 14, 256, 256);
        }
        for (int i = 0; i < sites.length; i++) {
            this.blitPip(matrixStack, 15 * i, 0, sites[i]);
        }
        matrixStack.popPose();
    }

    private static final int[] THUMPER_BAR_X = {22, 112};
    private static final int THUMPER_BAR_Y = 48;
    private static final int THUMPER_BAR_W = 86;
    private static final float THUMPER_BAR_SCALE = 86.0F / 131.0F;

    /** Client side: 3x3 site rooms have thumper pads, so their placed HUD shows the thumper row. */
    @OnlyIn(Dist.CLIENT)
    private boolean isStagedClient(ExtractionSite site) {
        return site.getSpan() > 1;
    }

    /**
     * One compact thumper bar: the main HP bar sprite at two thirds size with a shield strip along its top edge. A
     * slot with no thumper is drawn faded; a wrecked one gets a dark red wash.
     */
    @OnlyIn(Dist.CLIENT)
    private void renderThumperBar(PoseStack matrixStack, ExtractionSite site, int slot, int x, int y) {
        int state = site.getThumperState(slot);
        boolean standing = state == ExtractionSite.THUMPER_STANDING;
        float hpFrac = standing ? frac(site.get(ExtractionSite.THUMPER_HP[slot]), site.get(ExtractionSite.THUMPER_MAX_HP[slot])) : 0.0F;
        float shieldFrac = standing ? frac(site.get(ExtractionSite.THUMPER_SHIELD[slot]), site.get(ExtractionSite.THUMPER_MAX_SHIELD[slot])) : 0.0F;
        bindHud();
        if (state == ExtractionSite.THUMPER_NONE) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
        }
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0.0D);
        matrixStack.scale(THUMPER_BAR_SCALE, THUMPER_BAR_SCALE, 1.0F);
        this.blitBar(matrixStack, 0, 0, 0, 87, 0, 104, 131, 104, 130, 16, 126, hpFrac);
        matrixStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int innerLeft = x + 2;
        int innerRight = x + THUMPER_BAR_W - 9;
        if (standing && site.get(ExtractionSite.THUMPER_MAX_SHIELD[slot]) > 0.0F) {
            int w = Math.round((innerRight - innerLeft) * shieldFrac);
            GuiComponent.fill(matrixStack, innerLeft, y + 2, innerRight, y + 4, 0x80203A4A);
            if (w > 0) {
                GuiComponent.fill(matrixStack, innerLeft, y + 2, innerLeft + w, y + 4, 0xFF7FD7FF);
            }
        }
        if (state == ExtractionSite.THUMPER_WRECKED) {
            GuiComponent.fill(matrixStack, innerLeft, y + 2, innerRight, y + 9, 0xA0500000);
        }
    }

    /** Track, then the partial fill cut with a clean vertical edge, then the bright head column. */
    @OnlyIn(Dist.CLIENT)
    private void blitBar(PoseStack matrixStack, int blitX, int blitY, int trackU, int trackV, int fillU, int fillV, int edgeU, int edgeV, int spriteW, int spriteH, int barW, float fraction) {
        GuiComponent.blit(matrixStack, blitX, blitY, trackU, trackV, spriteW, spriteH, 256, 256);
        int w = Math.round(barW * clamp01(fraction));
        if (w >= 1) {
            GuiComponent.blit(matrixStack, blitX, blitY, fillU, fillV, PAD + w, spriteH, 256, 256);
        }
        if (w > 0 && w < barW) {
            GuiComponent.blit(matrixStack, blitX + PAD + w - 1, blitY, edgeU, edgeV, 3, spriteH, 256, 256);
        }
    }

    /** The bubble field is periodic with period 6, so a 6-tall window scrolled by v wraps forever. */
    @OnlyIn(Dist.CLIENT)
    private void blitBubbles(PoseStack matrixStack, int barX, int barY, int barH, int barW, int fillW, int marginLeft, int cutRight, float phase) {
        int bx = barX + marginLeft;
        int bw = Math.min(fillW - marginLeft, barW - cutRight - 1);
        if (bw > 1) {
            float sv = ((phase % 6.0F) + 6.0F) % 6.0F;
            GuiComponent.blit(matrixStack, bx, barY + (barH - 6) / 2, bw, 6, 0.0F, 172.0F + sv, bw, 6, 256, 256);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void blitPip(PoseStack matrixStack, int x, int y, ExtractionSite site) {
        String god = site.get(ExtractionSite.GOD);
        int[][] rows = PIP_UVS.get(god);
        if (rows == null) {
            if (UNKNOWN_PIP_GODS.add(god)) {
                WoldsVaults.LOGGER.error("Extraction HUD has no pip sprites for god '{}'; drawing the idona set instead.", god);
            }
            rows = PIP_UVS.get("idona");
        }
        int state = Math.max(0, Math.min(2, site.get(ExtractionSite.STATE)));
        GuiComponent.blit(matrixStack, x, y, rows[state][0], rows[state][1], 16, 16, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    private static void bindHud() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, HUD);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawCentered(Font font, MultiBufferSource.BufferSource buffer, PoseStack matrixStack, String text, float centerX, float y, int color) {
        TextComponent component = new TextComponent(text);
        font.drawInBatch(component.getVisualOrderText(), centerX - font.width(component) / 2.0F, y, color, true,
                matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
    }

    private static float clamp01(float value) {
        return Math.min(1.0F, Math.max(0.0F, value));
    }

    private static float frac(float current, float max) {
        return max <= 0.0F ? 0.0F : clamp01(current / max);
    }
}

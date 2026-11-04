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
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.data.key.TemplatePoolKey;
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
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
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
import java.util.HashSet;
import java.util.List;
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
    private static final ResourceLocation ROOM_POOL = WoldsVaults.id("vault/rooms/special/extraction");
    private static final BlockPos[] CAVE_ANCHORS = new BlockPos[]{
            new BlockPos(5, 20, 5), new BlockPos(41, 20, 5), new BlockPos(5, 20, 41), new BlockPos(41, 20, 41)
    };

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
        guarded("site selection", () -> {
            if (this.get(SITES).length == 0) {
                this.pickSites(vault);
            }
        });

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            if (data.getVault() == vault) {
                data.setProbability(0.0D);
            }
        });

        CommonEvents.LAYOUT_TEMPLATE_GENERATION.register(this, data -> guarded("extraction room layout", () -> {
            if (data.getVault() != vault || data.getPieceType() != VaultLayout.PieceType.ROOM || data.getTemplate() != null) {
                return;
            }
            ExtractionSite site = this.siteAtRegion(data.getRegion().getX(), data.getRegion().getZ());
            if (site == null) {
                return;
            }
            TemplatePoolKey poolKey = VaultRegistry.TEMPLATE_POOL.getKey(ROOM_POOL);
            if (poolKey == null) {
                WoldsVaults.LOGGER.error("Extraction room template pool '{}' is missing; the site room cannot be forced!", ROOM_POOL);
                return;
            }
            Template template = data.getLayout().getRoom(poolKey.get(vault.get(Vault.VERSION)), vault, vault.get(Vault.VERSION),
                    data.getRegion(), data.getRandom(), data.getSettings());
            this.applyGodPalette(data.getSettings(), site);
            data.setTemplate(template);
        }));

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
            if (entity.getTags().contains(ExtractionSpawnManager.TAG_CARRIER)) {
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
     * Seeds the site layout from the vault seed: ROOM cells on the 4-9 room ring around the start with
     * pairwise room separation of at least the configured minimum, each with a distinct god colour.
     * Falls back (with an error log) to relaxed rings if the rolled layout cannot satisfy the ring.
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
        int radius = Math.max(2, VaultUtils.getVaultRadius(layout, vault));
        List<int[]> ringCandidates = new ArrayList<>();
        List<int[]> anyCandidates = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                RegionPos region = RegionPos.of(x, z, cellX, cellZ);
                if (layout.getType(vault, region) != VaultLayout.PieceType.ROOM || (x == 0 && z == 0)) {
                    continue;
                }
                int roomDistance = Math.max(Math.abs(x), Math.abs(z)) / 2;
                anyCandidates.add(new int[]{x, z});
                if (roomDistance >= cfg.ringMinRooms && roomDistance <= cfg.ringMaxRooms) {
                    ringCandidates.add(new int[]{x, z});
                }
            }
        }
        Random random = new Random(vault.get(Vault.SEED) ^ 0x455854524143L);
        List<int[]> chosen = choose(ringCandidates, cfg.count, cfg.minSeparationRooms * 2, random);
        if (chosen.size() < cfg.count) {
            WoldsVaults.LOGGER.error("Extraction site ring {}..{} only fit {} of {} sites in this layout; relaxing to the whole vault.",
                    cfg.ringMinRooms, cfg.ringMaxRooms, chosen.size(), cfg.count);
            chosen = choose(anyCandidates, cfg.count, cfg.minSeparationRooms * 2, random);
        }
        if (chosen.size() < cfg.count) {
            WoldsVaults.LOGGER.error("Extraction site separation {} rooms could not be satisfied either; placing {} unseparated sites.",
                    cfg.minSeparationRooms, cfg.count);
            chosen = choose(anyCandidates, cfg.count, 0, random);
        }
        List<VaultGod> gods = new ArrayList<>(Arrays.asList(VaultGod.values()));
        Collections.shuffle(gods, random);
        ExtractionObjectiveConfig.JobEntry defaultJob = ModConfigs.EXTRACTION_OBJECTIVE.getJob(ModConfigs.EXTRACTION_OBJECTIVE.DEFAULT_EXTRACTOR.job);
        int requiredTicks = defaultJob == null ? 2400 : defaultJob.baseDurationTicks;
        ExtractionSite[] sites = new ExtractionSite[chosen.size()];
        for (int i = 0; i < chosen.size(); i++) {
            String god = gods.get(i % gods.size()).getName().toLowerCase(java.util.Locale.ROOT);
            sites[i] = new ExtractionSite(chosen.get(i)[0], chosen.get(i)[1], god, requiredTicks);
        }
        this.set(SITES, sites);
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
            BlockPos origin = this.siteRoomOrigin(site);
            BlockCuboid cuboid = BlockCuboid.of(origin, origin.offset(46, 46, 46));
            WorldZonesData.get(world.getServer()).getOrCreate(world.dimension()).add(new WorldZone().add(cuboid).setModify(false));
        }
        this.set(ZONES_ADDED, true);
    }

    private ExtractionSite siteAtRegion(int regionX, int regionZ) {
        for (ExtractionSite site : this.get(SITES)) {
            if (site.get(ExtractionSite.REGION_X) == regionX && site.get(ExtractionSite.REGION_Z) == regionZ) {
                return site;
            }
        }
        return null;
    }

    public BlockPos siteRoomOrigin(ExtractionSite site) {
        int cellX = this.get(CELL_X);
        int cellZ = this.get(CELL_Z);
        RegionPos region = RegionPos.of(site.get(ExtractionSite.REGION_X), site.get(ExtractionSite.REGION_Z), cellX, cellZ);
        return region.toBlockPos().above(cellX > 47 ? 0 : 9);
    }

    public AABB siteRoomBounds(ExtractionSite site) {
        BlockPos origin = this.siteRoomOrigin(site);
        return new AABB(origin, origin.offset(47, 47, 47));
    }

    public Vec3 sitePedestalCenter(ExtractionSite site) {
        BlockPos origin = this.siteRoomOrigin(site);
        return new Vec3(origin.getX() + 23.5D, origin.getY() + 21.0D, origin.getZ() + 23.5D);
    }

    public BlockPos randomCaveAnchor(ExtractionSite site, Random random) {
        BlockPos origin = this.siteRoomOrigin(site);
        BlockPos local = CAVE_ANCHORS[random.nextInt(CAVE_ANCHORS.length)];
        return origin.offset(local.getX(), local.getY(), local.getZ());
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
        ExtractionSite[] sites = this.get(SITES);
        for (int i = 0; i < sites.length; i++) {
            Vec3 pedestal = this.sitePedestalCenter(sites[i]);
            if (pedestal.distanceTo(Vec3.atCenterOf(clickedPos)) <= 3.5D) {
                target = sites[i];
                targetIndex = i;
                break;
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
        broadcast(this.vaultOf(extractor), new TextComponent("An extractor was destroyed! Its drill site holds its progress.").withStyle(ChatFormatting.RED));
    }

    public void onExtractorCollected(ExtractorEntity extractor) {
        this.vacate(extractor);
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

    private Vault vaultOf(ExtractorEntity extractor) {
        return VaultUtils.getVault(extractor.level).orElse(null);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        if (vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            return;
        }
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

    /** Marks every pedestal that still wants an extractor with a sparkle column visible from anywhere in its room. */
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
        for (int i = 0; i < sites.length; i++) {
            ExtractionSite site = sites[i];
            if (site.isComplete() || this.getLiveExtractor(world, site) != null) {
                continue;
            }
            AABB visibleFrom = this.siteRoomBounds(site).inflate(8.0D);
            List<ServerPlayer> watching = new ArrayList<>();
            for (ServerPlayer player : players) {
                if (player.level == world && visibleFrom.contains(player.position())) {
                    watching.add(player);
                }
            }
            Vec3 center = this.sitePedestalCenter(site);
            BlockPos probe = new BlockPos(center);
            if (watching.isEmpty() || !world.isLoaded(probe)) {
                continue;
            }
            double top = this.pedestalTop(world, probe, center.y, i);
            for (ServerPlayer player : watching) {
                world.sendParticles(player, ParticleTypes.END_ROD, true, center.x, top + 0.3D, center.z, 2, 0.25D, 0.2D, 0.25D, 0.01D);
                if (twinkle) {
                    world.sendParticles(player, ParticleTypes.FIREWORK, true, center.x, top + 0.6D, center.z, 3, 0.3D, 0.3D, 0.3D, 0.02D);
                }
            }
        }
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
        site.modify(ExtractionSite.PROGRESS, progress -> progress + 1.0F);
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
            int dx = Math.abs(playerRegion.getX() - site.get(ExtractionSite.REGION_X));
            int dz = Math.abs(playerRegion.getZ() - site.get(ExtractionSite.REGION_Z));
            if (dx <= 2 && dz <= 2) {
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

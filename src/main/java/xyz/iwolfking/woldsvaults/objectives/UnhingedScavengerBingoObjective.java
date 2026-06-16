package xyz.iwolfking.woldsvaults.objectives;

import iskallia.vault.VaultMod;
import iskallia.vault.config.ScavengerConfig;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.scavenger.*;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ScavengerBingoObjectiveAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.TileCandidateAccessor;

import java.lang.reflect.Constructor;
import java.util.*;

public class UnhingedScavengerBingoObjective extends ScavengerBingoObjective {
    public static final SupplierKey<Objective> KEY = SupplierKey.of("unhinged_scavenger_bingo", Objective.class).with(Version.v1_56, UnhingedScavengerBingoObjective::new);

    private static Constructor<?> candicateCtor = null;

    static {
        try {
            var clazz = Class.forName("iskallia.vault.core.vault.objective.ScavengerBingoObjective$TileCandidate");
            Constructor<?> ctor = clazz.getDeclaredConstructor(ItemStack.class, ScavengerBingoTile.SourceType.class, ResourceLocation.class, double.class, int.class);
            ctor.setAccessible(true);
            candicateCtor = ctor;

        } catch (Throwable e) {
            System.out.println(e);
        }

    }

    protected UnhingedScavengerBingoObjective() {
        super();
    }

    public static UnhingedScavengerBingoObjective of(int width, int height, float objectiveProbability, ResourceLocation entryPool) {
        return of(width, height, objectiveProbability, entryPool, false, null);
    }

    public static UnhingedScavengerBingoObjective of(int width, int height, float objectiveProbability, ResourceLocation entryPool, boolean blackout) {
        return of(width, height, objectiveProbability, entryPool, blackout, null);
    }

    public static UnhingedScavengerBingoObjective of(
        int width, int height, float objectiveProbability, ResourceLocation entryPool, boolean blackout, ResourceLocation modifierPool
    ) {
        UnhingedScavengerBingoObjective obj = new UnhingedScavengerBingoObjective();
        obj.set(WIDTH, width);
        obj.set(HEIGHT, height);
        obj.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        obj.set(ENTRY_POOL, entryPool);
        obj.set(BLACKOUT, blackout);
        if (modifierPool != null) {
            obj.set(MODIFIER_POOL, modifierPool);
        }

        return obj;
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            if (data.getVault() == vault) {
                this.ifPresent(OBJECTIVE_PROBABILITY, probability -> data.setProbability(probability.floatValue()));
            }
        });
        if (this.get(TILES).isEmpty()) {
            this.generateTiles(vault);
        }

        CommonEvents.LISTENER_JOIN.register(this, data -> {
            if (data.getVault() == vault) {
                if (data.getListener() instanceof Runner) {
                    this.set(JOINED, this.getOr(JOINED, 0) + 1);
                }
            }
        });

        for (ScavengeTask task : ModConfigs.UNHINGED_SCAVENGER.getTasks()) {
            task.initServer(world, vault, this);
        }

        CommonEvents.CHEST_LOOT_GENERATION.post().register(this, data -> {
            if (data.getPlayer().level == world) {
                ((ScavengerBingoObjectiveAccessor)this).callCountItems(data.getLoot(), vault);
            }
        });
        CommonEvents.COIN_STACK_LOOT_GENERATION.post().register(this, data -> {
            if (data.getPlayer().level == world) {
                ((ScavengerBingoObjectiveAccessor)this).callCountItems(data.getLoot(), vault);
            }
        });
        CommonEvents.ORE_LOOT_GENERATION_EVENT.register(this, data -> {
            if (data.getPlayer() != null && data.getPlayer().level == world) {
                ((ScavengerBingoObjectiveAccessor)this).callCountItemsFromWorld(data.getLoot());
            }
        });
        CommonEvents.ENTITY_DROPS.register(this, event -> {
            if (event.getEntityLiving().level == world) {
                for (ItemEntity itemEntity : event.getDrops()) {
                    ((ScavengerBingoObjectiveAccessor)this).callCountItemFromWorld(itemEntity.getItem());
                }
            }
        });
        CommonEvents.ITEM_SCAVENGE_TASK.register(this, data -> {
            if (data.getWorld() == world) {
                ((ScavengerBingoObjectiveAccessor)this).callCountItemsFromWorld(data.getItems());
            }
        });
        CommonEvents.PLAYER_ENTITY_PICKUP.register(this, event -> {
            if (event.getPlayer().level == world) {
                ((ScavengerBingoObjectiveAccessor)this).callCountItem(event.getItem().getItem(), vault);
            }
        });
        CommonEvents.GRID_GATEWAY_UPDATE.register(this, data -> {
            if (data.getLevel() == world) {
                data.getEntity().setCompletedBingos(this.getCompletedBingos());
            }
        });
        //super.super.initServer()
        this.get(CHILDREN).forEach((child) -> child.initServer(world, vault));
    }

    private void generateTiles(Vault vault) {
        int tileCount = this.getTileCount();
        ScavengerBingoTile.ObjList tiles = new ScavengerBingoTile.ObjList();
        ScavengerBingoObjective.BoolList settledTiles = new ScavengerBingoObjective.BoolList();
        ScavengerBingoObjective.BoolList settledBingos = new ScavengerBingoObjective.BoolList();

        for (int i = 0; i < tileCount; i++) {
            settledTiles.add(false);
        }

        for (int i = 0; i < this.getMaxBingos(); i++) {
            settledBingos.add(false);
        }

        RandomSource random = JavaRandom.ofInternal(vault.get(Vault.SEED));
        int level = vault.get(Vault.LEVEL).get();
        List<?> candidates = this.collectAllCandidates();
        Collections.shuffle(candidates, new Random(vault.get(Vault.SEED)));
        ScavengerConfig config = ModConfigs.UNHINGED_SCAVENGER;
        ResourceLocation entryPool = this.getOr(ENTRY_POOL, VaultMod.id("default"));
        ScavengerConfig.Entry entry = config.getEntry(entryPool, level).orElse(null);
        if (entry == null) {
            this.set(TILES, tiles);
            this.set(SETTLED_TILES, settledTiles);
            this.set(SETTLED_BINGOS, settledBingos);
        } else {
            Set<String> usedItems = new HashSet<>();
            int candidateIndex = 0;

            for (int i = 0; i < tileCount; i++) {
                TileCandidateAccessor candidate;
                for (candidate = null; candidateIndex < candidates.size(); candidateIndex++) {
                    TileCandidateAccessor c = (TileCandidateAccessor) candidates.get(candidateIndex);
                    String itemKey = ((ScavengerBingoObjectiveAccessor)this).callGetItemKey(c.getItem());
                    if (!usedItems.contains(itemKey)) {
                        candidate = c;
                        usedItems.add(itemKey);
                        candidateIndex++;
                        break;
                    }
                }

                if (candidate == null && !candidates.isEmpty()) {
                    candidate = (TileCandidateAccessor) candidates.get(random.nextInt(candidates.size()));
                }

                if (candidate != null) {
                    int baseCount = entry.getItemCount(random);
                    int itemCount = entry.getOverride() != null
                        ? (int)Math.ceil(baseCount * entry.getOverride().apply(candidate.getItem(), candidate.getMultiplier()))
                        : baseCount;
                    ScavengerBingoTile tile = new ScavengerBingoTile(candidate.getItem().copy(), candidate.getSourceType(), candidate.getIcon(), itemCount, candidate.getColor());
                    tiles.add(tile);
                }
            }

            this.set(TILES, tiles);
            this.set(SETTLED_TILES, settledTiles);
            this.set(SETTLED_BINGOS, settledBingos);
        }
    }

    private List<TileCandidateAccessor> collectAllCandidates() {
        List<TileCandidateAccessor> candidates = new ArrayList<>();
        ScavengerConfig config = ModConfigs.UNHINGED_SCAVENGER;

        for (ScavengeTask task : config.getTasks()) {
            if (task instanceof ChestScavengerTask chest) {
                chest.entries
                    .forEach(
                        (entry, weight) -> candidates.add(
                            createTileCandidate(
                                entry.item.copy(), ScavengerBingoTile.SourceType.CHEST, chest.icon, entry.multiplier, entry.color
                            )
                        )
                    );
            } else if (task instanceof CoinStacksScavengerTask coin) {
                coin.entries
                    .forEach(
                        (entry, weight) -> candidates.add(
                            createTileCandidate(
                                entry.item.copy(), ScavengerBingoTile.SourceType.COIN_STACKS, coin.icon, entry.multiplier, entry.color
                            )
                        )
                    );
            } else if (task instanceof OreScavengerTask ore) {
                ore.entries
                    .forEach(
                        (entry, weight) -> candidates.add(
                            createTileCandidate(entry.item.copy(), ScavengerBingoTile.SourceType.ORE, ore.icon, entry.multiplier, entry.color)
                        )
                    );
            } else if (task instanceof MobScavengerTask mob) {
                for (MobScavengerTask.Entry entry : mob.entries) {
                    candidates.add(
                        createTileCandidate(
                            entry.item.copy(), ScavengerBingoTile.SourceType.MOB, entry.icon != null ? entry.icon : mob.icon, entry.multiplier, mob.color
                        )
                    );
                }
            } else if (task instanceof CompoundScavengerTask compound) {
                for (ScavengeTask child : compound.children) {
                    ((ScavengerBingoObjectiveAccessor)this).callCollectCandidatesFromTask(child, (List)candidates);
                }
            }
        }

        return candidates;
    }

    private TileCandidateAccessor createTileCandidate(ItemStack item, ScavengerBingoTile.SourceType sourceType, ResourceLocation icon, double multiplier, int color) {
        try {
            return (TileCandidateAccessor) candicateCtor.newInstance(item, sourceType, icon, multiplier, color);
        } catch (Throwable e) {
            System.out.println(e);
        }
       return null;
    }


    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        ScavengerBingoTile.ObjList tiles = this.get(TILES);
        ScavengerBingoObjective.BoolList settledTiles = this.get(SETTLED_TILES);
        ScavengerBingoObjective.BoolList settledBingos = this.get(SETTLED_BINGOS);

        for (ScavengerBingoTile tile : tiles) {
            tile.tick(world, vault);
        }

        int joined = this.getOr(JOINED, 0);
        if (joined != ((ScavengerBingoObjectiveAccessor)this).getLastScaledJoined()) {
            ((ScavengerBingoObjectiveAccessor)this).setLastScaledJoined(joined);

            for (int i = 0; i < tiles.size(); i++) {
                if (!settledTiles.get(i)) {
                    tiles.get(i).scaleForPlayers(joined);
                }
            }
        }

        for (int ix = 0; ix < tiles.size(); ix++) {
            if (!settledTiles.get(ix) && tiles.get(ix).isCompleted()) {
                settledTiles.set(ix, true);
                ((ScavengerBingoObjectiveAccessor)this).callOnTileComplete(world, vault, ix);
            }
        }

        int previousBingos = this.getCompletedBingos();
        ((ScavengerBingoObjectiveAccessor)this).callCheckBingoCompletion(world, vault, settledTiles, settledBingos);
        if (this.getCompletedBingos() > previousBingos) {
            ((ScavengerBingoObjectiveAccessor)this).callGetBingoShuffleModifier(vault).ifPresent(modifier -> {
                if (modifier.shouldRegenerate()) {
                    this.regenerateIncompleteTiles(vault, settledTiles);
                } else {
                    ((ScavengerBingoObjectiveAccessor)this).callShuffleIncompleteTiles(settledTiles);
                }
            });
        }

        vault.get(Vault.LISTENERS).getAll(Runner.class).forEach(runner -> runner.getPlayer().ifPresent(player -> {
            for (int ixx = 0; ixx < player.getInventory().getContainerSize(); ixx++) {
                ItemStack stack = player.getInventory().getItem(ixx);
                if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("ScavBingoCounted")) {
                    stack.getTag().remove("ScavBingoCounted");
                    if (stack.getTag().isEmpty()) {
                        stack.setTag(null);
                    }
                }
            }
        }));
        if (this.isGatewayReady()) {
            super.tickServer(world, vault);
        }
    }

    private void regenerateIncompleteTiles(Vault vault, ScavengerBingoObjective.BoolList settledTiles) {
        ScavengerBingoTile.ObjList tiles = this.get(TILES);
        List<Integer> incompleteIndices = new ArrayList<>();

        for (int i = 0; i < tiles.size(); i++) {
            if (!settledTiles.get(i)) {
                incompleteIndices.add(i);
            }
        }

        if (!incompleteIndices.isEmpty()) {
            RandomSource random = JavaRandom.ofInternal(vault.get(Vault.SEED) ^ System.nanoTime());
            int level = vault.get(Vault.LEVEL).get();
            List<TileCandidateAccessor> candidates = this.collectAllCandidates();
            Collections.shuffle(candidates, new Random(random.nextLong()));
            ScavengerConfig config = ModConfigs.UNHINGED_SCAVENGER;
            ResourceLocation entryPool = this.getOr(ENTRY_POOL, VaultMod.id("default"));
            ScavengerConfig.Entry entry = config.getEntry(entryPool, level).orElse(null);
            if (entry != null) {
                Set<String> usedItems = new HashSet<>();
                int candidateIdx = 0;

                for (int idx : incompleteIndices) {
                    TileCandidateAccessor candidate = null;

                    while (candidateIdx < candidates.size()) {
                        TileCandidateAccessor c = candidates.get(candidateIdx++);
                        String key =  ((ScavengerBingoObjectiveAccessor)this).callGetItemKey(c.getItem());
                        if (!usedItems.contains(key)) {
                            candidate = c;
                            usedItems.add(key);
                            break;
                        }
                    }

                    if (candidate == null && !candidates.isEmpty()) {
                        candidate = candidates.get(random.nextInt(candidates.size()));
                    }

                    if (candidate != null) {
                        int baseCount = entry.getItemCount(random);
                        int itemCount = entry.getOverride() != null
                            ? (int)Math.ceil(baseCount * entry.getOverride().apply(candidate.getItem(), candidate.getMultiplier()))
                            : baseCount;
                        ScavengerBingoTile tile = new ScavengerBingoTile(candidate.getItem().copy(), candidate.getSourceType(), candidate.getIcon(), itemCount, candidate.getColor());
                        int joined = this.getOr(JOINED, 0);
                        if (joined > 1) {
                            tile.scaleForPlayers(joined);
                        }

                        tiles.set(idx, tile);
                    }
                }
            }
        }
    }
}
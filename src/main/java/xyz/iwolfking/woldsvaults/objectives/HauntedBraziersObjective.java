package xyz.iwolfking.woldsvaults.objectives;

import iskallia.vault.VaultMod;
import iskallia.vault.block.MonolithBlock;
import iskallia.vault.block.entity.MonolithTileEntity;
import iskallia.vault.config.MonolithConfig;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.MonolithObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.brazier.BrazierBlessing;
import iskallia.vault.core.vault.objective.brazier.BrazierHeatTier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveHelper;
import xyz.iwolfking.woldsvaults.objectives.haunted_braziers.HauntedBrazierHold;

import java.util.Optional;

public class HauntedBraziersObjective extends MonolithObjective {
    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("haunted_braziers", Objective.class)
            .with(Version.v1_12, HauntedBraziersObjective::new);
    public static final ResourceLocation HAUNTED_HUD = VaultMod.id("textures/gui/monolith/haunted_hud.png");

    private static final long BLESSING_SEED_SALT = 90039737L;
    private static final int MAX_BLESSING_ROLLS = 8;
    private static final long ROLL_CHECK_INTERVAL_TICKS = 10L;

    public HauntedBraziersObjective() {
    }

    protected HauntedBraziersObjective(int target, float objectiveProbability) {
        this.set(COUNT, 0);
        this.set(TARGET, target);
        this.set(BASE_TARGET, target);
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(HEAT, 0);
    }

    public static HauntedBraziersObjective of(int target, float objectiveProbability) {
        return new HauntedBraziersObjective(target, objectiveProbability);
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        ObjectiveHelper.handleAddingNormalizedToVault(vault, world);

//        ObjectiveHelper.addInitModifiersToVault(vault, resourceLocations -> {
//            resourceLocations.add(WoldsVaults.id("ghost_town"));
//            resourceLocations.add(VaultMod.id("haunting"));
//        });

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            if (data.getVault() == vault) {
                this.ifPresent(OBJECTIVE_PROBABILITY, probability -> data.setProbability((double) probability));
            }
        });

        this.registerObjectiveTemplate(world, vault);

        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.MONOLITH).register(this, data -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
            } else if (data.getState().getValue(MonolithBlock.STATE) == MonolithBlock.State.EXTINGUISHED) {
                if (vault.get(Vault.LISTENERS).getObjectivePriority(data.getPlayer().getUUID(), this) == 0) {
                    BlockEntity blockEntity = data.getWorld().getBlockEntity(data.getPos());
                    if (blockEntity instanceof MonolithTileEntity tile) {
                        data.setResult(InteractionResult.SUCCESS);
                        if (!tile.isHolding()) {
                            if (tile.getBlessing().isEmpty()) {
                                this.rollBlessing(vault, data.getPos()).ifPresent(tile::setBlessing);
                            }

                            if (tile.getBlessing().isPresent()) {
                                HauntedBrazierHold.start(world, vault, this, tile, data.getPos());
                            }
                        }
                    }
                }
            }
        });

        CommonEvents.MONOLITH_UPDATE.register(this, data -> {
            if (data.getWorld() == world) {
                if (data.getState().getValue(MonolithBlock.STATE) == MonolithBlock.State.EXTINGUISHED) {
                    MonolithTileEntity tile = data.getEntity();
                    if (tile.isHolding()) {
                        HauntedBrazierHold.tick(world, vault, this, tile, data.getPos());
                    } else if (tile.getBlessing().isEmpty()
                            && (world.getGameTime() + data.getPos().asLong()) % ROLL_CHECK_INTERVAL_TICKS == 0L
                            && this.isPlayerNear(world, data.getPos())) {
                        this.rollBlessing(vault, data.getPos()).ifPresent(tile::setBlessing);
                    }
                }
            }
        });

        this.get(CHILDREN).forEach(child -> child.initServer(world, vault));
    }

    private boolean isPlayerNear(VirtualWorld world, BlockPos pos) {
        double distance = xyz.iwolfking.woldsvaults.init.ModConfigs.HAUNTED_BRAZIERS.getRollNearDistance();
        return world.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, distance, false) != null;
    }

    @Override
    public Optional<BrazierBlessing> rollBlessing(Vault vault, BlockPos pos) {
        MonolithConfig config = ModConfigs.MONOLITH;
        BrazierHeatTier tier = config.tierAt(this.getHeat());
        Optional<ResourceLocation> pool = config.poolFor(tier);
        if (pool.isEmpty()) {
            return Optional.empty();
        }

        int level = vault.get(Vault.LEVEL).getOr(VaultLevel.VALUE, 0);
        ChunkRandom random = ChunkRandom.any();
        random.setBlockSeed(vault.get(Vault.SEED), pos, BLESSING_SEED_SALT + (long) tier.ordinal());
        MonolithConfig.LevelEntry levelEntry = config.forLevel(level);

        for (int roll = 0; roll < MAX_BLESSING_ROLLS; roll++) {
            Optional<VaultModifier<?>> rolled = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(pool.get(), level, random).stream().findFirst();
            if (rolled.isEmpty()) {
                return Optional.empty();
            }

            ResourceLocation modifierId = rolled.get().getId();
            if (!this.isHauntedBlessingCapped(vault, modifierId)) {
                int stacks = levelEntry.blessingStacks(modifierId).map(stackRoll -> stackRoll.get(random)).orElse(1);
                return Optional.of(new BrazierBlessing(modifierId, Math.max(1, stacks), tier));
            }
        }

        return Optional.empty();
    }

    private boolean isHauntedBlessingCapped(Vault vault, ResourceLocation modifierId) {
        return xyz.iwolfking.woldsvaults.init.ModConfigs.HAUNTED_BRAZIERS.blessingCap(modifierId)
                .map(cap -> stacksFromBraziers(vault, modifierId) >= (long) cap)
                .orElse(false);
    }

    private static long stacksFromBraziers(Vault vault, ResourceLocation modifierId) {
        return vault.get(Vault.MODIFIERS).getEntries().stream()
                .filter(entry -> modifierId.equals(entry.get(Modifiers.Entry.MODIFIER)))
                .filter(entry -> entry.getContext().getGroup().map(BRAZIER_MODIFIER_GROUP::equals).orElse(false))
                .count();
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        this.ifPresent(BASE_TARGET, value -> {
            double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, 0.0D).getIncrease();
            this.set(TARGET, (int) Math.round((double) value * (1.0D + increase)));
        });
        if (this.get(COUNT) >= this.get(TARGET)) {
            this.get(CHILDREN).forEach(child -> child.tickServer(world, vault));
        }
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if (listener instanceof Runner && this.get(COUNT) >= this.get(TARGET)) {
            super.tickListener(world, vault, listener);
        }
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if (this.get(COUNT) < this.get(TARGET)) {
            return objective == this;
        }
        for (Objective child : this.get(CHILDREN)) {
            if (child.isActive(world, vault, objective)) {
                return true;
            }
        }
        return false;
    }
}
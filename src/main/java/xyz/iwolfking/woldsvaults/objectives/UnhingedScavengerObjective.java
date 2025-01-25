package xyz.iwolfking.woldsvaults.objectives;

import implementslegend.mod.vaultfaster.event.ObjectiveTemplateEvent;
import iskallia.vault.VaultMod;
import iskallia.vault.block.DivineAltarBlock;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.block.entity.ScavengerAltarTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.basic.EnumAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockSetEvent;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerObjective;
import iskallia.vault.core.vault.objective.scavenger.ScavengeTask;
import iskallia.vault.core.vault.objective.scavenger.ScavengerGoal;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.GodBlessingItem;
import iskallia.vault.item.KeystoneItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.LoadingModList;
import xyz.iwolfking.woldsvaults.config.UnhingedScavengerConfig;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.util.List;
import java.util.function.Supplier;

public class UnhingedScavengerObjective extends ScavengerObjective {

    public static final FieldKey<UnhingedScavengerObjective.Config> CONFIG = FieldKey.of("config", UnhingedScavengerObjective.Config.class).with(Version.v1_19, Adapters.ofEnum(UnhingedScavengerObjective.Config.class, EnumAdapter.Mode.ORDINAL), DISK.all()).register(FIELDS);

    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("unhinged_scavenger", Objective.class).with(Version.v1_12, UnhingedScavengerObjective::new);

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
    }
    public UnhingedScavengerObjective(float objectiveProbability, Config config) {
        this.set(GOALS, new GoalMap());
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(CONFIG, config);
    }

    public UnhingedScavengerObjective() {
        this.set(GOALS, new GoalMap());
    }

    public static UnhingedScavengerObjective of(float objectiveProbability, UnhingedScavengerObjective.Config config) {
        return new UnhingedScavengerObjective(objectiveProbability, config);
    }


    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        boolean hasGeneratedModifiers = false;
        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifier.getId().equals(VaultMod.id("normalized"))) {
                hasGeneratedModifiers = true;
            }
        }

        if(!hasGeneratedModifiers && WoldsVaultsConfig.COMMON.normalizedModifierEnabled.get()) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("normalized"), 1);
        }

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            this.ifPresent(OBJECTIVE_PROBABILITY, probability -> data.setProbability(probability));
        });
        if(LoadingModList.get().getModFileById("vaultfaster") != null) {
            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(this, vault);
        }
        else {
            CommonEvents.BLOCK_SET.at(BlockSetEvent.Type.RETURN).in(world).register(this, data -> {
                PartialTile target = PartialTile.of(PartialBlockState.of(ModBlocks.PLACEHOLDER), PartialCompoundNbt.empty());
                target.getState().set(PlaceholderBlock.TYPE, iskallia.vault.block.PlaceholderBlock.Type.OBJECTIVE);
                if (target.isSubsetOf(PartialTile.of(data.getState()))) {
                    data.getWorld().setBlock(data.getPos(), ModBlocks.SCAVENGER_ALTAR.defaultBlockState(), 3);
                }

            });
        }

        CommonEvents.SCAVENGER_ALTAR_CONSUME.register(this, data -> {
            if (data.getLevel() == world && ((ScavengerAltarTileEntity)data.getTile()).getItemPlacedBy() != null) {
                Listener listener = (vault.get(Vault.LISTENERS)).get(((ScavengerAltarTileEntity)data.getTile()).getItemPlacedBy());
                if (listener instanceof Runner) {
                    BlockState state = data.getTile().getBlockState();
                    if (state.getBlock() == ModBlocks.DIVINE_ALTAR) {
                        if (((ScavengerAltarTileEntity)data.getTile()).getHeldItem().getItem() instanceof KeystoneItem keystone) {
                            if (keystone.getGod() != state.getValue(DivineAltarBlock.GOD)) {
                                return;
                            }
                        } else {
                            if (!(((ScavengerAltarTileEntity)data.getTile()).getHeldItem().getItem() instanceof GodBlessingItem)) {
                                return;
                            }

                            if (GodBlessingItem.getGod(((ScavengerAltarTileEntity)data.getTile()).getHeldItem()) != state.getValue(DivineAltarBlock.GOD)) {
                                return;
                            }
                        }
                    }

                    boolean creative = listener.getPlayer().map(ServerPlayer::isCreative).orElse(false);
                    CompoundTag nbt = ((ScavengerAltarTileEntity)data.getTile()).getHeldItem().getTag();
                    if (creative || nbt != null && nbt.getString("VaultId").equals((vault.get(Vault.ID)).toString())) {
                        List<ScavengerGoal> goals = this.get(GOALS).get(listener.get(Listener.ID));
                        for (ScavengerGoal goal: goals) {
                            goal.consume(((ScavengerAltarTileEntity)data.getTile()).getHeldItem());
                        }

                    }
                }
            }
        });

        for (ScavengeTask task : (this.getOr(CONFIG, Config.DEFAULT)).get().getTasks()) {
            task.initServer(world, vault, this);
        }

        (this.get(CHILDREN)).forEach(child -> child.initServer(world, vault));
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if (listener instanceof Runner && listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
            this.generateGoal(vault, listener);
        }

        ScavengerGoal.ObjList goal = this.get(GOALS).get(listener.get(Listener.ID));
        if (goal != null && goal.areAllCompleted()) {
            this.get(CHILDREN).forEach(child -> child.tickListener(world, vault, listener));
        }
    }

    private void generateGoal(Vault vault, Listener listener) {
        ScavengerGoal.ObjList list = new ScavengerGoal.ObjList();
        this.get(GOALS).put(listener.get(Listener.ID), list);
        JavaRandom random = JavaRandom.ofInternal(vault.get(Vault.SEED) ^ listener.get(Listener.ID).getMostSignificantBits());
        list.addAll((this.getOr(CONFIG, UnhingedScavengerObjective.Config.DEFAULT)).get().generateGoals(vault.get(Vault.LEVEL).get(), random));
    }




    public static enum Config {
        DEFAULT(() -> xyz.iwolfking.woldsvaults.init.ModConfigs.UNHINGED_SCAVENGER),
        DIVINE_PARADOX(() -> ModConfigs.DIVINE_PARADOX);

        private Supplier<UnhingedScavengerConfig> supplier;

        private Config(Supplier supplier) {
            this.supplier = supplier;
        }

        public UnhingedScavengerConfig get() {
            return this.supplier.get();
        }
    }
}

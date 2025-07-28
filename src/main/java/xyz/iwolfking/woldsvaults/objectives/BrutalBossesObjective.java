package xyz.iwolfking.woldsvaults.objectives;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import iskallia.vault.VaultMod;
import iskallia.vault.block.ObeliskBlock;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.compound.UUIDList;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockSetEvent;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.loading.LoadingModList;
import xyz.iwolfking.woldsvaults.api.helper.NormalizedHelper;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;
import xyz.iwolfking.woldsvaults.objectives.data.bosses.WoldBoss;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntSupplier;

public class BrutalBossesObjective extends ObeliskObjective {

    public static final SupplierKey<Objective> E_KEY = (SupplierKey)SupplierKey.of("brutal_bosses", Objective.class).with(Version.v1_12, BrutalBossesObjective::new);

    public BrutalBossesObjective(int target, IntSupplier wave, float objectiveProbability) {
        super(target, wave, objectiveProbability);
    }

    public BrutalBossesObjective() {
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
    }
    public static BrutalBossesObjective of(int target, IntSupplier wave, float objectiveProbability) {
        return new BrutalBossesObjective(target, wave, objectiveProbability);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        NormalizedHelper.handleAddingNormalizedToVault(vault, world);

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, (data) -> {
                this.ifPresent(OBJECTIVE_PROBABILITY, (probability) -> {
                    data.setProbability((double)probability);
                });
        });


        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.OBELISK).register(this, (data) -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
            } else if (this.hasObelisksLeft()) {
                BlockPos pos = data.getPos();
                if ((Boolean)data.getState().getValue(ObeliskBlock.FILLED)) {
                    data.setResult(InteractionResult.SUCCESS);
                } else if (data.getState().getValue(ObeliskBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != ModBlocks.OBELISK) {
                    data.setResult(InteractionResult.SUCCESS);
                } else if ((vault.get(Vault.LISTENERS)).getObjectivePriority(data.getPlayer().getUUID(), this) != 0) {
                    data.setResult(InteractionResult.SUCCESS);
                } else {
                    world.setBlock(pos, (BlockState)world.getBlockState(pos).setValue(ObeliskBlock.FILLED, true), 3);
                    world.setBlock(pos.above(), (BlockState)world.getBlockState(pos.above()).setValue(ObeliskBlock.FILLED, true), 3);
                    this.onObeliskActivated(world, vault, pos);
                    data.setResult(InteractionResult.SUCCESS);
                }
            }
        });
        this.registerObjectiveTemplate(world, vault);
        CommonEvents.BLOCK_SET.at(BlockSetEvent.Type.RETURN).in(world).register(this, (data) -> {
            PartialTile target = PartialTile.of(PartialBlockState.of(ModBlocks.PLACEHOLDER), PartialCompoundNbt.empty());
            target.getState().set(PlaceholderBlock.TYPE, PlaceholderBlock.Type.OBJECTIVE);
            if (target.isSubsetOf(PartialTile.of(data.getState()))) {
                BlockState lower = (BlockState)((BlockState)ModBlocks.OBELISK.defaultBlockState().setValue(ObeliskBlock.HALF, DoubleBlockHalf.LOWER)).setValue(ObeliskBlock.FILLED, false);
                BlockState upper = (BlockState)((BlockState)ModBlocks.OBELISK.defaultBlockState().setValue(ObeliskBlock.HALF, DoubleBlockHalf.UPPER)).setValue(ObeliskBlock.FILLED, false);
                data.getWorld().setBlock(data.getPos(), lower, 3);
                data.getWorld().setBlock(data.getPos().above(), upper, 3);
            }

        });
        CommonEvents.ENTITY_DEATH.register(this, (event) -> {
            if (event.getEntity().level == world) {
                Wave[] var3 = (Wave[])this.get(WAVES);
                int var4 = var3.length;


                for(int var5 = 0; var5 < var4; ++var5) {
                    Wave wave = var3[var5];
                    if (((UUIDList)wave.get(Wave.MOBS)).remove(event.getEntity().getUUID())) {

                        wave.modify(Wave.COUNT, (x) -> {
                            return x + 1;
                        });
                        MobModifier modifier = InfernalMobsCore.getMobModifiers(event.getEntityLiving());
                        List<VaultModifier<?>> modifiersForMsg = new ArrayList<>();

                        if (modifier != null && modifier.getModSize() != 0) {
                            String modNames = modifier.getLinkedModNameUntranslated().trim();
                            for (String modName : modNames.split("\\s+")) {
                                List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("bb_" + modName.toLowerCase()), 0, (RandomSource) JavaRandom.ofNanoTime());
                                if (!modifiers.isEmpty()) {
                                    Iterator modIter = modifiers.iterator();

                                    while (modIter.hasNext()) {
                                        VaultModifier<?> mod = (VaultModifier<?>) modIter.next();
                                        modifiersForMsg.add(mod);
                                        ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(mod, 1, true, (RandomSource) JavaRandom.ofNanoTime());
                                    }
                                }
                            }
                        } else {
                            // Add 2 random vault modifiers from BOSS_MODS_LIST
                            for (int i = 0; i < 2; i++) {
                                String modName = BrutalBossesRegistry.BOSS_MODS_LIST.getRandom().get().toLowerCase();
                                List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("bb_" + modName), 0, (RandomSource) JavaRandom.ofNanoTime());
                                if (!modifiers.isEmpty()) {
                                    Iterator modIter = modifiers.iterator();

                                    while (modIter.hasNext()) {
                                        VaultModifier<?> mod = (VaultModifier<?>) modIter.next();
                                        modifiersForMsg.add(mod);
                                        ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(mod, 1, true, (RandomSource) JavaRandom.ofNanoTime());
                                    }
                                }
                            }
                        }

                        vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
                            if(listener.getPlayer().isPresent()) {
                                modifiersForMsg.forEach(mod -> {
                                    listener.getPlayer().get().displayClientMessage(mod.getChatDisplayNameComponent(1).copy().append(" was added to The Vault!"), false);
                                });
                            }
                        });
                    }
                }

            }
        });
        ((ObjList)this.get(CHILDREN)).forEach((child) -> {
            child.initServer(world, vault);
        });
    }

    private void onObeliskActivated(VirtualWorld world, Vault vault, BlockPos pos) {
        this.playActivationEffects(world, pos);
        Wave wave = (Wave) Arrays.stream((Wave[])this.get(WAVES)).filter((w) -> {
            return !w.has(Wave.ACTIVE);
        }).findFirst().orElseThrow();
        wave.set(Wave.ACTIVE);
        RandomSource random = JavaRandom.ofNanoTime();

        for(int i = 0; i < (Integer)wave.get(Wave.TARGET); ++i) {
            ((UUIDList)wave.get(Wave.MOBS)).add(this.doSpawn(world, vault, pos, random).getUUID());
        }

    }

    @Override
    public LivingEntity doSpawn(VirtualWorld world, Vault vault, BlockPos pos, RandomSource random) {
        double min = 10.0;
        double max = 13.0;

        LivingEntity spawned;
        int x;
        int z;
        int y;
        for(spawned = null; spawned == null; spawned = spawnMob(world, vault, pos.getX() + x, pos.getY() + y, pos.getZ() + z, random)) {
            double angle = 6.283185307179586 * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            x = (int)Math.ceil(distance * Math.cos(angle));
            z = (int)Math.ceil(distance * Math.sin(angle));
            double xzRadius = Math.sqrt((double)(x * x + z * z));
            double yRange = Math.sqrt(max * max - xzRadius * xzRadius);
            y = random.nextInt((int)Math.ceil(yRange) * 2 + 1) - (int)Math.ceil(yRange);
        }

        return spawned;
    }


    @Nullable
    public static LivingEntity spawnMob(VirtualWorld world, Vault vault, int x, int y, int z, RandomSource random) {
        EntityType<?> type = BrutalBossesRegistry.BOSS_LIST.getRandom().get();
        Component bossName = BrutalBossesRegistry.BOSS_NAME_LIST.getRandom().get();
        LivingEntity entity = (LivingEntity) type.create(world);

        assert entity != null;
        entity.setGlowingTag(true);
        if(entity instanceof WoldBoss) {
            entity.setCustomName(new TextComponent("Wold").withStyle(ChatFormatting.GOLD));
        }
        else {
            entity.setCustomName(bossName);
        }
        entity.setCustomNameVisible(true);
        InfernalMobsCore.instance().addEntityModifiersByString(entity, BrutalBossesRegistry.getRandomMobModifiers());
        BlockState state = world.getBlockState(new BlockPos(x, y - 1, z));
        if (!state.isValidSpawn(world, new BlockPos(x, y - 1, z), entity.getType())) {
            return null;
        } else {
            AABB entityBox = entity.getType().getAABB((double)x + 0.5, (double)y, (double)z + 0.5);
            if (!world.noCollision(entityBox)) {
                return null;
            } else {
                entity.moveTo((double)((float)x + 0.5F), (double)((float)y + 0.2F), (double)((float)z + 0.5F), (float)(random.nextDouble() * 2.0 * Math.PI), 0.0F);
                world.addWithUUID(entity);
                return entity;
            }
        }
    }
}

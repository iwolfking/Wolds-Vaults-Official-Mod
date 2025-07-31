package xyz.iwolfking.woldsvaults.util;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.VaultMod;
import iskallia.vault.block.ObeliskBlock;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockSetEvent;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.event.common.TemplateGenerationEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.*;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.KillBossObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickTimer;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.DynamicTemplate;
import iskallia.vault.core.world.template.EmptyTemplate;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.entity.entity.bloodhorde.Tier5BloodHordeEntity;
import iskallia.vault.entity.entity.bloodmoon.Tier5BloodSkeletonEntity;
import iskallia.vault.init.*;
import iskallia.vault.network.message.MonolithIgniteMessage;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.blocks.FracturedObelisk;
import xyz.iwolfking.woldsvaults.blocks.MonolithControllerBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.items.LocatorItem;
import xyz.iwolfking.woldsvaults.modifiers.clock.KillMobTimeExtension;
import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * This class is a mess, its here because otherwise the corrupted objective class would be a mess<br>
 * So instead, this class becomes the mess, while the main objective class stays (relatively) clean
 */

public class CorruptedVaultHelper {
    private static final UUID HEALTH_REDUCTION_UUID = UUID.fromString("24edf251-5c02-4acb-9eff-8896bc140916");

    public static boolean isVaultCorrupted = false;

    public static void generateSlash(VirtualWorld world, BlockPos start) {
        Random random = world.getRandom();

        float angle = random.nextFloat() * ((float) Math.PI * 2);
        float angleDelta = (random.nextFloat() - 0.5f) * 0.05f;

        double px = start.getX() + 0.5;
        double pz = start.getZ() + 0.5;

        int length = 30 + random.nextInt(20);
        int baseWidth = 2 + random.nextInt(2);

        List<Vec3> slashPath = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            slashPath.add(new Vec3(px, 64, pz));

            angle += angleDelta;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            px += dx;
            pz += dz;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < slashPath.size(); i++) {
            Vec3 pos = slashPath.get(i);
            float taper = (float) Math.sin((Math.PI * i) / length);
            int taperedWidth = Math.max(1, Math.round(baseWidth * taper));
            int outerWidth = taperedWidth + 1;

            for (int x = -outerWidth; x <= outerWidth; x++) {
                for (int z = -outerWidth; z <= outerWidth; z++) {
                    double dist = x * x + z * z;
                    if (dist <= outerWidth * outerWidth + 0.2) {
                        for (int y = 63; y >= 0; y--) {
                            mutable.set(Math.round(pos.x()) + x, y, Math.round(pos.z()) + z);
                            if(!world.getBlockState(mutable).is(ModBlocks.VAULT_BEDROCK) && world.isLoaded(mutable)) {
                                world.setBlock(mutable, ModBlocks.VAULT_BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < slashPath.size(); i++) {
            Vec3 pos = slashPath.get(i);
            float taper = 1.0f - Math.abs((i - length / 2f) / (length / 2f));
            int taperedWidth = Math.max(1, Math.round(baseWidth * taper));

            for (int x = -taperedWidth; x <= taperedWidth; x++) {
                for (int z = -taperedWidth; z <= taperedWidth; z++) {
                    double dist = x * x + z * z;
                    if (dist <= taperedWidth * taperedWidth + 0.2) {
                        for (int y = 63; y >= 0; y--) {
                            mutable.set(Math.round(pos.x()) + x, y, Math.round(pos.z()) + z);
                            if (world.getBlockState(mutable).getMaterial().isSolid() && world.isLoaded(mutable) ) {
                                world.setBlock(mutable, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void spawnSpike(VirtualWorld world, BlockPos center, Random random, Player player) {
        Block[] possibleBlocks = {ModBlocks.ERROR_BLOCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, ModBlocks.VAULT_STONE};


        int baseRadius = 2 + random.nextInt(2); // 2-3 radius
        int spikeCount = 4 + random.nextInt(3); // 4-6 spikes


        for (int dx = -baseRadius; dx <= baseRadius; dx++) {
            for (int dz = -baseRadius; dz <= baseRadius; dz++) {
                if (dx * dx + dz * dz <= baseRadius * baseRadius + random.nextInt(2)) {
                    BlockPos pos = center.offset(dx, 0, dz);
                    if (world.isEmptyBlock(pos) && !(pos.distSqr(player.blockPosition()) < 3.0)) {
                        Block blockType = possibleBlocks[random.nextInt(possibleBlocks.length)];
                        world.setBlock(pos, blockType.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }


        for (int i = 0; i < spikeCount; i++) {
            int spikeHeight = 6 + random.nextInt(8); // 6-13 tall
            int xOffset = random.nextInt(5) - 2; // -2 to +2
            int zOffset = random.nextInt(5) - 2;

            BlockPos start = center.offset(xOffset, 1, zOffset);

            int thickness = 2;

            for (int h = 0; h < spikeHeight; h++) {
                BlockPos current = start.above(h).offset(xOffset * h / 5, 0, zOffset * h / 5); // Slight tilt outward

                for (int dx = -thickness; dx <= thickness; dx++) {
                    for (int dz = -thickness; dz <= thickness; dz++) {
                        if (Math.abs(dx) + Math.abs(dz) <= thickness) { // Diamond shape for tapering
                            BlockPos pos = current.offset(dx, 0, dz);
                            if (world.isEmptyBlock(pos) && !(pos.distSqr(player.blockPosition()) < 3.0)) {
                                Block blockType = possibleBlocks[random.nextInt(possibleBlocks.length)];
                                world.setBlock(pos, blockType.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }

                if (h % 3 == 0 && thickness > 0) {
                    thickness--; // Every few blocks, get thinner
                }
            }
        }
    }

    public static void fakeVictory(VirtualWorld world, Vault vault, Listener listener, CorruptedObjective objective) {
        int time = objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_TICKED_FAKE);

        listener.getPlayer().ifPresent(player -> {
            if (time == 0) {
                world.addFreshEntity(new FireworkRocketEntity(world, player.getX(), player.getY(), player.getZ(), new ItemStack(Items.FIREWORK_ROCKET)));
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 0.6F, 0.8F);

                TextComponent title = new TextComponent("Vault Completed!");
                title.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(14536734)));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
            }

            if (time >= 100 && time < 320 && time % 5 == 0) {
                String jumbled = jumbleCharacters("Vault Completed!", world.random);
                Component corrupted = ComponentUtils.corruptComponentServerSide(new TextComponent(jumbled).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#870c03"))));
                player.connection.send(new ClientboundSetTitleTextPacket(corrupted));

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BIT, SoundSource.MASTER, 0.8F, 0.5F + (world.random.nextFloat() * 0.5F));
            }
            if (time == 400) {
                world.setBlock(new BlockPos(0, 63, 0), ModBlocks.ERROR_BLOCK.defaultBlockState(), Block.UPDATE_ALL);

                player.teleportTo(0, 64, 0);
                player.connection.teleport(0, 64, 0, 0, 0);

                vault.get(Vault.MODIFIERS).addModifier(VaultModifierRegistry.get(VaultMod.id("crowded")), 10, true, ChunkRandom.any());
                vault.ifPresent(Vault.CLOCK, (clock) -> clock.set(TickClock.VISIBLE));

                FloatingItemEntity floatingItem = FloatingItemEntity.create(world, new BlockPos(0, 65, 5), new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.OBELISK_RESONATOR));
                world.addFreshEntity(floatingItem);

                world.playSound(null, new BlockPos(0, 64, 0), SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 0.5F, 0.8F);
            }
        });
    }





    public static String jumbleCharacters(String input, Random random) {
        StringBuilder result = new StringBuilder(input);

        int length = input.length();
        int amountToModify = Math.max(1, (int) (length * (0.3 + random.nextDouble() * 0.2))); // 30% to 50%

        Set<Integer> indices = new HashSet<>();
        while (indices.size() < amountToModify) {
            int index = random.nextInt(length);
            if (!Character.isWhitespace(input.charAt(index))) {
                indices.add(index);
            }
        }

        for (int i : indices) {
            char c = input.charAt(i);
            char replacement;

            if (Character.isDigit(c)) {
                replacement = (char) ('0' + random.nextInt(10));
            } else if (Character.isLetter(c)) {
                boolean upper = Character.isUpperCase(c);
                replacement = (char) ((upper ? 'A' : 'a') + random.nextInt(26));
            } else {
                replacement = (char) (33 + random.nextInt(15));
            }
            result.setCharAt(i, replacement);
        }

        return result.toString();
    }


    public static boolean eligibleForExtraTime(Vault vault) {
        return vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) > 0;
    }

    /**
     * Utility method that gets called upon interacting with a Fractured Obelisk
     *
     * @param world ServerLevel to play the effects in
     * @param pos Position to play the effects at
     */
    public static void playActivationEffects(VirtualWorld world, BlockPos pos) {
        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new MonolithIgniteMessage(pos));
        world.playSound(null, pos, ModSounds.ARTIFACT_BOSS_CATALYST_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Utility method that gets called upon interacting with a Fractured Obelisk<br>
     * Spreads {@code the_vault:error_block}s in a set radius of 7 blocks around itself
     *
     * @param world ServerLevel to modify the blocks in
     * @param startPos Position of the obelisk
     */
    public static void spreadErrorBlocks(VirtualWorld world, BlockPos startPos) {
        int radius = 7;
        Random random = world.getRandom();

        int centerX = startPos.getX();
        int centerZ = startPos.getZ();

        // Loop through a square bounding box that contains the circle
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2) <= Math.pow(radius, 2)) {
                    BlockPos currentPos = new BlockPos(x, startPos.getY() - 1, z);

                    // Check if the block is a full block
                    if (world.getBlockState(currentPos).isSolidRender(world, currentPos)) {
                        if (random.nextFloat() >= 0.5F) {
                            if(random.nextFloat() >= 0.2F) {
                                world.setBlock(currentPos, ModBlocks.ERROR_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                            } else {
                                world.setBlock(currentPos, ModBlocks.VOID_LIQUID_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void handleObeliskBelow(BlockUseEvent.Data data, Vault vault, VirtualWorld world, BlockPos pos) {
        if(data.getWorld().getBlockEntity(pos) instanceof FracturedObeliskTileEntity) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            CorruptedVaultHelper.spreadErrorBlocks(world, pos);

            ItemStack stack = xyz.iwolfking.woldsvaults.init.ModItems.RUINED_ESSENCE.getDefaultInstance();
            stack.getOrCreateTag().putUUID("VaultID", vault.get(Vault.ID));
            FloatingItemEntity floatingItem = FloatingItemEntity.create(world, pos.above(), stack);
            world.addFreshEntity(floatingItem);


            data.setResult(InteractionResult.SUCCESS);
        }
    }

    public static void handleObeliskAbove(BlockUseEvent.Data data, VirtualWorld world, BlockPos pos, CorruptedObjective objective) {
        objective.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.SECONDARY_COUNT, objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_COUNT) + 1);
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
        CorruptedVaultHelper.spawnSpike(world, pos.below(), world.random, data.getPlayer());

        if ((int) objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_COUNT) == objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_TARGET)) {
            objective.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.TRUE_COMPLETION, true);

        }
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        data.getPlayer().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1));

        int numEnemies = 20;
        int radius = 24;
        Random random = world.random;

        for (int i = 0; i < numEnemies; i++) {
            double angle = 2 * Math.PI * i / numEnemies;
            int x = pos.getX() + (int)(Math.cos(angle) * radius);
            int z = pos.getZ() + (int)(Math.sin(angle) * radius);
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos spawnPos = new BlockPos(x, y, z);

            if (world.isEmptyBlock(spawnPos) && world.getBlockState(spawnPos.below()).isSolidRender(world, spawnPos.below())) {
                if(world.getRandom().nextFloat() > 0.5F) {
                    Tier5BloodHordeEntity entity = new Tier5BloodHordeEntity(ModEntities.T5_BLOOD_HORDE, world);

                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 3)); // Speed 4 for 20 minutes
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 7)); // Strength 8 for 20 minutes
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 24000, 0)); // Resistance 1 for 20 minutes

                    entity.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360F, 0F);
                    world.addFreshEntity(entity);
                } else {
                    Tier5BloodSkeletonEntity entity = new Tier5BloodSkeletonEntity(ModEntities.T5_BLOOD_SKELETON, world);

                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 3)); // Speed 4 for 20 minutes
                    entity.addEffect(new MobEffectInstance(ModEffects.CORRUPTION, 24000, 7)); // Strength 8 for 20 minutes
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 24000, 0)); // Resistance 1 for 20 minutes

                    entity.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360F, 0F);
                    world.addFreshEntity(entity);
                }


            }
        }

        data.setResult(InteractionResult.SUCCESS);
    }

    public static int calculateGradualTimeIncrease(int timeLeftInTicks, int playerCount) {
        int timeLeftInSeconds = timeLeftInTicks / 20; // Convert ticks to seconds
        int maxTimeInSeconds = 900; // 15 minutes
        int maxAddTimeInTicks = Math.max(40, 120 - ((playerCount - 1) * 10)); // 6s, with each player reduce by half a second, min 2 seconds


        float fraction = 1 - ((float) timeLeftInSeconds / maxTimeInSeconds);
        return Math.max(0, Math.round(maxAddTimeInTicks * fraction));
    }


    public static void updateFracturedObeliskObfuscation(CorruptedObjective objective) {
        WoldCommonEvents.FRACTURED_OBELISK_UPDATE.register(objective, data -> {


            if(data.getWorld().getGameTime() % 20 == 0) {
                float obfuscation = 1 - (objective.get(CorruptedObjective.DATA).hasCompletedInitial()
                        ? (float) objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_COUNT) / objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_TARGET)
                        : (float) objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) / objective.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET));

                FracturedObeliskTileEntity tile = data.getEntity();

                tile.setInitialCompletion(objective.get(CorruptedObjective.DATA).hasCompletedInitial());
                tile.setPercentObfuscated(Math.min(1.0F, obfuscation));
            }
        });
    }

    // Black magic, I dont know
    public static void summonRoofSpikes(VirtualWorld world, Player player, int radius, int tickInterval, float chanceToSummon) {
        if (player.tickCount % tickInterval == 0) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            if (world.getRandom().nextFloat() < chanceToSummon) {
                mutable.set(
                        player.getX() + world.getRandom().nextInt(radius * 2) - radius,
                        64,
                        player.getZ() + world.getRandom().nextInt(radius * 2) - radius
                );

                double angle = world.getRandom().nextDouble() * 1.5 * Math.PI;
                double dx = Math.cos(angle);
                double dz = Math.sin(angle);
                double dy = 0.5 + world.getRandom().nextDouble() * 0.3;

                int length = 15 + world.getRandom().nextInt(10);
                float maxRadius = 1.5f + world.getRandom().nextFloat();

                double px = mutable.getX();
                double py = mutable.getY();
                double pz = mutable.getZ();

                BlockPos lastBlock = null;

                for (int i = 0; i < length; i++) {
                    float progress = i / (float) length;

                    int layerRadius;
                    if (i >= length - 2) {
                        layerRadius = 0;
                    } else {
                        float rad = (float) Math.pow(1.0 - progress, 1.2);
                        layerRadius = Math.max(1, Math.round(maxRadius * rad));
                    }

                    BlockPos center = new BlockPos(px, py, pz);

                    for (int x = -layerRadius; x <= layerRadius; x++) {
                        for (int z = -layerRadius; z <= layerRadius; z++) {
                            double dist = Math.sqrt(x * x + z * z);

                            if (dist <= layerRadius + 0.2 + (world.getRandom().nextDouble() - 0.5) * 0.3) {
                                BlockPos pos = center.offset(x, 0, z);

                                if (world.isEmptyBlock(pos)) {
                                    BlockState block = world.getRandom().nextFloat() < 0.15f
                                            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                            : Blocks.OBSIDIAN.defaultBlockState();

                                    world.setBlock(pos, block, Block.UPDATE_ALL);
                                    lastBlock = pos;
                                }
                            }
                        }
                    }

                    px += dx * 0.8;
                    py += dy * 0.8;
                    pz += dz * 0.8;
                }
                ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new MonolithIgniteMessage(mutable)); //TODO

                if (lastBlock != null && world.getRandom().nextFloat() > 0.5F) { // 50%
                    world.setBlock(lastBlock, xyz.iwolfking.woldsvaults.init.ModBlocks.NULLITE_ORE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    public static void summonLightning(VirtualWorld world, Player player, int radius, int tickInterval, float chanceToSummon) {
        if (player.tickCount % tickInterval == 0) {
            if(world.getRandom().nextFloat() < chanceToSummon) {
                BlockPos strikePos = new BlockPos(
                        player.getX() + world.getRandom().nextInt(radius * 2) - radius,
                        64,
                        player.getZ() + world.getRandom().nextInt(radius * 2) - radius
                );

                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.moveTo(strikePos.getX(), strikePos.getY(), strikePos.getZ());
                    world.addFreshEntity(lightning);
                }
            }
        }
    }


    public static void summonVoidSlashes(CorruptedObjective objective, VirtualWorld world, @NotNull Player player, int radius, int tickInterval, float chanceToSummon) {
        if (player.tickCount % tickInterval == 0 && objective.get(CorruptedObjective.DATA).hasCompletedInitial() && world.getRandom().nextFloat() < chanceToSummon) {
            int minDistance = 6; // Minimum safe distance, might work, might not, lmao

            double angle = world.getRandom().nextDouble() * 2 * Math.PI;
            double distance = minDistance + world.getRandom().nextDouble() * (radius - minDistance);

            int xOffset = (int) (Math.cos(angle) * distance);
            int zOffset = (int) (Math.sin(angle) * distance);

            BlockPos base = new BlockPos(
                    player.getX() + xOffset,
                    64,
                    player.getZ() + zOffset
            );

            CorruptedVaultHelper.generateSlash(world, base);
        }
    }

    public static void breakVaultPortal(VirtualWorld world, Vault vault, Player player) {
        TickClock tickClock = vault.get(Vault.CLOCK);
        if (tickClock.has(TickClock.PAUSED) && tickClock.has(TickClock.VISIBLE)) {
            vault.ifPresent(Vault.WORLD, manager -> {
                if(!(manager.get(WorldManager.PORTAL_LOGIC) instanceof ClassicPortalLogic logic)) return;

                if (logic.getPlayerStartPos(vault).map(start -> player.level.dimension().equals(world.dimension()) && player.distanceToSqr(Vec3.atCenterOf(start)) > 15 * 15).orElse(false)) {
                    logic.getPortals().forEach(portal -> {
                        BlockPos min = portal.get(PortalData.MIN);
                        BlockPos max = portal.get(PortalData.MAX);

                        if(world.getBlockState(min).isAir()) return;

                        for (int x = min.getX(); x <= max.getX(); x++) {
                            for (int y = min.getY(); y <= max.getY(); y++) {
                                for (int z = min.getZ(); z <= max.getZ(); z++) {
                                    BlockPos current = new BlockPos(x, y, z);
                                    world.destroyBlock(current, false);
                                }
                            }
                        }


                        player.sendMessage(
                                new TextComponent("You sense a portal shattering")
                                        .withStyle(Style.EMPTY.withItalic(true).withColor(11141120)),
                                Util.NIL_UUID
                        );
                        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.5F, 0.7F);


                        // Intended to hint at the player being able to kill mobs for time
                        Entity entityType = ModEntities.T5_BLOOD_HORDE.create(world);
                        if(entityType != null) {
                            entityType.moveTo(min, 0F, 0F);
                            world.addFreshEntity(entityType);
                        }
                    });
                    vault.get(Vault.CLOCK).remove(TickClock.PAUSED);
                }
            });
        }
    }

    public static void preventFruits(CorruptedObjective obj, Vault vault) {
        CommonEvents.FRUIT_EATEN.register(obj,  (data) -> {
            Vault playerVault = VaultUtils.getVault(data.getPlayer().getLevel()).orElse(null);
            if(playerVault != null && playerVault.equals(vault)) {
                data.setTime(0);
                data.getPlayer().displayClientMessage(new TextComponent("Seems pointless...").withStyle(ChatFormatting.RED), true);
            }
        });
    }

    public static void removeLocatorItem(CorruptedObjective obj, Vault vault) {
        CommonEvents.LISTENER_LEAVE.register(obj, data -> {
            if (data.getVault() == vault) {
                if (data.getListener() instanceof Runner) {

                    data.getListener().getPlayer().ifPresent(player -> {
                        for (InventoryUtil.ItemAccess items : InventoryUtil.findAllItems(player)) {
                            ItemStack stack = items.getStack();

                            if(stack.getItem() instanceof LocatorItem) {
                                items.setStack(ItemStack.EMPTY);
                            }
                        }
                    });
                }
            }
        });
    }



    public static void generateVaultModifierThresholds(CorruptedObjective obj, Random random) {
        float baseStart = 0.5F;
        int maxThresholds = 100;
        float maxCorruption = 25F;

        for (int i = 0; i < maxThresholds; i++) {
            baseStart+= (random.nextFloat() > 0.5F) ? 0.6F : 0.8F;

            obj.get(CorruptedObjective.CORRUPTION_THRESHOLDS).add(baseStart);

            if(baseStart > 10F) {
                baseStart -= (random.nextFloat() > 0.5F) ? 0.2F : 0.35F;
            }

            if(baseStart > maxCorruption) break;
        }
    }

    public static void handleObeliskUsage(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK).register(obj, (data) -> {
            if(data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
                return;
            }



            BlockPos pos = data.getPos();

            if (data.getState().getValue(ObeliskBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK) {
                data.setResult(InteractionResult.SUCCESS);
            }

            if(vault.get(Vault.LISTENERS).getObjectivePriority(data.getPlayer().getUUID(), obj) != 0)  {
                return;
            }

            CorruptedVaultHelper.playActivationEffects(world, pos);

            for (Objective objective : obj.get(CorruptedObjective.CHILDREN)) {
                if (objective instanceof KillBossObjective killBoss) {
                    killBoss.set(KillBossObjective.BOSS_POS, pos);
                }
            }

            if(pos.getY() < 64) {
                CorruptedVaultHelper.handleObeliskBelow(data, vault, world, pos);
            } else {
                CorruptedVaultHelper.handleObeliskAbove(data, world, pos, obj);
            }
        });
    }

    public static void handleMonolithUsage(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(xyz.iwolfking.woldsvaults.init.ModBlocks.MONOLITH_CONTROLLER).register(obj, (data) -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
                return;
            }

            BlockPos pos = data.getPos();

            if (data.getState().getValue(MonolithControllerBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != xyz.iwolfking.woldsvaults.init.ModBlocks.MONOLITH_CONTROLLER) {
                data.setResult(InteractionResult.SUCCESS);
            }

            Player player = data.getPlayer();
            ItemStack item = player.getMainHandItem();

            if(!item.hasTag()) {
                return;
            }

            CompoundTag nbt = item.getTag();
            boolean isFromThisVault = nbt != null && nbt.getUUID("VaultID").equals(vault.get(Vault.ID));

            if (item.getItem() == xyz.iwolfking.woldsvaults.init.ModItems.RUINED_ESSENCE
                    && obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) < obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET)
                    && isFromThisVault) {

                if(!player.getAbilities().instabuild) {
                    item.shrink(1);
                }

                obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.COUNT, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) + 1);
                world.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.75F * world.random.nextFloat() + 0.25F);


                if ((int) obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) == obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET)) {
                    obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.INITIAL_COMPLETION, true);

                    vault.ifPresent(Vault.CLOCK, (clock) -> clock.remove(TickClock.VISIBLE));
                }
            }

        });
    }

    public static void generateObjectiveBlocks(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(obj,
                (data) -> obj.ifPresent(CorruptedObjective.OBJECTIVE_PROBABILITY, (probability) -> data.setProbability((double)probability))
        );

        obj.registerObjectiveTemplate(world, vault);
//        // VaultFaster Objective placement fix or something
//        if(LoadingModList.get().getModFileById("vaultfaster") != null) {
//            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(obj, vault);
//
//            DynamicTemplate template = TemplateUtils.createTemplateFromPairs(
//                    new Pair<>(new BlockPos(0, 0, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.LOWER)),
//                    new Pair<>(new BlockPos(0, 1, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.UPPER))
//            );
//
//            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(obj, vault, template);
//        } else {
//            CommonEvents.BLOCK_SET.at(BlockSetEvent.Type.RETURN).in(world).register(obj, (data) -> {
//                PartialTile target = PartialTile.of(PartialBlockState.of(ModBlocks.PLACEHOLDER), PartialCompoundNbt.empty());
//                target.getState().set(PlaceholderBlock.TYPE, PlaceholderBlock.Type.OBJECTIVE);
//
//                if (target.isSubsetOf(PartialTile.of(data.getState()))) {
//                    data.getWorld().setBlock(data.getPos(), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState(), 3);
//                }
//            });
//        }

    }

    public static void handleKillTimeExtensions(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        CommonEvents.ENTITY_DEATH.register(obj, event -> {
            if(event.getEntity().level != world) return;
            if(obj.get(CorruptedObjective.DATA).hasCompletedInitial()) return;
            if(event.getSource().getEntity() instanceof Player && CorruptedVaultHelper.eligibleForExtraTime(vault)) {
                int timeLeft = vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME);
                int playerCount = vault.get(Vault.LISTENERS).getAll().size();
                int increase = CorruptedVaultHelper.calculateGradualTimeIncrease(timeLeft, playerCount);

                vault.get(Vault.CLOCK).addModifier(new KillMobTimeExtension(increase));


                if(increase != 0) {
                    obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.TIME_ADDEND_TICKS, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_ADDEND_TICKS) + increase);
                    obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK, 40); // Display the overlay for 2s
                }
            } else {
                event.getEntity().level.playSound(null, event.getEntity().blockPosition(), ModSounds.ARTIFACT_BOSS_CATALYST_HIT_WRONG, SoundSource.HOSTILE, 1.2F, 0.75F * world.random.nextFloat() + 0.65F);
            }
        });
    }

    public static void generateMonolithRoom(CorruptedObjective obj, Vault vault) {
        // Generating a specific room as the start room.
        CommonEvents.LAYOUT_TEMPLATE_GENERATION.register(obj, (data) -> {
            if (data.getVault() == vault && data.getPieceType() == VaultLayout.PieceType.ROOM) {
                Direction facing = data.getVault().get(Vault.WORLD).get(WorldManager.FACING);
                RegionPos back = data.getRegion().add(facing, -(data.getLayout().get(ClassicInfiniteLayout.TUNNEL_SPAN) + 1));
                if (back.getX() == 0 && back.getZ() == 0) {
                    TemplatePoolKey key = VaultRegistry.TEMPLATE_POOL.getKey(VaultMod.id("vault/rooms/special/lost_void"));
                    if (key == null) {
                        return;
                    }

                    data.setTemplate(data.getLayout().getRoom(key.get(vault.get(Vault.VERSION)), vault, vault.get(Vault.VERSION), data.getRegion(), data.getRandom(), data.getSettings()));
                    ResourceLocation theme = vault.get(Vault.WORLD).get(WorldManager.THEME);
                    ResourceLocation id = new ResourceLocation(theme.toString().replace("classic_vault_", "universal_"));
                    PaletteKey palette = VaultRegistry.PALETTE.getKey(id);
                    if (palette != null) {
                        data.getSettings().addProcessor(palette.get(Version.latest()));
                    }
                }

            }
        });
    }

    public static void generateRandomObelisks(CorruptedObjective obj, VirtualWorld world) {
        CommonEvents.TEMPLATE_GENERATION.in(world).at(TemplateGenerationEvent.Phase.POST).register(obj, data -> {
            if(data.getTemplate().getParent() instanceof EmptyTemplate) {
                return;
            }

            int attemptsPerChunk = 1;

            for(int i = 0; i < attemptsPerChunk; i++) {
                // Using ThreadLocalRandom because data.getRandom() sucks & places them in a gridlike pattern oftentimes
                int x = ThreadLocalRandom.current().nextInt(16) + (data.getChunkPos().x * 16);
                int z = ThreadLocalRandom.current().nextInt(16) + (data.getChunkPos().z * 16);
                int y = 64 + ThreadLocalRandom.current().nextInt(16);

                BlockPos pos = new BlockPos(x, y, z);
                ServerLevelAccessor serverLevelAccessor = data.getWorld();
                BlockState state = serverLevelAccessor.getBlockState(pos);


                if(state.getBlock() != Blocks.AIR) {
                    continue;
                }

                if(!serverLevelAccessor.getBlockState(pos.above()).isAir() || !serverLevelAccessor.getBlockState(pos.below()).isFaceSturdy(serverLevelAccessor, pos, Direction.UP)) {
                    continue;
                }


                IZonedWorld.runWithBypass(world, true, () -> {
                    PartialTile tile = PartialTile.of(
                            PartialBlockState.of(xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK),
                            PartialCompoundNbt.empty(),
                            pos
                    );

                    tile.place(serverLevelAccessor, pos, Block.UPDATE_ALL);
                    obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.SPAWNED_OBELISKS, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SPAWNED_OBELISKS) + 1);
                });
            }
        });
    }

    public static void setBaseVaultTimer(Vault vault) {
        vault.ifPresent(Vault.CLOCK, clock -> {
            if(clock instanceof TickTimer && clock.get(TickClock.GLOBAL_TIME) < 5) { // This is SO lazy but i dont care lmao
                clock.set(TickTimer.DISPLAY_TIME, 6000);
            }
        });
    }

    public static void summonWithers(VirtualWorld world, @NotNull ServerPlayer player, int radius, int amount) {
        for (int i = 0; i < amount; i++) {
            BlockPos spawnPos = new BlockPos(
                    player.getX() + world.getRandom().nextInt(radius * 2) - radius,
                    72,
                    player.getZ() + world.getRandom().nextInt(radius * 2) - radius
            );


            WitherBoss entity = new WitherBoss(EntityType.WITHER, world);

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 5)); // Speed 6
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1)); // Resistance 2

            entity.moveTo(spawnPos, 0, 0F);
            world.addFreshEntity(entity);
        }
    }

    public static void checkCorruptionEvents(CorruptedObjective obj, Vault vault, VirtualWorld world, float corruption) {
        for (Float threshold : obj.get(CorruptedObjective.CORRUPTION_THRESHOLDS)) {
            if (corruption >= threshold && !obj.get(CorruptedObjective.ACTIVE_THRESHOLDS).contains(threshold)) {
                if (world.getRandomPlayer() == null) return;

                ChunkRandom random = ChunkRandom.any();
                random.setBlockSeed(vault.get(Vault.SEED), world.getRandomPlayer().blockPosition(), 90039737L);

                List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(
                        obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.CORRUPTED_MODIFIER_POOL),
                        vault.get(Vault.LEVEL).get(),
                        random
                );

                for (VaultModifier<?> mod : modifiers) {
                    vault.get(Vault.MODIFIERS).addModifier(mod, 1, true, ChunkRandom.any());
                    world.players().forEach(player ->
                            player.sendMessage(
                                    mod.getChatDisplayNameComponent(1).copy().append(new TextComponent(" has been applied.").withStyle(ChatFormatting.RED)),
                                    Util.NIL_UUID
                            )
                    );
                }

                obj.get(CorruptedObjective.ACTIVE_THRESHOLDS).add(threshold);
                break;
            }
        }

        if(corruption > 25F) {
            if(obj.get(CorruptedObjective.DATA).hasCompletedInitial()) {
                vault.get(Vault.LISTENERS).getAll().forEach(listener ->
                        listener.getPlayer().ifPresent(player ->
                                reducePlayerHealth(player, 0.1F, 2, 400)
                        )
                );
            }


        }
    }

    public static void tickCorruption(CorruptedObjective obj, Vault vault, float currentMultiplier) {
        if(!vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.CORRUPTION, (float) vault.get(Vault.CLOCK).get(TickClock.LOGICAL_TIME) / 2500 * currentMultiplier);
        }
    }

    public static float setupVaultObjectiveValues(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        // Increase targets based on amount of players in the vault
        int playerCount = vault.get(Vault.LISTENERS).getAll().size();

        int baseTarget1 = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.BASE_TARGET);
        int baseTarget2 = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_BASE_TARGET);
        obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.TARGET, baseTarget1 + (playerCount * 5) - 5);
        obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.SECONDARY_TARGET, baseTarget2 + (playerCount * 5 - 5));



        float corruptionMultiplier = 1.0F + (playerCount * 0.2F);

        // If the secondary target is too high, we modify it to be 10 lower than the total amount of spawned obelisks, to make it fairer
        // This is unlikely to be ever hit, especially in high level vaults, and will NEVER be hit in infinite vaults, which will be the intended type of the vault.
        if (obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SPAWNED_OBELISKS) < obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_TARGET)) {
            obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.SECONDARY_TARGET, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SPAWNED_OBELISKS) - 10);
        }


        if (vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) < 0) {
            corruptionMultiplier += 1.0F;

            vault.get(Vault.LISTENERS).getAll().forEach(listener ->
                    listener.getPlayer().ifPresent(player -> {
                        if (player.tickCount % 600 == 0) {
                            player.addEffect(new MobEffectInstance(ModEffects.BLEED, 300, 1));
                        }
                    })
            );
        }

        // Increase the Corruption multiplier once the initial thing has been completed
        if (obj.get(CorruptedObjective.DATA).hasCompletedInitial()) {
            corruptionMultiplier += 1.0F;
        }

        return corruptionMultiplier;
    }

    public static void tickDisplayOverlay(CorruptedObjective obj, VirtualWorld world, Vault vault) {
        // Handle the Overlay display timer
        if (obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK) != 0) {
            obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK) - 1);

            if (obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK) == 0) {
                obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.TIME_ADDEND_TICKS, 0);
            }
        }

        // Play a sound upon the display time reaching 0
        if (vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) == 0) {
            for(Listener listener : vault.get(Vault.LISTENERS).getAll()) {
                listener.getPlayer().ifPresent(serverPlayer -> world.playSound(null, serverPlayer.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.4F, 0.75F));
            }
        }
    }


    public static boolean shouldDisplayEscapePrompt(CorruptedObjective obj) {
        return obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) >= obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET) &&
                obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_TICKED_FAKE) == 401;
    }

    private static void reducePlayerHealth(ServerPlayer player, float percentageToRemove, int minReduction, int interval) {
        if(player.tickCount % interval == 0) {
            AttributeInstance attributeInstance = player.getAttribute(Attributes.MAX_HEALTH);

            if (attributeInstance != null) {
                if (attributeInstance.getValue() < 10F) return;

                AttributeModifier existingModifier = attributeInstance.getModifier(HEALTH_REDUCTION_UUID);

                double reductionAmount = 0;

                if (existingModifier != null) {
                    reductionAmount += existingModifier.getAmount();
                    attributeInstance.removeModifier(HEALTH_REDUCTION_UUID);
                }

                reductionAmount -= Math.max(minReduction, attributeInstance.getValue() * percentageToRemove);
                attributeInstance.addPermanentModifier(new AttributeModifier(
                        HEALTH_REDUCTION_UUID,
                        "CorruptedVaultHealthReduction",
                        reductionAmount,
                        AttributeModifier.Operation.ADDITION
                ));
                player.displayClientMessage(new TextComponent("Your life seems to be slowly withering away..").withStyle(ChatFormatting.RED), true);
            }
        }
    }


    public static void removeHealthReductionAttribute(CorruptedObjective obj, Vault vault) {
        CommonEvents.LISTENER_LEAVE.register(obj, data -> {
            if (data.getVault() == vault) {
                if (data.getListener() instanceof Runner) {

                    data.getListener().getPlayer().ifPresent(player -> {
                        AttributeInstance attributeInstance = player.getAttribute(Attributes.MAX_HEALTH);
                        if (attributeInstance != null) {
                            attributeInstance.removeModifier(HEALTH_REDUCTION_UUID);
                        }
                    });
                }
            }
        });
    }


    public static void tickFakeVictory(CorruptedObjective obj) {
        obj.get(CorruptedObjective.DATA).set(CorruptedObjective.CData.TIME_TICKED_FAKE, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_TICKED_FAKE) + 1);
    }
}

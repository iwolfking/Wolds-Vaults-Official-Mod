package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import implementslegend.mod.vaultfaster.event.ObjectiveTemplateEvent;
import iskallia.vault.VaultMod;
import iskallia.vault.block.ObeliskBlock;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.*;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.data.sync.SyncMode;
import iskallia.vault.core.event.ClientEvents;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockSetEvent;
import iskallia.vault.core.event.common.BlockUseEvent;
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
import iskallia.vault.core.world.processor.Palette;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.DynamicTemplate;
import iskallia.vault.core.world.template.data.TemplatePool;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.init.*;
import iskallia.vault.network.message.MonolithIgniteMessage;
import iskallia.vault.network.message.VaultMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.blocks.FracturedObelisk;
import xyz.iwolfking.woldsvaults.blocks.MonolithControllerBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;
import xyz.iwolfking.woldsvaults.client.sfx.LoopSoundHandler;
import xyz.iwolfking.woldsvaults.data.compound.BlockPosList;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.events.vaultevents.client.WoldClientEvents;
import xyz.iwolfking.woldsvaults.modifiers.clock.KillMobTimeExtension;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.TemplateUtils;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

import java.util.*;

import static iskallia.vault.core.vault.time.TickClock.DISPLAY_TIME;

public class CorruptedObjective extends Objective {
    /* TODO FIX:
     * Sound playing in main menu
     * objective blocks not spawning outside dev
     */


    /* TODO Ideas:
     * == DONE: ==   Vault starts off with 5min
     * == DONE: ==   Players have to kill entities to gain time
     * == DONE: ==   Starts off with 5 mins, diminishing rewards -> 25mins -> 1 tick added per kill
     * == DONE: ==   Max of 30 minutes -> Indicate this through making the timer White when it reaches 29.5m
     * == DONE: ==   Show on hud that you gain time
     * == DONE: ==   Each 5min play an ominous "Tick" sound effect
     * == DONE: ==   override toolips for each item and add corruption LMFAO
     * == DONE: ==   Center Room houses The Monolith.
     * == DONE: ==   The Monolith needs to be charged with Ruined Essence TODO
     * == DONE: ==   Corruption essence decays over time, turning into "Vault Soot" if it fully decays, doesnt decay in the overworld, useful for something
     * Each Addition to the Monolith makes the Vault harder, but higher rewards TODO
     * Each Addition to the Monolith obstructs more of the tunnels
     * Monolith indicates Charge Level in some way.
     * The longer the player is in the vault, the higher "corruption"
     *   Makes the Player weaker
     *   Mining Fatigue, less mana regen, VERY minimal slowness
     *   Slowly start passively spawning mobs around the player
     * Corruption is lowered by "depositing" Ruined essebce into the monolith
     * == DONE: ==   Player cannot exit the vault -> Break the portal once the timer starts. Or custom room that doesnt even have a portal, might fuck some stuff tho.
     *  More corruption -> more tint around the screen
     *
     *
     * == DONE: ==   Once Monolith is fully charged then teleport player to the roof of the vault
     *
     * Maybe spawn end crystals around when charging the monolith lol
     *
     *
     * Custom theme with this
     */

    //TODO See how this works in a multiplayer scenario

    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("corrupted", Objective.class).with(Version.v1_31, CorruptedObjective::new);
    public static final ResourceLocation CORRUPTED_HUD = VaultMod.id("textures/gui/corrupted/hud.png");
    public static final ResourceLocation SHADER = new ResourceLocation("shaders/post/sobel.json");

    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> COUNT = FieldKey.of("count", Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Integer> TARGET = FieldKey.of("target", Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Integer> BASE_TARGET = FieldKey.of("base_target", Integer.class)
            .with(Version.v1_25, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class)
            .with(Version.v1_2, Adapters.FLOAT, DISK.all())
            .register(FIELDS);

    public static final FieldKey<ResourceLocation> STACK_MODIFIER_POOL = FieldKey.of("stack_modifier_pool", ResourceLocation.class)
            .with(Version.v1_22, Adapters.IDENTIFIER, DISK.all())
            .register(FIELDS);


    public static final FieldKey<Integer> DISPLAY_OVERLAY_TICK = FieldKey.of("display_overlay_tick", Integer.class)
            .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Integer> TIME_ADDEND_TICKS = FieldKey.of("time_addend_ticks", Integer.class)
            .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Boolean> INITIAL_COMPLETION = FieldKey.of("initial_completion", Boolean.class)
            .with(Version.v1_31, Adapters.BOOLEAN, DISK.all())
            .register(FIELDS);

    public static final FieldKey<Boolean> TRUE_COMPLETION = FieldKey.of("true_completion", Boolean.class)
            .with(Version.v1_31, Adapters.BOOLEAN, DISK.all())
            .register(FIELDS);

    public static final FieldKey<Integer> TIME_TICKED_FAKE = FieldKey.of("time_ticked_fake", Integer.class)
            .with(Version.v1_31, Adapters.INT_SEGMENTED_7, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<BlockPosList> BLOCKPOS_OBELISKS = FieldKey.of("blockpos_obelisks", BlockPosList.class)
            .with(Version.v1_31,
                    CompoundAdapter.of(BlockPosList::create),
                    DISK.all())
            .register(FIELDS);


    public static boolean queuedRefresh = true; // used for shader

    public CorruptedObjective() {
    }

    public CorruptedObjective(int target, float objectiveProbability, ResourceLocation stackModifierPool) {
        this.set(COUNT, 0);
        this.set(TARGET, target);
        this.set(BASE_TARGET, target);
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(STACK_MODIFIER_POOL, stackModifierPool);

        this.set(DISPLAY_OVERLAY_TICK, 0); // Duration for which the additional time is displayed on hud
        this.set(TIME_ADDEND_TICKS, 0); // Time added to display on hud
        this.set(INITIAL_COMPLETION, false); // Whether the primary objective was Completed
        this.set(TRUE_COMPLETION, false); // Whether the true Objective was completed
        this.set(TIME_TICKED_FAKE, 0); // The time ticked by the fake completion -> stops at 15s ticked

        this.set(BLOCKPOS_OBELISKS, BlockPosList.create()); // Obelisks that spawn on the roof
    }

    public static CorruptedObjective of(int target, float objectiveProbability, ResourceLocation stackModifierPool) {
        return new CorruptedObjective(target, objectiveProbability, stackModifierPool);
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }


    @Override
    public void initClient(Vault vault) {
        WoldClientEvents.RENDER_TICK_EVENT.register(vault, renderTickEvent -> {
            Minecraft mc = Minecraft.getInstance();
            GameRenderer render = mc.gameRenderer;

            if (queuedRefresh || render.currentEffect() == null) {
                render.loadEffect(SHADER);
                queuedRefresh = false;
            }
        });

        ClientEvents.CLIENT_TICK.register(vault, EventPriority.HIGH, data -> LoopSoundHandler.tick());

        VaultUtil.isVaultCorrupted = true;
        super.initClient(vault);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        vault.ifPresent(Vault.CLOCK, clock -> {
            if(clock instanceof TickTimer) {
                clock.set(TickTimer.DISPLAY_TIME, 6000);
            }
        });



        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this,
                (data) -> this.ifPresent(OBJECTIVE_PROBABILITY, (probability) -> data.setProbability((double)probability))
        );

        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK).register(this, (data) -> {
            if(data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
                return;
            }

            BlockPos pos = data.getPos();

            if (data.getState().getValue(ObeliskBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != ModBlocks.OBELISK) {
                data.setResult(InteractionResult.SUCCESS);
            }

            //if(data.getState().getValue(FracturedObelisk.FILLED)) return;
            if(vault.get(Vault.LISTENERS).getObjectivePriority(data.getPlayer().getUUID(), this) != 0) return;

            //world.setBlock(pos, world.getBlockState(pos).setValue(FracturedObelisk.FILLED, true), 3);
            this.playActivationEffects(world, pos);



            for (Objective objective : this.get(CHILDREN)) {
                if (objective instanceof KillBossObjective killBoss) {
                    killBoss.set(KillBossObjective.BOSS_POS, pos);
                }
            }


            if(pos.getY() < 64) {
                handleObeliskBelow(data, vault, world, pos);
            } else {
                handleObeliskAbove(data, vault, world, pos);
            }

        });

        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(xyz.iwolfking.woldsvaults.init.ModBlocks.MONOLITH_CONTROLLER).register(this, (data) -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
                return;
            }

            BlockPos pos = data.getPos();

            if (data.getState().getValue(MonolithControllerBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != xyz.iwolfking.woldsvaults.init.ModBlocks.MONOLITH_CONTROLLER) {
                data.setResult(InteractionResult.SUCCESS);
            }

            if (data.getPlayer().getMainHandItem().getItem() == xyz.iwolfking.woldsvaults.init.ModItems.RUINED_ESSENCE && this.get(COUNT) < this.get(TARGET)) {
                if(!data.getPlayer().getAbilities().instabuild) data.getPlayer().getMainHandItem().shrink(1);
                this.set(COUNT, this.get(COUNT) + 1);
                world.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.75F * world.random.nextFloat() + 0.25F);


                if ((int) this.get(COUNT) == this.get(TARGET)) {
                    this.set(INITIAL_COMPLETION, true);

                    vault.ifPresent(Vault.CLOCK, (clock) -> {
                        clock.set(TickClock.PAUSED);
                        clock.remove(TickClock.VISIBLE);
                    });
                }
            }

        });



        // VaultFaster Objective placement fix or something
        if(LoadingModList.get().getModFileById("vaultfaster") != null) {
            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(this, vault);

            DynamicTemplate template = TemplateUtils.createTemplateFromPairs(
                    new Pair<>(new BlockPos(0, 0, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.LOWER)),
                    new Pair<>(new BlockPos(0, 1, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.UPPER))
            );

            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(this, vault, template);
        } else {
            CommonEvents.BLOCK_SET.at(BlockSetEvent.Type.RETURN).in(world).register(this, (data) -> {
                PartialTile target = PartialTile.of(PartialBlockState.of(ModBlocks.PLACEHOLDER), PartialCompoundNbt.empty());
                target.getState().set(PlaceholderBlock.TYPE, PlaceholderBlock.Type.OBJECTIVE);

                if (target.isSubsetOf(PartialTile.of(data.getState()))) {
                    data.getWorld().setBlock(data.getPos(), xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState(), 3); // Might fuck up due to the double size
                }
            });
        }

        WoldCommonEvents.FRACTURED_OBELISK_UPDATE.register(this, (data) -> {
            if(data.getWorld() != world) return;

            if(data.getEntity().isGenerated() /*&& data.getState().getValue(CorruptedMonolith.FILLED)*/) {
                return; // If its generated
            }

            ResourceLocation pool = this.get(STACK_MODIFIER_POOL);
            if (pool != null) {
                int level = vault.get(Vault.LEVEL).getOr(VaultLevel.VALUE, 0);
                ChunkRandom random = ChunkRandom.any();
                random.setBlockSeed(vault.get(Vault.SEED), data.getPos(), 90039737L);

                for (VaultModifier<?> vaultModifier : ModConfigs.VAULT_MODIFIER_POOLS.getRandom(pool, level, random)) {
                    data.getEntity().addModifier(vaultModifier);
                }
            }
            data.getEntity().setGenerated(true);
        });

        CommonEvents.ENTITY_DEATH.register(this, event -> {
            if(event.getEntity().level != world) return;
            if(event.getSource().getEntity() instanceof Player && this.eligibleForExtraTime(vault)) {
                int timeLeft = vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME);
                int increase = calculateGradualTimeIncrease(timeLeft);

                vault.get(Vault.CLOCK).addModifier(new KillMobTimeExtension(increase));


                if(increase != 0) {
                    this.set(TIME_ADDEND_TICKS, this.get(TIME_ADDEND_TICKS) + increase);
                    this.set(DISPLAY_OVERLAY_TICK, 40); // display the overlay for 2s
                }
            } else {
                event.getEntity().level.playSound(null, event.getEntity().blockPosition(), ModSounds.ARTIFACT_BOSS_CATALYST_HIT_WRONG, SoundSource.HOSTILE, 1.2F, 0.75F * world.random.nextFloat() + 0.65F);
            }
        });

        // Generating a specific room as the start room.
        CommonEvents.LAYOUT_TEMPLATE_GENERATION.register(this, (data) -> {
            if (data.getVault() == vault && data.getPieceType() == VaultLayout.PieceType.ROOM) {
                Direction facing = data.getVault().get(Vault.WORLD).get(WorldManager.FACING);
                RegionPos back = data.getRegion().add(facing, -(data.getLayout().get(ClassicInfiniteLayout.TUNNEL_SPAN) + 1));
                if (back.getX() == 0 && back.getZ() == 0) {
                    TemplatePoolKey key = VaultRegistry.TEMPLATE_POOL.getKey(VaultMod.id("vault/rooms/special/lost_void"));
                    if (key == null) {
                        return;
                    }

                    data.setTemplate(data.getLayout().getRoom(key.get(vault.get(Vault.VERSION)), vault.get(Vault.VERSION), data.getRegion(), data.getRandom(), data.getSettings()));
                    ResourceLocation theme = vault.get(Vault.WORLD).get(WorldManager.THEME);
                    ResourceLocation id = new ResourceLocation(theme.toString().replace("classic_vault_", "universal_"));
                    PaletteKey palette = VaultRegistry.PALETTE.getKey(id);
                    if (palette != null) {
                        data.getSettings().addProcessor(palette.get(Version.latest()));
                    }
                }

            }
        });

        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, 0.0).getIncrease();
        this.set(TARGET, (int)Math.round((double)this.get(BASE_TARGET) * (1.0 + increase)));




        if(this.get(DISPLAY_OVERLAY_TICK) != 0) {
            this.set(DISPLAY_OVERLAY_TICK, this.get(DISPLAY_OVERLAY_TICK) - 1);

            if(this.get(DISPLAY_OVERLAY_TICK) == 0) {
                this.set(TIME_ADDEND_TICKS, 0);
            }
        }

        if(this.isCompleted()) {
            super.tickServer(world, vault);
        }

        // wither spawn sfx
        if(vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) == 0) {
            for(Listener listener : vault.get(Vault.LISTENERS).getAll()) {
                listener.getPlayer().ifPresent(serverPlayer -> world.playSound(null, serverPlayer.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.4F, 0.75F));
            }
        }

        if(vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) < 0) {

        }
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if(listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if(listener instanceof Runner && this.isCompleted()) {
            super.tickListener(world, vault, listener);
        }

        if(this.get(INITIAL_COMPLETION) && this.get(TIME_TICKED_FAKE) <= 400) {
            fakeVictory1(world, vault, listener);
        }

        if(this.get(INITIAL_COMPLETION) && this.get(TIME_TICKED_FAKE) > 400) {
            listener.getPlayer().ifPresent(player -> {
                BlockPos playerPos = player.blockPosition();
                List<BlockPos> obeliskPos = new ArrayList<>(this.get(BLOCKPOS_OBELISKS));

                BlockPos closest = obeliskPos.stream()
                        .min(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)))
                        .orElse(null);

                if (!(closest == null) && !playerPos.closerThan(closest, 16.0)) { // What
                    if(world.getGameTime() % 200 == 0) {
                        spawnGuidanceParticles(world, player.blockPosition(), closest);
                    }
                }
            });
        }

        listener.getPlayer().ifPresent(player -> {
            TickClock tickClock = vault.get(Vault.CLOCK);
            if (tickClock.has(TickClock.PAUSED) && tickClock.has(TickClock.VISIBLE)) {
                vault.ifPresent(Vault.WORLD, manager -> {
                    if(!(manager.get(WorldManager.PORTAL_LOGIC) instanceof ClassicPortalLogic logic)) return;

                    if (logic.getPlayerStartPos(vault).map(start -> player.level.dimension().equals(world.dimension()) && player.distanceToSqr(Vec3.atCenterOf(start)) > 15 * 15).orElse(false)) {
                        logic.getPortals().forEach(d -> {
                            BlockPos min = d.get(PortalData.MIN);
                            BlockPos max = d.get(PortalData.MAX);

                            for (int x = min.getX(); x <= max.getX(); x++) {
                                for (int y = min.getY(); y <= max.getY(); y++) {
                                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                                        BlockPos current = new BlockPos(x, y, z);
                                        world.destroyBlock(current, false);
                                    }
                                }
                            }

                            for (ServerPlayer player1 : world.players()) {
                                player1.sendMessage(
                                        new TextComponent("You sense a portal shattering")
                                                .withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_RED.getColor())),
                                        Util.NIL_UUID
                                );
                                player1.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.5F, 0.7F);
                            }
                        });
                        vault.get(Vault.CLOCK).remove(TickClock.PAUSED);
                    }
                });
            }
            ModNetwork.CHANNEL.sendTo(new VaultMessage.Sync(player, vault, SyncMode.FULL), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        });


    }





    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;




//                TickTimer timer = ((TickTimer) (Object)this);
//                if(!timer.has(VISIBLE)) return;
//                int color = getTextColor(timer.get(DISPLAY_TIME));
//                MutableComponent cmp = ComponentUtils.corruptComponent(new TextComponent(UIHelper.formatTimeString(timer.get(DISPLAY_TIME))));
//                FontHelper.drawStringWithBorder(matrixStack, cmp, -12, 13, color, 0xFF000000);
//
//        }

        if (this.get(DISPLAY_OVERLAY_TICK) > 0) {
            if (player != null) {
                float alpha = Math.min(1.0f, this.get(DISPLAY_OVERLAY_TICK) / (float) 40); // 2s base
                int textColor = (int) (255 * alpha) << 24 | 0xFFFFFF; // Apply alpha to the color (ARGB format)

                MutableComponent cmp = new TextComponent("+" + (this.get(TIME_ADDEND_TICKS) / 20.0) + "s");
                FontHelper.drawStringWithBorder(matrixStack, cmp, 76, window.getGuiScaledHeight() - 44, textColor, 0xFF000000);
            }
        }

        if (this.get(COUNT) >= this.get(TARGET) && this.get(TIME_TICKED_FAKE) == 401) {
            int midX = window.getGuiScaledWidth() / 2;
            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            MutableComponent txt = new TextComponent("Find an escape").withStyle(ChatFormatting.RED);

            float messageWidth = font.width(txt);
            float drawX = midX - (messageWidth / 2.0F);

            font.drawInBatch(txt.getVisualOrderText(), drawX, 9.0F, -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
            buffer.endBatch();
            return true;
        }


        int current = this.get(COUNT);
        int total = this.get(TARGET);

        Component txt = new TextComponent(String.valueOf(current)).withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true))
                .append(new TextComponent(" / ").withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true)))
                .append(new TextComponent(String.valueOf(total)).withStyle(style -> style.withColor(ChatFormatting.DARK_RED).withObfuscated(true).withBold(true)));

        int midX = window.getGuiScaledWidth() / 2;


        matrixStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int previousTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, CORRUPTED_HUD);

        float progress = (float)current / (float)total;
        matrixStack.translate((midX - 80), 8.0F, 0.0F);
        GuiComponent.blit(matrixStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
        GuiComponent.blit(matrixStack, 0, 8, 0.0F, 30.0F, 13 + (int)(130.0F * progress), 10, 200, 100);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, previousTexture);

        matrixStack.popPose();


        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        matrixStack.pushPose();
        matrixStack.scale(0.6F, 0.6F, 0.6F);
        float xPos = (float)midX / 0.6F - (float)font.width(txt) / 2.0F;
        float yPos = font.lineHeight + 22;
        font.drawInBatch(txt.getVisualOrderText(), xPos, yPos, -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());

        buffer.endBatch();
        matrixStack.popPose();

        return true;
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if(!isCompleted()) {
            return objective == this;
        }

        for(Objective child : this.get(CHILDREN)) {
            if(child.isActive(world, vault, objective)) return true;
        }

        return false;
    }




    /* Vault Objective Methods */

    private void fakeVictory1(VirtualWorld world, Vault vault, Listener listener) {
        int time = this.get(TIME_TICKED_FAKE);

        listener.getPlayer().ifPresent(player -> {
            if (time == 0) {
                // First tick setup: firework, sound, clean title
                FireworkRocketEntity fireworks = new FireworkRocketEntity(world, player.getX(), player.getY(), player.getZ(), new ItemStack(Items.FIREWORK_ROCKET));
                world.addFreshEntity(fireworks);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 0.6F, 0.8F);

                TextComponent title = new TextComponent("Vault Completed!!");
                title.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(14536734)));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
            }

            if (time >= 100 && time < 350 && time % 5 == 0) {
                String jumbled = jumbleCharacters("Vault Completed!!", world.random);
                Component corrupted = ComponentUtils.corruptComponent(new TextComponent(jumbled).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#870c03"))));
                player.connection.send(new ClientboundSetTitleTextPacket(corrupted));

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BIT, SoundSource.MASTER, 0.8F, 0.5F + (world.random.nextFloat() * 0.5F));
            }
            if (time == 400) {
                spawnObelisksAround(world, new BlockPos(0, 64, 0));
                player.teleportTo(0, 64, 0);

                world.playSound(null, new BlockPos(0, 64, 0), SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 0.5F, 0.8F);
            }
        });

        this.set(TIME_TICKED_FAKE, this.get(TIME_TICKED_FAKE) + 1);
    }

    private String jumbleCharacters(String input, Random random) {
        StringBuilder result = new StringBuilder(input);

        int length = input.length();
        int numToCorrupt = Math.max(1, (int) (length * (0.3 + random.nextDouble() * 0.2))); // 30% to 50%

        Set<Integer> indices = new HashSet<>();
        while (indices.size() < numToCorrupt) {
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
                replacement = (char) (33 + random.nextInt(15)); // symbols like ! " # $
            }
            result.setCharAt(i, replacement);
        }

        return result.toString();
    }

    public void spawnObelisksAround(VirtualWorld world, BlockPos center) {
        // Maybe instead of this, we spawn them on the worldgen via the event :3

        Random random = world.getRandom();
        int count = 5 + random.nextInt(6); // 5–10 obelisks

        for (int i = 0; i < count; i++) {
            int offsetX = -128 + random.nextInt(257); // -128 -> 128
            int offsetZ = -128 + random.nextInt(257);
            BlockPos pos = center.offset(offsetX, 0, offsetZ);
            BlockPos targetPos = new BlockPos(pos.getX(), 64, pos.getZ());

            if (world.isEmptyBlock(targetPos)) {
                world.setBlock(targetPos, xyz.iwolfking.woldsvaults.init.ModBlocks.FRACTURED_OBELISK.defaultBlockState(), 3);
                 this.get(BLOCKPOS_OBELISKS).add(targetPos);
            }
        }
    }

    private void spawnGuidanceParticles(VirtualWorld level, BlockPos from, BlockPos to) {
        if (to == null) return;
        Vec3 dir = Vec3.atCenterOf(to).subtract(Vec3.atCenterOf(from)).normalize();
        Random random = level.getRandom();

        for (int i = 1; i <= 30; i++) {
            Vec3 pos = Vec3.atCenterOf(from).add(dir.scale(i * 3)) // distance between steps
                    .add(random.nextGaussian(), 0.3, random.nextGaussian());

            for (int j = 0; j < 3; j++) {
                pos.add(random.nextGaussian() * 0.1, 0.3 + random.nextDouble() * 0.2, random.nextGaussian() * 0.1);
                level.sendParticles(ModParticles.YELLOW_FLAME.get(), pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0);
            }

        }
    }




    /**
     * Utility method that gets called upon interacting with a Fractured Obelisk
     *
     * @param world ServerLevel to play the effects in
     * @param pos Position to play the effects at
     */
    private void playActivationEffects(VirtualWorld world, BlockPos pos) {
        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new MonolithIgniteMessage(pos)); // TODO
        world.playSound(null, pos, ModSounds.ARTIFACT_BOSS_CATALYST_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Utility method that gets called upon interacting with a Fractured Obelisk<br>
     * Spreads {@code the_vault:error_block}s in a set radius of 7 blocks around itself
     *
     * @param world ServerLevel to modify the blocks in
     * @param startPos Position of the obelisk
     */
    private void spreadErrorBlocks(VirtualWorld world, BlockPos startPos) {
        int radius = 7;
        Random random = world.getRandom();

        // Center coordinates
        int centerX = startPos.getX();
        int centerZ = startPos.getZ();

        // Loop through a square bounding box that contains the circle
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // Check if the current position is within the circular area
                if (Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2) <= Math.pow(radius, 2)) {
                    BlockPos currentPos = new BlockPos(x, startPos.getY() - 1, z); // Target the block directly below

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

    private void handleObeliskBelow(BlockUseEvent.Data data, Vault vault, VirtualWorld world, BlockPos pos) {
        if(data.getWorld().getBlockEntity(pos) instanceof FracturedObeliskTileEntity tile && !tile.getModifiers().isEmpty()) {
            Iterator<Map.Entry<ResourceLocation, Integer>> it = tile.getModifiers().entrySet().iterator();
            TextComponent suffix = new TextComponent("");

            while(it.hasNext()) {
                Map.Entry<ResourceLocation, Integer> entry = it.next();
                VaultModifier<?> modifier = VaultModifierRegistry.get(entry.getKey());
                suffix.append(modifier.getChatDisplayNameComponent(entry.getValue()));

                if(it.hasNext()) {
                    suffix.append(new TextComponent(", "));
                }
            }
            TextComponent text = new TextComponent("");
            if (!tile.getModifiers().isEmpty()) {
                text.append(new TextComponent("???").withStyle(ChatFormatting.RED))
                        .append(new TextComponent(" added ").withStyle(ChatFormatting.GRAY))
                        .append(ComponentUtils.partiallyObfuscate(suffix, 0.4))
                        .append(new TextComponent(".").withStyle(ChatFormatting.GRAY));

            }

            ChunkRandom random = ChunkRandom.any();
            random.setBlockSeed(vault.get(Vault.SEED), data.getPos(), 90039737L);

            tile.getModifiers().forEach((modifierx, count) -> {
                (vault.get(Vault.MODIFIERS)).addModifier(VaultModifierRegistry.get(modifierx), count, true, random);
            });

            for(Listener listener : vault.get(Vault.LISTENERS).getAll()) {
                listener.getPlayer().ifPresent(other -> {
                    other.displayClientMessage(text, false);
                });
            }

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            spreadErrorBlocks(world, pos);

            FloatingItemEntity floatingItem = FloatingItemEntity.create(world, pos.above(), new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.RUINED_ESSENCE));
            world.addFreshEntity(floatingItem);


            data.setResult(InteractionResult.SUCCESS);
        }
    }

    private void handleObeliskAbove(BlockUseEvent.Data data, Vault vault, VirtualWorld world, BlockPos pos) {

    }

    private int calculateGradualTimeIncrease(int timeLeftInTicks) {
        int timeLeftInSeconds = timeLeftInTicks / 20; // Convert ticks to seconds
        int maxTimeInSeconds = 1800; // 30 minutes
        int maxAddTimeInTicks = 150; // 7.5s

        float fraction = 1 - ((float) timeLeftInSeconds / maxTimeInSeconds);
        return Math.max(0, Math.round(maxAddTimeInTicks * fraction));
    }

    private boolean isCompleted() {
        return this.get(COUNT) >= this.get(TARGET) && this.get(INITIAL_COMPLETION) && this.get(TRUE_COMPLETION);
    }

    private boolean eligibleForExtraTime(Vault vault) {
        return vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME) > 0;
    }
}
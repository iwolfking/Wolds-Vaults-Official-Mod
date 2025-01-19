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
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.*;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockSetEvent;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.KillBossObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.DynamicTemplate;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.init.*;
import iskallia.vault.network.message.MonolithIgniteMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.blocks.CorruptedMonolith;
import xyz.iwolfking.woldsvaults.blocks.tiles.CorruptedMonolithTileEntity;
import xyz.iwolfking.woldsvaults.events.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.TemplateUtils;

import java.util.*;

import static iskallia.vault.core.vault.Vault.LISTENERS;

public class CorruptedObjective extends Objective {

    /* TODO Ideas:
     * Vault starts off with 5min TODO
     * Players have to kill entities to gain time
     * Starts off with 5 seconds, diminishing rewards -> 25mins -> 1 tick added per kill TODO
     * Max of 30 minutes -> Indicate this through making the timer White when it reaches 29.5m TODO
     * Show on hud that you gain time TODO
     * Each 5min play an ominous "Tick" sound effect TODO
     * Mob killed spreads "Corruption" similar to sculk
     * Center Room houses The Monolith.
     * The Monolith needs to be charged with Corrupted Essence
     * Corruption essence decays over time, turning into "Vault Soot" if it fully decays, doesnt decay in the overworld, useful for something
     * Each Addition to the Monolith makes the Vault harder, but higher rewards (?)
     * Each Addition to the Monolith obstructs more of the tunnels
     * Monolith indicates Charge Level in some way.
     * The longer the player is in the vault, the higher "corruption"
     *   Makes the Player weaker
     *   Mining Fatigue, less mana regen, VERY minimal slowness
     *   Slowly start passively spawning mobs around the player
     * Corruption is lowered by "depositing" corruption pieces into the monolith
     * Player cannot exit the vault -> Break the portal once the timer starts. Or custom room that doesnt even have a portal, might fuck some stuff tho.
     * More corruption -> more tint around the screen, (pls apply after render) (cake tint)
     *
     *  Ensure the essence cannot be put in external inventories in the vault.
     * Once Monolith is fully charged then
     * - teleport player to the roof of the vault
     * - ...
     * steal shader from uhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
     * you know the mod a1
     *
     *
     *
     * Custom theme with this
     */

    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("corrupted", Objective.class).with(Version.v1_31, CorruptedObjective::new);
    public static final ResourceLocation CORRUPTED_HUD = VaultMod.id("textures/gui/corrupted/hud.png");

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


    public static final FieldKey<Integer> SECOND_TARGET = FieldKey.of("second_target", Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
            .register(FIELDS);

    public static final FieldKey<Integer> BASE_SECOND_TARGET = FieldKey.of("base_second_target", Integer.class)
            .with(Version.v1_25, Adapters.INT_SEGMENTED_3, DISK.all().or(CLIENT.all()))
            .register(FIELDS);


    public static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class)
            .with(Version.v1_2, Adapters.FLOAT, DISK.all())
            .register(FIELDS);

    public static final FieldKey<ResourceLocation> STACK_MODIFIER_POOL = FieldKey.of("stack_modifier_pool", ResourceLocation.class)
            .with(Version.v1_22, Adapters.IDENTIFIER, DISK.all())
            .register(FIELDS);

    public static final FieldKey<Boolean> COMPLETED_FIRST_TARGET = FieldKey.of("completed_first_target", Boolean.class)
            .with(Version.v1_25, Adapters.BOOLEAN, DISK.all().or(CLIENT.all()))
            .register(FIELDS);



    public CorruptedObjective() {
    }

    public CorruptedObjective(int target, int secondTarget, float objectiveProbability, ResourceLocation stackModifierPool) {
        this.set(COUNT, 0);
        this.set(TARGET, target); // Actual target is double of this, it loops around
        this.set(BASE_TARGET, target); // Actual target is double of this, it loops around
        this.set(SECOND_TARGET, secondTarget);
        this.set(BASE_SECOND_TARGET, secondTarget);
        this.set(OBJECTIVE_PROBABILITY, objectiveProbability);
        this.set(STACK_MODIFIER_POOL, stackModifierPool);
        this.set(COMPLETED_FIRST_TARGET, false);
    }

    public static CorruptedObjective of(int target, int secondTarget, float objectiveProbability, ResourceLocation stackModifierPool) {
        return new CorruptedObjective(target, secondTarget, objectiveProbability, stackModifierPool);
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
    public void initServer(VirtualWorld world, Vault vault) {
        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, (data) -> {
            this.ifPresent(OBJECTIVE_PROBABILITY, (probability) -> {
                data.setProbability((double)probability);
            });
        });

        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_MONOLITH).register(this, (data) -> {
            if(data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
                return;
            }

            BlockPos pos = data.getPos();

            if (data.getState().getValue(ObeliskBlock.HALF) == DoubleBlockHalf.UPPER && world.getBlockState(pos = pos.below()).getBlock() != ModBlocks.OBELISK) {
                data.setResult(InteractionResult.SUCCESS);
            }

            if(data.getState().getValue(CorruptedMonolith.FILLED)) return;



            if(vault.get(Vault.LISTENERS).getObjectivePriority(data.getPlayer().getUUID(), this) != 0) return;

            world.setBlock(pos, world.getBlockState(pos).setValue(CorruptedMonolith.FILLED, true), 3);
            this.playActivationEffects(world, pos);

            if(!this.get(COMPLETED_FIRST_TARGET)) {
                this.set(COUNT, this.get(COUNT) + 1);  // Maybe summon mobs like ObeliskObjective
            } else {
                this.set(COUNT, this.get(COUNT) -1);
            }


            for (Objective objective : this.get(CHILDREN)) {
                if (objective instanceof KillBossObjective killBoss) {
                    killBoss.set(KillBossObjective.BOSS_POS, pos);
                }
            }

            if(data.getWorld().getBlockEntity(pos) instanceof CorruptedMonolithTileEntity tile && !tile.getModifiers().isEmpty()) {
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

                for(Listener listener : vault.get(LISTENERS).getAll()) {
                    listener.getPlayer().ifPresent(other -> {
                        other.displayClientMessage(text, false);
                    });
                }

                world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                spreadErrorBlocks(world, pos);

                FloatingItemEntity floatingItem = FloatingItemEntity.create(world, pos.above(), new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.RUINED_ESSENCE));
                world.addFreshEntity(floatingItem); // TODO item that progressively decays -> 5min and its useless besides soul value

                data.setResult(InteractionResult.SUCCESS);
            }
        });



        // VaultFaster Objective placement fix or something
        if(LoadingModList.get().getModFileById("vaultfaster") != null) {
            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(this, vault);

            DynamicTemplate template = TemplateUtils.createTemplateFromPairs(
                    new Pair<>(new BlockPos(0, 0, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_MONOLITH.defaultBlockState().setValue(CorruptedMonolith.HALF, DoubleBlockHalf.LOWER)),
                    new Pair<>(new BlockPos(0, 1, 0), xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_MONOLITH.defaultBlockState().setValue(CorruptedMonolith.HALF, DoubleBlockHalf.UPPER))
            );

            ObjectiveTemplateEvent.INSTANCE.registerObjectiveTemplate(this, vault, template);
        } else {
            CommonEvents.BLOCK_SET.at(BlockSetEvent.Type.RETURN).in(world).register(this, (data) -> {
                PartialTile target = PartialTile.of(PartialBlockState.of(ModBlocks.PLACEHOLDER), PartialCompoundNbt.empty());
                target.getState().set(PlaceholderBlock.TYPE, PlaceholderBlock.Type.OBJECTIVE);

                if (target.isSubsetOf(PartialTile.of(data.getState()))) {
                    data.getWorld().setBlock(data.getPos(), xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_MONOLITH.defaultBlockState(), 3); // Might fuck up due to the double size
                }
            });
        }

        WoldCommonEvents.CORRUPTED_MONOLITH_UPDATE.register(this, (data) -> {
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

        // Generating a specific room as the start room.
//        CommonEvents.LAYOUT_TEMPLATE_GENERATION.register(this, (data) -> {
//            if (data.getVault() == vault && data.getPieceType() == VaultLayout.PieceType.ROOM) {
//                Direction facing = (Direction)((WorldManager)data.getVault().get(Vault.WORLD)).get(WorldManager.FACING);
//                RegionPos back = data.getRegion().add(facing, -((Integer)data.getLayout().get(ClassicInfiniteLayout.TUNNEL_SPAN) + 1));
//                if (back.getX() == 0 && back.getZ() == 0 || data.getRandom().nextFloat() < (Float)this.getOr(OBJECTIVE_PROBABILITY, 0.0F)) {
//                    TemplatePoolKey key = (TemplatePoolKey) VaultRegistry.TEMPLATE_POOL.getKey(VaultMod.id("vault/rooms/special/boss"));
//                    if (key == null) {
//                        return;
//                    }
//
//                    data.setTemplate(data.getLayout().getRoom((TemplatePool)key.get((Version)vault.get(Vault.VERSION)), (Version)vault.get(Vault.VERSION), data.getRegion(), data.getRandom(), data.getSettings()));
//                    ResourceLocation theme = (ResourceLocation)((WorldManager)vault.get(Vault.WORLD)).get(WorldManager.THEME);
//                    ResourceLocation id = new ResourceLocation(theme.toString().replace("classic_vault_", "universal_"));
//                    PaletteKey palette = (PaletteKey)VaultRegistry.PALETTE.getKey(id);
//                    if (palette != null) {
//                        data.getSettings().addProcessor((Palette)palette.get(Version.latest()));
//                    }
//                }
//
//            }
//        });

        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, 0.0).getIncrease();
        this.set(TARGET, (int)Math.round((double)this.get(BASE_TARGET) * (1.0 + increase)));

        if (this.get(COUNT) >= this.get(TARGET)) {
            this.set(COMPLETED_FIRST_TARGET, true);
        }

        // If it looped around, tick super.
        if(this.get(COUNT) >= this.get(TARGET) && this.get(COMPLETED_FIRST_TARGET)) {
            super.tickServer(world, vault);
        }
    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
        if(listener.getPriority(this) < 0) {
            listener.addObjective(vault, this);
        }

        if(listener instanceof Runner && this.get(COUNT) >= this.get(TARGET)) {
            super.tickListener(world, vault, listener);
        }
    }




    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
        if (this.get(COUNT) >= this.get(TARGET)) {
            int midX = window.getGuiScaledWidth() / 2;
            Font font = Minecraft.getInstance().font;
            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            MutableComponent txt = new TextComponent("")
                    .append(new TextComponent("Pillage").withStyle(style -> style.withColor(ChatFormatting.DARK_RED).withObfuscated(true)))
                    .append(new TextComponent(", or ").withStyle(ChatFormatting.RED))
                    .append(new TextComponent("Exit").withStyle(style -> style.withColor(ChatFormatting.DARK_RED).withObfuscated(true)))
                    .append(new TextComponent(" to Complete").withStyle(ChatFormatting.RED));
            FormattedCharSequence var18 = txt.getVisualOrderText();
            float var19 = (float)midX - (float)font.width(txt) / 2.0F;
            font.drawInBatch(var18, var19, 9.0F, -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
            buffer.endBatch();
            return true;
        }


        int current = this.get(COUNT);
        int total = this.get(TARGET) - 1;


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

        Font font = Minecraft.getInstance().font;
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
        if(this.get(COUNT) < this.get(TARGET)) {
            return objective == this;
        }

        for(Objective child : this.get(CHILDREN)) {
            if(child.isActive(world, vault, objective)) return true;
        }

        return false;
    }

    private void playActivationEffects(VirtualWorld world, BlockPos pos) {
        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new MonolithIgniteMessage(pos)); // TODO
        world.playSound(null, pos, ModSounds.ARTIFACT_BOSS_CATALYST_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void spreadErrorBlocks(Level world, BlockPos startPos) {
        // Define the radius of the circular area
        int radius = 7; // Half of the 7x7 area
        Random random = new Random();

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
                        // Apply randomness (50% chance) before replacing
                        if (random.nextFloat() >= 0.5F) {
                            // Replace the block with the ERROR_BLOCK
                            world.setBlock(currentPos, ModBlocks.ERROR_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
    }
}

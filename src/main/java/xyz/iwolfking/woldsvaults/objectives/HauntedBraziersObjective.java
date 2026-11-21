package xyz.iwolfking.woldsvaults.objectives;


import iskallia.vault.VaultMod;
import iskallia.vault.block.MonolithBlock;
import iskallia.vault.block.entity.MonolithTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.MonolithObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.brazier.BrazierBlessing;
import iskallia.vault.core.vault.objective.brazier.BrazierHold;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveHelper;

import java.util.*;

//TODO: Rework Haunted Braziers a bit to better suit the new Brazier objective
public class HauntedBraziersObjective extends MonolithObjective {
    public static final SupplierKey<Objective> E_KEY = SupplierKey.of("haunted_braziers", Objective.class).with(Version.v1_12, HauntedBraziersObjective::new);
    public static final ResourceLocation HAUNTED_HUD = VaultMod.id("textures/gui/monolith/haunted_hud.png");
    public HauntedBraziersObjective() {
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return E_KEY;
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


    public void initServer(VirtualWorld world, Vault vault) {
        ObjectiveHelper.handleAddingNormalizedToVault(vault, world);

        ObjectiveHelper.addInitModifiersToVault(vault, resourceLocations -> {
            resourceLocations.add(WoldsVaults.id("ghost_town"));
            resourceLocations.add(VaultMod.id("haunting"));
        });

        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, (data) -> {
            if (data.getVault() == vault) {
                this.ifPresent(OBJECTIVE_PROBABILITY, (probability) -> data.setProbability((double)probability));
            }
        });
        this.registerObjectiveTemplate(world, vault);
        CommonEvents.BLOCK_USE.in(world).at(BlockUseEvent.Phase.HEAD).of(ModBlocks.MONOLITH).register(this, (data) -> {
            if (data.getHand() != InteractionHand.MAIN_HAND) {
                data.setResult(InteractionResult.SUCCESS);
            } else if (data.getState().getValue(MonolithBlock.STATE) == MonolithBlock.State.EXTINGUISHED) {
                if (vault.get(Vault.LISTENERS).getObjectivePriority(data.getPlayer().getUUID(), this) == 0) {
                    BlockEntity patt6702$temp = data.getWorld().getBlockEntity(data.getPos());
                    if (patt6702$temp instanceof MonolithTileEntity tile) {
                        data.setResult(InteractionResult.SUCCESS);
                        if (!tile.isHolding()) {
                            if (tile.getBlessing().isEmpty()) {
                                Optional<BrazierBlessing> var10000 = this.rollBlessing(vault, data.getPos());
                                Objects.requireNonNull(tile);
                                var10000.ifPresent(tile::setBlessing);
                            }

                            if (tile.getBlessing().isPresent()) {
                                BrazierHold.start(world, vault, this, tile, data.getPos());
                            }
                        }
                    }
                }
            }
        });
        CommonEvents.MONOLITH_UPDATE.register(this, (data) -> {
            if (data.getWorld() == world) {
                if (data.getState().getValue(MonolithBlock.STATE) == MonolithBlock.State.EXTINGUISHED) {
                    MonolithTileEntity tile = data.getEntity();
                    if (tile.isHolding()) {
                        BrazierHold.tick(world, vault, this, tile, data.getPos());
                    } else {
                        if (tile.getBlessing().isEmpty() && (world.getGameTime() + data.getPos().asLong()) % 10L == 0L && this.isPlayerNear(world, data.getPos())) {
                            Optional<BrazierBlessing> var10000 = this.rollBlessing(vault, data.getPos());
                            Objects.requireNonNull(tile);
                            var10000.ifPresent(tile::setBlessing);
                        }

                    }
                }
            }
        });
        this.get(CHILDREN).forEach((child) -> child.initServer(world, vault));
    }

    private boolean isPlayerNear(VirtualWorld world, BlockPos pos) {
        double distance = ModConfigs.MONOLITH.getRollNearDistance();
        return world.getNearestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, distance, false) != null;
    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        if (this.get(COUNT) < this.get(TARGET)) {
            return objective == this;
        } else {
            for (Objective child : this.get(CHILDREN)) {
                if (child.isActive(world, vault, objective)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
        double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, 0.0).getIncrease();
        this.set(TARGET, (int)Math.round((double)this.get(BASE_TARGET) * (1.0 + increase)));
        if (this.get(COUNT) >= this.get(TARGET)) {
            (this.get(CHILDREN)).forEach(child -> child.tickServer(world, vault));
        }

    }

    //TODO: Implement HauntedBrazierHud
//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
//        if (this.get(COUNT) >= this.get(TARGET)) {
//            int midX = window.getGuiScaledWidth() / 2;
//            Font font = Minecraft.getInstance().font;
//            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//            Component txt = (new TextComponent("Grave Rob, or Exit to Complete")).withStyle(ChatFormatting.AQUA);
//            font.drawInBatch(txt.getVisualOrderText(), midX - font.width(txt) / 2.0F, 9.0F, -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
//            buffer.endBatch();
//            return true;
//        } else {
//            int current = this.get(COUNT);
//            int total = this.get(TARGET);
//            Component txt = new TextComponent(String.valueOf(current)).withStyle(ChatFormatting.WHITE).append((new TextComponent(" / ")).withStyle(ChatFormatting.WHITE)).append((new TextComponent(String.valueOf(total))).withStyle(ChatFormatting.WHITE));
//            int midX = 0;
//            matrixStack.pushPose();
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            int previousTexture = RenderSystem.getShaderTexture(0);
//            RenderSystem.setShaderTexture(0, HAUNTED_HUD);
//            float progress = (float)current / (float)total;
//            matrixStack.translate((midX - 80), 8.0, 0.0);
//            GuiComponent.blit(matrixStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
//            GuiComponent.blit(matrixStack, 0, 8, 0.0F, 30.0F, 13 + (int)(130.0F * progress), 10, 200, 100);
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderSystem.setShaderTexture(0, previousTexture);
//            matrixStack.popPose();
//            Font font = Minecraft.getInstance().font;
//            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//            matrixStack.pushPose();
//            matrixStack.scale(0.6F, 0.6F, 0.6F);
//            font.drawInBatch(txt.getVisualOrderText(), midX / 0.6F - font.width(txt) / 2.0F, (9 + 22), -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
//            buffer.endBatch();
//            matrixStack.popPose();
//            return true;
//        }
//    }
}

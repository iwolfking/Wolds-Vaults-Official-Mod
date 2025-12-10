package xyz.iwolfking.woldsvaults.objectives;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.vhapi.api.events.vault.VaultEvents;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class SurvivalObjective extends Objective {
    public static final ResourceLocation HUD = WoldsVaults.id("textures/gui/survival/hud.png");

    public static final SupplierKey<Objective> KEY = SupplierKey.of("survival", Objective.class).with(Version.latest(), SurvivalObjective::new);
    public static final FieldRegistry FIELDS = Objective.FIELDS.merge(new FieldRegistry());
    public static final FieldKey<Float> INITIAL_LIFE = FieldKey.of("initial_life", Float.class).with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> LIFE_REDUCTION = FieldKey.of("life_reduction", Float.class).with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> TIME_PAST = FieldKey.of("time_past", Integer.class).with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> TIME_REQUIRED = FieldKey.of("time_required", Integer.class).with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> OBJECTIVE_PROBABILITY = FieldKey.of("objective_probability", Float.class).with(Version.v1_2, Adapters.FLOAT, DISK.all()).register(FIELDS);

    protected SurvivalObjective() {
    }

    protected SurvivalObjective(int target) {
        this.set(TIME_REQUIRED, target * 20);
        this.set(TIME_PAST, 0);
        this.set(INITIAL_LIFE, 1.0F);
        this.set(LIFE_REDUCTION, 0.01F);
        this.set(OBJECTIVE_PROBABILITY, 0.0F);
    }

    public static SurvivalObjective of(int target) {
        return new SurvivalObjective(target);
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault) {
        CommonEvents.OBJECTIVE_PIECE_GENERATION.register(this, data -> {
            this.ifPresent(OBJECTIVE_PROBABILITY, probability -> data.setProbability(probability));
        });

        //VaultEvents.GOD_ALTAR_COMPLETED.in(world).register(this, (data -> this.set(COUNT, this.get(COUNT) + 1)));
        super.initServer(world, vault);
    }

    @Override
    public void tickServer(VirtualWorld world, Vault vault) {
//        this.ifPresent(BASE_TARGET, (value) -> {
//            double increase = CommonEvents.OBJECTIVE_TARGET.invoke(world, vault, 0.0).getIncrease();
//            this.set(TARGET, (int)Math.round((double)this.get(BASE_TARGET) * (1.0 + increase)));
//        });
//        if (this.get(COUNT) >= this.get(TARGET)) {
//            super.tickServer(world, vault);
//        }

    }

    @Override
    public void tickListener(VirtualWorld world, Vault vault, Listener listener) {
//        if (listener.getPriority(this) < 0) {
//            listener.addObjective(vault, this);
//        }
//
//        if (listener instanceof Runner && this.get(COUNT) >= this.get(TARGET)) {
//            super.tickListener(world, vault, listener);
//        }

    }


    @OnlyIn(Dist.CLIENT)
    public boolean render(Vault vault, PoseStack matrixStack, Window window, float partialTicks, Player player) {
//        if (this.get(COUNT) >= this.get(TARGET)) {
//            int midX = window.getGuiScaledWidth() / 2;
//            Font font = Minecraft.getInstance().font;
//            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//            Component txt = (new TextComponent("The gods are pleased, you may exit when ready.")).withStyle(ChatFormatting.GOLD);
//            font.drawInBatch(txt.getVisualOrderText(), midX - font.width(txt) / 2.0F, 9.0F, -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
//            buffer.endBatch();
//            return true;
//        } else {
            Component txt = new TextComponent("Survive!");
            int midX = window.getGuiScaledWidth() / 2;
            matrixStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int previousTexture = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, HUD);
            float progress = (float)1.0F / (float)0.2F;
            matrixStack.translate((midX - 80), 8.0, 0.0);
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
            font.drawInBatch(txt.getVisualOrderText(), midX / 0.6F - font.width(txt) / 2.0F, (9 + 22), -1, true, matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
            buffer.endBatch();
            matrixStack.popPose();
            return true;

    }

    @Override
    public boolean isActive(VirtualWorld world, Vault vault, Objective objective) {
        return true;
//        if (this.get(COUNT) < this.get(TARGET)) {
//            return objective == this;
//        } else {
//            for (Objective child : this.get(CHILDREN)) {
//                if (child.isActive(world, vault, objective)) {
//                    return true;
//                }
//            }
//            return false;
//        }
    }

    @Override
    public SupplierKey<Objective> getKey() {
        return KEY;
    }


}

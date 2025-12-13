package xyz.iwolfking.woldsvaults.api.inventory_hud;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.client.render.hud.InventoryHudHelper;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.item.ItemShardPouch;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.types.InventoryHudElementOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ShardPouchHudModule extends AbstractHudModule<ModuleRenderContext> {

    private static final int SHARD_TEXT_COLOR = 0xFFD142F5;

    private final Supplier<ItemStack> pouchSupplier;
    private final VaultOption<InventoryHudElementOptions> option;

    public ShardPouchHudModule(
            Supplier<ItemStack> pouchSupplier,
            VaultOption<InventoryHudElementOptions> option
    ) {
        super(
                "shard_pouch",
                "Shard Pouch",
                "Displays the Soul Shard count from your Shard Pouch."
        );
        this.pouchSupplier = pouchSupplier;
        this.option = option;
    }

    @Override
    protected void renderModule(ModuleRenderContext context) {
        PoseStack poseStack = context.getPoseStack();
        LocalPlayer player = Minecraft.getInstance().player;
        boolean editing = context.isEditing();

        if (player == null) {
            return;
        }

        InventoryHudElementOptions opts = this.option.getValue();
        ItemStack pouchStack = this.pouchSupplier.get();
        int shardCount = ItemShardPouch.getShardCount(player);

        // -------------------------------------------------
        // Icon
        // -------------------------------------------------
        if (!pouchStack.isEmpty() || editing) {
            InventoryHudHelper.renderScaledGuiItem(
                    poseStack,
                    pouchStack,
                    0,
                    0,
                    false
            );
        }

        // -------------------------------------------------
        // Text
        // -------------------------------------------------
        if ((shardCount > 0 || editing)
                && !opts.getDisplayMode().equals(InventoryHudElementOptions.DisplayMode.NEVER)) {

            Font font = Minecraft.getInstance().font;
            String text = editing ? "0" : String.valueOf(shardCount);

            int scaledIconSize = 16;
            int textOffsetX = 18;
            int textOffsetY = 4;
            int belowOffsetY = 18;
            int aboveOffsetY = -10;

            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 999.0);

            switch (opts.getTextPosition()) {
                case RIGHT -> font.drawShadow(
                        poseStack,
                        text,
                        textOffsetX,
                        textOffsetY,
                        SHARD_TEXT_COLOR
                );

                case LEFT -> font.drawShadow(
                        poseStack,
                        text,
                        -20.0F,
                        textOffsetY,
                        SHARD_TEXT_COLOR
                );

                case BELOW -> font.drawShadow(
                        poseStack,
                        text,
                        (scaledIconSize / 2f) - (font.width(text) / 2f),
                        belowOffsetY,
                        SHARD_TEXT_COLOR
                );

                case ABOVE -> font.drawShadow(
                        poseStack,
                        text,
                        (scaledIconSize / 2f) - (font.width(text) / 2f),
                        aboveOffsetY,
                        SHARD_TEXT_COLOR
                );

                case ONTOP -> font.drawShadow(
                        poseStack,
                        text,
                        16 - font.width(text),
                        16 - 9,
                        SHARD_TEXT_COLOR
                );
            }

            poseStack.popPose();
        }
    }

    // ------------------------------------------------------------------------
    // HUD plumbing
    // ------------------------------------------------------------------------

    @Override
    public HudPosition getPosition() {
        return this.option.getValue().getHudPosition();
    }

    @Override
    public void setPosition(HudPosition pos) {
        InventoryHudElementOptions opts = this.option.getValue();
        opts.setHudPosition(pos);
        this.option.setValue(opts);
    }

    @Override
    public float getScale() {
        return this.option.getValue().getSize();
    }

    @Override
    public void resetToDefaultPosition() {
        InventoryHudElementOptions def = this.option.getDefaultValue();
        InventoryHudElementOptions current = this.option.getValue();
        current.setHudPosition(def.getHudPosition());
        this.option.setValue(current);
    }

    // ------------------------------------------------------------------------
    // Editor bounds
    // ------------------------------------------------------------------------

    @Override
    protected int baseWidth(ModuleRenderContext context) {
        InventoryHudElementOptions opts = this.option.getValue();
        Font font = Minecraft.getInstance().font;

        int textWidth = font.width("9999");

        return switch (opts.getTextPosition()) {
            case LEFT, RIGHT -> 16 + 2 + textWidth;
            default -> Math.max(16, textWidth);
        };
    }

    @Override
    protected int baseHeight(ModuleRenderContext context) {
        InventoryHudElementOptions opts = this.option.getValue();
        Font font = Minecraft.getInstance().font;

        return switch (opts.getTextPosition()) {
            case ABOVE, BELOW -> 16 + font.lineHeight + 2;
            default -> 16;
        };
    }

    @Override
    public boolean isHidden(ModuleRenderContext context) {
        return this.option.getValue()
                .getDisplayMode()
                .equals(InventoryHudElementOptions.DisplayMode.NEVER);
    }
}

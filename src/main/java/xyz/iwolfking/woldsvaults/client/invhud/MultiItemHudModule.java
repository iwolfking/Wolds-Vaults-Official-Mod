package xyz.iwolfking.woldsvaults.client.invhud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import iskallia.vault.VaultMod;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.client.render.hud.InventoryHudHelper;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.settings.ConditionalModuleSetting;
import iskallia.vault.client.render.hud.module.settings.ModuleSetting;
import iskallia.vault.client.render.hud.module.settings.SliderSetting;
import iskallia.vault.client.render.hud.module.settings.ToggleButtonSetting;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.gear.IVaultUsesItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.types.InventoryHudElementOptions;
import iskallia.vault.util.Alignment;
import iskallia.vault.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.intellij.lang.annotations.Pattern;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchUseTotals;
import xyz.iwolfking.woldsvaults.client.invhud.PouchUseDisplay;

public class MultiItemHudModule extends AbstractHudModule<ModuleRenderContext> {
    private final Supplier<List<ItemStack>> stackSupplier;
    private final VaultOption<InventoryHudElementOptions> option;

    public MultiItemHudModule(@Pattern("^[a-z0-9_]+$") String key, Supplier<List<ItemStack>> stackSupplier, VaultOption<InventoryHudElementOptions> option) {
        super(key, StringUtils.convertToTitleCase(key), "Displays the " + StringUtil.stripColor(StringUtils.convertToTitleCase(key)) + " in the inventory HUD.");
        this.stackSupplier = stackSupplier;
        this.option = option;
    }


    public VaultOption<InventoryHudElementOptions> getOption() {
        return this.option;
    }

    protected void renderModule(ModuleRenderContext context) {
        boolean editing = context.isEditing();
        PoseStack poseStack = context.getPoseStack();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            InventoryHudElementOptions opts = this.option.getValue();
            var idx = 0;
            for (var stack : this.stackSupplier.get()) {
                renderStack(stack, editing, opts, poseStack, idx, PouchUseDisplay.equippedUses(player, stack));
                idx++;
            }
        }
    }

    private void renderStack(ItemStack stack, boolean editing, InventoryHudElementOptions opts, PoseStack poseStack, int idx,
            @Nullable PouchUseTotals pooledUses) {
        int yOff = 18 * idx;
        if (opts.getHudPosition().getScreenAnchor().getVertical() == Alignment.Vertical.TOP) {
            yOff *= 1;
        } else if (opts.getHudPosition().getScreenAnchor().getVertical() == Alignment.Vertical.BOTTOM) {
            yOff *= -1;
        } else if (opts.getHudPosition().getScreenAnchor().getVertical() == Alignment.Vertical.MIDDLE && opts.getHudPosition().getScreenAnchor().getHorizontal() != Alignment.Horizontal.CENTER) {
            yOff *= 0;
        }

        int xOff = 18 * idx;
        if (opts.getHudPosition().getScreenAnchor().getVertical() != Alignment.Vertical.MIDDLE || opts.getHudPosition().getScreenAnchor().getHorizontal() == Alignment.Horizontal.CENTER) {
            xOff *= 0;
        } else if (opts.getHudPosition().getScreenAnchor().getHorizontal() == Alignment.Horizontal.LEFT) {
            xOff *= 1;
        } else if (opts.getHudPosition().getScreenAnchor().getHorizontal() == Alignment.Horizontal.RIGHT) {
            xOff *= -1;
        }

        if (!stack.isEmpty() || editing) {
            float durability = 1.0F;
            if (stack.isDamaged()) {
                durability = (float)(stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            }

            if (stack.getItem() instanceof IVaultUsesItem item && VaultUsesHelper.getUses(stack) > 0 && !VaultUsesHelper.getUsedVaults(stack).isEmpty()) {
                durability = (float)(VaultUsesHelper.getUses(stack) - VaultUsesHelper.getUsedVaults(stack).size()) / VaultUsesHelper.getUses(stack);
            }
            if (pooledUses != null) {
                durability = pooledUses.fraction();
            }
            boolean render = switch (opts.getDisplayMode()) {
                case ALWAYS -> true;
                case NEVER -> editing;
                case DURABILITY_LOW -> durability <= 0.25F || editing;
                case DURABILITY_VERY_LOW -> durability <= 0.1F || editing;
            };
            if (render) {
                if ((!editing || !stack.isEmpty()) && !opts.isUseOverlay()) {
                    InventoryHudHelper.renderScaledGuiItem(poseStack, stack, xOff, yOff, false);
                    if (opts.getIndicator().equals(InventoryHudElementOptions.Indicator.BAR)) {
                        renderUsesBar(poseStack, stack, xOff, yOff, pooledUses);
                    }
                } else {
                    ResourceLocation baseTexture = this.getBaseTexture();
                    ResourceLocation overlayTexture = this.getOverlayTexture();
                    InventoryHudHelper.renderOverlayItem(poseStack, this.key(), xOff, yOff, durability, 1.0F, baseTexture, overlayTexture);
                    if (opts.getIndicator().equals(InventoryHudElementOptions.Indicator.BAR) && (!editing || !stack.isEmpty())) {
                        renderUsesBar(poseStack, stack, xOff, yOff, pooledUses);
                    }
                }

                if (opts.getIndicator().isTextBased() && !opts.getDisplayMode().equals(InventoryHudElementOptions.DisplayMode.NEVER)) {
                    int percent = (int)(durability * 100.0F);
                    int color;
                    if (percent < 5) {
                        color = 14286848;
                    } else if (percent < 20) {
                        color = 14305280;
                    } else if (percent < 40) {
                        color = 14322176;
                    } else if (percent < 60) {
                        color = 16641536;
                    } else if (percent < 80) {
                        color = 13696256;
                    } else {
                        color = 8649984;
                    }

                    String text = percent + "%";
                    if (opts.getIndicator().equals(InventoryHudElementOptions.Indicator.NUMBER) && this.isUsesItem(stack)) {
                        text = String.valueOf(pooledUses == null ? PouchHelper.remainingUses(stack) : pooledUses.remaining());
                    }

                    if (stack.getItem() instanceof VaultGearItem item && item.isBroken(stack)) {
                        text = "BROKEN";
                        color = 14286848;
                    }

                    int scaledIconSize = 16;
                    int textOffsetX = 18;
                    int textOffsetY = 4;
                    int belowOffsetY = 18;
                    int aboveOffsetY = -10;
                    Font font = Minecraft.getInstance().font;
                    poseStack.pushPose();
                    poseStack.translate(xOff, yOff, 999.0);
                    switch (opts.getTextPosition()) {
                        case RIGHT:
                            font.drawShadow(poseStack, text, textOffsetX, textOffsetY, color);
                            break;
                        case LEFT:
                            font.drawShadow(poseStack, text, -20.0F, textOffsetY, color);
                            break;
                        case BELOW:
                            font.drawShadow(poseStack, text, scaledIconSize / 2 - font.width(text) / 2, belowOffsetY, color);
                            break;
                        case ABOVE:
                            font.drawShadow(poseStack, text, scaledIconSize / 2 - font.width(text) / 2, aboveOffsetY, color);
                            break;
                        case ONTOP:
                            font.drawShadow(poseStack, text, 16 - font.width(text), 16 - 9, color);
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    private static void renderUsesBar(PoseStack pose, ItemStack stack, int x, int y, @Nullable PouchUseTotals pooledUses) {
        if (pooledUses == null) {
            InventoryHudHelper.renderUsesBar(pose, stack, x, y, 1.0F);
            return;
        }
        float fraction = pooledUses.fraction();
        int width = Math.round(13 * fraction);
        int color = 0xFF000000 | Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        GuiComponent.fill(pose, x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        GuiComponent.fill(pose, x + 2, y + 13, x + 2 + width, y + 14, color);
        RenderSystem.enableDepthTest();
    }

    @Override
    public HudPosition getPosition() {
        InventoryHudElementOptions opts = this.option.getValue();
        return opts.getHudPosition();
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
        current.setSize(def.getSize());
        this.option.setValue(current);
        this.option.resetToDefault();
    }

    protected boolean isUsesItem(ItemStack item) {
        return item.getItem() instanceof IVaultUsesItem;
    }

    private ResourceLocation getBaseTexture() {
        String var1 = this.key();

        return switch (var1) {
            case "trinket_1" -> VaultMod.id("textures/gui/inventory_hud/blue_trinket.png");
            case "trinket_2" -> VaultMod.id("textures/gui/inventory_hud/red_trinket.png");
            case "trinket_3" -> VaultMod.id("textures/gui/inventory_hud/green_trinket.png");
            case "backpack" -> VaultMod.id("textures/gui/inventory_hud/pouch_base.png");
            default -> VaultMod.id("textures/gui/inventory_hud/" + this.key() + "_base.png");
        };
    }

    private ResourceLocation getOverlayTexture() {
        String var1 = this.key();

        return switch (var1) {
            case "trinket_1" -> VaultMod.id("textures/gui/inventory_hud/blue_trinket.png");
            case "trinket_2" -> VaultMod.id("textures/gui/inventory_hud/red_trinket.png");
            case "trinket_3" -> VaultMod.id("textures/gui/inventory_hud/green_trinket.png");
            case "backpack" -> VaultMod.id("textures/gui/inventory_hud/pouch_overlay.png");
            default -> VaultMod.id("textures/gui/inventory_hud/" + this.key() + "_overlay.png");
        };
    }

    @Override
    public List<ModuleSetting> getSettings() {
        final InventoryHudElementOptions opts = this.option.getValue();
        var list = new ArrayList<ModuleSetting>();


        list.add(
            new ToggleButtonSetting(
                "display_mode",
                new TextComponent("Display Mode"),
                new TextComponent("Controls the display mode of the item. E.g. Always on, based on durability, or never."),
                () -> MultiItemHudModule.this.getDisplayModeName(opts.getDisplayMode()),
                () -> MultiItemHudModule.this.option.setValue(opts.setDisplayMode(opts.getDisplayMode().next()))
            )
        );
        if (!opts.getDisplayMode().equals(InventoryHudElementOptions.DisplayMode.NEVER)) {
            list.addAll(
                List.of(
                    new SliderSetting(
                        "size",
                        new TextComponent("Size"),
                        new TextComponent("Controls the size of the item in the inventory HUD."),
                        () -> new TextComponent("Size"),
                        () -> opts.getSize() - 0.5F,
                        value -> MultiItemHudModule.this.option.setValue(opts.setSize(value + 0.5F))
                    ),
                    new ToggleButtonSetting(
                        "indicator",
                        new TextComponent("Indicator"),
                        new TextComponent("Controls whether to use an overlay indicator for the item."),
                        () -> StringUtils.convertToTitleCase(opts.getIndicator().name()),
                        () -> MultiItemHudModule.this.option
                            .setValue(opts.setIndicator(opts.getIndicator().cycle(MultiItemHudModule.this.isUsesItem(MultiItemHudModule.this.stackSupplier.get().get(0)))))
                    ),
                    new ToggleButtonSetting(
                        "use_overlay",
                        new TextComponent("Overlay"),
                        new TextComponent("Whether to use the item overlay (or item texture)"),
                        () -> opts.isUseOverlay() ? "ON" : "OFF",
                        () -> MultiItemHudModule.this.option.setValue(opts.setUseOverlay(!opts.isUseOverlay()))
                    ),
                    new ConditionalModuleSetting(
                        new ToggleButtonSetting(
                            "text_position",
                            new TextComponent("Text Position"),
                            new TextComponent("Controls the position of the text relative to the item."),
                            () -> opts.getTextPosition().name(),
                            () -> MultiItemHudModule.this.option.setValue(opts.setTextPosition(opts.getTextPosition().next()))
                        ),
                        () -> opts.getIndicator().isTextBased()
                    )
                )
            );
        }
        return list;
    }

    public boolean isHidden(ModuleRenderContext context) {
        return this.option.getValue().getDisplayMode().equals(InventoryHudElementOptions.DisplayMode.NEVER);
    }

    protected int baseWidth(ModuleRenderContext context) {
        return 16;
    }

    protected int baseHeight(ModuleRenderContext context) {
        return 16;
    }

    public static List<ItemStack> getCurioSlotsByName(Player player, String identifier) {
        if (player == null) {
            return List.of(ItemStack.EMPTY);
        }
        List<SlotResult> slots = CuriosApi.getCuriosHelper().findCurios(player, identifier);
        List<ItemStack> stacks = new ArrayList<>();

        for (var slot: slots) {
            if (slot.stack() != null && !slot.stack().isEmpty()) {
                stacks.add(slot.stack());
            }
        }

        ItemStack pouch = PouchHelper.equipped(player);
        if (PouchHelper.COLORS.contains(identifier) && PouchHelper.isPouch(pouch)) {
            PouchContents contents = PouchCapability.get(pouch);
            for (int index : PouchHelper.validSelection(pouch, contents, contents.activeIndices())) {
                ItemStack stack = contents.getStackInSlot(index);
                if (identifier.equals(PouchHelper.color(stack))) {
                    stacks.add(stack);
                }
            }
        }

        return stacks.isEmpty() ? List.of(ItemStack.EMPTY) : stacks;
    }

}

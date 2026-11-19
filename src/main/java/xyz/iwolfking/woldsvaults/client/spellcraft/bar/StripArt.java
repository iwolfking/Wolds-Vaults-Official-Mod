package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellCooldowns;

import javax.annotation.Nullable;
import java.util.function.Function;

/** How the spell bar strip draws its bar pickers and the content of one slot. */
final class StripArt {
    static final int IMPLICIT_RING = 0xFFE0B040;
    private static final int PICK_ICON = 1;
    private static final int BROKEN = 0x80FF3030;
    private static final int COOLING = 0x90000000;
    private static final int LOCK = 0xFFFF5050;
    private static final int BADGE = 8;

    private StripArt() {
    }

    /** One bar picker tile of {@code size}: the abilities icon for the default bar, else the item's icon. */
    static void picker(PoseStack pose, int px, int py, int size, boolean selected, @Nullable ItemStack stack) {
        if (selected) {
            SpellcraftDraw.insetGrey(pose, px, py, px + size, py + size);
        } else {
            SpellcraftDraw.insetDarkGrey(pose, px, py, px + size, py + size);
        }
        if (stack == null) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, selected ? 1.0F : 0.55F);
            ScreenTextures.TAB_ICON_ABILITIES.blit(pose, px + PICK_ICON, py + PICK_ICON, 0, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, px + PICK_ICON, py + PICK_ICON);
        }
        if (selected) {
            SpellcraftDraw.ring(pose, px, py, px + size, py + size, stack == null ? SpellcraftPalette.WHITE : SpellcraftPalette.ARCANE_GLOW);
        }
    }

    /** A slot frame and its ability or spell icon, with the locked, unlearned, hidden and cooling marks. */
    static void slot(PoseStack pose, AbilityTree tree, SpellBarView.Slot slot, Function<String, String> specOf, int x, int y) {
        SpellcraftDraw.slot(pose, x, y, false);
        String spec = slot.group().isEmpty() ? null : specOf.apply(slot.group());
        String icon = spec != null ? spec : slot.group();
        switch (slot.state()) {
            case ABILITY -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, slot.unlearned() ? 0.4F : 1.0F);
                if (slot.unlearned()) {
                    SpellcraftDraw.redX(pose, x, y);
                }
            }
            case SPELL -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, slot.locked() ? 0.45F : 1.0F);
                if (slot.locked()) {
                    GuiComponent.fill(pose, x, y, x + 16, y + 16, BROKEN);
                    GuiComponent.fill(pose, x + 5, y + 7, x + 11, y + 13, LOCK);
                    GuiComponent.fill(pose, x + 6, y + 4, x + 7, y + 7, LOCK);
                    GuiComponent.fill(pose, x + 9, y + 4, x + 10, y + 7, LOCK);
                    GuiComponent.fill(pose, x + 6, y + 4, x + 10, y + 5, LOCK);
                }
                SpellcraftDraw.ring(pose, x - 1, y - 1, x + 17, y + 17, slot.implicit() ? IMPLICIT_RING : SpellcraftPalette.ARCANE_GLOW);
                if (!slot.implicit()) {
                    SpellcraftDraw.spellStar(pose, x, y);
                }
                if (slot.unlearned()) {
                    SpellcraftDraw.redX(pose, x, y);
                }
            }
            case HIDDEN -> {
                SpellcraftDraw.ability(pose, icon, x, y, 16, 0.45F);
                SpellcraftDraw.strike(pose, x, y);
            }
            case EMPTY -> {
            }
        }
        if (slot.filled() && SpellCooldowns.blocking(tree, slot.groups()) != null) {
            GuiComponent.fill(pose, x, y, x + 16, y + 16, COOLING);
        }
    }

    /** The source item of an item spell, at half size in the slot's bottom right corner. */
    static void badge(SpellBarView.Slot slot, int x, int y) {
        if (slot.state() != SpellBarView.State.SPELL || slot.itemKey().isEmpty()) {
            return;
        }
        ItemStack stack = ClientSpellBar.stackOf(slot.itemKey());
        if (stack.isEmpty()) {
            return;
        }
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(x + 16 - BADGE, y + 16 - BADGE, 250.0);
        modelView.scale(0.5F, 0.5F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}

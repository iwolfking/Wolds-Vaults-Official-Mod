package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.AbilityDialog;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SkillMenuSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarView;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.StatText;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.SlotCostStats;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbstractDialogAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;

import java.awt.Rectangle;

@Mixin(value = AbilityDialog.class, remap = false)
public class MixinAbilityDialogSpellBar {
    @Unique private static final int GAP = 5;
    @Unique private static final int RAISE = 4;
    @Unique private static final int THIRD_LINE_Y = 29;

    @Shadow @Final private AbilityTree abilityTree;
    @Shadow private String selectedAbility;
    @Shadow private AbilityWidget selectedAbilityWidget;

    @Unique private String woldsvaults$slotLine;
    @Unique private int woldsvaults$slotColor;

    /**
     * Adds a third line to Vault Hunters' ability dialog heading: the ability's slot and key on the shown spell bar (in
     * classic mode its quick-fire key), then mana and cooldown when they fit. It is worked out before the heading draws so the name lines can be raised
     * for it.
     */
    @Inject(method = "renderHeading", at = @At("HEAD"))
    private void woldsvaults$prepareSlotLine(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.woldsvaults$slotLine = null;
        SkillMenuSpellBar menu = SkillMenuSpellBar.current();
        if (menu == null || ((Object) this).getClass() != AbilityDialog.class || this.selectedAbility == null || this.selectedAbilityWidget == null) {
            return;
        }
        Skill skill = this.abilityTree.getForId(this.selectedAbility).orElse(null);
        Skill groupSkill = skill == null ? null : skill.getParent();
        if (groupSkill == null) {
            return;
        }
        String group = groupSkill.getId();
        SpellBarView view = menu.shown();
        int slot = view.slotOf(group);
        Font font = Minecraft.getInstance().font;
        String head = null;
        if (slot >= 0 && ClientSpellBar.mode() == BarMode.CLASSIC) {
            String key = SpellcraftNames.quickfireLabel(group, font, 30);
            head = key.isEmpty() ? SpellcraftNames.tr("dialog.no_quickfire_key") : SpellcraftNames.tr("dialog.quickfire_key", key);
        } else if (slot >= 0) {
            String key = SpellcraftNames.keyLabel(slot, font, 30);
            head = key.isEmpty() ? SpellcraftNames.tr("dialog.slot", slot + 1) : SpellcraftNames.tr("dialog.slot_key", slot + 1, key);
        }
        if (slot >= 0 && view.slot(slot).state() == SpellBarView.State.SPELL) {
            head = SpellcraftNames.tr("dialog.slot_spell", head, view.slot(slot).spellName());
        }
        String numbers = null;
        if (this.selectedAbility.equals(AbilityLookup.specOf(this.abilityTree).apply(group))) {
            SlotCostStats.Numbers stats;
            SpellcraftConfig config = ModConfigs.SPELLCRAFT;
            SpellBarView.Slot shown = slot >= 0 ? view.slot(slot) : null;
            if (shown != null && shown.state() == SpellBarView.State.SPELL && shown.model() != null && config != null) {
                stats = SlotCostStats.spell(config, this.abilityTree, shown.model());
            } else {
                stats = SlotCostStats.plain(this.abilityTree, group);
            }
            if (stats != null) {
                numbers = StatText.numbers("dialog.numbers", stats);
            }
        }
        Rectangle bounds = ((AbstractDialogAccessor) this).woldsvaults$getBounds();
        int x = 10 + this.selectedAbilityWidget.getWidth() + GAP;
        int room = bounds.width - 19 - x;
        String line;
        if (head == null) {
            line = numbers;
        } else if (numbers != null && font.width(head + "  " + numbers) <= room) {
            line = head + "  " + numbers;
        } else {
            line = head;
        }
        if (line == null || room <= 0) {
            return;
        }
        this.woldsvaults$slotLine = font.plainSubstrByWidth(line, room);
        boolean spell = slot >= 0 && (ClientSpellBar.mode() == BarMode.SLOT || view.slot(slot).state() == SpellBarView.State.SPELL);
        this.woldsvaults$slotColor = spell && view.isEffective() ? SpellcraftPalette.ARCANE_GLOW : SpellcraftPalette.WHITE;
    }

    /** Raises Vault Hunters' name and level lines to make room for the third line. */
    @ModifyArg(method = "renderHeading", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 1, remap = true), index = 1)
    private double woldsvaults$raiseHeading(double y) {
        return this.woldsvaults$slotLine == null ? y : y - RAISE;
    }

    @Inject(method = "renderHeading", at = @At("TAIL"))
    private void woldsvaults$drawSlotLine(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (this.woldsvaults$slotLine == null || this.selectedAbilityWidget == null) {
            return;
        }
        int x = 10 + this.selectedAbilityWidget.getWidth() + GAP;
        Minecraft.getInstance().font.drawShadow(poseStack, this.woldsvaults$slotLine, x, THIRD_LINE_Y, this.woldsvaults$slotColor);
        RenderSystem.enableDepthTest();
    }
}

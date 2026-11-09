package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

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
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientSpellStats;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarMenu;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarView;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftGui;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbstractDialogAccessor;

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

    /** Works out the heading's third line: the slot and key on the shown spell bar, then mana and cooldown when they fit. */
    @Inject(method = "renderHeading", at = @At("HEAD"))
    private void woldsvaults$prepareSlotLine(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.woldsvaults$slotLine = null;
        SpellBarMenu menu = SpellBarMenu.current();
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
        if (slot >= 0) {
            String key = SpellcraftGui.keyLabel(slot, font, 30);
            head = key.isEmpty() ? SpellcraftGui.tr("dialog.slot", slot + 1) : SpellcraftGui.tr("dialog.slot_key", slot + 1, key);
            if (view.slot(slot).state() == SpellBarView.State.SPELL) {
                head = SpellcraftGui.tr("dialog.slot_spell", head, view.slot(slot).spellName());
            }
        }
        String numbers = null;
        if (this.selectedAbility.equals(SpellcraftRules.specOf(this.abilityTree).apply(group))) {
            ClientSpellStats.Numbers stats;
            FocusAugmentsConfig config = FocusAugments.config();
            SpellBarView.Slot shown = slot >= 0 ? view.slot(slot) : null;
            if (shown != null && shown.state() == SpellBarView.State.SPELL && shown.model() != null && config != null) {
                stats = ClientSpellStats.spell(config, this.abilityTree, shown.model());
            } else {
                stats = ClientSpellStats.plain(this.abilityTree, group);
            }
            if (stats != null) {
                String cooldown = SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(stats.cooldownAfter()));
                numbers = stats.hasMana() ? SpellcraftGui.tr("dialog.numbers", Math.round(stats.manaAfter()), cooldown) : cooldown;
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
        this.woldsvaults$slotColor = slot >= 0 && view.isItem() ? SpellcraftGui.ARCANE_GLOW : SpellcraftGui.WHITE;
    }

    /** Raises VH's name and level lines to make room for the third line. */
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
    }
}

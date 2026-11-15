package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;

import javax.annotation.Nullable;

public final class SpellcraftNames {
    private static final String LANG = "woldsvaults.spellcraft.";

    private SpellcraftNames() {
    }

    public static String tr(String key, Object... args) {
        return I18n.get(LANG + key, args);
    }

    public static String keyLabel(int slot, Font font, int room) {
        KeyMapping key = ModKeybinds.spellSlots[slot];
        if (key == null || key.isUnbound()) {
            return "";
        }
        String label = key.getKey().getType() == InputConstants.Type.MOUSE
                ? "M" + (key.getKey().getValue() + 1)
                : key.getTranslatedKeyMessage().getString();
        return font.plainSubstrByWidth(label, room);
    }

    public static String keyName(int slot) {
        KeyMapping key = ModKeybinds.spellSlots[slot];
        if (key == null || key.isUnbound()) {
            return "";
        }
        return key.getTranslatedKeyMessage().getString();
    }

    /** An ability group named by its current specialization. */
    public static String abilityName(@Nullable AbilityTree tree, String group) {
        if (tree != null) {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() != null
                    && specialized.getSpecialization().getName() != null) {
                return specialized.getSpecialization().getName();
            }
        }
        return AbilityLookup.groupName(group);
    }

    /** An ability group's own name, ignoring its specialization. */
    public static String groupName(@Nullable AbilityTree tree, String group) {
        if (tree != null) {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill != null && skill.getName() != null) {
                return skill.getName();
            }
        }
        return AbilityLookup.groupName(group);
    }

    /** An ability group's specialization tier, empty when it has no unlocked tiered specialization. */
    public static String tier(@Nullable AbilityTree tree, String group) {
        Skill skill = tree == null ? null : tree.getForId(group).orElse(null);
        if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() instanceof TieredSkill tiered && tiered.isUnlocked()) {
            return String.valueOf(tiered.getActualTier());
        }
        return "";
    }

    /** A stat label from a tooltip lang key, without the colon and space that end it there. */
    public static String label(String key) {
        String text = tr(key).stripTrailing();
        return text.endsWith(":") ? text.substring(0, text.length() - 1) : text;
    }

    /** Upper-cases the first letter after leading colour tags, unless a digit, sign or space comes first. */
    public static String capitalize(String text) {
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '<') {
                int close = text.indexOf('>', i);
                if (close < 0) {
                    break;
                }
                i = close + 1;
                continue;
            }
            if (Character.isLetter(c)) {
                return text.substring(0, i) + Character.toUpperCase(c) + text.substring(i + 1);
            }
            if (Character.isDigit(c) || c == '+' || c == '-' || c == ' ' || c == '%' || c == '.') {
                return text;
            }
            i++;
        }
        return text;
    }

    public static MutableComponent colored(String text, int color) {
        return new TextComponent(text).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
    }
}

package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DefaultBar {
    private DefaultBar() {
    }

    static void migrate(ServerPlayer player, SpellBarData.PlayerBar saved, AbilityTree tree) {
        List<String> unlocked = unlocked(tree);
        for (int slot = 0; slot < Math.min(SpellcraftItemData.SLOTS, unlocked.size()); slot++) {
            saved.defaults[slot] = unlocked.get(slot);
            if (tree.getSelected() != null && tree.getSelected().getId().equals(unlocked.get(slot))) {
                saved.selected = slot;
            }
        }
        saved.migrated = true;
        SpellBarData.get(player.getServer()).setDirty();
    }

    /**
     * The classic mode bar: one slot per learned ability group, in tree order. Abilities past the last slot are left
     * off, which is logged once per player.
     */
    static String[] classic(ServerPlayer player, AbilityTree tree) {
        List<String> unlocked = unlocked(tree);
        String[] base = new String[SpellcraftItemData.SLOTS];
        Arrays.fill(base, "");
        for (int slot = 0; slot < Math.min(base.length, unlocked.size()); slot++) {
            base[slot] = unlocked.get(slot);
        }
        if (unlocked.size() > base.length) {
            WoldLogger.errorOnce("DefaultBar:classic:" + player.getUUID(),
                    "Spell bar: {} has {} learned abilities but the classic bar has {} slots; {} are left off the bar",
                    player.getGameProfile().getName(), unlocked.size(), base.length, unlocked.subList(base.length, unlocked.size()));
        }
        return base;
    }

    private static List<String> unlocked(AbilityTree tree) {
        List<String> groups = new ArrayList<>();
        for (Skill skill : tree.skills) {
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                groups.add(specialized.getId());
            }
        }
        return groups;
    }

    /** Sets a default-bar slot (empty string clears it); the ability moves if it was on another default slot. */
    @Nullable
    public static String editDefault(ServerPlayer player, int index, String group) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS) {
            return "no such slot";
        }
        if (!group.isEmpty() && AbilityLookup.specOf(AbilityLookup.tree(player)).apply(group) == null) {
            return group + " is not an ability";
        }
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        if (!group.isEmpty()) {
            for (int i = 0; i < saved.defaults.length; i++) {
                if (group.equals(saved.defaults[i])) {
                    saved.defaults[i] = "";
                }
            }
        }
        saved.defaults[index] = group;
        SpellBarData.get(player.getServer()).setDirty();
        SpellBars.refresh(player);
        return null;
    }
}

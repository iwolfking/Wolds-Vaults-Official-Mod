package xyz.iwolfking.woldsvaults.integration.vhatcaniroll;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls;

import java.util.ArrayList;
import java.util.List;

/** The lines Vhat Can I Roll shows for a mythic god charm: every implicit, prefix and suffix it can draw. */
public final class VHCIRMythicCharms {
    private static final VaultGearModifier.AffixType[] SLOTS = {
            VaultGearModifier.AffixType.IMPLICIT, VaultGearModifier.AffixType.PREFIX, VaultGearModifier.AffixType.SUFFIX};

    private VHCIRMythicCharms() {
    }

    /** One bold header per affix slot, then one line per roll, in the form the gear page lists modifiers. */
    public static List<Component> lines(VaultGod god) {
        List<Component> lines = new ArrayList<>();
        for (VaultGearModifier.AffixType slot : SLOTS) {
            List<MythicCharmRolls.RollPreview> previews = MythicCharmRolls.previews(god, slot);
            if (previews.isEmpty()) {
                continue;
            }
            lines.add(new TextComponent(slot.name()).withStyle(ChatFormatting.BOLD));
            for (MythicCharmRolls.RollPreview preview : previews) {
                TextComponent line = new TextComponent("");
                line.append(new TextComponent(preview.name()).withStyle(ChatFormatting.WHITE));
                line.append(new TextComponent(" " + preview.range()).withStyle(ChatFormatting.GRAY));
                lines.add(line);
            }
        }
        return lines;
    }
}

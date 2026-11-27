package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.CrystalEntry;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = CrystalEntry.class, remap = false)
public class MixinCrystalEntry {

    /**
     * Configures a crystal's modifiers before its theme, so the theme infusion roll can see modifiers
     * the crystal carries, such as Dulled from the Crystal Duller. Only the configure order changes;
     * tooltips and ticks keep the base child order.
     */
    @Redirect(method = "configure", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/CrystalEntry;getChildren()Ljava/util/Collection;"))
    private Collection<CrystalEntry> woldsvaults$configureModifiersBeforeTheme(CrystalEntry entry) {
        Collection<CrystalEntry> children = entry.getChildren();
        if (!(entry instanceof CrystalData)) {
            return children;
        }

        List<CrystalEntry> ordered = new ArrayList<>(children);
        int themeIndex = -1;
        int modifiersIndex = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i) instanceof CrystalTheme) {
                themeIndex = i;
            } else if (ordered.get(i) instanceof CrystalModifiers) {
                modifiersIndex = i;
            }
        }

        if (themeIndex < 0 || modifiersIndex < 0) {
            WoldLogger.error("Crystal configure order fallback: theme index {} or modifiers index {} not found, configuring in base order", themeIndex, modifiersIndex);
            return children;
        }
        if (themeIndex > modifiersIndex) {
            return children;
        }

        CrystalEntry theme = ordered.remove(themeIndex);
        ordered.add(modifiersIndex, theme);
        return ordered;
    }
}

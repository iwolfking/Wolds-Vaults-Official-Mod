package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class ThemeHelper {
    public static Optional<String> getAugmentForTheme(ResourceLocation themeId) {
        return ModConfigs.THEME_AUGMENT_LORE.augments.entrySet().stream()
                .filter(entry -> entry.getValue().contains(themeId))
                .map(Map.Entry::getKey)
                .findFirst();
    }
}

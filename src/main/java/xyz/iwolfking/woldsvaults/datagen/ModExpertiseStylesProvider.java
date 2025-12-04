package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.helper.SkillFrame;
import iskallia.vault.config.ExpertisesGUIConfig;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractExpertiseStyleProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.Map;

public class ModExpertiseStylesProvider extends AbstractExpertiseStyleProvider {
    protected ModExpertiseStylesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    Map<String, SkillStyle> newStyles = new HashMap<>();

    @Override
    public void registerConfigs() {
        ModConfigs.EXPERTISES_GUI = new ExpertisesGUIConfig().readConfig();
        add("wolds_expertises", builder -> {
            addInDirection(ExpertiseStyleDirection.ABOVE, "Companion_Loyalty", "ShopReroll", VaultMod.id("gui/skills/negotiator"), builder);
            addInDirection(ExpertiseStyleDirection.LEFT, "Unbreakable", "Grave_Insurance", VaultMod.id("gui/skills/grave_insurance"), builder);
            addInDirection(ExpertiseStyleDirection.RIGHT, "Jeweler", "Augmentation_Luck", VaultMod.id("gui/skills/augmentation_luck"), builder);
            addInDirection(ExpertiseStyleDirection.BELOW, "Lucky_Altar", "Craftsman", VaultMod.id("gui/skills/reroll"), builder);
            addInDirection(ExpertiseStyleDirection.BELOW, "Craftsman", "Pylon_Pilferer", VaultMod.id("gui/skills/pylon_pilferer"), builder);
            builder.addStyle("Surprise_Favors", new SkillStyle(210, 70, VaultMod.id("gui/skills/ward"), SkillFrame.STAR));
        });
    }

    private void addInDirection(ExpertiseStyleDirection direction, String targetAbility, String abilityName, ResourceLocation icon, Builder builder) {
        SkillStyle style;
        SkillStyle newStyle = null;

        if(ModConfigs.EXPERTISES_GUI.getStyles().containsKey(targetAbility)) {
            style = ModConfigs.EXPERTISES_GUI.getStyles().get(targetAbility);
            newStyle = new SkillStyle(style.x + direction.x, style.y + direction.y, icon, SkillFrame.STAR);
            builder.addStyle(abilityName, newStyle);
        }
        else if(newStyles.containsKey(targetAbility)) {
            style = newStyles.get(targetAbility);
            newStyle = new SkillStyle(style.x + direction.x, style.y + direction.y, icon, SkillFrame.STAR);
            builder.addStyle(abilityName, newStyle);
        }

        if(newStyle != null) {
            newStyles.put(abilityName, newStyle);
        }
    }

    private enum ExpertiseStyleDirection {
        ABOVE(0, 70),
        BELOW(0, -70),
        LEFT(-70, 0),
        RIGHT(70, 0);

        private final int x;
        private final int y;

        ExpertiseStyleDirection(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}

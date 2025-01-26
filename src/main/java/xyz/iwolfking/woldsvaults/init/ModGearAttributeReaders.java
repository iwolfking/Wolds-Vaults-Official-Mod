package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.modifiers.vault.readers.*;

public class ModGearAttributeReaders {
    public static ThemeModifierReader themeReader(String modifierName, int rgbColor, String format) {
        return new ThemeModifierReader(modifierName, rgbColor, format);
    }

    public static ThemePoolModifierReader themePoolReader(String modifierName, int rgbColor, String format) {
        return new ThemePoolModifierReader(modifierName, rgbColor, format);
    }


    public static ObjectiveModifierReader objectiveReader(String modifierName, int rgbColor, String format) {
        return new ObjectiveModifierReader(modifierName, rgbColor, format);
    }


    public static StaticPlaceholderReader staticPlaceholderReader(String modifierName, int rgbColor, String format) {
        return new StaticPlaceholderReader(modifierName, rgbColor, format);
    }

    public static DifficultyModifierReader difficultyReader(String modifierName, int rgbColor, String format) {
        return new DifficultyModifierReader(modifierName, rgbColor, format);
    }

    public static InscriptionModifierReader inscriptionReader(String modifierName, int rgbColor, String format) {
        return new InscriptionModifierReader(modifierName, rgbColor, format);
    }
  
   public static WeaponTypeReader weaponTypeReader(String modifierName, int rgbColor, String format) {
        return new WeaponTypeReader(modifierName, rgbColor, format);
    }
}

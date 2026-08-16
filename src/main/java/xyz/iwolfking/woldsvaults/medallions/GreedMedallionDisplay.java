package xyz.iwolfking.woldsvaults.medallions;

import net.minecraft.ChatFormatting;

/**
 * Display naming and band colouring for greed medallions, kept out of {@link GreedMedallionTier}
 * so the tier enum stays a pure data table. Band colours escalate with rank so a medallion's tier
 * is readable at a glance in the workbench, in the crystal tooltip and in chat.
 */
public final class GreedMedallionDisplay {
    private GreedMedallionDisplay() {
    }

    /** "Scavenger 1" ... "Champion 3", "Legend". */
    public static String getDisplayName(GreedMedallionTier tier) {
        String band = capitalize(tier.getBand());
        return tier.getTierInBand() > 0 ? band + " " + tier.getTierInBand() : band;
    }

    public static ChatFormatting getBandColor(GreedMedallionTier tier) {
        return switch (tier.getBand()) {
            case "scavenger" -> ChatFormatting.GRAY;
            case "looter" -> ChatFormatting.GREEN;
            case "hunter" -> ChatFormatting.AQUA;
            case "master" -> ChatFormatting.LIGHT_PURPLE;
            case "champion" -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    /**
     * The underlined paradigm line the design doc attaches to each band: every paradigm shift at or
     * below this tier persists, so only the highest one is worth printing as the medallion's
     * headline behaviour.
     */
    public static String getParadigmLine(GreedMedallionTier tier) {
        if (tier.vaultChampionEnraged()) {
            return "The Vault Champion Grows Enraged";
        }
        if (tier.vaultChampionHunts()) {
            return "The Vault Champion Hunts You";
        }
        if (tier.assassinsGainInfernalModifiers()) {
            return "Assassins Gain Infernal Modifiers";
        }
        if (tier.assassinsGainBuffingAuras()) {
            return "Assassins Gain Buffing Auras";
        }
        if (tier.assassinsInflictNegativeEffects()) {
            return "Assassins Inflict Negative Effects on Hit";
        }
        if (tier.assassinsInheritVaultModifiers()) {
            return "Assassins Inherit Vault Modifiers";
        }
        return "Assassins Begin to Spawn";
    }

    private static String capitalize(String band) {
        if (band == null || band.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(band.charAt(0)) + band.substring(1);
    }
}

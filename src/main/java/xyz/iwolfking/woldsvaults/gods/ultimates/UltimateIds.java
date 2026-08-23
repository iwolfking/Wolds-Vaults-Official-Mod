package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.UUID;

/**
 * Every identifier the god ultimates hang off: skill ids, damage and clock primitive keys, and the fixed UUIDs the
 * timed buffs are torn down by.
 */
public final class UltimateIds {
    public static final String STIRRINGS_OF_POWER = "Stirrings_Of_Power";
    public static final String DORMANT = "Stirrings_Of_Power_Dormant";
    public static final String COPE_DE_GRACE = "Cope_De_Grace";
    public static final String SAVIOR = "Savior";
    public static final String EYES_OF_GOD = "Eyes_Of_God";
    public static final String BULLET_TIME = "Bullet_Time";

    public static final ResourceLocation COPE_RESISTANCE_STAGE = WoldsVaults.id("cope_de_grace_resistance");
    public static final ResourceLocation COPE_ACCUMULATOR_STAGE = WoldsVaults.id("cope_de_grace_accumulator");
    public static final ResourceLocation SAVIOR_RESISTANCE_STAGE = WoldsVaults.id("savior_resistance");
    public static final ResourceLocation COPE_STACKS_FACTOR = WoldsVaults.id("cope_de_grace_stacks");
    public static final ResourceLocation BULLET_TIME_CLOCK_FACTOR = WoldsVaults.id("bullet_time");

    public static final UUID COPE_ATTACK_DAMAGE = UUID.fromString("3f1b6a52-1d70-4d18-9f3a-6c1a1b0d0001");
    public static final UUID COPE_ABILITY_POWER = UUID.fromString("3f1b6a52-1d70-4d18-9f3a-6c1a1b0d0002");
    public static final UUID BULLET_TIME_ATTACK_SPEED = UUID.fromString("3f1b6a52-1d70-4d18-9f3a-6c1a1b0d0003");
    public static final UUID BULLET_TIME_MOVEMENT_SPEED = UUID.fromString("3f1b6a52-1d70-4d18-9f3a-6c1a1b0d0004");

    private UltimateIds() {
    }

    public static String specializationFor(VaultGod god) {
        if (god == null) {
            return DORMANT;
        }
        return switch (god) {
            case IDONA -> COPE_DE_GRACE;
            case VELARA -> SAVIOR;
            case TENOS -> EYES_OF_GOD;
            case WENDARR -> BULLET_TIME;
        };
    }
}

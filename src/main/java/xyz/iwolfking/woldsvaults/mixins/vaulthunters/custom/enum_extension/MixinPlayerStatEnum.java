package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import com.google.gson.annotations.SerializedName;
import iskallia.vault.util.calc.PlayerStat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;


import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = PlayerStat.class, remap =false)
public class MixinPlayerStatEnum {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static PlayerStat[] $VALUES;

    @Unique
    @SerializedName("thorns_scaling_damage")
    private static final PlayerStat THORNS_SCALING_DAMAGE = enumExpansion$addVariant("thorns_scaling_damage");

    @Unique
    @SerializedName("dismantle_chance")
    private static final PlayerStat DISMANTLE_CHANCE = enumExpansion$addVariant("dismantle_chance");

    @Unique
    @SerializedName("velara_affinity")
    private static final PlayerStat VELARA_AFFINITY = enumExpansion$addVariant("velara_affinity");

    @Unique
    @SerializedName("idona_affinity")
    private static final PlayerStat IDONA_AFFINITY = enumExpansion$addVariant("idona_affinity");

    @Unique
    @SerializedName("wendarr_affinity")
    private static final PlayerStat WENDARR_AFFINITY = enumExpansion$addVariant("wendarr_affinity");

    @Unique
    @SerializedName("tenos_affinity")
    private static final PlayerStat TENOS_AFFINITY = enumExpansion$addVariant("tenos_affinity");

    @Invoker("<init>")
    public static PlayerStat enumExpansion$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static PlayerStat enumExpansion$addVariant(String internalName) {
        ArrayList<PlayerStat> variants = new ArrayList<>(Arrays.asList(MixinPlayerStatEnum.$VALUES));
        PlayerStat type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1);
        variants.add(type);
        MixinPlayerStatEnum.$VALUES = variants.toArray(new PlayerStat[0]);
        return type;
    }
}

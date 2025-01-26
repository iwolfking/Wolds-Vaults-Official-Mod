package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.config.gear.VaultGearTypeConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.data.WeightedList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.data.gear.UnusualModifiers;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearRollTypeConfigAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearRollTypeConfigRollTypeAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearTierConfigAccessor;

import java.util.Map;


@Mixin(value = ModConfigs.class, remap = false)
public class MixinModConfigs {

    @Shadow public static Map<ResourceLocation, VaultGearTierConfig> VAULT_GEAR_CONFIG;
    private static final int TOTAL_MAP_TIERS = 10;

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private static void onReloadConfigs(CallbackInfo ci) {
        xyz.iwolfking.woldsvaults.init.ModConfigs.register();
        //Fix eternal attributes config
        ModConfigs.ETERNAL_ATTRIBUTES = xyz.iwolfking.woldsvaults.init.ModConfigs.ETERNAL_ATTRIBUTES;

        //Initialize unusual modifier values
        for(ResourceLocation config : ModConfigs.VAULT_GEAR_CONFIG.keySet()) {
            if(UnusualModifiers.UNUSUAL_MODIFIERS_MAP_PREFIX.containsKey(config)) {
                ((VaultGearTierConfigAccessor)ModConfigs.VAULT_GEAR_CONFIG.get(config)).getModifierGroup().put(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), UnusualModifiers.UNUSUAL_MODIFIERS_MAP_PREFIX.get(config));
            }
        }
        for(ResourceLocation config : ModConfigs.VAULT_GEAR_CONFIG.keySet()) {
            if(UnusualModifiers.UNUSUAL_MODIFIERS_MAP_SUFFIX.containsKey(config)) {
                ((VaultGearTierConfigAccessor)ModConfigs.VAULT_GEAR_CONFIG.get(config)).getModifierGroup().put(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), UnusualModifiers.UNUSUAL_MODIFIERS_MAP_SUFFIX.get(config));
            }
        }

        //Initialize gear configs for map tiers
        for(int i = 1; i < TOTAL_MAP_TIERS; i++) {
            VAULT_GEAR_CONFIG.put(VaultMod.id("map_" + i), new VaultGearTierConfig(VaultMod.id("map_" + i)).readConfig());
        }

        //Add new gear roll types
        VaultGearTypeConfig.RollType mythicRoll = new VaultGearTypeConfig.RollType(new WeightedList<>(Map.of(VaultGearRarity.valueOf("MYTHIC"), 1)));
        ((VaultGearRollTypeConfigRollTypeAccessor)mythicRoll).setColor(15597727);

        VaultGearTypeConfig.RollType sacredRoll = new VaultGearTypeConfig.RollType(new WeightedList<>(Map.of(VaultGearRarity.valueOf("SACRED"), 1)));
        ((VaultGearRollTypeConfigRollTypeAccessor)sacredRoll).setColor(13631559);

        ((VaultGearRollTypeConfigAccessor)ModConfigs.VAULT_GEAR_TYPE_CONFIG).getRolls().put("Mythic", mythicRoll);
        ((VaultGearRollTypeConfigAccessor)ModConfigs.VAULT_GEAR_TYPE_CONFIG).getRolls().put("Sacred", sacredRoll);



    }


}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.loot.LootTriggerAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModGearAttributeGenerators;
import iskallia.vault.init.ModGearAttributeReaders;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.api.util.MutableModifierReader;
import xyz.iwolfking.woldsvaults.api.util.WoldTexFX;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = ModGearAttributes.class, remap = false)
public class MixinModGearAttributes {
    @Shadow
    @Mutable
    public static final VaultGearAttribute<Float> BLOCK = attr(
            "block",
            VaultGearAttributeType.floatType(),
            ModGearAttributeGenerators.floatRange(),
            new MutableModifierReader<>(
                    WoldActiveFlags.IS_USING_SAFER_SPACE,
                    ModGearAttributeReaders.percentageReader("Block Chance", 16109454),

                new WoldTexFX.Corrupted<>(
                new WoldTexFX.Enclose<>("-",
                    ModGearAttributeReaders.percentageReader("Barrier Cooldown", 11430865)))
            ),
            VaultGearAttributeComparator.floatComparator()
    );


    @Shadow
    private static <T> VaultGearAttribute<T> attr(String name, VaultGearAttributeType<T> type, ConfigurableAttributeGenerator<T, ?> generator, VaultGearModifierReader<T> reader, @Nullable VaultGearAttributeComparator<T> comparator) {
        return new VaultGearAttribute<>(VaultMod.id(name), type, generator, reader, comparator);
    }

    @Shadow
    @Final
    private static List<VaultGearAttribute<? extends LootTriggerAttribute>> LOOT_TRIGGER_ATTRIBUTES;

    @Inject(method = "registerAssociations", at = @At("TAIL"))
    private static void addLootTriggerAttributes(CallbackInfo ci) {
        LOOT_TRIGGER_ATTRIBUTES.add(xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEART_FRAGMENT_ON_LOOT);
    }
}

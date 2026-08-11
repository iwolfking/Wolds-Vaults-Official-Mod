package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.VaultMod;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.modifier.modifier.HunterModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = Modifiers.class, remap = false)
public abstract class MixinModifiers extends DataObject<Modifiers> {

    private static final ResourceLocation DAYCARE_ID = VaultMod.id("daycare");
    private static final int MAX_DAYCARE_STACKS = 1;

    /**
     * Caps Daycare at one stack per vault. Every modifier stack gets its own modifier context UUID,
     * so the duplicate check in EntityAttributeModifier never sees the other stacks and both of
     * Daycare's children apply once per stack. Two stacks therefore sum to -100% mob size, which
     * leaves mobs invisible, and -100% mob damage. Zeroing the amount drops the whole group rather
     * than its children, so the lower_damage child that Unchallenge Stack also uses is untouched.
     */
    @ModifyVariable(method = "addModifier(Liskallia/vault/core/vault/modifier/spi/VaultModifier;IZLiskallia/vault/core/random/RandomSource;Ljava/util/function/Consumer;)Liskallia/vault/core/vault/Modifiers;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int limitDaycareToOneStack(int amount, VaultModifier<?> modifier, int ignored0, boolean ignored1, RandomSource ignored2, Consumer<ModifierContext> ignored3) {
        if (!DAYCARE_ID.equals(modifier.getId())) {
            return amount;
        }

        return ((Modifiers) (Object) this).hasModifier(DAYCARE_ID) ? 0 : Math.min(amount, MAX_DAYCARE_STACKS);
    }

//    @Shadow
//    @Final
//    protected static FieldKey<Modifiers.Entry.List> ENTRIES;
//
//    @Inject(method = "addModifier(Liskallia/vault/core/vault/modifier/spi/VaultModifier;IZLiskallia/vault/core/random/RandomSource;Ljava/util/function/Consumer;)Liskallia/vault/core/vault/Modifiers;", at = @At("HEAD"), cancellable = true)
//    private void preventAddingMultipleHunterModifiers(VaultModifier<?> modifier, int amount, boolean display, RandomSource random, Consumer<ModifierContext> configurator, CallbackInfoReturnable<Modifiers> cir) {
//        if(modifier instanceof HunterModifier || modifier.getId().equals(VaultMod.id("kill_hunter"))) {
//            if(this.get(ENTRIES).stream().anyMatch(entry -> {
//                VaultModifier<?> existingMod = entry.getModifier().orElse(null);
//
//                if(existingMod == null) {
//                    return false;
//                }
//
//                return modifier.getId().equals(existingMod.getId());
//            })) {
//                cir.setReturnValue((Modifiers)(Object)this);
//            }
//        }
//    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.MixinModifierContextAccessor;

import java.util.Optional;

@Mixin(value = ModifierContext.class, remap = false)
public abstract class MixinModifierContext extends DataObject<ModifierContext> implements MixinModifierContextAccessor {

    @Shadow @Final public static FieldRegistry FIELDS;
    @Unique
    private static FieldKey<Float> woldsVaults$VALUE = FieldKey.of("value", Float.class).with(Version.v1_5, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);

    @Override
    public Optional<Float> woldsVaults_Dev$getValue() {
        return Optional.of(this.get(woldsVaults$VALUE));
    }

    @Override
    public void woldsVaults_Dev$setValue(Float value) {
        this.set(woldsVaults$VALUE, value);
    }

    @Inject(method = "copy", at = @At(value = "TAIL", target = "Liskallia/vault/core/vault/modifier/spi/ModifierContext;setIf(Liskallia/vault/core/data/key/GenericFieldKey;Ljava/lang/Object;Ljava/util/function/Predicate;)Liskallia/vault/core/data/IDataObject;"))
    private void copyValue(CallbackInfoReturnable<ModifierContext> cir) {
        if(cir.getReturnValue() != null) {
            cir.getReturnValue().setIf(woldsVaults$VALUE, this.get(woldsVaults$VALUE), v -> this.has(woldsVaults$VALUE));
        }
    }
}

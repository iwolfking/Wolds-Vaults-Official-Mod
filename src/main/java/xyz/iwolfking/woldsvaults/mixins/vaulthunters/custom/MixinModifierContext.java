package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.lib.MixinModifierContextAccessor;

import java.util.Optional;

@Mixin(value = ModifierContext.class, remap = false)
public abstract class MixinModifierContext extends DataObject<ModifierContext> implements MixinModifierContextAccessor {

    @Shadow @Final public static FieldRegistry FIELDS;
    @Unique
    private static FieldKey<Float> VALUE = FieldKey.of("value", Float.class).with(Version.v1_5, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);

    @Override
    public Optional<Float> woldsVaults_Dev$getValue() {
        return Optional.of(this.get(VALUE));
    }

    @Override
    public void woldsVaults_Dev$setValue(Float value) {
        this.set(VALUE, value);
    }
}

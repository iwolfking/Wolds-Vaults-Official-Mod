package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.modifier.EmptyModifier;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

@Mixin(value = VaultModifierStack.class, remap = false)
public class MixinVaultModifierStack {
    @Shadow private VaultModifier<?> modifier;
    @Shadow private int size;

    /**
     * @author iwolfking
     * @reason Serialize value based modifier values
     */
    @Overwrite
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", this.modifier.getId().toString());
        nbt.putInt("size", this.size);
        if(modifier instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
            nbt.putFloat("value", settableValueVaultModifier.properties().getValue());
        }
        return nbt;
    }

    /**
     * @author iwolfking
     * @reason Deserialize value based modifier values
     */
    @Overwrite
    public void deserializeNBT(CompoundTag nbt) {
        ResourceLocation id = new ResourceLocation(nbt.getString("id"));
        this.modifier = VaultModifierRegistry.getOrDefault(id, EmptyModifier.INSTANCE);
        if(modifier instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
            settableValueVaultModifier.properties().setValue(nbt.getFloat("value"));
        }
        this.size = nbt.getInt("size");
    }

    /**
     * @author iwolfking
     * @reason Encode value for settable modifiers
     */
    @Overwrite
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.modifier.getId());
        buffer.writeInt(this.size);
        if(modifier instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
            buffer.writeFloat(settableValueVaultModifier.properties().getValue());
        }
    }

    /**
     * @author iwolfking
     * @reason Decode value for settable modifiers
     */
    @Overwrite
    public static VaultModifierStack decode(FriendlyByteBuf buffer) {
        ResourceLocation resourceLocation = buffer.readResourceLocation();
        VaultModifier<?> modifier = VaultModifierRegistry.getOrDefault(resourceLocation, EmptyModifier.INSTANCE);
        int size = buffer.readInt();
        if(modifier instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
            settableValueVaultModifier.properties().setValue(buffer.readFloat());
        }
        return new VaultModifierStack(modifier, size);
    }
}

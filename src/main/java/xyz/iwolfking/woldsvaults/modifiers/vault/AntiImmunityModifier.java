package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public class AntiImmunityModifier extends VaultModifier<AntiImmunityModifier.Properties> {
    public AntiImmunityModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.GRANTED_EFFECT.register(context.getUUID(), (data) -> {
            if (world == data.getWorld()) {
                if (!context.hasTarget() || context.getTarget().equals(data.getPlayer().getUUID())) {
                    data.getEffects().addAmplifier(this.properties.getEffect(), 1);
                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final ResourceLocation id;

        public Properties(ResourceLocation id) {
            this.id = id;
        }



        public ResourceLocation getId() {
            return this.id;
        }

        public MobEffect getEffect() {
            return ForgeRegistries.MOB_EFFECTS.getValue(id);
        }
    }
}

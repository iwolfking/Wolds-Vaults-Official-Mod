package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.PlayerEffectModifier;
import iskallia.vault.core.vault.modifier.reputation.ScalarReputationProperty;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.common.MinecraftForge;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class PlayerEffectModifierSettable extends SettableValueVaultModifier<PlayerEffectModifierSettable.Properties> {
    public PlayerEffectModifierSettable(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
        MinecraftForge.EVENT_BUS.register(this);
    }


    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.GRANTED_EFFECT.register(context.getUUID(), (data) -> {
            if (world == data.getWorld()) {
                if (!context.hasTarget() || context.getTarget().equals(data.getPlayer().getUUID())) {
                    if (data.getFilter().test((this.properties).effect)) {
                        data.getEffects().addAmplifier((this.properties).effect, (int) (this.properties).getValue());
                    }

                }
            }
        });
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final MobEffect effect;

        public Properties(MobEffect effect) {
            this.effect = effect;
        }

        public MobEffect getEffect() {
            return this.effect;
        }
    }
}

package xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifierConfig;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

public class MobEffectSmoothieModifier extends SmoothieModifier {


    public MobEffectSmoothieModifier(SmoothieModifierConfig config) {
        super(config);
    }

    @Override
    public void instantTrigger(Player player, Vault vault) {
        if(vault == null) {
            return;
        }

        if(this.getConfig() instanceof Config mobEffectSmoothieConfig) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(mobEffectSmoothieConfig.mobEffect);

            if(effect != null) {
                player.addEffect(new MobEffectInstance(effect, mobEffectSmoothieConfig.duration, mobEffectSmoothieConfig.amplifier));
            }
        }
    }

    @Override
    public MutableComponent getTooltipDisplay() {
        if(this.getConfig() instanceof Config mobEffectSmoothieConfig) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(mobEffectSmoothieConfig.mobEffect);
            if (effect != null) {
                return new TextComponent("Inflicts you with ").append(new TextComponent(effect.getDisplayName().getString() + " " + mobEffectSmoothieConfig.amplifier + 1).withStyle(Style.EMPTY.withColor(effect.getColor()))).append(" for " + mobEffectSmoothieConfig.duration / 20 + " seconds");
            }
        }

        return new TextComponent("");
    }

    public static class Config extends SmoothieModifierConfig {
        @Expose
        private final ResourceLocation mobEffect;
        @Expose
        private final int duration;
        @Expose
        private final int amplifier;

        public Config(ResourceLocation id, ResourceLocation mobEffect, int duration, int amplifier) {
            super(id);
            this.mobEffect = mobEffect;
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }
}

package xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifierConfig;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

public class VaultModifierPoolSmoothieModifier extends SmoothieModifier {


    public VaultModifierPoolSmoothieModifier(SmoothieModifierConfig config) {
        super(config);
    }

    @Override
    public void instantTrigger(Player player, Vault vault) {
        if(vault == null) {
            return;
        }

        if(this.getConfig() instanceof Config smoothieModifierPoolConfig) {
            VaultModifierUtils.addModifierFromPool(vault, smoothieModifierPoolConfig.modifierPool);
        }
    }

    @Override
    public MutableComponent getTooltipDisplay() {
        if(this.getConfig() instanceof Config smoothieModifierPoolConfig) {
            return new TextComponent("Adds a random ").append(new TextComponent(smoothieModifierPoolConfig.modifierPoolName).withStyle(Style.EMPTY.withColor(smoothieModifierPoolConfig.modifierPoolNameColor)).append(" modifier"));
        }

        return new TextComponent("");
    }

    public static class Config extends SmoothieModifierConfig {
        @Expose
        private final ResourceLocation modifierPool;
        @Expose
        private final String modifierPoolName;
        @Expose
        private final TextColor modifierPoolNameColor;

        public Config(ResourceLocation id, ResourceLocation modifierPool, String modifierPoolName, TextColor modifierPoolNameColor) {
            super(id);
            this.modifierPool = modifierPool;
            this.modifierPoolName = modifierPoolName;
            this.modifierPoolNameColor = modifierPoolNameColor;
        }
    }
}

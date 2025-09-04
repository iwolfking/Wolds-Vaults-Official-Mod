package xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifierConfig;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

public class VaultModifierSmoothieModifier extends SmoothieModifier {


    public VaultModifierSmoothieModifier(SmoothieModifierConfig config) {
        super(config);
    }

    @Override
    public void instantTrigger(Player player, Vault vault) {
        if(vault == null) {
            return;
        }

        if(this.getConfig() instanceof Config smoothieModifierPoolConfig) {
            VaultModifierUtils.addModifier(vault, smoothieModifierPoolConfig.modifierId, smoothieModifierPoolConfig.modifierCount);
        }
    }

    @Override
    public MutableComponent getTooltipDisplay() {
        if(this.getConfig() instanceof Config smoothieModifierPoolConfig) {
            return new TextComponent("Adds ").append(VaultModifierRegistry.get(smoothieModifierPoolConfig.modifierId).getChatDisplayNameComponent(smoothieModifierPoolConfig.modifierCount)).append(" to the vault");
        }

        return new TextComponent("");
    }

    public static class Config extends SmoothieModifierConfig {
        @Expose
        private final ResourceLocation modifierId;
        @Expose
        private final int modifierCount;

        public Config(ResourceLocation id, ResourceLocation modifierId, int modifierCount) {
            super(id);
            this.modifierId = modifierId;
            this.modifierCount = modifierCount;
        }
    }
}

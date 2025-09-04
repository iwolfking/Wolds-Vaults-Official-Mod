package xyz.iwolfking.woldsvaults.items.fruits.smoothie;

import iskallia.vault.core.vault.Vault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class SmoothieModifier {
    SmoothieModifierConfig config;

    public SmoothieModifier(SmoothieModifierConfig config) {
        this.config = config;
    }

    public abstract void instantTrigger(Player player, Vault vault);

    public abstract MutableComponent getTooltipDisplay();

    public SmoothieModifierConfig getConfig() {
        return this.config;
    }
}

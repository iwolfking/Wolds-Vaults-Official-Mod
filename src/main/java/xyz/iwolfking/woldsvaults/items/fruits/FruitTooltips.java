package xyz.iwolfking.woldsvaults.items.fruits;

import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.Nullable;

public class FruitTooltips {
    public static @Nullable Component getHPTooltip() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        var maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return null;
        }
        var existingModifier = maxHealth.getModifier(ItemVaultFruit.MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID);

        double multOld = 1.0;
        double mult = 1.0849795594911;
        if (existingModifier != null) {
            mult = existingModifier.getAmount() + 1;
            multOld = existingModifier.getAmount() + 1;
        }

        mult *= 0.827;
        var currVal = maxHealth.getValue();
        var nexVal = (maxHealth.getValue() / multOld) * mult;
        return new TextComponent("Removes").withStyle(ChatFormatting.GRAY).append(
            new TextComponent(String.format(" %.1f ❤ from max health", 0.5*(currVal - nexVal))).withStyle(ChatFormatting.RED));
    }

    public static @Nullable Component getFruitCountTooltip() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        var maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return null;
        }
        var existingModifier = maxHealth.getModifier(ItemVaultFruit.MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID);

        double multOld = 1.0;
        double mult = 1.0849795594911;
        if (existingModifier != null) {
            mult = existingModifier.getAmount() + 1;
            multOld = existingModifier.getAmount() + 1;
        }

        int canEat = 0;
        double simHp = maxHealth.getValue();
        while (simHp >= 5) {
            mult *= 0.827;
            canEat++;
            simHp = (maxHealth.getValue() / multOld) * mult;
        }

        if (canEat > 0) {
            return new TextComponent("You can eat "+canEat+" more fruit" + (canEat > 1 ? "s" : ""))
                .withStyle(ChatFormatting.DARK_GRAY);
        } else {
            return new TextComponent("You can't eat any more fruits")
                .withStyle(ChatFormatting.DARK_GRAY);
        }
    }
}

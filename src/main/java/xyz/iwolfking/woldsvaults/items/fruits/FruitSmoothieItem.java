package xyz.iwolfking.woldsvaults.items.fruits;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.SmoothieModifierConfig;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers.MobEffectSmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers.VaultModifierPoolSmoothieModifier;
import xyz.iwolfking.woldsvaults.items.fruits.smoothie.modifiers.VaultModifierSmoothieModifier;

import java.util.List;

public class FruitSmoothieItem extends ItemVaultFruit {

    private Config smoothieConfig;

    public FruitSmoothieItem(ResourceLocation id, int extraVaultTicks) {
        super(id, extraVaultTicks);
        this.smoothieConfig = new Config("Companion Smoothie", TextColor.fromLegacyFormat(ChatFormatting.AQUA), List.of(new VaultModifierPoolSmoothieModifier(new VaultModifierPoolSmoothieModifier.Config(WoldsVaults.id("add_frenzy"), VaultMod.id("companion_positive"), "Positive Companion", TextColor.fromLegacyFormat(ChatFormatting.GOLD))), new VaultModifierSmoothieModifier(new VaultModifierSmoothieModifier.Config(WoldsVaults.id("new_modifier"), VaultMod.id("peaceful"), 1)), new MobEffectSmoothieModifier(new MobEffectSmoothieModifier.Config(WoldsVaults.id("effect_name"), ResourceLocation.tryParse("minecraft:strength"), 60, 4))));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if(smoothieConfig != null) {
            tooltip.add(new TextComponent("Smoothie Modifiers").withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent("------------------").withStyle(ChatFormatting.GOLD));
            for(SmoothieModifier modifier : smoothieConfig.modifiers) {
                tooltip.add(new TextComponent(""));
                tooltip.add(modifier.getTooltipDisplay());
            }
        }
        tooltip.add(new TextComponent(""));
        super.appendHoverText(itemStack, worldIn, tooltip, tooltipFlag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return this.smoothieConfig != null ? new TextComponent(smoothieConfig.name).withStyle(Style.EMPTY.withColor(smoothieConfig.nameColor)) : new TextComponent("Unconfigured Fruit Smoothie");
    }

    @Override
    protected void successEaten(Level level, ServerPlayer sPlayer) {
        for(SmoothieModifier modifier : smoothieConfig.modifiers) {
            modifier.instantTrigger(sPlayer, VaultUtils.getVault(level).orElse(null));
        }
        super.successEaten(level, sPlayer);
    }

    class Config {
        @Expose
        public String name;

        @Expose
        public TextColor nameColor;

        @Expose
        public List<SmoothieModifier> modifiers;


        public Config(String name, TextColor nameColor, List<SmoothieModifier> modifiers) {
            this.name = name;
            this.nameColor = nameColor;
            this.modifiers = modifiers;
        }
    }
}

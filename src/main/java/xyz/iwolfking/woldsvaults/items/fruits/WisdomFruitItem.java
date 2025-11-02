package xyz.iwolfking.woldsvaults.items.fruits;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.ItemVaultFruit;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.List;
import java.util.Random;

public class WisdomFruitItem extends ItemVaultFruit {

    private static final Random rand = new Random();
    public WisdomFruitItem(ResourceLocation id) {
        super(id, 0);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag tooltipFlag) {

        MutableComponent cmp = (new TextComponent("Adds ")).withStyle(ChatFormatting.GRAY).append((new TextComponent("a random Hunter modifier ")).withStyle(ChatFormatting.GREEN)).append(" to the Vault");
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Adds")).withStyle(ChatFormatting.GRAY).append((new TextComponent(" 1x Random Harsh Negative")).withStyle(ChatFormatting.RED)).append(new TextComponent(" to the Vault").withStyle(ChatFormatting.GRAY)));
        tooltip.add(cmp);
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Only edible inside a Vault")).withStyle(ChatFormatting.RED));
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (!level.isClientSide() && entityLiving instanceof ServerPlayer player) {

                this.successEaten(level, player);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.MASTER, 1.0F, 1.0F);
        }

        return entityLiving.eat(level, stack);
    }

    @Override
    protected void successEaten(Level level, ServerPlayer sPlayer) {
        if(ServerVaults.get(level).isPresent()) {
            Vault vault = ServerVaults.get(level).get();
            List<VaultModifier<?>> hunter_modifier = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("hunters_enchanted_random"), 0, JavaRandom.ofNanoTime());
            List<VaultModifier<?>> negative_modifier = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("medium_negative"), 0, JavaRandom.ofNanoTime());
            for(VaultModifier<?> mod : hunter_modifier) {
                vault.get(Vault.MODIFIERS).addModifier(mod, 1, true, ChunkRandom.any());
                VaultModifierUtils.sendModifierAddedMessage(sPlayer, mod, 1);
            }

            for(VaultModifier<?> mod : negative_modifier) {
                vault.get(Vault.MODIFIERS).addModifier(mod, 1, true, ChunkRandom.any());
                VaultModifierUtils.sendModifierAddedMessage(sPlayer, mod, 1);
            }

        }

    }
}

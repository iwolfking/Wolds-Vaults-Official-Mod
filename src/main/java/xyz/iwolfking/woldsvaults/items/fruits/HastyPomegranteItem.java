package xyz.iwolfking.woldsvaults.items.fruits;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.List;
import java.util.Random;

public class HastyPomegranteItem extends ItemVaultFruit {
    private static final Random rand = new Random();
    public HastyPomegranteItem(ResourceLocation id, int extraVaultTicks) {
        super(id, extraVaultTicks);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag tooltipFlag) {
        int seconds = Mth.floor(this.extraVaultTicks / 20.0F);
        String timeText = String.format("%d seconds", seconds);
        if (seconds > 90) {
            int minutes = seconds / 60;
            timeText = String.format("%d minutes", minutes);
        }

        MutableComponent cmp = (new TextComponent("Adds ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(timeText)).withStyle(ChatFormatting.GREEN)).append(" to the Vault timer");
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Adds")).withStyle(ChatFormatting.GRAY).append((new TextComponent(" 2x Rapid Mobs")).withStyle(ChatFormatting.RED)).append(new TextComponent(" to the Vault (max 6)").withStyle(ChatFormatting.GRAY)));
        tooltip.add((new TextComponent("Removes")).withStyle(ChatFormatting.GRAY).append((new TextComponent(" 10% max health")).withStyle(ChatFormatting.RED)).append(new TextComponent(" after 6 stacks have been added.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(cmp);
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Only edible inside a Vault")).withStyle(ChatFormatting.RED));
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (!level.isClientSide() && entityLiving instanceof ServerPlayer player) {
            if (!this.onEaten(level, player)) {
                return stack;
            }

            if(ServerVaults.get(level).isPresent()) {
                Vault vault = ServerVaults.get(level).get();
                int rapidMobsCount = 0;

                for(VaultModifier<?> mod : vault.get(Vault.MODIFIERS).getModifiers()) {
                    if(mod.getId().equals(VaultMod.id("infuriated_mobs"))) {
                        rapidMobsCount += 1;
                    }
                }

                if(rapidMobsCount >= 6) {
                    this.reducePlayerMaxHealth(player);
                }
                else {
                    this.successEaten(level, player);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.MASTER, 1.0F, 1.0F);
        }

        return entityLiving.eat(level, stack);
    }

    @Override
    protected void successEaten(Level level, ServerPlayer sPlayer) {
        if(ServerVaults.get(level).isPresent()) {
            Vault vault = ServerVaults.get(level).get();
            VaultModifier<?> modifier = VaultModifierRegistry.get(VaultMod.id("infuriated_mobs"));
            if(modifier != null) {
                VaultModifierUtils.sendModifierAddedMessage(sPlayer, modifier, 2);
                vault.get(Vault.MODIFIERS).addModifier(modifier, 2, true, ChunkRandom.any());
            }

        }

    }
}

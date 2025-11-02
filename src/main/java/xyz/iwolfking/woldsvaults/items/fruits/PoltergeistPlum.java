package xyz.iwolfking.woldsvaults.items.fruits;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.ItemVaultFruit;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.List;
import java.util.Random;

public class PoltergeistPlum extends ItemVaultFruit {
    private static final Random rand = new Random();
    public PoltergeistPlum(ResourceLocation id) {
        super(id, 4800);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag tooltipFlag) {
        int seconds = Mth.floor(this.extraVaultTicks / 20.0F);
        String timeText = String.format("%d seconds", seconds);
        if (seconds > 90) {
            int minutes = seconds / 60;
            timeText = String.format("%d minutes", minutes);
        }

        MutableComponent cmp = (new TextComponent("Adds ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(timeText)).withStyle(ChatFormatting.GREEN)).append(" to the Vault timer, ").append(new TextComponent("30 seconds ").withStyle(ChatFormatting.GREEN)).append(new TextComponent(" if one has been consumed already.").withStyle(ChatFormatting.GRAY));
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Adds")).withStyle(ChatFormatting.GRAY).append((new TextComponent(" 1x Vexation")).withStyle(ChatFormatting.RED)).append(new TextComponent(" to the Vault.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(cmp);
        tooltip.add(TextComponent.EMPTY);
        tooltip.add((new TextComponent("Only edible inside a Vault")).withStyle(ChatFormatting.RED));
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (!level.isClientSide() && entityLiving instanceof ServerPlayer player) {
            if(ServerVaults.get(level).isPresent()) {
                Vault vault = ServerVaults.get(level).get();
                boolean hasVexation = false;

                for(VaultModifier<?> mod : vault.get(Vault.MODIFIERS).getModifiers()) {
                    if(mod.getId().equals(VaultMod.id("vexation"))) {
                        hasVexation = true;
                        break;
                    }
                }

                if(hasVexation) {
                    if (!this.specialOnEaten(level, player, 600)) {
                        return stack;
                    }
                    this.successEaten(level, player);
                }
                else {
                    if (!this.specialOnEaten(level, player, this.extraVaultTicks)) {
                        return stack;
                    }
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
            VaultModifier<?> vexationMod = VaultModifierRegistry.get(VaultMod.id("vexation"));
            if(vexationMod != null) {
                VaultModifierUtils.sendModifierAddedMessage(sPlayer, vexationMod, 1);
                vault.get(Vault.MODIFIERS).addModifier(VaultModifierRegistry.get(VaultMod.id("vexation")), 1, true, ChunkRandom.any());
            }

        }
    }

    public boolean specialOnEaten(Level level, Player player, int ticks) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float effectiveness = snapshot.getAttributeValue(ModGearAttributes.FRUIT_EFFECTIVENESS, VaultGearAttributeTypeMerger.floatSum());
        int time = (int)(ticks * (1.0F + effectiveness));
        CommonEvents.FRUIT_EATEN.invoke(this, player, time);
        return true;
    }
}

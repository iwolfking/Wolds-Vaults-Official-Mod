package xyz.iwolfking.woldsvaults.api.core.vault_events.legacy;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.LegacyVaultEvent;

import java.util.Iterator;
import java.util.List;

public class VaultModifierVaultEvent extends LegacyVaultEvent {

    @Expose
    private final String modifierString;

    public VaultModifierVaultEvent(String eventName, String eventDescription, String primaryColor, String modifierString) {
        super(eventName, eventDescription, primaryColor);
        this.modifierString = modifierString;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        ChunkRandom random = ChunkRandom.any();

            random.setBlockSeed(vault.get(Vault.SEED), pos, 90039737L);
            TextComponent text = new TextComponent("");
            MutableComponent eventMessage = new TextComponent("");
            List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id(modifierString), 0, JavaRandom.ofNanoTime());
            if(!modifiers.isEmpty()) {
                eventMessage.append(player.getDisplayName());
                eventMessage.append(new TextComponent(" encountered a ").withStyle(ChatFormatting.GRAY));
                eventMessage.append(getEventMessage());
                Iterator modIter = modifiers.iterator();

                while(modIter.hasNext()) {
                    VaultModifier<?> mod = (VaultModifier<?>) modIter.next();
                    TextComponent suffix = (TextComponent) mod.getChatDisplayNameComponent(1);
                    text.append(player.getDisplayName()).append((new TextComponent(" added ")).withStyle(ChatFormatting.GRAY)).append(suffix).append((new TextComponent(".")).withStyle(ChatFormatting.GRAY));
                    if(modIter.hasNext()) {
                        text.append("\n");
                    }
                    vault.get(Vault.MODIFIERS).addModifier(mod, 1, true, random);
                }

                super.triggerEvent(pos, player, vault);

                for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
                    listener.getPlayer().ifPresent(other -> {
                        player.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                        other.displayClientMessage(text, false);
                    });
                }
            }
    }

    @Override
    public Component getEventMessage() {
        return new TextComponent("Random " +  this.getEventName() + " Modifier Event!").withStyle(Style.EMPTY.withColor(getPrimaryColor())).withStyle(getHoverDescription());
    }


}

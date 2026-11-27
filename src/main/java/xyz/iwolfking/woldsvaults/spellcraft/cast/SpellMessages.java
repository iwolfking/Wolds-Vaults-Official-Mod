package xyz.iwolfking.woldsvaults.spellcraft.cast;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public final class SpellMessages {
    private SpellMessages() {
    }

    public static void show(ServerPlayer player, String message) {
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }
}

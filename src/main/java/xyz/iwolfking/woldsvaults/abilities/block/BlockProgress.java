package xyz.iwolfking.woldsvaults.abilities.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;


final class BlockProgress<A extends AbstractBlockAbility> {
    final A ability;
    final float required;
    final int raisedTick;
    float lifted;

    BlockProgress(A ability, float required, int raisedTick) {
        this.ability = ability;
        this.required = Math.max(required, 1.0F);
        this.raisedTick = raisedTick;
    }

    boolean lift(float amount) {
        this.lifted += amount;
        return this.lifted >= this.required;
    }

    int clearTicks(ServerPlayer player) {
        return player.server.getTickCount() - this.raisedTick;
    }

    void showProgress(ServerPlayer player, String key, ChatFormatting color) {
        player.displayClientMessage(new TranslatableComponent(key, Math.round(Math.min(this.lifted, this.required)),
                Math.round(this.required)).withStyle(color), true);
    }

    static void announceLift(ServerPlayer player, String key, ChatFormatting color, int clearTicks, int buffTicks) {
        player.displayClientMessage(new TranslatableComponent(key, seconds(clearTicks), seconds(buffTicks))
                .withStyle(color), true);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private static String seconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0F);
    }
}

package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

public class PlaySoundTask implements VaultEventTask {

    private final SoundEvent sound;
    private final float pitch;
    private final float volume;
    private final SoundSource source;

    public PlaySoundTask(SoundEvent sound, float pitch, float volume, SoundSource source) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
        this.source = source;
    }

    public PlaySoundTask(SoundEvent sound) {
        this.sound = sound;
        this.pitch = 1.0F;
        this.volume = 1.0F;
        this.source = SoundSource.PLAYERS;
    }

    public PlaySoundTask(SoundEvent sound, SoundSource source) {
        this.sound = sound;
        this.pitch = 1.0F;
        this.volume = 1.0F;
        this.source = source;
    }



    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        player.getLevel().playSound(null, pos, sound, source, volume, pitch);
    }
}

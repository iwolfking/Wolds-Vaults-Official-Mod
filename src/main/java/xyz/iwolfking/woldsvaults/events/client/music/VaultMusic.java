package xyz.iwolfking.woldsvaults.events.client.music;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

public class VaultMusic {
    public static Music VAULT_LOOP(SoundEvent event) {
        return new Music(event, 0, 0, true);
    }
}
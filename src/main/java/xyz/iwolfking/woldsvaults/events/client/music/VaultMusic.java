package xyz.iwolfking.woldsvaults.events.client.music;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

public class VaultMusic {
    // Defines a looping music entry for the vault.
    public static Music VAULT_LOOP(SoundEvent event) {
        return new Music(event, 0, 0, true);
    }
}
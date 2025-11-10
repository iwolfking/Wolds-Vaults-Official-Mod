package xyz.iwolfking.woldsvaults.events.client;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.objective.HeraldMusicHandler;
import iskallia.vault.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.events.client.music.VaultMusic;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MusicEvents {

    private static boolean isPlayingVaultMusic = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        var manager = mc.getMusicManager();
        var soundManager = mc.getSoundManager();

        boolean shouldPlay = shouldPlayVaultMusic();

        if (shouldPlay && !isPlayingVaultMusic) {
            manager.stopPlaying();
            manager.startPlaying(VaultMusic.VAULT_LOOP(getVaultTrack()));
            isPlayingVaultMusic = true;
        } else if (!shouldPlay && isPlayingVaultMusic) {
            manager.stopPlaying();
            soundManager.stop(null, net.minecraft.sounds.SoundSource.MUSIC);
            isPlayingVaultMusic = false;
        }
    }

    private static SoundEvent getVaultTrack() {
        return ModSounds.VAULT_AMBIENT_LOOP;
    }

    private static boolean shouldPlayVaultMusic() {
        return WoldsVaultsConfig.CLIENT.playVaultMusic.get() && ClientVaults.getActive().isPresent();
    }
}

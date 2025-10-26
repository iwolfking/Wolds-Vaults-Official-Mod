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
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MusicEvents {
    public static SimpleSoundInstance TRACK;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            SoundManager manager = Minecraft.getInstance().getSoundManager();
            if (!shouldPlay() || manager.isActive(HeraldMusicHandler.TRACK)) {
                if (manager.isActive(TRACK)) {
                    manager.stop(TRACK);
                }

            } else {
                if (!manager.isActive(TRACK) && shouldPlay()) {
                    TRACK = new SimpleSoundInstance(
                            getTrack().getLocation(), SoundSource.MASTER, 0.3F, 1.0F, true, 0, SoundInstance.Attenuation.LINEAR, 0.0, 0.0, 0.0, true
                    );
                    manager.play(TRACK);
                }
            }
        }
    }

    private static SoundEvent getTrack() {
        return ModSounds.VAULT_AMBIENT_LOOP;
    }

    private static boolean shouldPlay() {
        return WoldsVaultsConfig.CLIENT.playVaultMusic.get() && ClientVaults.getActive().isPresent();
    }
}

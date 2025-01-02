package xyz.iwolfking.woldsvaults.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.init.ModSounds;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoundLoopHandler {
    private static boolean isPlaying = false;
    private static SimpleSoundInstance activeSound = null;
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (VaultModifierUtils.isVaultCorrupted) {
            startAmbientSound();
        } else {
            stopAmbientSound();
        }
    }

    private static void startAmbientSound() {
        if (!isPlaying) {
            activeSound = new SimpleSoundInstance(
                    ModSounds.DARK.getLocation(),
                    SoundSource.AMBIENT,
                    0.2f,
                    1.0f,
                    true,
                    0,
                    SimpleSoundInstance.Attenuation.NONE,
                    0, 0, 0,
                    false
            );
            mc.getSoundManager().play(activeSound);
            isPlaying = true;
        }
    }

    private static void stopAmbientSound() {
        if (isPlaying) {
            if (activeSound != null) {
                mc.getSoundManager().stop(activeSound);
            }

            isPlaying = false;
            activeSound = null;
        }
    }
}

package xyz.iwolfking.woldsvaults.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.init.ModSounds;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoundHandler {
    private static int ambientSoundTimer = 0;
    public static boolean isLoopPlaying = false;
    private static SimpleSoundInstance activeSound = null;
    private static final Minecraft mc = Minecraft.getInstance();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Only execute during the END phase
        if (event.phase != TickEvent.Phase.END) return;

        // Ensure the player and level are valid
        if (mc.player == null || mc.level == null || mc.getSoundManager() == null) return;

        // Check if the vault is corrupted
        boolean isCorrupted = VaultUtil.isVaultCorrupted;

        // Manage the looping sound
        manageLoop(isCorrupted);

        // Play ambient sound at intervals
        if (isCorrupted) {
            playAmbientSound();
        }
    }

    private static void manageLoop(boolean isCorrupted) {
        if (isCorrupted) {
            // Start the looped sound if it's not already playing
            if (!isLoopPlaying) {
                activeSound = new SimpleSoundInstance(
                        ModSounds.DARK.getLocation(),
                        SoundSource.AMBIENT,
                        0.2f, // Volume
                        1.0f, // Pitch
                        true, // Looping
                        0, // Delay
                        SimpleSoundInstance.Attenuation.NONE, // Attenuation type
                        (float) mc.player.getX(),
                        (float) mc.player.getY(),
                        (float) mc.player.getZ(),
                        false
                );
                mc.getSoundManager().play(activeSound);
                isLoopPlaying = true;
            }
        } else if (activeSound != null){

            mc.getSoundManager().stop(activeSound);
            isLoopPlaying = false;
            activeSound = null;

        }
    }

    private static void playAmbientSound() {
        int soundInterval = 600; // 30 seconds TODO -> randomize for prod
        if (ambientSoundTimer >= soundInterval) {
            mc.level.playSound(
                    mc.player,
                    mc.player.blockPosition(),
                    ModSounds.OMINOUS_AMBIENCE,
                    SoundSource.AMBIENT,
                    1.0f,
                    ThreadLocalRandom.current().nextFloat() * 0.5f + 0.7f
            );

            // Reset the timer
            ambientSoundTimer = 0;
        } else {
            // Increment the timer
            ambientSoundTimer++;
        }
    }
}

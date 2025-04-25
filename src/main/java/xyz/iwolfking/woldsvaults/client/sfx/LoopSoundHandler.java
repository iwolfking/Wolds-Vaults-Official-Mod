package xyz.iwolfking.woldsvaults.client.sfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.init.ModSounds;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LoopSoundHandler {
    private static boolean ticked = false;
    private static boolean shouldPlay = false;
    private static SimpleSoundInstance track;
    private static int ambientSoundTimer = 0;
    private static int ambientSoundInterval = getRandomInterval();


    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        SoundManager manager = Minecraft.getInstance().getSoundManager();

        if(!ticked) {
            if(manager.isActive(track)) {
                manager.stop(track);
            }
            return;
        }

        if(!manager.isActive(track) && shouldPlay) {
            track = new SimpleSoundInstance(
                    ModSounds.DARK.getLocation(),
                    SoundSource.MASTER,
                    0.3F, 1.0F,
                    false, 0,
                    SoundInstance.Attenuation.LINEAR,
                    0.0D, 0.0D, 0.0D,
                    true
            );

            manager.play(track);
        }

        ticked = false;


        // Plays an ambient sound
        if (Minecraft.getInstance().level != null) {
            ambientSoundTimer++;

            if (ambientSoundTimer >= ambientSoundInterval) {
                // Play ambient sound

                SimpleSoundInstance ambient = new SimpleSoundInstance(
                        ModSounds.OMINOUS_AMBIENCE.getLocation(),
                        SoundSource.AMBIENT,
                        0.5F, // volume
                        0.9F + Minecraft.getInstance().level.random.nextFloat() * 0.2F, // pitch 0.9–1.1
                        false, 0,
                        SoundInstance.Attenuation.LINEAR,
                        0.0D, 0.0D, 0.0D,
                        false
                );
                manager.play(ambient);

                // Reset timer with new random interval
                ambientSoundTimer = 0;
                ambientSoundInterval = getRandomInterval();
            }
        }
    }

    public static void tick() {
        ticked = true;
        LoopSoundHandler.shouldPlay = true;
    }

    private static int getRandomInterval() {
        Random random = new Random();
        return 600 + random.nextInt(601); // 600–1200 ticks
    }
}

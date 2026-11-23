package xyz.iwolfking.woldsvaults.client.bullet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * A Bullet Bill's flight sound, following it and behaving as PneumaticCraft's jet boots sound: rising in pitch and volume
 * with speed while it flies, and winding down over a second once the Bill stops on the ground or the form ends. The
 * rider hears their own at full strength however far back the camera sits; others hear a giant's from further off.
 * It plays assets/woldsvaults/sounds/bullet_bill/flight.ogg if a mod or resource pack provides one, or else
 * PneumaticCraft's jet hiss, or the elytra's rush of air when that mod isn't loaded.
 */
public class BulletBillSound extends AbstractTickableSoundInstance {
    private static final ResourceLocation CUSTOM_FILE = WoldsVaults.id("sounds/bullet_bill/flight.ogg");
    private static final String CUSTOM_SOUND = WoldsVaults.MOD_ID + ":bullet_bill/flight";
    private static final ResourceLocation JET_HISS = new ResourceLocation("pneumaticcraft", "leaking_gas_low");
    private static final int WIND_DOWN_TICKS = 20;
    private static final int RUNNING = Integer.MAX_VALUE;
    private static final float BASE_VOLUME = 1.0F;

    private final Player player;
    private final ClientBulletBill.Bill bill;
    private final boolean rider;
    private final float carry;
    private float targetPitch = 0.7F;
    private int windDown = RUNNING;

    public BulletBillSound(Player player, ClientBulletBill.Bill bill) {
        super(fallback(), SoundSource.PLAYERS);
        this.player = player;
        this.bill = bill;
        this.rider = player == Minecraft.getInstance().player;
        this.carry = this.rider ? 1.0F : bill.tuning.scale();
        this.looping = true;
        this.delay = 0;
        this.pitch = 0.5F;
        this.volume = BASE_VOLUME * this.carry;
        if (this.rider) {
            this.relative = true;
            this.attenuation = Attenuation.NONE;
        } else {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }
    }

    private static SoundEvent fallback() {
        SoundEvent hiss = ForgeRegistries.SOUND_EVENTS.getValue(JET_HISS);
        return hiss != null ? hiss : SoundEvents.ELYTRA_FLYING;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        if (!Minecraft.getInstance().getResourceManager().hasResource(CUSTOM_FILE)) {
            return super.resolve(manager);
        }
        this.sound = new Sound(CUSTOM_SOUND, 1.0F, 1.0F, 1, Sound.Type.FILE, true, false, 16);
        return new WeighedSoundEvents(this.getLocation(), null);
    }

    @Override
    public void tick() {
        if (!this.player.isAlive()) {
            return;
        }
        boolean flying = !this.bill.ended && this.bill.flying();
        if (this.windDown == RUNNING && (this.bill.ended || (!flying && (this.player.isOnGround() || this.player.isFallFlying())))) {
            this.windDown = WIND_DOWN_TICKS;
        }
        if (this.windDown <= WIND_DOWN_TICKS) {
            if (flying && !this.player.isOnGround()) {
                this.windDown = RUNNING;
            } else {
                this.windDown--;
            }
        }
        if (!this.rider) {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
        if (this.windDown > 0 && this.windDown <= WIND_DOWN_TICKS) {
            this.targetPitch = 0.5F;
            this.volume = Math.max(0.0F, BASE_VOLUME - (WIND_DOWN_TICKS - this.windDown) / 50.0F) * this.carry;
        } else if (flying) {
            float speed = (float) Math.sqrt(this.player.distanceToSqr(this.player.xo, this.player.yo, this.player.zo));
            this.targetPitch = 0.9F + speed / 15.0F;
            this.volume = (BASE_VOLUME + speed / 15.0F) * this.carry;
        } else {
            this.targetPitch = 0.9F;
            this.volume = BASE_VOLUME * 0.8F * this.carry;
        }
        this.pitch += (this.targetPitch - this.pitch) / 10.0F;
        if (this.player.isInWater()) {
            this.pitch *= 0.75F;
            this.volume *= 0.5F;
        }
    }

    @Override
    public boolean isStopped() {
        return !this.player.isAlive() || this.windDown <= 0;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}

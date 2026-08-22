package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.init.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Set;
import java.util.UUID;

/**
 * The effect-piercing aura carried by Vault Champions and greed assassins.
 *
 * <p>Standing inside it means harmful effects land whatever the player is wearing. Two entirely
 * separate systems have to be beaten for that to be true. Gear effect avoidance and effect immunity
 * run through {@code GearAttributeEvents#avoidPotionEffect}, and the Shell of the Carapace trinket
 * has its own blanket refusal in {@code DamageImmunityTrinket}. Both are
 * {@code PotionApplicableEvent} listeners at default priority that set the result to DENY, so a
 * single listener at {@code LOWEST} setting ALLOW overrides both regardless of which registered
 * first - the result is only read once every listener has run.
 *
 * <p>There is precedent in the base mod for exactly this: avoidance is switched off wholesale inside
 * rebirth vaults, which is the arena the Vessel normally fights in. The aura is that idea made
 * local, and extended to cover the trinket.
 *
 * <p>Time Acceleration is exempt. It is a harmful effect by category but what it actually does is
 * speed up the vault clock, and letting a hunting boss strip a player's protection against a timer
 * penalty would be a different mechanic than the one being asked for.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VaultChampionAura {
    private static volatile Set<UUID> members = Set.of();

    private VaultChampionAura() {
    }

    static void setMembers(Set<UUID> next) {
        members = next == null ? Set.of() : Set.copyOf(next);
    }

    public static boolean contains(ServerPlayer player) {
        return player != null && members.contains(player.getUUID());
    }

    public static boolean contains(UUID playerId) {
        return playerId != null && members.contains(playerId);
    }

    public static boolean isEmpty() {
        return members.isEmpty();
    }

    static void clear() {
        members = Set.of();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pierceEffectAvoidance(PotionEvent.PotionApplicableEvent event) {
        if (members.isEmpty() || !(event.getEntityLiving() instanceof ServerPlayer player)) {
            return;
        }
        MobEffect effect = event.getPotionEffect().getEffect();
        if (effect.isBeneficial() || effect == ModEffects.TIMER_ACCELERATION) {
            return;
        }
        if (contains(player)) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}

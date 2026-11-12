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
 * The effect-piercing aura carried by Vault Champions and greed assassins: inside it, harmful effects land
 * whatever the player wears. A {@code LOWEST}-priority listener setting ALLOW overrides both gear effect
 * avoidance and the Shell of the Carapace trinket's refusal. Time Acceleration is exempt.
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

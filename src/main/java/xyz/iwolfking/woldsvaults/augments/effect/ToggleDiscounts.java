package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.skill.ability.effect.spi.core.ToggleManaAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.talent.type.EtherealTalent;
import iskallia.vault.util.calc.ManaCostHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Discount on a toggle a recipe switched on: VH drains the toggle's mana every tick, so while it stays on the share
 * of that per-tick cost is refunded. Ends when the toggle goes off by any means, or the player logs out.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ToggleDiscounts {
    private static final Map<UUID, Map<String, Float>> ACTIVE = new ConcurrentHashMap<>();

    private ToggleDiscounts() {
    }

    public static void start(ServerPlayer player, String abilityId, float fraction) {
        ACTIVE.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(abilityId, fraction);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Map<String, Float> discounts = ACTIVE.get(player.getUUID());
        if (discounts == null) {
            return;
        }
        discounts.entrySet().removeIf(entry -> !refundTick(player, entry.getKey(), entry.getValue()));
        if (discounts.isEmpty()) {
            ACTIVE.remove(player.getUUID());
        }
    }

    private static boolean refundTick(ServerPlayer player, String abilityId, float fraction) {
        Optional<ToggleManaAbility> toggle = AugmentAbility.serverTree(player).getForId(abilityId)
                .filter(TieredSkill.class::isInstance)
                .map(TieredSkill.class::cast)
                .filter(TieredSkill::isUnlocked)
                .map(TieredSkill::getChild)
                .filter(ToggleManaAbility.class::isInstance)
                .map(ToggleManaAbility.class::cast)
                .filter(ToggleManaAbility::isActive);
        if (toggle.isEmpty()) {
            return false;
        }
        if (!player.isCreative()) {
            float perTick = ManaCostHelper.adjustManaCost(player, toggle.get(), toggle.get().getManaCostPerSecond() / 20.0F);
            ManaRefunds.refund(player, perTick * paidShare(player) * fraction);
        }
        return true;
    }

    private static float paidShare(ServerPlayer player) {
        float share = 1.0F;
        for (EtherealTalent talent : PlayerTalentsData.get(player.getLevel()).getTalents(player)
                .getAll(EtherealTalent.class, Skill::isUnlocked)) {
            share *= 1.0F - Mth.clamp(talent.getChance(), 0.0F, 1.0F);
        }
        return share;
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }
}

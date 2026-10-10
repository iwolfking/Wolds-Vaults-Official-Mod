package xyz.iwolfking.woldsvaults.api.util;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityStunnedEvent;
import iskallia.vault.skill.talent.type.LightningDamageTalent;
import iskallia.vault.skill.talent.type.LightningStunTalent;
import iskallia.vault.util.EffectInstances;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class LightningTalentHelper {
    public static float getMinimumDamageBonus(ServerPlayer player) {
        return TalentHelper.getTalent(player, LightningDamageTalent.class).map(LightningDamageTalent::getIncreasedDamageDealtPercentage).orElse(0F);
    }

    public static Pair<Float, Float> getStunChance(ServerPlayer player) {
        return TalentHelper.getTalent(player, LightningStunTalent.class).map((talent) -> Pair.of(talent.getStunChance(), talent.getStunDuration())).orElse(Pair.of(0F, 0F));
    }

    public static void tryToApplyStun(ServerPlayer player, LivingEntity target, Pair<Float, Float> stunChanceAndDuration) {
        if (player.level.random.nextFloat() < stunChanceAndDuration.getFirst()) {
            target.addEffect(EffectInstances.stunning((int)(stunChanceAndDuration.getSecond() * 20.0F), 0));
            CommonEvents.ENTITY_STUNNED.invoke(new EntityStunnedEvent.Data(player, target));
        }
    }
}

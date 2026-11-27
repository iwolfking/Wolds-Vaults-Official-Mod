package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.ability.effect.ChargedBoltsAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(value = ChargedBoltsAbility.class, remap = false)
public interface ChargedBoltsAbilityInvoker {
    @Invoker("hitTarget")
    void woldsvaults$hitTarget(ServerPlayer player, LivingEntity target, Map<Integer, Integer> targetHitCounts);

    @Invoker("getSpreadOffset")
    double woldsvaults$getSpreadOffset(int index, int bolts);

    @Invoker("isBlocked")
    static boolean woldsvaults$isBlocked(ServerPlayer player, Vec3 previous, Vec3 current) {
        throw new AssertionError();
    }

    @Invoker("canHit")
    static boolean woldsvaults$canHit(ServerPlayer player, LivingEntity entity) {
        throw new AssertionError();
    }

    @Invoker("distanceToSegment")
    static double woldsvaults$distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        throw new AssertionError();
    }

    @Invoker("rotateY")
    static Vec3 woldsvaults$rotateY(Vec3 vector, double radians) {
        throw new AssertionError();
    }

    @Invoker("trail")
    static void woldsvaults$trail(ServerLevel level, Vec3 pos, Vec3 direction) {
        throw new AssertionError();
    }

    @Invoker("spark")
    static void woldsvaults$spark(ServerLevel level, Vec3 pos, float size) {
        throw new AssertionError();
    }
}

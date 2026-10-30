package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.gear.trinket.TrinketHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class SpeedCapEvents {
    private static final UUID SPEED_CAP_MODIFIER_UUID = UUID.nameUUIDFromBytes("woldsvaults:weighted_boots_speed_cap".getBytes(StandardCharsets.UTF_8));
    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final double BASE_SPEED = 0.1D;
    private static final double EPSILON = 1.0E-5D;

    /**
     * Applies the Weighted Boots speed cap as a MULTIPLY_TOTAL modifier on the final movement speed attribute value,
     * so it clamps after every other buff and debuff. The vanilla sprint modifier is excluded from the capped value,
     * meaning sprinting multiplies on top of the cap exactly as it would for a player whose natural speed equals the cap.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) {
            return;
        }
        AttributeModifier existing = speedAttribute.getModifier(SPEED_CAP_MODIFIER_UUID);
        int capPercent = getActiveCapPercent(player);
        if (capPercent <= 0) {
            if (existing != null) {
                speedAttribute.removeModifier(SPEED_CAP_MODIFIER_UUID);
            }
            return;
        }
        double uncapped = speedAttribute.getValue();
        if (existing != null) {
            uncapped /= 1.0D + existing.getAmount();
        }
        AttributeModifier sprint = speedAttribute.getModifier(SPRINT_MODIFIER_UUID);
        double nominal = sprint != null ? uncapped / (1.0D + sprint.getAmount()) : uncapped;
        double capValue = BASE_SPEED * capPercent / 100.0D;
        if (nominal <= 0.0D || nominal <= capValue + EPSILON) {
            if (existing != null) {
                speedAttribute.removeModifier(SPEED_CAP_MODIFIER_UUID);
            }
            return;
        }
        double amount = capValue / nominal - 1.0D;
        if (existing != null) {
            if (Math.abs(existing.getAmount() - amount) < EPSILON) {
                return;
            }
            speedAttribute.removeModifier(SPEED_CAP_MODIFIER_UUID);
        }
        speedAttribute.addTransientModifier(new AttributeModifier(SPEED_CAP_MODIFIER_UUID, "WoldsWeightedBootsSpeedCap", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static int getActiveCapPercent(ServerPlayer player) {
        int cap = 0;
        for (TrinketHelper.TrinketStack<SpeedLimitTrinketEffect> trinketStack : TrinketHelper.getTrinkets(player, SpeedLimitTrinketEffect.class)) {
            if (trinketStack.stack() == null || !trinketStack.isUsable(player)) {
                continue;
            }
            int stackCap = SpeedLimitTrinketEffect.getCapPercent(trinketStack.stack());
            if (stackCap <= 0) {
                continue;
            }
            if (cap == 0 || stackCap < cap) {
                cap = stackCap;
            }
        }
        return cap;
    }
}

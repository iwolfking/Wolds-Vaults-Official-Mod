package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.ThornsHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Thorns scaling and ability power scaling: flat damage added to normal melee hits after Damage Increase,
 * the damage multiplier registry and every other NORMAL-priority multiplier, and before every LOW and
 * LOWEST one (god nodes, the global god product, Trial Mastery, Rampage, lucky hits, Vulnerable).
 *
 * <p>Forge has no priority between NORMAL and LOW, and listeners sharing a priority run in an order fixed
 * only at launch. So the stage listens at LOW itself and every LOW-priority outgoing multiplier calls
 * {@link #apply} first; whichever runs first adds the damage, and the event is remembered so it is never
 * added twice.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class PostMultiplierFlatAdds {
    private static final Set<LivingHurtEvent> APPLIED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private PostMultiplierFlatAdds() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        apply(event);
    }

    /**
     * Adds thorns scaling and ability power scaling to this hit once. Safe to call from any LOW or LOWEST
     * handler before it multiplies the amount.
     */
    public static void apply(LivingHurtEvent event) {
        if (ChainScope.active()) {
            return;
        }
        if (event.isCanceled() || !APPLIED.add(event)) {
            return;
        }
        if (!WoldEventHelper.isNormalAttack() || WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet() || event.getSource().isProjectile()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player) || !(player.getMainHandItem().getItem() instanceof VaultGearItem)) {
            return;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float added = 0.0F;
        float thornsScaling = snapshot.getAttributeValue(ModGearAttributes.THORNS_SCALING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
        if (thornsScaling > 0.0F) {
            added += ThornsHelper.getAdditionalThornsFlatDamage(player) * thornsScaling;
        }
        float apScaling = snapshot.getAttributeValue(ModGearAttributes.AP_SCALING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
        if (apScaling > 0.0F) {
            added += WoldAttributeHelper.getAdditionalAbilityPower(player) * apScaling;
        }
        if (added > 0.0F) {
            event.setAmount(event.getAmount() + added);
        }
    }
}

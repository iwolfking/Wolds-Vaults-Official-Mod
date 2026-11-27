package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/**
 * Thorns %: multiplies flat thorns by {@code 1 + the summed Thorns % attribute}, on every read of flat
 * thorns, the way Ability Power % multiplies ability power.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ThornsPercentStat {
    private static final Object LISTENER_REF = new Object();
    private static final int MULTIPLICATIVE_PRIORITY = -500;

    private ThornsPercentStat() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CommonEvents.PLAYER_STAT.of(PlayerStat.THORNS_DAMAGE_FLAT)
                .register(LISTENER_REF, ThornsPercentStat::onThornsFlat, MULTIPLICATIVE_PRIORITY));
    }

    private static void onThornsFlat(PlayerStatEvent.Data data) {
        LivingEntity entity = data.getEntity();
        if (entity == null) {
            return;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        float percent = snapshot.getAttributeValue(ModGearAttributes.THORNS_FLAT_PERCENT, VaultGearAttributeTypeMerger.floatSum());
        if (percent != 0.0F) {
            data.setValue(data.getValue() * (1.0F + percent));
        }
    }
}

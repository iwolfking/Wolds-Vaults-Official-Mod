package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModEffects;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * The hyperboss takes no %-max-health damage-over-time: with hyper-scaled health pools a
 * single Bleed tick (amplifier × 2.5% of max health, see MixinBleedEffect) deals billions.
 * Bleed is denied at application here; Reaving's on-hit proc is neutralized by pre-applying
 * its own once-per-mob latch effect in {@link HyperBossManager}.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperBossEffectImmunity {
    private HyperBossEffectImmunity() {
    }

    @SubscribeEvent
    public static void denyBleedOnHyperboss(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.BLEED) {
            return;
        }
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof VaultBossEntity)) {
            return;
        }
        boolean hyper = ServerVaults.get(entity.level)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
        if (hyper) {
            event.setResult(Event.Result.DENY);
        }
    }
}

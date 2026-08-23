package xyz.iwolfking.woldsvaults.gods.charms;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/** Mythic charm suffix: adds whole-percent stacks of {@code woldsvaults:omega_cascading} on vault join. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OmegaCascading {
    public static final ResourceLocation MODIFIER_ID = WoldsVaults.id("omega_cascading");

    private OmegaCascading() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(OmegaCascading::register);
    }

    private static void register() {
        CommonEvents.LISTENER_JOIN.register(OmegaCascading.class, data -> {
            Vault vault = data.getVault();
            ServerPlayer player = data.getListener().getPlayer().orElse(null);
            if (vault == null || player == null) {
                return;
            }
            float percent = AttributeSnapshotHelper.getInstance().getSnapshot(player)
                    .getAttributeValue(ModGearAttributes.OMEGA_CASCADING, VaultGearAttributeTypeMerger.floatSum());
            int stacks = Math.round(percent * 100.0F);
            if (stacks <= 0) {
                return;
            }
            VaultModifier<?> modifier = VaultModifierRegistry.getOpt(MODIFIER_ID).orElse(null);
            if (modifier == null) {
                WoldsVaults.LOGGER.error("Omega Cascading modifier {} is not registered; the charm suffix does nothing. "
                        + "Check vault_modifiers.json.", MODIFIER_ID);
                return;
            }
            ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(modifier, stacks, true, JavaRandom.ofNanoTime());
        });
    }
}

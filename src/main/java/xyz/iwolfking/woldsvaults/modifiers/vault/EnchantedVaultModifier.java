package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperModifierPolicy;
import java.util.List;

public class EnchantedVaultModifier extends VaultModifier<EnchantedVaultModifier.Properties> {

    public EnchantedVaultModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.PLAYER_TICK.register(context.getUUID(), EventPriority.HIGHEST, (event) -> {
            if(event.side.isServer()) {
                if(!((event.player.tickCount % this.properties.getTicksPerCheck()) == 0)) {
                    return;
                }

                if(!(event.player.getLevel().dimension().equals(world.dimension()))) {
                    return;
                }

                if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
                    return;
                }

                if(event.player.getRandom().nextDouble() < this.properties.getChance()) {
                    VaultEvent vaultEvent;
                    if(!this.properties.eventTags.isEmpty()) {
                        vaultEvent = EnchantedEventsRegistry.getEventsWithTags(this.properties.eventTags).getRandom().orElse(null);
                    }
                    else {
                        vaultEvent = EnchantedEventsRegistry.getEvents().getRandom().orElse(null);
                    }

                    if(vaultEvent == null) {
                        return;
                    }

                    if(!vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty()
                            && HyperModifierPolicy.isBannedEnchantedEvent(vaultEvent.getId())) {
                        WoldsVaults.LOGGER.info("Skipped enchanted event {} — it is banned in Hyper vaults.", vaultEvent.getId());
                        return;
                    }
                    vaultEvent.triggerEvent(event.player::getOnPos, (ServerPlayer) event.player, vault, false, VaultEvent.EventDisplayType.LEGACY);
                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final double chance;
        @Expose
        private final int ticksPerCheck;
        @Expose
        private final List<EventTag> eventTags;

        public Properties(double chance, int ticksPerCheck) {
            this.chance = chance;
            this.ticksPerCheck = ticksPerCheck;
            this.eventTags = List.of();
        }

        public Properties(double chance, int ticksPerCheck, List<EventTag> tags) {
            this.chance = chance;
            this.ticksPerCheck = ticksPerCheck;
            this.eventTags = tags;
        }



        public double getChance() {
            return this.chance;
        }
        public int getTicksPerCheck() {
            return this.ticksPerCheck;
        }

    }
}

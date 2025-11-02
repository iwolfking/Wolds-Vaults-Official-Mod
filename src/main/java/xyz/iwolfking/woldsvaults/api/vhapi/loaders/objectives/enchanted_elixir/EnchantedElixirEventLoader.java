package xyz.iwolfking.woldsvaults.api.vhapi.loaders.objectives.enchanted_elixir;

import iskallia.vault.util.data.WeightedList;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.woldsvaults.api.vhapi.loaders.objectives.enchanted_elixir.config.EnchantedElixirEventConfig;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.VaultModifierVaultEvent;

public class EnchantedElixirEventLoader extends VaultConfigProcessor<EnchantedElixirEventConfig> {
    public EnchantedElixirEventLoader() {
        super(new EnchantedElixirEventConfig(), "enchanted_elixir_events");
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {
        for(EnchantedElixirEventConfig config : CUSTOM_CONFIGS.values()) {
            for (WeightedList.Entry<VaultModifierVaultEvent> modifierEnchantedEventEntry : config.MODIFIER_ENCHANTED_EVENTS) {
                EnchantedEventsRegistry.register(modifierEnchantedEventEntry.value, ((double)modifierEnchantedEventEntry.weight), modifierEnchantedEventEntry.value.isOmega, modifierEnchantedEventEntry.value.isPositive);
            }
        }
        this.CUSTOM_CONFIGS.clear();
    }
}

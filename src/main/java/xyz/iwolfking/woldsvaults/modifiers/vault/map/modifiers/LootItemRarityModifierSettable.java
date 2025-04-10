package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.LootGenerationEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.loot.generator.LootGenerator;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.Optional;
import java.util.UUID;

public class LootItemRarityModifierSettable extends SettableValueVaultModifier<SettableValueVaultModifier.Properties> {
    public LootItemRarityModifierSettable(ResourceLocation id, SettableValueVaultModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> {
            return t.formatted((int)Math.abs(p.getValue() * (double)s * 100.0));
        });
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.LOOT_GENERATION.pre().register(context.getUUID(), (data) -> {
            this.getGenerator(vault, data, context).ifPresent((generator) -> {
                generator.itemRarity = (float)((double)generator.itemRarity + ((SettableValueVaultModifier.Properties)this.properties).getValue());
            });
        });
        CommonEvents.LOOT_GENERATION.post().register(context.getUUID(), (data) -> {
            this.getGenerator(vault, data, context).ifPresent((generator) -> {
                generator.itemRarity = (float)((double)generator.itemRarity - ((SettableValueVaultModifier.Properties)this.properties).getValue());
            });
        });
    }

    public Optional<TieredLootTableGenerator> getGenerator(Vault vault, LootGenerationEvent.Data data, ModifierContext context) {
        LootGenerator var5 = data.getGenerator();
        if (var5 instanceof TieredLootTableGenerator generator) {
            if (generator.getSource() == null) {
                return Optional.empty();
            } else {
                UUID uuid = generator.getSource().getUUID();
                if (!((Listeners)vault.get(Vault.LISTENERS)).contains(uuid)) {
                    return Optional.empty();
                } else {
                    return context.hasTarget() && !context.getTarget().equals(uuid) ? Optional.empty() : Optional.of(generator);
                }
            }
        } else {
            return Optional.empty();
        }
    }
}

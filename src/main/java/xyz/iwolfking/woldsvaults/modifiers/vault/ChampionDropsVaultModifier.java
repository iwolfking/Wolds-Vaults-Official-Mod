package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;

public class ChampionDropsVaultModifier extends VaultModifier<ChampionDropsVaultModifier.Properties>{
    public ChampionDropsVaultModifier(ResourceLocation id, ChampionDropsVaultModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> t.formatted((int)(p.getAddend() * s * 100.0F)));
    }

    public static class Properties {
        @Expose
        private final float addend;

        public Properties(float percent) {
            this.addend = percent;
        }

        public float getAddend() {
            return this.addend;
        }
    }
}

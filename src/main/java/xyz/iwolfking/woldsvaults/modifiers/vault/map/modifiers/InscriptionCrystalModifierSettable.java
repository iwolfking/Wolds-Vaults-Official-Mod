package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.item.data.InscriptionData;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class InscriptionCrystalModifierSettable extends SettableValueVaultModifier<InscriptionCrystalModifierSettable.Properties> {

    public InscriptionCrystalModifierSettable(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private ResourceLocation pool;

        @Expose
        private int color;

        public Properties(ResourceLocation pool, int color) {
            this.pool = pool;
            this.color = color;
        }

        public ResourceLocation getPool() {
            return this.pool;
        }


        public String getDisplayName() {
            TemplatePoolKey templatePool = VaultRegistry.TEMPLATE_POOL.getKey(pool);
            return templatePool.getName();
        }

        public InscriptionData getData() {
            InscriptionData data = InscriptionData.empty();
            int roomCount = (int)getValue();
            data.add(pool, roomCount, color);
            return data;
        }
    }
}

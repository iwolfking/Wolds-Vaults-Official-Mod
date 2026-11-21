package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;

//TODO: Reimplement this for Brazier changes?
public class BrazierPoolsModifier extends VaultModifier<BrazierPoolsModifier.Properties> {

    public BrazierPoolsModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

//    @Override
//    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
//        CommonEvents.MONOLITH_UPDATE.register(context.getUUID(), (event) -> {
//            if(!event.getEntity().isGenerated()) {
//                if(event.getEntity().getLevel() != null && world.getLevel().dimension().equals(event.getEntity().getLevel().dimension())) {
//                    event.getEntity().removeModifiers();
//                    ModConfigs.VAULT_MODIFIER_POOLS.getRandom(this.properties.getId(), vault.get(Vault.LEVEL).getOr(VaultLevel.VALUE, 0), ChunkRandom.ofNanoTime()).forEach(event.getEntity()::addModifier);
//                }
//            }
//        }, 15000);
//    }

    public static class Properties {
        @Expose
        private final ResourceLocation id;

        public Properties(ResourceLocation id) {
            this.id = id;
        }

        public ResourceLocation getId() {
            return this.id;
        }
    }
}

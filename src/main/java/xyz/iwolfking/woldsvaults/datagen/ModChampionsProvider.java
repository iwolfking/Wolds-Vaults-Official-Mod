package xyz.iwolfking.woldsvaults.datagen;

import com.infamous.dungeons_mobs.DungeonsMobs;
import iskallia.vault.config.ChampionsConfig;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.data.entity.PartialEntity;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractChampionProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModChampionsProvider extends AbstractChampionProvider {
    protected ModChampionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_champs_config", builder -> {
            builder.addChampionChance(EntityPredicate.of(WoldsVaults.id("purple_ghost").toString(), false).orElse(EntityPredicate.FALSE), 1.0F);

            builder.addChampionChance(EntityPredicate.of("dungeons_mobs:quick_growing_vine", false).orElse(EntityPredicate.FALSE), -10.0F);
            builder.addChampionChance(EntityPredicate.of("dungeons_mobs:poison_quill_vine", false).orElse(EntityPredicate.FALSE), -10.0F);
            builder.addEntityAttributeOverride(EntityPredicate.of(WoldsVaults.id("purple_ghost").toString(), false).orElse(EntityPredicate.FALSE), attributeOverrideBasicListBuilder -> {
                attributeOverrideBasicListBuilder.add(new ChampionsConfig.AttributeOverride("minecraft:generic.max_health", 5.0, "multiply"));
                attributeOverrideBasicListBuilder.add(new ChampionsConfig.AttributeOverride("minecraft:generic.attack_damage", 2.0, "multiply"));
            });
        });
    }
}

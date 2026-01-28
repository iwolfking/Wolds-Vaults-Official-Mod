package xyz.iwolfking.woldsvaults.integration.dungeon_mobs;

import com.infamous.dungeons_mobs.mod.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.ModList;

public class DungeonMobsBellHighlighting {

    private static final boolean IS_LOADED = ModList.get().isLoaded("dungeons_mobs");
    public static boolean shouldHighlight(EntityType<?> entityType) {
        if (IS_LOADED) {
            return Checker.shouldHighlight(entityType);
        }
        return false;
    }


    // this class won't be loaded if dungeons mobs mod is not present
    private static class Checker {
        private static boolean shouldHighlight(EntityType<?> entityType) {
            return entityType == ModEntityTypes.MAGE.get() || entityType == ModEntityTypes.ILLUSIONER.get();
        }
    }
}

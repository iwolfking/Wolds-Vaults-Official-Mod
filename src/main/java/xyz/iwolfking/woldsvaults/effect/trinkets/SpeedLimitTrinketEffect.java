package xyz.iwolfking.woldsvaults.effect.trinkets;

import iskallia.vault.gear.trinket.TrinketEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SpeedLimitTrinketEffect extends TrinketEffect.Simple {
    public static final String SPEED_CAP_TAG = "woldsvaults:speed_cap";
    public static final int MAX_CAP_PERCENT = 100000;

    public SpeedLimitTrinketEffect(ResourceLocation name) {
        super(name);
    }

    /**
     * Reads the configured speed cap from the trinket stack, in percent of base player speed (0.1). 0 means uncapped.
     */
    public static int getCapPercent(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SPEED_CAP_TAG, Tag.TAG_INT)) {
            return 0;
        }
        return sanitizeCapPercent(tag.getInt(SPEED_CAP_TAG));
    }

    public static void setCapPercent(ItemStack stack, int capPercent) {
        stack.getOrCreateTag().putInt(SPEED_CAP_TAG, sanitizeCapPercent(capPercent));
    }

    public static int sanitizeCapPercent(int capPercent) {
        return Math.max(0, Math.min(capPercent, MAX_CAP_PERCENT));
    }
}

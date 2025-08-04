package xyz.iwolfking.woldsvaults.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum ExtendedToolType {
    NONE("none"),
    RAKER("raker"),
    RAVAGER("ravager");
    private final String id;
    private final String description;
    private final ExtendedToolType[] parents;
    private final int packed;

    ExtendedToolType(String id, ExtendedToolType... parents) {
        this.id = id;
        this.description = Util.makeDescriptionId("item", VaultMod.id("tool." + id));
        this.parents = parents;
        this.packed = parents.length == 0 ? 1 << this.ordinal() : Arrays.stream(this.parents).mapToInt((value) -> 1 << value.ordinal()).sum();
    }

    public String getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public ExtendedToolType[] getParents() {
        return this.parents;
    }

    public int getPacked() {
        return this.packed;
    }

    public static @Nullable ExtendedToolType of(ItemStack stack) {
        GearDataCache data = GearDataCache.of(stack);
        boolean picking = data.hasAttribute(ModGearAttributes.PICKING);
        boolean axing = data.hasAttribute(ModGearAttributes.AXING);
        boolean shoveling = data.hasAttribute(ModGearAttributes.SHOVELLING);
        boolean reaping = data.hasAttribute(ModGearAttributes.REAPING);
        boolean hammering = data.hasAttribute(ModGearAttributes.HAMMERING);
        if(!picking && !axing && !shoveling && !reaping && !hammering) {
            return NONE;
        }
        else if(hammering && reaping && (axing || picking || shoveling)) {
            return RAVAGER;
        }
        else if(hammering && reaping) {
            return RAKER;
        }
        else {
            return null;
        }
    }

    public boolean has(ExtendedToolType type) {
        return this == type || Arrays.stream(this.parents).anyMatch((toolType) -> {
            return toolType == type;
        });
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.UniqueGearConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(value = UniqueGearConfig.Entry.class, remap = false)
public interface UniqueGearConfigEntryAccessor {
    @Accessor("item")
    void setItem(Item item);

    @Accessor("lore")
    void setLore(String lore);

    @Accessor("modifierIdentifiers")
    void setModifierIdentifiers(Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers);
}

package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import com.radimous.vhatcaniroll.logic.Items;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(value = Items.class, remap = false)
public abstract class MixinItems {
    @Shadow
    public static ItemStack withTransmog(ItemStack stack, ResourceLocation transmog) {
        return null;
    }

    /**
     * @author iwolfking
     * @reason Add map
     */
    @Overwrite
    public static List<ItemStack> getWoldGearItems() {
        List<ItemStack> woldItems = new ArrayList<>();
        List<Pair<String, String>> woldItemFields = Arrays.asList(Pair.of("BATTLESTAFF", "the_vault:gear/battlestaff/battlestaff_redstone"), Pair.of("TRIDENT", "the_vault:gear/trident/orange"), Pair.of("PLUSHIE", "the_vault:gear/plushie/hrry"), Pair.of("LOOT_SACK", "the_vault:gear/loot_sack/bundle"), Pair.of("RANG", "the_vault:gear/rang/wooden"), Pair.of("MAP", "the_vault:gear/map/common"));

        try {
            Class<?> woldItemClass = Class.forName("xyz.iwolfking.woldsvaults.init.ModItems");

            for (Pair<String, String> woldFieldTransmogs : woldItemFields) {
                try {
                    String woldFieldName = (String) woldFieldTransmogs.getLeft();
                    Item item = (Item) woldItemClass.getField(woldFieldName).get((Object) null);
                    woldItems.add(withTransmog(new ItemStack(item), new ResourceLocation((String) woldFieldTransmogs.getRight())));
                } catch (SecurityException | NoSuchFieldException | IllegalAccessException |
                         IllegalArgumentException var7) {
                }
            }
        } catch (ClassNotFoundException var8) {
        }
        ItemStack etchingStack = new ItemStack(ModItems.ETCHING);
        AttributeGearData data = AttributeGearData.read(etchingStack);
        data.createOrReplaceAttributeValue(ModGearAttributes.STATE, VaultGearState.UNIDENTIFIED);
        data.write(etchingStack);
        woldItems.add(etchingStack);
        return woldItems;
    }
}

package xyz.iwolfking.woldsvaults.api.gear.actions.modifications;

import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Random;

public class MapTierModification extends GearModification {
    public MapTierModification() {
        super(WoldsVaults.id("reforge_map_tier"));
    }

    @Override
    public ItemStack getDisplayStack() {
        return ModItems.INSCRIBING_FOCUS.getDefaultInstance();
    }

    @Override
    public Result doModification(ItemStack itemStack, ItemStack itemStack1, Player player, Random random) {
        return WoldGearModifierHelper.increaseMapTier(itemStack);
    }
}

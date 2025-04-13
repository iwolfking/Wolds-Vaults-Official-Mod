package xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual;

import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Random;

public class BlazingGearModification extends GearModification {
    public BlazingGearModification() {
        super(WoldsVaults.id("unfreeze_all"));
    }

    @Override
    public ItemStack getDisplayStack() {
        return ModItems.BLAZING_FOCUS.getDefaultInstance();
    }

    @Override
    public Result doModification(ItemStack itemStack, ItemStack itemStack1, Player player, Random random) {
        return WoldGearModifierHelper.unfreezeAll(itemStack);
    }
}

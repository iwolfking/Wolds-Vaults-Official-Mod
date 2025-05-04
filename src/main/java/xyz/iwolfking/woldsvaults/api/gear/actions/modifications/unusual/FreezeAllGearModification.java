package xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual;

import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Random;

public class FreezeAllGearModification extends GearModification {
    public FreezeAllGearModification() {
        super(WoldsVaults.id("freeze_all"));
    }

    @Override
    public ItemStack getDisplayStack() {
        return ModItems.SUSPENSION_FOCUS.getDefaultInstance();
    }

    @Override
    public Result doModification(ItemStack itemStack, ItemStack itemStack1, Player player, Random random) {
        return WoldGearModifierHelper.freezeGoodModifier(itemStack);
    }
}

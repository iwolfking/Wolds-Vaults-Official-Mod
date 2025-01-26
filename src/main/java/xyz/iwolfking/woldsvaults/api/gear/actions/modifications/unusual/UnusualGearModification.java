package xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Random;

public class UnusualGearModification extends GearModification {
    public UnusualGearModification() {
        super(VaultMod.id("add_unusual_modifier"));
    }

    public ItemStack getDisplayStack() {
        return new ItemStack(ModItems.ECCENTRIC_FOCUS);
    }

    public GearModification.Result doModification(ItemStack stack, ItemStack materialStack, Player player, Random rand) {
        return WoldGearModifierHelper.addUnusualModifier(stack, player.getLevel().getGameTime(), rand);
    }
}

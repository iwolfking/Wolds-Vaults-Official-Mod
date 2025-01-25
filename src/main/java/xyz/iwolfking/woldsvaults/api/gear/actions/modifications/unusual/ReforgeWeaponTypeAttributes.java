package xyz.iwolfking.woldsvaults.api.gear.actions.modifications.unusual;

import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.modification.GearModification;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Random;

public class ReforgeWeaponTypeAttributes extends GearModification {
    public ReforgeWeaponTypeAttributes() {
        super(WoldsVaults.id("reforge_weapon_type"));
    }

    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(ModItems.WEAPON_TYPE_FOCUS);
    }

    @Override
    public Result doModification(ItemStack itemStack, ItemStack itemStack1, Player player, Random random) {
        return VaultGearModifierHelper.reForgeAllModifiersTagged(itemStack, random, "weaponType");
    }
}

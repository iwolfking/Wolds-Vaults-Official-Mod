package xyz.iwolfking.woldsvaults.integration.vhatcaniroll;


import com.mojang.datafixers.util.Pair;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VHCIRWoldsItems {

    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {

        Pair<Integer, List<ItemStack>> items = Pair.of(0, List.of(
            withTransmog(ModItems.BATTLESTAFF, VaultMod.id("gear/battlestaff/battlestaff_redstone")),
            withTransmog(ModItems.TRIDENT, VaultMod.id("gear/trident/orange")),
            withTransmog(ModItems.PLUSHIE, VaultMod.id("gear/plushie/hrry")),
            withTransmog(ModItems.LOOT_SACK, VaultMod.id("gear/loot_sack/bundle")),
            withTransmog(ModItems.RANG, VaultMod.id("gear/rang/wooden")),
            withTransmog(ModItems.MAP, VaultMod.id("gear/map/common_map")),
            withTransmog(ModItems.SCEPTER, VaultMod.id("gear/scepter/dull_scepter")),
            withTransmog(ModItems.DAGGER_MAIN, VaultMod.id("gear/dagger/scrappy/backstabber")),
            withTransmog(ModItems.DAGGER_SUB, VaultMod.id("gear/dagger/common/trusty_dagger")),
            withTransmog(ModItems.VAULT_ROD, VaultMod.id("gear/rod/vanilla"))
        ));

        InterModComms.sendTo(
            "vhatcaniroll",
            "register_items",
            () -> items
        );
    }
    private static ItemStack withTransmog(Item stack, ResourceLocation transmog) {
        return withTransmog(new ItemStack(stack), transmog);
    }
    private static ItemStack withTransmog(ItemStack stack, ResourceLocation transmog) {
        ItemStack displayStack = stack.copy();
        VaultGearData gearData = VaultGearData.read(displayStack);
        gearData.setState(VaultGearState.IDENTIFIED);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, transmog);
        gearData.write(displayStack);
        return displayStack;
    }
}

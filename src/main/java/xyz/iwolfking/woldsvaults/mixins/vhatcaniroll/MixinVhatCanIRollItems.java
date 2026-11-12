package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vhatcaniroll")
        }
)
@Mixin(targets = "com.radimous.vhatcaniroll.logic.Items", remap = false)
public abstract class MixinVhatCanIRollItems {

    @Shadow
    public static List<ItemStack> getVHGearItems() {
        throw new AssertionError();
    }

    /**
     * Replaces VHat Can I Roll's fixed list of addon gear with every registered vault gear item that has a
     * gear modifier config, so new Wold's Vaults gear types show up without a VHat Can I Roll update.
     */
    @Inject(method = "getWoldGearItems", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$registryGearItems(CallbackInfoReturnable<List<ItemStack>> cir) {
        Set<Item> listed = new HashSet<>();
        for (ItemStack stack : getVHGearItems()) {
            listed.add(stack.getItem());
        }
        List<ItemStack> gear = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!(item instanceof VaultGearItem) || listed.contains(item)) {
                continue;
            }
            ResourceLocation id = item.getRegistryName();
            if (id == null || !ModConfigs.VAULT_GEAR_CONFIG.containsKey(id)) {
                continue;
            }
            gear.add(woldsvaults$displayStack(item, id));
        }
        cir.setReturnValue(gear);
    }

    private static ItemStack woldsvaults$displayStack(Item item, ResourceLocation id) {
        ItemStack stack = new ItemStack(item);
        VaultGearData data = VaultGearData.read(stack);
        data.setState(VaultGearState.IDENTIFIED);
        Optional<ResourceLocation> model = ModDynamicModels.REGISTRIES.getAssociatedRegistry(item)
                .flatMap(registry -> registry.getIds().stream().min(Comparator.comparing(ResourceLocation::toString)));
        if (model.isPresent()) {
            data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, model.get());
        } else {
            WoldLogger.warn("VHat Can I Roll gear list: no dynamic model registered for {}, showing it without a transmog", id);
        }
        data.write(stack);
        return stack;
    }
}

package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.ensorcellation;


import iskallia.vault.client.gui.framework.element.EnchanterEnchantSelectorElement;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.util.EnchantmentEntry;
import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.data.enchantments.BannedEnchantmentsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(value = EnchanterEnchantSelectorElement.EnchanterEnchantSelectorModel.class, remap = false)
public abstract class MixinEnchanterEnchantSelectorModel {
    @Shadow @Final private ObservableSupplier<ItemStack> inputSupplier;
    @Shadow @Final private Supplier<String> searchFilter;

    @Shadow
    public static boolean canCraft(ItemStack gearStack, EnchantmentEntry config) {
        return false;
    }

    /**
     * @author iwolfking
     * @reason Remove ensorcellation vanilla enchants from Vault Enchanter
     */
    @Overwrite
    public List<EnchantmentEntry> getEntries() {
        ItemStack input = this.inputSupplier.get();
        if (input.isEmpty()) {
            return Collections.emptyList();
        } else {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return Collections.emptyList();
            } else {
                String searchTerm = this.searchFilter.get().toLowerCase(Locale.ROOT);
                Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(input);
                List<EnchantmentEntry> out = new ArrayList<>();

                for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
                    if (!enchantment.isCurse() && enchantment.canEnchant(input)) {
                        if (input.getItem() instanceof VaultGearItem || input.getItem() instanceof ToolItem || input.getItem().getRegistryName().toString().equals("the_vault:tool")) {
                            if (BannedEnchantmentsData.BANNED_ENCHANT_REGISTRY_NAMES.contains(enchantment.getRegistryName().toString())) {
                                continue;
                            }
                        } else {
                            if (BannedEnchantmentsData.BANNED_ENCHANT_REGISTRY_NAMES.contains(enchantment.getRegistryName().toString()) && !enchantment.getRegistryName().toString().equals("minecraft:mending")) {
                                continue;
                            }
                        }
                        String enchantName = (new TranslatableComponent(enchantment.getDescriptionId())).getString();
                        if (enchantName.toLowerCase(Locale.ROOT).contains(searchTerm)) {
                            out.add(new EnchantmentEntry(enchantment, enchantment.getMaxLevel()));
                        }
                    }
                }

                Map<EnchantmentEntry, Boolean> canCraftLookup = new HashMap<>();
                out.forEach(enchantmentEntry -> canCraftLookup.put(enchantmentEntry, canCraft(input, enchantmentEntry)));

                Map<EnchantmentEntry, Boolean> alreadyHasLookup = new HashMap<>();
                out.forEach(enchantmentEntry -> alreadyHasLookup.put(enchantmentEntry,  currentEnchantments.getOrDefault(enchantmentEntry.getEnchantment(), 0) >= enchantmentEntry.getLevel()));

                out.sort(Comparator.comparing(o -> o.getEnchantment().getRegistryName().toString()));
                out.sort((c1, c2) -> -Boolean.compare( canCraftLookup.get(c1),  canCraftLookup.get(c2)));
                out.sort((c1, c2) -> Boolean.compare( alreadyHasLookup.get(c1), alreadyHasLookup.get(c2)));
                return out;
            }
        }
    }
}

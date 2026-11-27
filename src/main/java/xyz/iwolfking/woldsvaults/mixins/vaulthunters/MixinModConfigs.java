package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.IntRangeEntry;
import iskallia.vault.config.entry.recipe.ConfigToolRecipe;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.item.tool.ToolMaterial;
import iskallia.vault.item.tool.ToolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.UniqueRollTables;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearCommonConfigAccessor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Mixin(value = ModConfigs.class, remap = false)
public class MixinModConfigs {

    @Shadow public static Map<ResourceLocation, VaultGearTierConfig> VAULT_GEAR_CONFIG;
  
    private static final int TOTAL_MAP_TIERS = 6;

    /**
     * Registers the shared ancient unique tier table, one mythic tier table per gear config and the per-tier map
     * tables right after the gear config map is built, so vhapi's gear overlays apply to all of them.
     */
    @Inject(method = "register", at = @At(value = "FIELD", target = "Liskallia/vault/init/ModConfigs;VAULT_GEAR_CONFIG:Ljava/util/Map;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER), remap = false)
    private static void registerDerivedGearConfigs(CallbackInfo ci) {
        Set<ResourceLocation> baseGearConfigs = new HashSet<>(VAULT_GEAR_CONFIG.keySet());
        VAULT_GEAR_CONFIG.put(UniqueRollTables.ANCIENT_TABLE, new VaultGearTierConfig(UniqueRollTables.ANCIENT_TABLE).readConfig());
        for (ResourceLocation loc : baseGearConfigs) {
            VAULT_GEAR_CONFIG.put(VaultMod.id(loc.getPath() + "_mythic"), new VaultGearTierConfig(VaultMod.id(loc.getPath() + "_mythic")).readConfig());
        }
        for (int i = 1; i < TOTAL_MAP_TIERS; i++) {
            VAULT_GEAR_CONFIG.put(VaultMod.id("map_" + i), new VaultGearTierConfig(VaultMod.id("map_" + i)).readConfig());
        }
    }

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private static void onReloadConfigs(CallbackInfo ci) {
        xyz.iwolfking.woldsvaults.init.ModConfigs.register();

        ((VaultGearCommonConfigAccessor)ModConfigs.VAULT_GEAR_COMMON).getCraftingPotentialRanges().put(VaultGearRarity.valueOf("MYTHIC"), new IntRangeEntry(250, 650));


        ToolType[] basicTypes = new ToolType[]{ToolType.PICK, ToolType.AXE, ToolType.SHOVEL, ToolType.HAMMER, ToolType.SICKLE};
        ToolMaterial toolMaterial = ToolMaterial.valueOf("NULLITE");
        List<ItemStack> ingredients = List.of(
                new ItemStack(iskallia.vault.init.ModItems.ECHOING_INGOT, 36),
                new ItemStack(ModBlocks.VAULT_BRONZE, 4096),
                new ItemStack(ModItems.WOLD_STAR_CHUNK, 3),
                new ItemStack(ModItems.NULLITE_CRYSTAL, 32)
                );

        for(ToolType toolType : basicTypes) {

            ItemStack out = ToolItem.create(toolMaterial, toolType);
            ConfigToolRecipe recipe = new ConfigToolRecipe(out, toolType, toolMaterial);


            for (ItemStack i : ingredients) {
                recipe.addInput(i);
            }
            ModConfigs.TOOL_RECIPES.getConfigRecipes().add(recipe);
        }

        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) {
            srv.getPlayerList().getPlayers().forEach((player) -> ModConfigs.TOOL_RECIPES.syncTo(ModConfigs.TOOL_RECIPES, player));
        }
    }

}

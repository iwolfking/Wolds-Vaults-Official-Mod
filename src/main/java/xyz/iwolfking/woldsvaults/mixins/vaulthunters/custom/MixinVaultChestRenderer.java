package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.block.model.VaultChestModel;
import iskallia.vault.block.render.VaultChestRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.Map;

@Mixin(VaultChestRenderer.class)
public class MixinVaultChestRenderer {

//    @Final
//    @Shadow
//    private static Map<Block, VaultChestModel> NORMAL_MODEL_MAP;
//
//    // There is probably a better way to this, but trying to inject in <clinit> complained about it being immutable
//    @Shadow
//    @Final
//    public static final Map<Block, Material> NORMAL_MATERIAL_MAP  = Map.ofEntries(Map.entry(iskallia.vault.init.ModBlocks.WOODEN_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_chest"))), Map.entry(iskallia.vault.init.ModBlocks.GILDED_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_gilded_chest"))), Map.entry(iskallia.vault.init.ModBlocks.LIVING_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_mossy_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ORNATE_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_ornate_chest"))), Map.entry(iskallia.vault.init.ModBlocks.TREASURE_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_treasure_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ALTAR_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_altar_chest"))), Map.entry(iskallia.vault.init.ModBlocks.HARDENED_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_hardened_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ENIGMA_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_enigma_chest"))), Map.entry(iskallia.vault.init.ModBlocks.FLESH_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_flesh_chest"))), Map.entry(iskallia.vault.init.ModBlocks.GILDED_STRONGBOX, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_gilded_strongbox"))), Map.entry(iskallia.vault.init.ModBlocks.ORNATE_STRONGBOX, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_ornate_strongbox"))), Map.entry(iskallia.vault.init.ModBlocks.LIVING_STRONGBOX, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_living_strongbox"))), Map.entry(iskallia.vault.init.ModBlocks.WOODEN_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_chest"))), Map.entry(iskallia.vault.init.ModBlocks.GILDED_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_gilded_chest"))), Map.entry(iskallia.vault.init.ModBlocks.LIVING_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_mossy_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ORNATE_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_ornate_chest"))), Map.entry(iskallia.vault.init.ModBlocks.TREASURE_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_treasure_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ALTAR_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_altar_chest"))), Map.entry(iskallia.vault.init.ModBlocks.HARDENED_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_hardened_chest"))), Map.entry(iskallia.vault.init.ModBlocks.ENIGMA_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_enigma_chest"))), Map.entry(iskallia.vault.init.ModBlocks.FLESH_CHEST_PLACEABLE, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_flesh_chest"))), Map.entry(ModBlocks.CORRUPTED_CHEST, new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/corrupted_chest"))));
//    @Unique
//    private static final ModelLayerLocation CORRUPTED_LOCATION = new ModelLayerLocation(VaultMod.id("corrupted_chest"), "main");
//    @Inject(method = "<init>", at = @At("TAIL"))
//    private void appendAdditionalChestModels(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
//        var corruptedChest = new VaultChestModel(context.bakeLayer(CORRUPTED_LOCATION));
//        NORMAL_MODEL_MAP.put(ModBlocks.CORRUPTED_CHEST, corruptedChest);
//    }
}
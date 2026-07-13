package xyz.iwolfking.woldsvaults.client.init;

import iskallia.vault.VaultMod;
import iskallia.vault.block.TreasureDoorBlock;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModChestModels {
    public static final ResourceLocation CHEST_SHEET = ResourceLocation.parse("textures/atlas/chest.png");

    private static final Material ISKALLIUM = new Material(Sheets.CHEST_SHEET, treasureChest("iskallium"));
    private static final Material GORGINITE = new Material(Sheets.CHEST_SHEET, treasureChest("gorginite"));
    private static final Material SPARKLETINE = new Material(Sheets.CHEST_SHEET, treasureChest("sparkletine"));
    private static final Material ASHIUM = new Material(Sheets.CHEST_SHEET, treasureChest("ashium"));
    private static final Material BOMIGNITE = new Material(Sheets.CHEST_SHEET, treasureChest("bomignite"));
    private static final Material TUBIUM = new Material(Sheets.CHEST_SHEET, treasureChest("tubium"));
    private static final Material UPALINE = new Material(Sheets.CHEST_SHEET, treasureChest("upaline"));
    private static final Material PUFFIUM = new Material(Sheets.CHEST_SHEET, treasureChest("puffium"));
    private static final Material PETZANITE = new Material(Sheets.CHEST_SHEET, treasureChest("petzanite"));
    private static final Material XENIUM = new Material(Sheets.CHEST_SHEET, treasureChest("xenium"));

    private static ResourceLocation treasureChest(String type) {
        return WoldsVaults.id("entity/chest/locked_treasure/"+ type);
    }

    public static Material getMaterialForType(TreasureDoorBlock.Type type) {
        return switch(type) { // genius switch
            case ISKALLIUM -> ISKALLIUM;
            case GORGINITE -> GORGINITE;
            case SPARKLETINE -> SPARKLETINE;
            case ASHIUM -> ASHIUM;
            case BOMIGNITE -> BOMIGNITE;
            case TUBIUM -> TUBIUM;
            case UPALINE -> UPALINE;
            case PUFFIUM -> PUFFIUM;
            case PETZANITE -> PETZANITE;
            case XENIUM -> XENIUM;
        };
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void stitchTextures(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(CHEST_SHEET)) {
            event.addSprite(treasureChest("iskallium"));
            event.addSprite(treasureChest("gorginite"));
            event.addSprite(treasureChest("sparkletine"));
            event.addSprite(treasureChest("ashium"));
            event.addSprite(treasureChest("bomignite"));
            event.addSprite(treasureChest("tubium"));
            event.addSprite(treasureChest("upaline"));
            event.addSprite(treasureChest("puffium"));
            event.addSprite(treasureChest("petzanite"));
            event.addSprite(treasureChest("xenium"));
        }
    }
}

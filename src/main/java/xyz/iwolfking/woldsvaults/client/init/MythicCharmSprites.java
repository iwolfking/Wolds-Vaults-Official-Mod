package xyz.iwolfking.woldsvaults.client.init;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/** Stitches the mythic charm particle sprites, which this jar ships under the base charm particle paths. */
@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD, modid = WoldsVaults.MOD_ID)
public final class MythicCharmSprites {
    private MythicCharmSprites() {
    }

    @SubscribeEvent
    public static void stitchSprites(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location() != InventoryMenu.BLOCK_ATLAS) {
            return;
        }
        for (VaultGod god : VaultGod.values()) {
            event.addSprite(VaultMod.id("particle/charm/" + god.getSerializedName() + "/mythic"));
        }
    }
}

package xyz.iwolfking.woldsvaults.client.init;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Stitches the mythic charm particle sprites. The base mod stitches its charm particles per god
 * for the four base rarities only; the mythic rarity's sprites ship in this jar under the same
 * {@code the_vault:particle/charm/<god>/mythic} paths the base {@code getParticleLoc} resolves.
 */
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

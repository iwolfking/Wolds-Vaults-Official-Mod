package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.gui.screen.options.ui.OptionsCanvas;
import iskallia.vault.client.gui.screen.options.ui.OptionsInput;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.crystal.CrystalCraftTab", remap = false)
public abstract class MixinCrystalCraftTab {

    @Unique
    private static final int WOLDS_VAULTS$COLS = 7;
    @Unique
    private static final int WOLDS_VAULTS$VISIBLE_ROWS = 3;
    @Unique
    private int woldsVaults$scrollRow = 0;
    @Unique
    private int woldsVaults$maxScrollRow = 0;

    @Shadow
    private List<VaultForgeRecipe> recipes;

    @Shadow
    @Final
    private OptionsCanvas canvas;

    @Shadow
    @Final
    private OptionsInput input;

    @Inject(method = "drawGrid", at = @At("HEAD"))
    private void woldsVaults$prepareScroll(List<VaultForgeRecipe> shown, long now, CallbackInfo ci) {
        int totalRows = (shown.size() + WOLDS_VAULTS$COLS - 1) / WOLDS_VAULTS$COLS;
        this.woldsVaults$maxScrollRow = Math.max(0, totalRows - WOLDS_VAULTS$VISIBLE_ROWS);
        if (this.woldsVaults$scrollRow > this.woldsVaults$maxScrollRow) {
            this.woldsVaults$scrollRow = this.woldsVaults$maxScrollRow;
        }
        if (this.woldsVaults$scrollRow < 0) {
            this.woldsVaults$scrollRow = 0;
        }

        if (this.woldsVaults$maxScrollRow > 0) {
            this.input.hit(this.canvas, "craft:grid_scroll", 8.0F, 66.0F, 160.0F, 70.0F)
                    .scrollsList(wheelDelta -> this.woldsVaults$scrollRow = Math.max(
                            0,
                            Math.min(this.woldsVaults$maxScrollRow, this.woldsVaults$scrollRow + wheelDelta)
                    ));
        }
    }

    @ModifyExpressionValue(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I"
            )
    )
    private int woldsVaults$limitVisibleCount(int originalSize) {
        int start = this.woldsVaults$scrollRow * WOLDS_VAULTS$COLS;
        if (start >= originalSize) {
            return 0;
        }
        return Math.min(originalSize - start, WOLDS_VAULTS$COLS * WOLDS_VAULTS$VISIBLE_ROWS);
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;get(I)Ljava/lang/Object;"
            )
    )
    private int woldsVaults$offsetIndex(int index) {
        return index + this.woldsVaults$scrollRow * WOLDS_VAULTS$COLS;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;sprite(Liskallia/vault/client/atlas/TextureAtlasRegion;FF)V"
            ),
            index = 2
    )
    private float woldsVaults$offsetSpriteY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F - 1.0F;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;fill(FFFFI)V",
                    ordinal = 0
            ),
            index = 1
    )
    private float woldsVaults$offsetSelectedBorderY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F - 1.0F;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;fill(FFFFI)V",
                    ordinal = 1
            ),
            index = 1
    )
    private float woldsVaults$offsetSelectedFillY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;item(Lnet/minecraft/world/item/ItemStack;FFFLiskallia/vault/client/gui/screen/options/ui/IconLook;)V"
            ),
            index = 2
    )
    private float woldsVaults$offsetItemY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;fill(FFFFI)V"
            ),
            index = 1
    )
    private float woldsVaults$offsetHoverFillY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F;
    }

    @ModifyArg(
            method = "drawGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/client/gui/screen/options/ui/OptionsInput;hit(Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;Ljava/lang/String;FFFF)Liskallia/vault/client/gui/screen/options/ui/OptionsInput$Hit;"
            ),
            index = 3
    )
    private float woldsVaults$offsetHitY(float y, @Local(name = "row") int row) {
        return 69.0F + row * 22.0F;
    }

    @Inject(method = "drawGrid", at = @At("TAIL"))
    private void woldsVaults$drawScrollHint(List<VaultForgeRecipe> shown, long now, CallbackInfo ci) {
        if (this.woldsVaults$maxScrollRow <= 0) {
            return;
        }

        int totalPages = this.woldsVaults$maxScrollRow + 1;
        float trackX = 164.0F;
        float trackY = 68.0F;
        float trackH = 66.0F;
        canvas.fill(trackX, trackY, 2.0F, trackH, 0x44000000);

        float thumbH = Math.max(10.0F, trackH / totalPages);
        float thumbTravel = trackH - thumbH;
        float thumbY = trackY + ((thumbTravel * this.woldsVaults$scrollRow) / this.woldsVaults$maxScrollRow);
        canvas.fill(trackX, thumbY, 2.0F, thumbH, 0xCCFFFFFF);
    }
}
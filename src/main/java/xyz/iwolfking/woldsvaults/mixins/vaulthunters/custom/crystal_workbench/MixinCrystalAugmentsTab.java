package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import iskallia.vault.client.gui.screen.options.ui.OptionsCanvas;
import iskallia.vault.client.gui.screen.options.ui.OptionsInput;
import iskallia.vault.container.crystal.CrystalWorkbenchAugments;
import iskallia.vault.world.theme.ThemeSeries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.crystal.CrystalAugmentsTab", remap = false)
public abstract class MixinCrystalAugmentsTab {

    @Unique
    private static final int WOLDS_VAULTS$REGULAR_SPOTS = 10;
    @Unique
    private static final int WOLDS_VAULTS$SEASONAL_SPOTS = 3;
    @Unique
    private static final int WOLDS_VAULTS$COLS = 7;
    @Unique
    private static final float WOLDS_VAULTS$ROW_Y = 109.0F;

    @Unique
    private int woldsVaults$scrollRow = 0;
    @Unique
    private int woldsVaults$maxScrollRow = 0;

    @Shadow
    @Final
    private OptionsCanvas canvas;

    @Shadow
    @Final
    private OptionsInput input;

    @Shadow
    private void drawButton(ThemeSeries series, float x, float y, long now) {
    }

    @Inject(method = "draw", at = @At("TAIL"))
    private void woldsVaults$drawNewAugments(long now, CallbackInfo ci) {
        List<ThemeSeries> regular = new ArrayList<>();
        List<ThemeSeries> seasonal = new ArrayList<>();

        for (ThemeSeries series : CrystalWorkbenchAugments.craftableSeries()) {
            (series.seasonal() ? seasonal : regular).add(series);
        }

        List<ThemeSeries> overflow = new ArrayList<>();
        for (int i = WOLDS_VAULTS$REGULAR_SPOTS; i < regular.size(); i++) {
            overflow.add(regular.get(i));
        }
        for (int i = WOLDS_VAULTS$SEASONAL_SPOTS; i < seasonal.size(); i++) {
            overflow.add(seasonal.get(i));
        }

        if (overflow.isEmpty()) {
            this.woldsVaults$scrollRow = 0;
            this.woldsVaults$maxScrollRow = 0;
            return;
        }

        int totalRows = (overflow.size() + WOLDS_VAULTS$COLS - 1) / WOLDS_VAULTS$COLS;
        this.woldsVaults$maxScrollRow = Math.max(0, totalRows - 1);
        this.woldsVaults$scrollRow = Math.max(0, Math.min(this.woldsVaults$maxScrollRow, this.woldsVaults$scrollRow));

        if (this.woldsVaults$maxScrollRow > 0) {
            this.input.hit(this.canvas, "augments:overflow_scroll", 8.0F, 108.0F, 160.0F, 32.0F)
                    .scrollsList(wheelDelta -> this.woldsVaults$scrollRow = Math.max(
                            0,
                            Math.min(this.woldsVaults$maxScrollRow, this.woldsVaults$scrollRow + wheelDelta)
                    ));
        }

        int startIdx = this.woldsVaults$scrollRow * WOLDS_VAULTS$COLS;
        int endIdx = Math.min(overflow.size(), startIdx + WOLDS_VAULTS$COLS);

        for (int idx = startIdx; idx < endIdx; idx++) {
            int col = idx - startIdx;
            float x = 11.0F + col * 22.0F;
            this.drawButton(overflow.get(idx), x, WOLDS_VAULTS$ROW_Y, now);
        }

        if (this.woldsVaults$maxScrollRow > 0) {
            int totalPages = this.woldsVaults$maxScrollRow + 1;
            float trackX = 165.0F;
            float trackY = 109.0F;
            float trackH = 28.0F;
            this.canvas.fill(trackX, trackY, 2.0F, trackH, 0x44000000);

            float thumbH = Math.max(8.0F, trackH / totalPages);
            float thumbTravel = trackH - thumbH;
            float thumbY = trackY + ((thumbTravel * this.woldsVaults$scrollRow) / this.woldsVaults$maxScrollRow);
            this.canvas.fill(trackX, thumbY, 2.0F, thumbH, 0xCCFFFFFF);
        }
    }
}
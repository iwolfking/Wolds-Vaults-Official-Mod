package xyz.iwolfking.woldsvaults.client.init;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.helper.ObjectiveHudPreview.Kind;
import iskallia.vault.client.gui.screen.options.ui.OptionsCanvas;
import iskallia.vault.core.vault.overlay.VaultOverlay;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.vhapi.api.extensions.client.ObjectiveHudPreviewKindFactory;
import xyz.iwolfking.woldsvaults.objectives.AlchemyObjective;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;
import xyz.iwolfking.woldsvaults.objectives.ZealotObjective;

public class ModObjectiveHudPreviewKinds {
    public static final Kind ALCHEMY = ObjectiveHudPreviewKindFactory.INSTANCE.createWithPreview(
            "ALCHEMY",
            ModObjectiveHudPreviewKinds::drawAlchemyPreview
    );
    public static final Kind ENCHANTED_ELIXIR = ObjectiveHudPreviewKindFactory.INSTANCE.create("ENCHANTED_ELIXIR");
    public static final Kind HAUNTED_BRAZIERS = ObjectiveHudPreviewKindFactory.INSTANCE.create("HAUNTED_BRAZIERS");
    public static final Kind UNHINGED_SCAV = ObjectiveHudPreviewKindFactory.INSTANCE.create("UNHINGED_SCAVENGER");
    public static final Kind SENSELESS_SCAVINGO = ObjectiveHudPreviewKindFactory.INSTANCE.create("SENSELESS_SCAVINGO");
    public static final Kind BALLISTIC_BINGO = ObjectiveHudPreviewKindFactory.INSTANCE.create("BALLISTIC_BINGO");
    public static final Kind BRUTAL_BOSSES = ObjectiveHudPreviewKindFactory.INSTANCE.createWithPreview(
            "BRUTAL_BOSSES",
            ModObjectiveHudPreviewKinds::drawBrutalBossesPreview
    );
    public static final Kind ZEALOT = ObjectiveHudPreviewKindFactory.INSTANCE.createWithPreview(
            "ZEALOT",
            ModObjectiveHudPreviewKinds::drawZealotPreview
    );
    public static final Kind SURVIVAL = ObjectiveHudPreviewKindFactory.INSTANCE.createWithPreview(
            "SURVIVAL",
            ModObjectiveHudPreviewKinds::drawSurvivalPreview
    );

    public static void init() {
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawAlchemyPreview(OptionsCanvas canvas, int boardSize, float x, float y, float width, float height, long now) {
        float fill = ObjectiveHudPreviewKindFactory.loopedFill(now);
        PoseStack pose = ObjectiveHudPreviewKindFactory.pushBar(canvas, x, y, width, height);

        canvas.drawWithTexture(AlchemyObjective.HUD, () -> {
            pose.pushPose();
            pose.scale(0.8F, 0.8F, 1.0F);
            GuiComponent.blit(pose, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
            GuiComponent.blit(pose, 0, 8, 0.0F, 30.0F, 13 + (int) (130.0F * fill), 10, 200, 100);
            pose.popPose();
        });

        boolean done = fill >= 1.0F;
        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                done ? "Brew more Potions or Exit!" : "Brew Potions",
                80.0F,
                13.0F,
                0.6F,
                0xFFFFFF
        );
        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                done ? "Ready to Complete" : String.format("%.0f%%", fill * 100.0F),
                80.0F,
                31.0F,
                0.6F,
                done ? 5635925 : 0xFFFFFF
        );

        pose.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawBrutalBossesPreview(OptionsCanvas canvas, int boardSize, float x, float y, float width, float height, long now) {
        int waveCount = 3;
        int targetPerWave = 1;

        float fill = ObjectiveHudPreviewKindFactory.loopedFill(now);
        int activeWave = Math.min(waveCount - 1, (int) Math.floor(fill * waveCount));
        int completedWaves = fill >= 1.0F ? waveCount : (int) Math.floor(fill * waveCount);

        float itemBoxWidth = 22.0F;
        float gapWidth = 7.0F;
        float stepX = itemBoxWidth + gapWidth;
        float iconWidth = 12.0F;
        float iconHeight = 22.0F;

        float rowWidth = waveCount * itemBoxWidth + (waveCount - 1) * gapWidth;
        float scale = Math.min(1.0F, (width - 8.0F) / Math.max(1.0F, rowWidth));
        float blockHeight = 36.0F * scale;

        PoseStack pose = canvas.pose();
        pose.pushPose();
        pose.translate(
                x + (width - rowWidth * scale) / 2.0F,
                y + (height - blockHeight) / 2.0F,
                0.0F
        );
        pose.scale(scale, scale, 1.0F);

        for (int i = 0; i < waveCount; i++) {
            boolean isCompleted = i < completedWaves;
            boolean isActive = !isCompleted && i == activeWave && fill < 1.0F;

            float slotX = i * stepX;
            float iconX = slotX + (itemBoxWidth - iconWidth) / 2.0F;
            float iconY = 0.0F;

            canvas.drawWithTexture(VaultOverlay.VAULT_HUD, () -> {
                if (isActive) {
                    GuiComponent.blit(pose, (int) iconX, (int) iconY, 77.0F, 84.0F, (int) iconWidth, (int) iconHeight, 256, 256);
                } else {
                    GuiComponent.blit(pose, (int) iconX, (int) iconY, 64.0F, 84.0F, (int) iconWidth, (int) iconHeight, 256, 256);
                }
            });

            int count = isCompleted ? targetPerWave : 0;
            String label = count + "/" + targetPerWave;
            int color = isCompleted ? 5635925 : (isActive ? 0xFF5555 : 13948116);

            ObjectiveHudPreviewKindFactory.smallText(canvas, label, slotX + itemBoxWidth / 2.0F, iconHeight + 9.0F, 0.6F, color);
        }

        pose.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawSurvivalPreview(OptionsCanvas canvas, int boardSize, float x, float y, float width, float height, long now) {
        int previewRequiredSeconds = 120;

        float fill = ObjectiveHudPreviewKindFactory.loopedFill(now);
        int survivedSeconds = Math.round(previewRequiredSeconds * fill);
        boolean complete = fill >= 1.0F;

        PoseStack pose = ObjectiveHudPreviewKindFactory.pushBar(canvas, x, y, width, height);

        canvas.drawWithTexture(SurvivalObjective.HUD, () -> {
            pose.pushPose();
            pose.scale(0.8F, 0.8F, 1.0F);
            GuiComponent.blit(pose, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
            GuiComponent.blit(pose, 0, 8, 0.0F, 30.0F, 13 + (int) (130.0F * fill), 10, 200, 100);
            pose.popPose();
        });

        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                complete ? "Survive for Rewards!" : "Survive!",
                80.0F,
                13.0F,
                0.6F,
                0xFFFFFF
        );
        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                survivedSeconds + " / " + previewRequiredSeconds + " s",
                80.0F,
                31.0F,
                0.6F,
                complete ? 5635925 : 0xFFFFFF
        );

        pose.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawZealotPreview(OptionsCanvas canvas, int boardSize, float x, float y, float width, float height, long now) {
        int targetCount = 4;

        float fill = ObjectiveHudPreviewKindFactory.loopedFill(now);
        int currentCount = Math.round(targetCount * fill);
        boolean complete = fill >= 1.0F;

        PoseStack pose = ObjectiveHudPreviewKindFactory.pushBar(canvas, x, y, width, height);

        canvas.drawWithTexture(ZealotObjective.HUD, () -> {
            pose.pushPose();
            pose.scale(0.8F, 0.8F, 1.0F);
            GuiComponent.blit(pose, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
            GuiComponent.blit(pose, 0, 8, 0.0F, 30.0F, 13 + (int) (130.0F * fill), 10, 200, 100);
            pose.popPose();
        });

        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                complete ? "The gods are pleased, you may exit when ready." : "Complete God Altars",
                80.0F,
                13.0F,
                0.6F,
                complete ? 0xFFAA00 : 0xFFFFFF
        );
        ObjectiveHudPreviewKindFactory.smallText(
                canvas,
                currentCount + " / " + targetCount,
                80.0F,
                31.0F,
                0.6F,
                complete ? 5635925 : 0xFFFFFF
        );

        pose.popPose();
    }
}
package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public class TimeTrialLeaderboardScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(
                    "woldsvaults",
                    "textures/gui/timetrial_leaderboard.png"
            );

    private final String objective;
    private final long timeRemaining;
    private final List<TimeTrialLeaderboardEntry> entries;

    private int leftPos;
    private int topPos;

    private final int imageWidth = 256;
    private final int imageHeight = 256;

    public TimeTrialLeaderboardScreen(
            String objective,
            long timeRemaining,
            List<TimeTrialLeaderboardEntry> entries
    ) {
        super(new TextComponent("Weekly Time Trial"));
        this.objective = objective;
        this.timeRemaining = timeRemaining;
        this.entries = entries;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);

        RenderSystem.setShaderTexture(0, BG);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        drawCenteredString(
                pose,
                font,
                "Weekly Time Trial",
                width / 2,
                topPos + 10,
                0xFFD700
        );

        renderHeader(pose);
        renderEntries(pose);

        super.render(pose, mouseX, mouseY, partialTick);
    }

    private void renderHeader(PoseStack pose) {

        fill(
                pose,
                leftPos + 12,
                topPos + 26,
                leftPos + imageWidth - 12,
                topPos + 70,
                0x33000000
        );

        font.draw(
                pose,
                "Objective",
                leftPos + 20,
                topPos + 32,
                0xAAAAAA
        );

        font.draw(
                pose,
                objective.toUpperCase(),
                leftPos + 20,
                topPos + 44,
                0xFFFFFF
        );

        String remaining = "Ends in " + formatRemaining(timeRemaining);
        font.draw(
                pose,
                remaining,
                leftPos + imageWidth - 20 - font.width(remaining),
                topPos + 44,
                0xAAAAAA
        );
    }

    private void renderEntries(PoseStack pose) {
        int x = leftPos + 12;
        int y = topPos + 80;
        int rowWidth = imageWidth - 24;

        for (int i = 0; i < entries.size(); i++) {
            if(i > 9) {
                break;
            }
            TimeTrialLeaderboardEntry e = entries.get(i);

            int bgColor = (i % 2 == 0) ? 0x33000000 : 0x22000000;
            fill(pose, x, y - 2, x + rowWidth, y + 16, bgColor);

            drawPlayerHead(pose, e.uuid(), x + 4, y + 4);

            font.draw(
                    pose,
                    "#" + (i + 1),
                    x + 20,
                    y + 4,
                    rankColor(i + 1)
            );

            font.draw(
                    pose,
                    e.name(),
                    x + 40,
                    y + 4,
                    0xFFFFFF
            );

            String time = formatTime(e.time());
            int timeX = leftPos + imageWidth - 20 - font.width(time);
            font.draw(
                    pose,
                    time,
                    timeX,
                    y + 4,
                    0xAAAAAA
            );

            y += 18;
        }
    }

    private void drawPlayerHead(PoseStack pose, UUID uuid, int x, int y) {
        PlayerInfo info = Minecraft.getInstance()
                .getConnection()
                .getPlayerInfo(uuid);

        ResourceLocation skin = info != null
                ? info.getSkinLocation()
                : DefaultPlayerSkin.getDefaultSkin(uuid);

        RenderSystem.setShaderTexture(0, skin);

        blit(pose, x, y, 8, 8, 8, 8, 64, 64);

        blit(pose, x, y, 40, 8, 8, 8, 64, 64);
    }

    private static int rankColor(int rank) {
        return switch (rank) {
            case 1 -> 0xFFD700;
            case 2 -> 0xC0C0C0;
            case 3 -> 0xCD7F32;
            default -> 0xFFFFFF;
        };
    }

    private static String formatTime(long ticks) {
        return String.format("%.2f s", ticks / 20.0);
    }

    private static String formatRemaining(long millis) {
        long seconds = millis / 1000;
        return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

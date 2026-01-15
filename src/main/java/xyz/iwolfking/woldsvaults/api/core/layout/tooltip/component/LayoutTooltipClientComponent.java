package xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent.RoomType;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent.Tunnel;

public record LayoutTooltipClientComponent(LayoutTooltipComponent tooltipComponent) implements ClientTooltipComponent {
    private static final int ROOM_SIZE = 6;
    private static final int GAP_SIZE = 2;
    private static final int TUNNEL_THICKNESS = 2;
    @Override public int getHeight() {
        if (!Screen.hasShiftDown()) {
            return 9 + 2;
        }

        int cellSize = ROOM_SIZE + GAP_SIZE * tooltipComponent.tunnelSpan();

        int heightAddition = hasInscriptions() ? 10 : 0;

        return tooltipComponent.rooms().length * cellSize + 2 + heightAddition;
    }

    @Override public int getWidth(Font pFont) {
        if (!Screen.hasShiftDown()) {
            return pFont.width("SHIFT for preview");
        }
        int cellSize = ROOM_SIZE + GAP_SIZE * tooltipComponent.tunnelSpan();
        RoomType[][] lines = tooltipComponent.rooms();
        if (lines.length == 0) return 0;
        return lines[0].length * cellSize + 2;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
        RoomType[][] lines = tooltipComponent.rooms();

        int cellSize = ROOM_SIZE + GAP_SIZE * tooltipComponent.tunnelSpan();

        poseStack.pushPose();
        poseStack.translate(x, y, 500);

        if (Screen.hasShiftDown()) {
            renderRooms(poseStack, lines, cellSize);
            renderHorizontalTunnels(poseStack, cellSize);
            renderVerticalTunnels(poseStack, cellSize);



            // If there are inscriptions and no seed is set, display a note
            if (hasInscriptions()) {
                String note = "Inscriptions are approximations";
                int textY = lines.length * cellSize + GAP_SIZE;
                font.draw(poseStack, note, 0, textY, ChatFormatting.GRAY.getColor());
            }
        } else {
            font.draw(poseStack, "SHIFT for preview", 0, 0, ChatFormatting.DARK_GRAY.getColor());
        }

        poseStack.popPose();
    }

    public boolean hasInscriptions() {
        for (RoomType[] row : tooltipComponent.rooms()) {
            for (RoomType type : row) {
                if (type == RoomType.INSCRIPTION) {
                    return true;
                }
            }
        }

        return false;
    }


    private static void renderRooms(PoseStack poseStack, RoomType[][] lines, int cellSize) {
        for (int row = 0; row < lines.length; row++) {
            RoomType[] line = lines[row];

            for (int col = 0; col < line.length; col++) {
                RoomType type = line[col];
                if (type == null) continue;
                if (type == RoomType.EMPTY) continue;

                int x0 = col * cellSize + GAP_SIZE;
                int x1 = x0 + ROOM_SIZE;
                int y0 = row * cellSize + GAP_SIZE;
                int y1 = y0 + ROOM_SIZE;

                int color;
                switch (type) {
                    case ROOM -> color = 0xFFDDDDDD;
                    case INSCRIPTION -> color = 0xFF006600;
                    case PORTAL -> color = 0xFFFF0000;
                    default -> color = 0xFFFF00FF;
                }

                GuiComponent.fill(poseStack, x0, y0, x1, y1, color);
            }
        }
    }

    private void renderHorizontalTunnels(PoseStack poseStack, int cellSize) {
        Tunnel[][] hTunnels = tooltipComponent.horizontalTunnels();
        for (int row = 0; row < hTunnels.length; row++) {
            Tunnel[] line = hTunnels[row];

            for (int col = 0; col < line.length; col++) {
                if (line[col] == Tunnel.TUNNEL) {
                    int x0 = col * cellSize + ROOM_SIZE + GAP_SIZE;
                    int x1 = x0 + GAP_SIZE * tooltipComponent().tunnelSpan();

                    int y0 = row * cellSize + GAP_SIZE + ROOM_SIZE / 2 - TUNNEL_THICKNESS / 2;
                    int y1 = y0 + TUNNEL_THICKNESS;

                    GuiComponent.fill(poseStack, x0, y0, x1, y1, 0xFF888888);
                }
            }
        }
    }

    private void renderVerticalTunnels(PoseStack poseStack, int cellSize) {
        Tunnel[][] vTunnels = tooltipComponent.verticalTunnels();
        for (int row = 0; row < vTunnels.length; row++) {
            Tunnel[] line = vTunnels[row];

            for (int col = 0; col < line.length; col++) {
                if (line[col] == Tunnel.TUNNEL) {
                    int y0 = row * cellSize + ROOM_SIZE + GAP_SIZE;
                    int y1 = y0 + GAP_SIZE * tooltipComponent().tunnelSpan();

                    int x0 = col * cellSize + GAP_SIZE + ROOM_SIZE / 2 - TUNNEL_THICKNESS / 2;
                    int x1 = x0 + TUNNEL_THICKNESS;

                    GuiComponent.fill(poseStack, x0, y0, x1, y1, 0xFF888888);
                }
            }
        }
    }

}
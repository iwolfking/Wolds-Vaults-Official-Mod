package xyz.iwolfking.woldsvaults.client;

import iskallia.vault.container.spi.ForgeRecipeContainer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundSwitchWorkstationPacket;
import xyz.iwolfking.woldsvaults.workstation.WorkstationTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class WorkstationScreenTabs {
    private static final Map<AbstractContainerScreen<?>, List<BlockPos>> TABS = new WeakHashMap<>();
    private static final Map<AbstractContainerScreen<?>, BlockPos> CURRENT = new WeakHashMap<>();
    private static final Map<AbstractContainerScreen<?>, List<Button>> BUTTONS = new WeakHashMap<>();
    public static volatile BlockPos LAST_OPENED_POS = null;
    public static volatile long LAST_OPENED_TIME = 0L;

    public static void registerButtons(AbstractContainerScreen<?> screen, List<Button> buttons) {
        BUTTONS.put(screen, buttons);
    }

    public static List<Button> getButtons(net.minecraft.client.gui.screens.Screen screen) {
        return BUTTONS.get(screen);
    }

    private static BlockPos resolveOpenedPos(AbstractContainerScreen<?> screen, Minecraft mc) {
        if (LAST_OPENED_POS != null && System.currentTimeMillis() - LAST_OPENED_TIME <= 3000L) {
            try {
                if (mc.level != null && WorkstationTabs.isWorkstation(mc.level.getBlockState(LAST_OPENED_POS).getBlock())) {
                    return LAST_OPENED_POS;
                }
            } catch (Exception ignored) {}
        }
        try {
            if (mc.hitResult instanceof BlockHitResult bhr && WorkstationTabs.isWorkstation(mc.level.getBlockState(bhr.getBlockPos()).getBlock())) {
                return bhr.getBlockPos();
            }
        } catch (Exception ignored) {}
        try {
            Object menu = screen.getMenu();
            if (menu instanceof ForgeRecipeContainer<?> forge) {
                BlockPos fp = forge.getTilePos();
                if (fp != null && WorkstationTabs.isWorkstation(mc.level.getBlockState(fp).getBlock())) {
                    return fp;
                }
                if (fp != null) {
                    return fp;
                }
            }
        } catch (Exception ignored) {}

        if (LAST_OPENED_POS != null && WorkstationTabs.isWorkstation(mc.level.getBlockState(LAST_OPENED_POS).getBlock())) {
            return LAST_OPENED_POS;
        }
        return null;
    }

    public static List<Button> createTabs(AbstractContainerScreen<?> screen) {
        List<Button> buttons = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return buttons;
        }

        BlockPos currentPos = resolveOpenedPos(screen, mc);
        BlockPos center = currentPos;
        if (center == null) {
            HitResult hr = mc.hitResult;
            if (hr instanceof BlockHitResult bhr && WorkstationTabs.isWorkstation(mc.level.getBlockState(bhr.getBlockPos()).getBlock())) {
                center = bhr.getBlockPos();
            }
        }
        if (center == null) {
            center = mc.player.blockPosition();
        }

        List<BlockPos> tabs = WorkstationTabs.detect(mc.level, center);
        if (tabs.size() < 2) {
            return buttons;
        }

        TABS.put(screen, tabs);
        CURRENT.put(screen, currentPos);

        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        int imageWidth = screen.getXSize();
        int imageHeight = screen.getYSize();
        int tabW = 24;
        int tabH = 24;
        int spacing = 2;
        int count = tabs.size();

        int totalW = count * tabW + (count - 1) * spacing;
        int xStart = guiLeft + (imageWidth - totalW) / 2;
        if (xStart < 0) {
            xStart = 0;
        }
        int y = guiTop - tabH - 22;

        for (int i = 0; i < count; i++) {
            BlockPos pos = tabs.get(i);
            final BlockPos finalPos = pos;
            int x = xStart + i * (tabW + spacing);
            int yPos = y;
            Button btn = new Button(x, y, tabW, tabH, TextComponent.EMPTY, b -> {
                LAST_OPENED_POS = finalPos;
                LAST_OPENED_TIME = System.currentTimeMillis();
                ModNetwork.sendToServer(new ServerboundSwitchWorkstationPacket(finalPos));
            }) {
                @Override
                public void renderButton(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                    boolean selected = false;
                    BlockPos cur = CURRENT.get(screen);
                    if (cur != null && cur.equals(finalPos)) {
                        selected = true;
                    }
                    int bg = selected ? 0xFF555555 : 0xFFAAAAAA;
                    GuiComponent.fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, bg);
                    BlockState st = mc.level.getBlockState(finalPos);
                    Block block = st.getBlock();
                    ItemStack stack = block.asItem().getDefaultInstance();
                    if (stack.isEmpty()) {
                        stack = st.getBlock().getCloneItemStack(st, null, mc.level, finalPos, mc.player);
                    }
                    mc.getItemRenderer().renderAndDecorateItem(stack, this.x + 5, this.y + 5);
                    int border = isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF222222;
                    GuiComponent.fill(poseStack, this.x, this.y, this.x + this.width, this.y + 1, border);
                    GuiComponent.fill(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, border);
                    GuiComponent.fill(poseStack, this.x, this.y, this.x + 1, this.y + this.height, border);
                    GuiComponent.fill(poseStack, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, border);
                }
            };
            buttons.add(btn);
        }
        return buttons;
    }
}

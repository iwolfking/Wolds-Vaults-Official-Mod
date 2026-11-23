package xyz.iwolfking.woldsvaults.client.loadingzone;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneMarkerBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.LoadingZoneMarkerTileEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZoneShape;
import xyz.iwolfking.woldsvaults.network.message.LoadingZoneConfigMessage;

/**
 * Sets a loading zone marker's link name, and a post's zone height, corner number and where the zone leads, or an
 * arrival marker's facing.
 */
public class LoadingZoneScreen extends Screen {
    private static final String[] COMPASS = {"S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE"};
    private static final int WIDTH = 200;
    private static final int ROW = 24;

    private final BlockPos pos;
    private final boolean arrival;
    private final String startLink;
    private final String startPocket;
    private EditBox linkBox;
    private EditBox pocketBox;
    private int zoneHeight;
    private int order;
    private int rotation;
    private LoadingZoneShape.Leads leads;
    private int left;
    private int top;

    private LoadingZoneScreen(LoadingZoneMarkerTileEntity marker, int rotation) {
        super(new TextComponent(marker.isArrival() ? "Loading Zone Arrival" : "Loading Zone Post"));
        this.pos = marker.getBlockPos();
        this.arrival = marker.isArrival();
        this.startLink = marker.getLink();
        this.startPocket = marker.getPocket();
        this.zoneHeight = marker.getHeight();
        this.order = marker.getOrder();
        this.rotation = rotation;
        this.leads = marker.getLeads();
    }

    public static void open(LoadingZoneMarkerTileEntity marker) {
        BlockState state = marker.getBlockState();
        int rotation = state.hasProperty(LoadingZoneArrivalBlock.ROTATION) ? state.getValue(LoadingZoneArrivalBlock.ROTATION) : 0;
        Minecraft.getInstance().setScreen(new LoadingZoneScreen(marker, rotation));
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = this.height / 2 - (this.arrival ? 60 : 90);
        String link = this.linkBox == null ? this.startLink : this.linkBox.getValue();
        this.linkBox = new EditBox(this.font, this.left, this.top + 12, WIDTH, 20, new TextComponent("Link name"));
        this.linkBox.setMaxLength(LoadingZoneMarkerBlock.MAX_LINK_LENGTH);
        this.linkBox.setValue(link);
        this.addRenderableWidget(this.linkBox);
        this.setInitialFocus(this.linkBox);

        int row = this.top + 12 + ROW + 12;
        int buttons = this.top + 12 + ROW * 4 + 8;
        if (this.arrival) {
            stepper(row, () -> this.rotation = (this.rotation + 15) & 15, () -> this.rotation = (this.rotation + 1) & 15);
        } else {
            stepper(row, () -> this.zoneHeight = Math.max(1, this.zoneHeight - 1), () -> this.zoneHeight = Math.min(LoadingZoneMarkerBlock.MAX_HEIGHT, this.zoneHeight + 1));
            stepper(row + ROW, () -> this.order = Math.max(0, this.order - 1), () -> this.order = Math.min(LoadingZoneMarkerBlock.MAX_ORDER, this.order + 1));
            this.addRenderableWidget(new Button(this.left, row + ROW * 2, WIDTH, 20, leadsLabel(), button -> {
                this.leads = this.leads.next();
                button.setMessage(leadsLabel());
                this.pocketBox.visible = this.leads == LoadingZoneShape.Leads.POCKET;
            }));
            String pocket = this.pocketBox == null ? this.startPocket : this.pocketBox.getValue();
            this.pocketBox = new EditBox(this.font, this.left, row + ROW * 3 + 10, WIDTH, 20, new TextComponent("Pocket template or pool"));
            this.pocketBox.setMaxLength(LoadingZoneMarkerBlock.MAX_POCKET_LENGTH);
            this.pocketBox.setValue(pocket);
            this.pocketBox.visible = this.leads == LoadingZoneShape.Leads.POCKET;
            this.addRenderableWidget(this.pocketBox);
            buttons = row + ROW * 4 + 14;
        }
        this.addRenderableWidget(new Button(this.left, buttons, WIDTH / 2 - 2, 20, new TextComponent("Done"), button -> save()));
        this.addRenderableWidget(new Button(this.left + WIDTH / 2 + 2, buttons, WIDTH / 2 - 2, 20, new TextComponent("Cancel"), button -> this.onClose()));
    }

    private TextComponent leadsLabel() {
        return new TextComponent("Leads to: " + this.leads.label());
    }

    private void stepper(int y, Runnable down, Runnable up) {
        this.addRenderableWidget(new Button(this.left + WIDTH - 44, y, 20, 20, new TextComponent("-"), button -> down.run()));
        this.addRenderableWidget(new Button(this.left + WIDTH - 20, y, 20, 20, new TextComponent("+"), button -> up.run()));
    }

    private void save() {
        String pocket = this.pocketBox == null ? "" : this.pocketBox.getValue();
        ModNetwork.sendToServer(new LoadingZoneConfigMessage(this.pos, this.linkBox.getValue(), this.zoneHeight, this.order, this.rotation, this.leads, pocket));
        this.onClose();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            save();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void tick() {
        this.linkBox.tick();
        if (this.pocketBox != null) {
            this.pocketBox.tick();
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pose);
        drawCenteredString(pose, this.font, this.title, this.width / 2, this.top - 14, 0xFFFFFF);
        this.font.drawShadow(pose, this.arrival ? "Link name (the zone that sends players here)" : "Link name (posts that share it form one zone)", this.left, this.top, 0xA0A0A0);
        int row = this.top + 12 + ROW + 18;
        if (this.arrival) {
            this.font.drawShadow(pose, "Players arrive facing " + COMPASS[this.rotation] + " (" + this.rotation * 22.5F + "°)", this.left, row, 0xE0E0E0);
        } else {
            this.font.drawShadow(pose, "Zone height: " + this.zoneHeight + (this.zoneHeight == 1 ? " block" : " blocks"), this.left, row, 0xE0E0E0);
            this.font.drawShadow(pose, this.order == 0 ? "Corner number: automatic" : "Corner number: " + this.order, this.left, row + ROW, 0xE0E0E0);
            int hint = row - 6 + ROW * 3;
            switch (this.leads) {
                case ARRIVAL -> this.font.drawShadow(pose, "The arrival marker with this link, in this room", this.left, hint, 0xA0A0A0);
                case POCKET -> this.font.drawShadow(pose, "Vault template or pool id, like woldsvaults:vault/...", this.left, hint, 0xA0A0A0);
                case RETURN -> this.font.drawShadow(pose, "Back to where the player entered the pocket", this.left, hint, 0xA0A0A0);
            }
        }
        super.render(pose, mouseX, mouseY, partialTicks);
    }
}

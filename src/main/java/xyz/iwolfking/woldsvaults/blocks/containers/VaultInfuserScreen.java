package xyz.iwolfking.woldsvaults.blocks.containers;

import com.blakebr0.cucumber.client.screen.BaseContainerScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser.EjectModeSwitchButton;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser.InputLimitSwitchButton;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultInfuserTileEntity;

import java.util.ArrayList;
import java.util.List;

public class VaultInfuserScreen extends BaseContainerScreen<VaultInfuserContainer> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/infuser.png");
    private VaultInfuserTileEntity tile;

    public VaultInfuserScreen(VaultInfuserContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 176, 194);
    }

    @Override
    public void init() {
        super.init();

        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        var pos = this.getMenu().getPos();

        this.addRenderableWidget(new EjectModeSwitchButton(x + 69, y + 30, pos));
        this.addRenderableWidget(new InputLimitSwitchButton(x + 91, y + 74, pos, this::isLimitingInput));

        this.tile = this.getTileEntity();
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        super.render(matrix, mouseX, mouseY, partialTicks);

        if (mouseX > x + 60 && mouseX < x + 85 && mouseY > y + 74 && mouseY < y + 83) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getMaterialCount() < 1) {
                tooltip.add(new TextComponent(""));
            } else {
                if (this.hasMaterialStack()) {
                    tooltip.add(this.getMaterialStackDisplayName());
                }

                var text = new TextComponent(number(this.getMaterialCount()) + " / " + number(this.getMaterialsRequired()));

                tooltip.add(text);
            }

            this.renderComponentTooltip(matrix, tooltip, mouseX, mouseY);
        }

        if (mouseX > x + 68 && mouseX < x + 79 && mouseY > y + 28 && mouseY < y + 39) {
            if (this.isEjecting()) {
                this.renderTooltip(matrix, new TextComponent("Ejecting"), mouseX, mouseY);
            } else {
                this.renderTooltip(matrix, new TextComponent("Not Ejecting"), mouseX, mouseY);
            }
        }

        if (mouseX > x + 90 && mouseX < x + 98 && mouseY > y + 73 && mouseY < y + 84) {
            if (this.isLimitingInput()) {
                this.renderTooltip(matrix, new TextComponent("Limited Input"), mouseX, mouseY);
            } else {
                this.renderTooltip(matrix, new TextComponent("Unlimited Input"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        this.font.draw(stack, title, (float) (this.imageWidth / 2 - this.font.width(title) / 2), 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 8.0F, this.imageHeight - 94.0F, 4210752);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        super.renderDefaultBg(stack, partialTicks, mouseX, mouseY);

        int x = this.getGuiLeft();
        int y = this.getGuiTop();


        if (this.hasRecipe()) {
            if (this.getMaterialCount() > 0 && this.getMaterialsRequired() > 0) {
                int i2 = this.getMaterialBarScaled(26);
                this.blit(stack, x + 60, y + 74, 194, 19, i2 + 1, 10);
            }

            if (this.getProgress() > 0 && this.getInfuseDuration() > 0) {
                int i2 = this.getProgressBarScaled(24);
                this.blit(stack, x + 96, y + 47, 194, 0, i2 + 1, 16);
            }
        }

        if (this.isLimitingInput()) {
            this.blit(stack, x + 90, y + 74, 203, 56, 9, 10);
        }
    }

    private Component getMaterialStackDisplayName() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var container = this.getMenu();
            var tile = level.getBlockEntity(container.getPos());

            if (tile instanceof VaultInfuserTileEntity compressor) {
                var materialStack = compressor.getMaterialStack();

                return materialStack.getHoverName();
            }
        }

        return new TextComponent("");
    }

    private VaultInfuserTileEntity getTileEntity() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getPos());

            if (tile instanceof VaultInfuserTileEntity compressor)
                return compressor;
        }

        return null;
    }

    public boolean isEjecting() {
        if (this.tile == null)
            return false;

        return this.tile.isEjecting();
    }

    public boolean isLimitingInput() {
        if (this.tile == null)
            return false;

        return this.tile.isLimitingInput();
    }

    public boolean hasRecipe() {
        if (this.tile == null)
            return false;

        return this.tile.hasRecipe();
    }

    public boolean hasMaterialStack() {
        if (this.tile == null)
            return false;

        return this.tile.hasMaterialStack();
    }

    public int getProgress() {
        if (this.tile == null)
            return 0;

        return this.tile.getProgress();
    }

    public int getMaterialCount() {
        if (this.tile == null)
            return 0;

        return this.tile.getMaterialCount();
    }



    public int getEnergyRequired() {
        if (this.tile == null)
            return 0;

        return this.tile.getEnergyRequired();
    }

    public int getInfuseDuration() {
        if (this.tile == null)
            return 0;

        return this.tile.getInfuseDuration();
    }


    public int getMaterialsRequired() {
        if (this.tile == null)
            return 0;

        return this.tile.getMaterialsRequired();
    }


    public int getMaterialBarScaled(int pixels) {
        int i = Mth.clamp(this.getMaterialCount(), 0, this.getMaterialsRequired());
        int j = this.getMaterialsRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public int getProgressBarScaled(int pixels) {
        int i = this.getProgress();
        return (int) (this.getInfuseDuration() != 0 && i != 0 ? (long) i * pixels / this.getInfuseDuration() : 0);
    }
}

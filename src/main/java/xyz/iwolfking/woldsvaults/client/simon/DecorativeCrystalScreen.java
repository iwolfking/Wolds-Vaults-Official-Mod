package xyz.iwolfking.woldsvaults.client.simon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import xyz.iwolfking.woldsvaults.blocks.DecorativeCrystalBlock;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.DecorativeCrystalMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes a placed {@link DecorativeCrystalBlock}: its shape, its stage, its colour, whether it's Prismatic, and rerolls
 * which layout of the shape it uses. Every click applies straight away, and the controls sit down the left with the game
 * unpaused and undimmed, so the crystal can be watched changing behind them. The chosen shape, stage and colour are the
 * greyed-out buttons, and a Prismatic crystal has no colour to choose.
 */
public class DecorativeCrystalScreen extends Screen {
    private static final int LEFT = 16;
    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_STEP = 52;
    private static final int LABEL_DROP = 11;
    private static final int ROW_STEP = 36;
    private static final int STACK_STEP = BUTTON_HEIGHT + 4;

    private final BlockPos pos;
    private final List<Button> shapeButtons = new ArrayList<>();
    private final List<Button> stageButtons = new ArrayList<>();
    private final List<Button> colorButtons = new ArrayList<>();
    private Button prismaticButton;
    private Button rerollButton;
    private DecorativeCrystalBlock.Shape shape;
    private int variant;
    private int stage;
    private SimonCrystalEntity.Color color;
    private boolean prismatic;
    private int top;

    private DecorativeCrystalScreen(BlockPos pos, BlockState state) {
        super(new TextComponent("Decorative Crystal"));
        this.pos = pos;
        this.shape = state.getValue(DecorativeCrystalBlock.SHAPE);
        this.variant = state.getValue(DecorativeCrystalBlock.VARIANT);
        this.stage = state.getValue(DecorativeCrystalBlock.STAGE);
        this.color = state.getValue(DecorativeCrystalBlock.COLOR);
        this.prismatic = state.getValue(DecorativeCrystalBlock.PRISMATIC);
    }

    public static void open(BlockPos pos, BlockState state) {
        Minecraft.getInstance().setScreen(new DecorativeCrystalScreen(pos, state));
    }

    @Override
    protected void init() {
        this.top = this.height / 2 - 92;
        this.shapeButtons.clear();
        this.stageButtons.clear();
        this.colorButtons.clear();
        String[] stages = SimonCrystalEntity.STAGE_NAMES;
        int width = stages.length * BUTTON_STEP - (BUTTON_STEP - BUTTON_WIDTH);
        DecorativeCrystalBlock.Shape[] shapes = DecorativeCrystalBlock.Shape.values();
        int shapeStep = (width + 2) / shapes.length;
        for (int i = 0; i < shapes.length; i++) {
            DecorativeCrystalBlock.Shape chosen = shapes[i];
            this.shapeButtons.add(this.addRenderableWidget(new Button(LEFT + i * shapeStep, this.top + LABEL_DROP, shapeStep - 2, BUTTON_HEIGHT, new TextComponent(chosen.label), button -> {
                this.shape = chosen;
                this.apply();
            })));
        }
        for (int i = 0; i < stages.length; i++) {
            int chosen = i;
            this.stageButtons.add(this.addRenderableWidget(new Button(LEFT + i * BUTTON_STEP, this.top + ROW_STEP + LABEL_DROP, BUTTON_WIDTH, BUTTON_HEIGHT,
                    new TextComponent(StringUtils.capitalize(stages[i])), button -> {
                this.stage = chosen;
                this.apply();
            })));
        }
        SimonCrystalEntity.Color[] colors = SimonCrystalEntity.Color.values();
        for (int i = 0; i < colors.length; i++) {
            SimonCrystalEntity.Color chosen = colors[i];
            this.colorButtons.add(this.addRenderableWidget(new Button(LEFT + i * BUTTON_STEP, this.top + ROW_STEP * 2 + LABEL_DROP, BUTTON_WIDTH, BUTTON_HEIGHT,
                    new TextComponent(StringUtils.capitalize(chosen.id)).withStyle(Style.EMPTY.withColor(chosen.rgb)), button -> {
                this.color = chosen;
                this.apply();
            })));
        }
        int stack = this.top + ROW_STEP * 3;
        this.prismaticButton = this.addRenderableWidget(new Button(LEFT, stack, width, BUTTON_HEIGHT, TextComponent.EMPTY, button -> {
            this.prismatic = !this.prismatic;
            this.apply();
        }));
        this.rerollButton = this.addRenderableWidget(new Button(LEFT, stack + STACK_STEP, width, BUTTON_HEIGHT, TextComponent.EMPTY, button -> {
            this.variant = (this.variant + 1) % DecorativeCrystalBlock.VARIANTS;
            this.apply();
        }));
        this.addRenderableWidget(new Button(LEFT, stack + STACK_STEP * 2, width, BUTTON_HEIGHT, new TextComponent("Done"), button -> this.onClose()));
        this.refresh();
    }

    private void apply() {
        this.refresh();
        ModNetwork.sendToServer(new DecorativeCrystalMessage(this.pos, this.shape.ordinal(), this.variant, this.stage, this.color.ordinal(), this.prismatic));
    }

    private void refresh() {
        for (int i = 0; i < this.shapeButtons.size(); i++) {
            this.shapeButtons.get(i).active = i != this.shape.ordinal();
        }
        for (int i = 0; i < this.stageButtons.size(); i++) {
            this.stageButtons.get(i).active = i != this.stage;
        }
        for (int i = 0; i < this.colorButtons.size(); i++) {
            this.colorButtons.get(i).active = !this.prismatic && i != this.color.ordinal();
        }
        this.prismaticButton.setMessage(new TextComponent("Prismatic: " + (this.prismatic ? "On" : "Off")));
        this.rerollButton.setMessage(new TextComponent("Reroll (" + (this.variant + 1) + " of " + DecorativeCrystalBlock.VARIANTS + ")"));
    }

    // closes itself if the crystal is broken or left behind
    @Override
    public void tick() {
        if (this.minecraft.level == null || this.minecraft.player == null
                || !(this.minecraft.level.getBlockState(this.pos).getBlock() instanceof DecorativeCrystalBlock)
                || this.minecraft.player.distanceToSqr(Vec3.atCenterOf(this.pos)) > DecorativeCrystalBlock.REACH_SQR) {
            this.onClose();
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.font.drawShadow(pose, this.title, LEFT, this.top - 14, 0xFFFFFF);
        this.font.drawShadow(pose, "Shape", LEFT, this.top, 0xE0E0E0);
        this.font.drawShadow(pose, "Stage", LEFT, this.top + ROW_STEP, 0xE0E0E0);
        this.font.drawShadow(pose, "Colour", LEFT, this.top + ROW_STEP * 2, 0xE0E0E0);
        super.render(pose, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

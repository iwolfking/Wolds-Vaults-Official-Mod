package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.spi.ElementStore;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import iskallia.vault.client.gui.screen.player.legacy.widget.SkillWidget;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.SkillTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryTreeContainer;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Shared logic of the four read-only armory tree screens (Abilities, Talents, Expertises, Prestige): the armory
 * chrome swap, the saved-point overlay, the heading subtext, the "Load:" line and the +N / -N node tags.
 */
@OnlyIn(Dist.CLIENT)
public final class ArmoryTreeView {
    public static final int LOAD_CHANGE = 0x0F5A1C;
    public static final int LOAD_BLOCKED = 0x7E0000;
    public static final int LOAD_NONE = 0x404040;
    public static final int TAG_UP = 0xFF6EF08C;
    public static final int TAG_DOWN = 0xFFFF5A5A;
    public static final int TAG_BORDER = 0xFF101010;
    public static final int OVERLAY = 0xFFFFD27A;
    public static final int NAME_LEARNED = 0xFFF7C7;
    public static final int NAME_UNLEARNED = 0xFFFFFF;
    public static final int SUB_UNLEARNED = 0xC4C4C4;
    public static final String ARROW = " " + '▶' + " ";

    private final ArmoryViewInfo info;
    private final int savedPoints;

    private ArmoryTreeView(ArmoryViewInfo info, int savedPoints) {
        this.info = info;
        this.savedPoints = savedPoints;
    }

    /** The view for an armory tree screen's menu. The four screens are registered only for armory tree menus. */
    public static ArmoryTreeView of(AbstractContainerMenu menu) {
        if (menu instanceof ArmoryTreeContainer<?> container) {
            return new ArmoryTreeView(container.info(), container.savedPoints());
        }
        WoldsVaults.LOGGER.error("Armory: a tree screen opened on {}, which is not an armory tree menu", menu.getClass().getName());
        throw new IllegalStateException("Armory tree screen on a non-armory menu");
    }

    public ArmoryViewInfo info() {
        return this.info;
    }

    /** Whether the open page has a save. */
    public boolean saved() {
        int page = this.info.page();
        return page >= 0 && page < this.info.pageSaved().size() && this.info.pageSaved().get(page);
    }

    public MutableComponent title() {
        return new TextComponent(ArmoryTabs.title(this.info.tab()) + " - Build " + (this.info.page() + 1));
    }

    /** Whether Load on this armory would apply this tab: skills need the Skills upgrade, prestige a Creative Armory. */
    public boolean canApply() {
        return this.info.tab() == ArmoryTab.PRESTIGE ? this.info.creative() : this.info.skillsUpgrade();
    }

    /**
     * Swaps VH's tab strip for the armory's: removes {@link SkillTabContainerElement} (and anything {@code extra}
     * matches), then adds the armory top strip where VH placed its strip and the build tabs on the dialog's right edge.
     */
    public void installChrome(ElementStore store, Consumer<IElement> remove, Consumer<IElement> add,
                              Supplier<ISpatial> tabContent, Predicate<IElement> extra) {
        Set<IElement> found = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IElement> all = new ArrayList<>();
        all.addAll(store.getRenderedElementList());
        all.addAll(store.getGuiEventElementList());
        all.addAll(store.getSpatialElementList());
        all.addAll(store.getLayoutElementList());
        all.addAll(store.getTooltipElementList());
        boolean strip = false;
        for (IElement element : all) {
            boolean vhStrip = element instanceof SkillTabContainerElement;
            strip |= vhStrip;
            if (vhStrip || extra.test(element)) {
                found.add(element);
            }
        }
        if (!strip) {
            WoldsVaults.LOGGER.warn("Armory: the {} view found no VH tab strip to replace", this.info.tab());
        }
        found.forEach(remove);
        add.accept(ArmoryTabs.topStrip(Spatials.positionXYZ(15, -28, 1), this.info)
                .layout((screen, gui, parent, world) -> world.translateXY(tabContent.get())));
        add.accept(ArmoryTabs.buildTabs(Spatials.positionXYZ(0, 0, 1), this.info)
                .layout((screen, gui, parent, world) -> world.translateXY(screen.width() - 21, 44)));
    }

    /** Draws "N &lt;unit&gt;s saved" (or "Nothing saved") where VH draws the unspent point count. */
    public void renderPointOverlay(PoseStack poseStack, int screenWidth, String unit) {
        String text = this.saved()
                ? this.savedPoints + " " + unit + (this.savedPoints == 1 ? "" : "s") + " saved"
                : "Nothing saved";
        Font font = Minecraft.getInstance().font;
        font.drawShadow(poseStack, text, (float) (screenWidth - 30 - font.width(text)), 12.0F, OVERLAY);
    }

    /** Heading subtext for a tiered node holding {@code saved} of {@code max} tiers in the saved tree. */
    public String savedText(int saved, int max) {
        if (!this.saved()) {
            return "Nothing saved";
        }
        if (saved <= 0) {
            return "Saved: not learned";
        }
        return max <= 1 ? "Saved: learned" : "Saved: Tier " + saved + "/" + max;
    }

    /** The Load line for a tiered node: what Load would change it from ({@code live}) to ({@code saved}). */
    public Component loadLine(int live, int saved, int max) {
        String change = max <= 1
                ? learnedWord(live) + ARROW + learnedWord(saved)
                : "Tier " + live + ARROW + saved;
        return this.loadLine(live != saved, change);
    }

    /** The Load line given whether Load changes the node and how ({@code change}). */
    public Component loadLine(boolean changes, String change) {
        if (!this.saved()) {
            return colored("Load: nothing saved", LOAD_NONE);
        }
        if (!changes) {
            return colored("Load: no change", LOAD_NONE);
        }
        if (!this.canApply()) {
            String reason = this.info.tab() == ArmoryTab.PRESTIGE ? "only a Creative Armory loads prestige" : "needs the Skills upgrade";
            return colored("Load: " + change + " (" + reason + ")", LOAD_BLOCKED);
        }
        return colored("Load: " + change, LOAD_CHANGE);
    }

    private static String learnedWord(int tier) {
        return tier > 0 ? "learned" : "not learned";
    }

    public static MutableComponent colored(String text, int rgb) {
        return new TextComponent(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    /** The description box reaching down into the empty button band: bottom at dialog height - 10. */
    public static Rectangle descriptionBounds(Rectangle heading, Rectangle dialog) {
        int top = heading.y + heading.height + 10;
        return new Rectangle(heading.x, top, heading.width, dialog.height - 15 - top);
    }

    /** Renders the Load line, a blank line, then {@code description}, into {@code container}. */
    public static void renderDescription(PoseStack poseStack, ScrollableContainer container, Component load, @Nullable Component description) {
        Rectangle bounds = container.getRenderableBounds();
        MutableComponent text = new TextComponent("").append(load);
        if (description != null) {
            text.append("\n\n").append(description);
        }
        int lines = UIHelper.renderWrappedText(poseStack, text, bounds.width, 10);
        container.setInnerHeight(lines * 10 + 20);
        RenderSystem.enableDepthTest();
    }

    /**
     * SkillDialog's heading with the armory subtext: frame, node widget, name and {@code subText}.
     * {@code indent} is 5 for talents and expertises and 10 for prestige, as in the VH dialogs.
     */
    public static void renderSkillHeading(GuiComponent gui, PoseStack poseStack, Rectangle headingBounds, SkillWidget<?> widget,
                                          @Nullable SkillStyle style, String name, String subText, boolean learned, double indent,
                                          int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ScreenTextures.UI_RESOURCE);
        Rectangle widgetBounds = widget.getClickableBounds();
        UIHelper.renderContainerBorder(gui, poseStack, headingBounds, 14, 44, 2, 2, 2, 2, -7631989);
        poseStack.pushPose();
        poseStack.translate(indent, 0.0, 0.0);
        poseStack.pushPose();
        poseStack.translate(widgetBounds.width, 13.0, 0.0);
        FontHelper.drawTextComponent(poseStack, colored(name, learned ? NAME_LEARNED : NAME_UNLEARNED), false);
        poseStack.translate(0.0, 10.0, 0.0);
        FontHelper.drawTextComponent(poseStack, colored(subText, learned ? NAME_LEARNED : SUB_UNLEARNED), false);
        poseStack.popPose();
        if (style != null) {
            poseStack.translate(-style.x, -style.y, 0.0);
        }
        poseStack.translate(widgetBounds.getWidth() / 2.0, 23.0, 0.0);
        widget.render(poseStack, mouseX, mouseY, partialTicks);
        poseStack.popPose();
    }

    /**
     * Draws a node tag ("+N" green, "-N" red, 0.75 scale, dark border) with its right edge at {@code right} and its top
     * just above {@code top}. Call inside the pan region's clip; re-enables the depth test the clip relies on.
     */
    public static void renderTag(PoseStack poseStack, float right, float top, int delta) {
        if (delta == 0) {
            return;
        }
        String text = delta > 0 ? "+" + delta : "-" + (-delta);
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(right, top - 2.0F, 5.0);
        poseStack.scale(0.75F, 0.75F, 1.0F);
        FontHelper.drawStringWithBorder(poseStack, text, -font.width(text), 0.0F, delta > 0 ? TAG_UP : TAG_DOWN, TAG_BORDER);
        poseStack.popPose();
        RenderSystem.enableDepthTest();
    }

    /** Unmodified tier of every tiered node in {@code tree}, by id. */
    public static Map<String, Integer> tiers(@Nullable SkillTree tree) {
        Map<String, Integer> out = new HashMap<>();
        if (tree == null) {
            WoldsVaults.LOGGER.warn("Armory: no live tree to compare the saved view against; every saved node is tagged");
            return out;
        }
        tree.iterate(TieredSkill.class, tiered -> {
            if (tiered.getId() != null) {
                out.put(tiered.getId(), tiered.getUnmodifiedTier());
            }
        });
        return out;
    }

    /** Unmodified tier of node {@code id} in {@code tree}; 0 when it is missing or not tiered. */
    public static int tier(@Nullable SkillTree tree, String id) {
        Skill skill = tree == null ? null : tree.getForId(id).orElse(null);
        return skill instanceof TieredSkill tiered ? tiered.getUnmodifiedTier() : 0;
    }
}

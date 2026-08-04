package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.client.gui.helper.SkillFrame;
import iskallia.vault.config.entry.SkillStyle;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentStyleProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.List;

public abstract class WoldsTalentStyleProvider extends AbstractTalentStyleProvider {
    public static final int DEFAULT_X_SPACING = 64;
    public static final int DEFAULT_Y_SPACING = 80;

    public WoldsTalentStyleProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    public static class LayoutBuilder extends AbstractTalentStyleProvider.Builder {

        public LayoutBuilder addRow(int startX, int y, SkillFrame frameType, List<TalentElement> elements) {
            int currentX = startX;

            for (TalentElement element : elements) {
                if (element instanceof TalentData talent) {
                    SkillFrame nodeFrame = talent.frameType() != null ? talent.frameType() : frameType;
                    this.addStyle(
                            talent.name(),
                            new SkillStyle(currentX, y, talent.icon(), nodeFrame, talent.inactiveIcon())
                    );
                    currentX += DEFAULT_X_SPACING;

                } else if (element instanceof ChoiceGroup group) {
                    int count = group.choices().size();
                    int totalChoiceWidth = (count - 1) * group.choiceSpacing();
                    int choiceStartX = currentX - (totalChoiceWidth / 2);

                    for (int i = 0; i < count; i++) {
                        TalentData choice = group.choices().get(i);
                        int choiceX = choiceStartX + (i * group.choiceSpacing());
                        SkillFrame nodeFrame = choice.frameType() != null ? choice.frameType() : frameType;

                        this.addStyle(
                                choice.name(),
                                new SkillStyle(choiceX, y, choice.icon(), nodeFrame, choice.inactiveIcon())
                        );
                    }
                    currentX += DEFAULT_X_SPACING;

                } else if (element instanceof EmptySpace gap) {
                    currentX += gap.pixelWidth();
                }
            }
            return this;
        }

        public LayoutBuilder addRowBelow(int startX, int relativeToY, SkillFrame frameType, List<TalentElement> elements) {
            return addRow(startX, relativeToY + DEFAULT_Y_SPACING, frameType, elements);
        }
    }

    public sealed interface TalentElement permits TalentData, ChoiceGroup, EmptySpace {}

    public record EmptySpace(int pixelWidth) implements TalentElement {
        public static EmptySpace slots(int count) {
            return new EmptySpace(count * DEFAULT_X_SPACING);
        }
        public static EmptySpace px(int pixels) {
            return new EmptySpace(pixels);
        }
    }

    public record ChoiceGroup(List<TalentData> choices, int choiceSpacing) implements TalentElement {
        public ChoiceGroup(List<TalentData> choices) {
            this(choices, 48);
        }

        public static ChoiceGroup of(TalentData... choices) {
            return new ChoiceGroup(List.of(choices));
        }

        public static ChoiceGroup of(int spacing, TalentData... choices) {
            return new ChoiceGroup(List.of(choices), spacing);
        }
    }

    public record TalentData(
            String name,
            ResourceLocation icon,
            @Nullable ResourceLocation inactiveIcon,
            @Nullable SkillFrame frameType
    ) implements TalentElement {

        public TalentData(String name, String iconPath) {
            this(name, ResourceLocation.parse(iconPath), null, null);
        }

        public TalentData(String name, String iconPath, SkillFrame frame) {
            this(name, ResourceLocation.parse(iconPath), null, frame);
        }
    }
}
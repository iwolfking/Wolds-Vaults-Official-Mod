package xyz.iwolfking.woldsvaults.client.screens.stats;

import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.player.element.StatLabelElement;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.client.gui.screen.player.element.StatLabelListElement;

import java.util.List;
import java.util.Map;

/** The Statistics tab's player stat list split into categories: a header row, then the category's rows. */
public class CategorizedStatListElement extends StatLabelListElement<CategorizedStatListElement> {
    private static final int ROW_HEIGHT = 11;
    private static final int HEADER_GAP = 3;

    public CategorizedStatListElement(ISpatial spatial, Map<String, List<StatLabelElementBuilder<?>>> groups) {
        super(spatial, List.of());
        int y = 0;
        boolean first = true;
        for (Map.Entry<String, List<StatLabelElementBuilder<?>>> group : groups.entrySet()) {
            if (group.getValue().isEmpty()) {
                continue;
            }
            if (!first) {
                y += HEADER_GAP;
            }
            first = false;
            this.addElement(new StatCategoryHeaderElement(Spatials.positionY(y).height(StatCategoryHeaderElement.HEIGHT), group.getKey())
                    .layout((screen, gui, parent, world) -> world.width(parent)));
            y += StatCategoryHeaderElement.HEIGHT;
            for (StatLabelElementBuilder<?> builder : group.getValue()) {
                int rowY = y;
                StatLabelElement<?> row = builder.build(0, 0);
                row.layout((screen, gui, parent, world) -> world.translateY(rowY).width(parent));
                this.addElement(row);
                y += ROW_HEIGHT;
            }
        }
    }
}

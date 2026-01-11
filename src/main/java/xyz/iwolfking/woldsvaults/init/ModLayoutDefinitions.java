package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.api.core.layout.*;

public class ModLayoutDefinitions {
    public static void init() {
        LayoutRegistry.register(new CircleLayoutDefinition());
        LayoutRegistry.register(new SpiralLayoutDefinition());
        LayoutRegistry.register(new PolygonLayoutDefinition());
        LayoutRegistry.register(new InfiniteLayoutDefinition());
    }
}

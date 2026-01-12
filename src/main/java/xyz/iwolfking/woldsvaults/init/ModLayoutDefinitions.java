package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.api.core.layout.*;
import xyz.iwolfking.woldsvaults.api.core.layout.definitions.*;

public class ModLayoutDefinitions {
    public static void init() {
        LayoutDefinitionRegistry.register(new CircleLayoutDefinition());
        LayoutDefinitionRegistry.register(new SpiralLayoutDefinition());
        LayoutDefinitionRegistry.register(new PolygonLayoutDefinition());
        LayoutDefinitionRegistry.register(new InfiniteLayoutDefinition());
        LayoutDefinitionRegistry.register(new TunnelLayoutDefinition());
        LayoutDefinitionRegistry.register(new RingsLayoutDefinition());
        LayoutDefinitionRegistry.register(new WaveLayoutDefinition());
    }
}

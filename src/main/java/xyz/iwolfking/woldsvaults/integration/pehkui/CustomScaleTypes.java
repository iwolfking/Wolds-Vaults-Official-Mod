package xyz.iwolfking.woldsvaults.integration.pehkui;

import virtuoel.pehkui.api.*;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class CustomScaleTypes {


    public static final ScaleModifier SIZE_MODIFIER = registerModifier("size_modifier", new TypedScaleModifier(() -> CustomScaleTypes.SIZE));
    public static final ScaleModifier THICKNESS_MODIFIER = registerModifier("thickness_modifier", new TypedScaleModifier(() -> CustomScaleTypes.THICKNESS));
    public static final ScaleModifier SIZE_NO_MOVEMENT_MODIFIER = registerModifier("size_no_movement_modifier",new TypedScaleModifier(() -> CustomScaleTypes.SIZE_NO_MOVEMENT));

    public static final ScaleType SIZE_NO_MOVEMENT = registerScale("size_no_movement", CustomScaleTypes.SIZE_NO_MOVEMENT_MODIFIER);
    public static final ScaleType SIZE = registerScale("size", CustomScaleTypes.SIZE_MODIFIER);
    public static final ScaleType THICKNESS = registerScale("thickness", CustomScaleTypes.THICKNESS_MODIFIER);


    private static ScaleType registerScale(String id, ScaleModifier dependantModifier) {
        final ScaleType.Builder builder = ScaleType.Builder.create().affectsDimensions();

        builder.addDependentModifier(dependantModifier);

        return ScaleRegistries.register(ScaleRegistries.SCALE_TYPES,
                WoldsVaults.id(id),
                builder.build());
    }
    public static ScaleModifier registerModifier(String id, ScaleModifier modifier) {
        return ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, WoldsVaults.id(id), modifier);
    }

    public static void init() {
        ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().add(SIZE_MODIFIER);
        ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(SIZE_MODIFIER);
        ScaleTypes.VISIBILITY.getDefaultBaseValueModifiers().add(SIZE_MODIFIER);
        ScaleTypes.MOTION.getDefaultBaseValueModifiers().add(SIZE_MODIFIER);

        ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().add(SIZE_NO_MOVEMENT_MODIFIER);
        ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(SIZE_NO_MOVEMENT_MODIFIER);
        ScaleTypes.VISIBILITY.getDefaultBaseValueModifiers().add(SIZE_NO_MOVEMENT_MODIFIER);

        ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(THICKNESS_MODIFIER);
    }
}

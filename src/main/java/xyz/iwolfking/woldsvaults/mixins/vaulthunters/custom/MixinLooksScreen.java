package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.screen.player.looks.LooksScreen;
import iskallia.vault.looks.LooksCategory;
import iskallia.vault.www.patreon.PatreonTier;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LooksScreen.class, remap = false)
public abstract class MixinLooksScreen {

    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"
        )
    )
    private static List<Object> woldsVaults$replaceAuraTabInListOf(
        Object e1, Object e2, Object e3, Object e4, Object e5, Operation<List<Object>> original
    ) {
        List<Object> list = original.call(e1, e2, e3, e4, e5);
        try {
            Class<?> categoryTabClass = Class.forName(
                "iskallia.vault.client.gui.screen.player.looks.LooksScreen$CategoryTab"
            );
            Method categoryMethod = categoryTabClass.getDeclaredMethod("category");
            categoryMethod.setAccessible(true);

            Constructor<?> ctor = categoryTabClass.getDeclaredConstructor(
                LooksCategory.class,
                String.class,
                TextureAtlasRegion.class,
                String.class,
                int.class
            );
            ctor.setAccessible(true);

            Object replacementAuraTab = ctor.newInstance(
                LooksCategory.AURA,
                "Aura",
                null,
                PatreonTier.LEGEND.getDisplayPrefix(),
                PatreonTier.LEGEND.getColor().getValue()
            );

            List<Object> updated = new ArrayList<>(list.size());
            for (Object item : list) {
                if (categoryMethod.invoke(item) == LooksCategory.AURA) {
                    updated.add(replacementAuraTab);
                } else {
                    updated.add(item);
                }
            }
            return List.copyOf(updated);
        } catch (ReflectiveOperationException e) {
            return list;
        }
    }
}
package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.render.hud.module.PickupNotifierModule;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import xyz.iwolfking.woldsvaults.client.invhud.SliderSettingWithoutPercentage;
import xyz.iwolfking.woldsvaults.init.ModOptions;

import java.util.ArrayList;
import java.util.Arrays;

//TODO: Probably not needed any more?
@Mixin(value = PickupNotifierModule.class, remap = false)
public class MixinPickupNotifierModule {

//    @ModifyConstant(method = "renderModule(Liskallia/vault/client/render/hud/module/context/ModuleRenderContext;)V", constant = @Constant(intValue = 4))
//    private int modifyItemCount2(int constant){
//        return Math.max(0, ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.getValue()) - 1;
//    }
//
//    @ModifyConstant(method = {
//        "baseHeight(Liskallia/vault/client/render/hud/module/context/ModuleRenderContext;)I",
//        "renderModule(Liskallia/vault/client/render/hud/module/context/ModuleRenderContext;)V"
//    }, constant = @Constant(intValue = 5))
//    private int modifyHeight(int constant){
//        return Math.max(0, ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.getValue());
//    }
//
//    @ModifyConstant(method = {
//        "baseHeight(Liskallia/vault/client/render/hud/module/context/ModuleRenderContext;)I",
//        "renderModule(Liskallia/vault/client/render/hud/module/context/ModuleRenderContext;)V"
//    }, constant = @Constant(intValue = 3))
//    private int modifyHeightEditing(int constant){
//        return Math.max(0, ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.getValue());
//    }
//
//
//
//    @ModifyArg(method = "getSettings", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;asList([Ljava/lang/Object;)Ljava/util/List;"))
//    private Object[] modifyArg(Object[] a){
//        var list = new ArrayList<>(Arrays.asList(a)); // make it modifiable
//        list.add(new SliderSettingWithoutPercentage("pnItemCount", new TextComponent("Item Count"),
//            new TextComponent("Controls Max Item Count"),
//            () -> new TextComponent("Item Count: " + ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.getValue()),
//            () -> ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.getValue() / 32f,
//            value -> ModOptions.PICKUP_NOTIFIER_ITEM_COUNT.setValue(Math.max(0, Math.round(value * 32f)))
//        ));
//        return list.toArray();
//    }
}

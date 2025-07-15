package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.container.CurioContainerHandler;
import iskallia.vault.container.WardrobeContainer;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = CurioContainerHandler.class, remap = false)
public abstract class MixinCurioContainerHandler {
    @Shadow protected abstract LazyOptional<ICuriosItemHandler> getCuriosHandler();

    @Shadow protected abstract int calculateScrollStartIndex(int targetIndex, Map<String, ICurioStacksHandler> curioMap);

    @Shadow protected abstract void addSlots(Map<String, ICurioStacksHandler> curioMap, int startingIndex, int offsetY);

    @Shadow @Final private int offsetY;

    /**
     * @author iwolfking
     * @reason test
     */
    @Overwrite
    private void addCurioSlots(int targetIndex) {
        this.getCuriosHandler().ifPresent((itemHandler) -> {
            Map<String, ICurioStacksHandler> curioMap = itemHandler.getCurios();
            Map<String, ICurioStacksHandler> filteredCurioMap = curioMap.entrySet().stream().filter(stringICurioStacksHandlerEntry -> !stringICurioStacksHandlerEntry.getKey().equals("trinket_pouch")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            int startingIndex = this.calculateScrollStartIndex(targetIndex, filteredCurioMap);
            this.addSlots(filteredCurioMap, startingIndex, this.offsetY);
        });
    }
}

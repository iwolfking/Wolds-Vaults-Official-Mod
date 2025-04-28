package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import iskallia.vault.integration.IntegrationSB;
import iskallia.vault.util.InventoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.helper.WoldInventoryUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Mixin(value = InventoryUtil.class, remap = false)
public abstract class MixinInventoryUtil {
    @Shadow @Final @Mutable
    private static Set<Function<InventoryUtil.ItemAccess, List<InventoryUtil.ItemAccess>>> CONTENT_ACCESSORS = Set.of(IntegrationSB::getBackpackItemAccess, MixinInventoryUtil::getShulkerBoxAccess, MixinInventoryUtil::getBundleItemAccess, MixinInventoryUtil::getSatchelItemAccess, MixinInventoryUtil::getSupplementariesSafeAccess, MixinInventoryUtil::getSupplementariesSackAccess, MixinInventoryUtil::getBotaniaBaubleBoxAccess, InventoryUtil::getKeyringItemAccess, InventoryUtil::getCoinPouchItemAccess, WoldInventoryUtil::getCoinPouchItemAccess);

    @Shadow
    private static List<InventoryUtil.ItemAccess> getBotaniaBaubleBoxAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getSupplementariesSackAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getSupplementariesSafeAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getSatchelItemAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getBundleItemAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getKeyringItemAccess(InventoryUtil.ItemAccess access) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getCoinPouchItemAccess(InventoryUtil.ItemAccess access) {
        return null;
    }

    @Shadow
    private static List<InventoryUtil.ItemAccess> getShulkerBoxAccess(InventoryUtil.ItemAccess containerAccess) {
        return null;
    }
}

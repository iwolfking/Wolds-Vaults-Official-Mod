package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerBlackMarketData;
import org.spongepowered.asm.mixin.Mixin;

//TODO: Remove this
@Mixin(value = PlayerBlackMarketData.BlackMarket.class, remap = false)
public class MixinPlayerBlackMarketData {

}

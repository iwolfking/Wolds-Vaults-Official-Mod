package xyz.iwolfking.woldsvaults.mixins.plugins;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.List;

public class WoldMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> list, String s) {
        if(LoadingModList.get().getModFileById("ars_nouveau") != null) {
            if(s.equals("com.hollingsworth.arsnouveau.common.mixin.elytra.ClientElytraMixin")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("placebo") != null) {
            if(s.equals("shadows.placebo.mixin.ItemStackMixin")) {
                return true;
            }
        }
      
        if(LoadingModList.get().getModFileById("black_market_tweaks") != null) {
            if(s.equals("dev.attackeight.black_market_tweaks.mixin.BlackMarketTileEntityMixin")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("puzzleslib") != null){
            if(s.equals("fuzs.puzzleslib.mixin.client.MinecraftForgeMixin")) {
                return true;
            }
        }

        if(s.equals("com.dog.serverportals.mixin.PlayerListMixin")) {
            return true;
        }

        //Use Wold's version of this mixin instead of Unobtaniums
        if(s.equals("xyz.iwolfking.unobtainium.mixin.the_vault.fixes.FixSpawnersInRaidRooms")) {
            return true;
        }

        if(s.equals("com.dog.serverportals.mixin.ServerGamePacketListenerImplMixin")) {
            return true;
        }

        //Me on my way to strangle Pat
        if(s.equals("com.infamous.dungeons_mobs.mixin.SpiderModelMixin")) {
            return true;
        }

        if(s.equals("net.joseph.vaultfilters.mixin.other.MixinAttributeResearchBypass")) {
            return true;
        }

        if(s.equals("org.valkyrienskies.mod.mixin.feature.container_distance_check.MixinRandomizableContainerBlockEntity")) {
            return true;
        }




        return false;
    }
}

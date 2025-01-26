package xyz.iwolfking.woldsvaults.mixins.plugins;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.List;

public class WoldMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> list, String s) {
        if(LoadingModList.get().getModFileById("qolhunters") != null) {
            //I mixin same class, don't need the changes made in this.
            if(s.equals("io.iridium.qolhunters.mixin.brazier.MixinMonolithRenderer")) {
                return true;
            }
            //We don't want any descriptions meddled with in Wold's.
            else if (s.contains("io.iridium.qolhunters.mixin.betterdescriptions")) {
                return true;
            }
            //I mixin same class, don't need the change made here.
            else if(s.equals("io.iridium.qolhunters.mixin.paradox.MixinGateLockRenderer")) {
                return true;
            }
            else if(s.contains("io.iridium.qolhunters.mixin.rarityhighlighter")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("unobtainium") != null) {
            if(s.equals("xyz.iwolfking.unobtainium.mixin.the_vault.fixes.FixVaultChestStepBreaking")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("ars_nouveau") != null) {
            if(s.equals("com.hollingsworth.arsnouveau.common.mixin.elytra.ClientElytraMixin")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("the_vault") != null) {
            if(s.equals("iskallia.vault.mixin.MixinWorldChunk")) {
                return true;
            }
        }

        if(LoadingModList.get().getModFileById("vaultfaster") != null) {
            if(s.equals("implementslegend.mod.vaultfaster.mixin.MixinTemplateProcessorModifier")) {
                return true;
            }
        }



        return false;
    }
}

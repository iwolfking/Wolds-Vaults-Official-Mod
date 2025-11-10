package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.util.ThemeBlockRetriever;
import iskallia.vault.core.world.template.data.DirectTemplateEntry;
import iskallia.vault.core.world.template.data.IndirectTemplateEntry;
import iskallia.vault.core.world.template.data.TemplateEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = ThemeBlockRetriever.class, remap = false)
public class MixinThemeBlockRetriever {

    @ModifyArg(method = "generateRooms", at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/template/data/TemplatePool;iterate(Ljava/util/function/Predicate;)V"))
    private static Predicate<TemplateEntry> handleIndirectTemplates(Predicate<TemplateEntry> par1, @Local(name="palettes") List<PaletteKey> palettes) {
        return (template) -> woldsVaults$getPalettesRec(template, palettes); // using LinkedHashMap would be more efficient, but idc
    }

    @Unique private static boolean woldsVaults$getPalettesRec(TemplateEntry template, List<PaletteKey> palettes) {
        if (template instanceof IndirectTemplateEntry ite) { // we need to recursively visit all indirect templates to get all palettes used in rooms
            ite.getReference().get(Version.latest()).iterate(innerTemplate -> woldsVaults$getPalettesRec(innerTemplate, palettes));
        }
        if (template instanceof DirectTemplateEntry dte) {
            dte.getPalettes().forEach(palettes::add);
        }
        return true;
    }
}

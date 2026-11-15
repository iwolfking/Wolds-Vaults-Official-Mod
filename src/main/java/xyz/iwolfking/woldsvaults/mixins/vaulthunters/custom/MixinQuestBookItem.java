package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import iskallia.vault.item.QuestBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuestBookItem.class, remap = false)
public class MixinQuestBookItem {
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Liskallia/vault/item/QuestBookItem;openQuestScreen()V"), remap = true)
    private void openFTBQuestBook(QuestBookItem instance, Operation<Void> original) {
        ClientQuestFile.INSTANCE.openQuestGui();
    }
}

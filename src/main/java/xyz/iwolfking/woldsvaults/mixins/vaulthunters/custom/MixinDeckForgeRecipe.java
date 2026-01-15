package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.gear.crafting.recipe.DeckForgeRecipe;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.expertises.DeckMasterExpertise;

import java.util.List;
import java.util.Random;

@Mixin(value = DeckForgeRecipe.class, remap = false)
public class MixinDeckForgeRecipe {
    @Inject(method = "createOutput", at = @At(value = "TAIL"), cancellable = true)
    private void test(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack outputStack = cir.getReturnValue();
        Random random = new Random();
        if(outputStack.getItem() instanceof CardDeckItem) {
            ExpertiseTree tree = PlayerExpertisesData.get(crafter.getLevel()).getExpertises(crafter);

            float randomModChance = 0.0F;
            String forcedRollid = null;
            String poolId = "@default";

            for(DeckMasterExpertise expertise : tree.getAll(DeckMasterExpertise.class, DeckMasterExpertise::isUnlocked)) {
                randomModChance += expertise.getChance();
                forcedRollid = expertise.getForcedRollId();
                poolId = expertise.getPoolId();
            }

            if(randomModChance >= random.nextFloat()) {
                CardDeck deck = CardDeckItem.getCardDeck(outputStack).orElse(null);

                if(deck == null) {
                    return;
                }

                DeckModifier<?> modifier = ModConfigs.DECK_MODIFIERS.getRandom(poolId, ChunkRandom.ofNanoTime()).orElse(null);

                if(modifier == null) {
                    System.out.println("Modifier was null");
                    return;
                }

                if(forcedRollid != null) {
                    modifier.applyModifierRoll(forcedRollid);
                }

                modifier.onPopulate(ChunkRandom.ofNanoTime());

                deck.setSocketCount(deck.getSocketCount() + 1);
                deck.addModifier(modifier, ChunkRandom.ofNanoTime());
                CardDeckItem.setCardDeck(outputStack, deck);
                cir.setReturnValue(outputStack);
            }
        }
    }
}

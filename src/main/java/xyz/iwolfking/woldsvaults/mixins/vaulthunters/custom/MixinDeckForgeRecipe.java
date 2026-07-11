package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.gear.crafting.recipe.DeckForgeRecipe;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;
import xyz.iwolfking.woldsvaults.expertises.DeckMasterExpertise;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(value = DeckForgeRecipe.class, remap = false)
public class MixinDeckForgeRecipe {

    @Inject(method = "addCraftingDisplayTooltip", at = @At(value = "INVOKE", target = "Liskallia/vault/item/CardDeckItem;appendLayoutPreview(Ljava/lang/String;Ljava/util/List;Z)V"))
    private void addImplicitModifierText(ItemStack result, List<Component> out, CallbackInfo ci) {
        //Special handling for this deck!
        if(CardDeckItem.getId(result).equals("mystery")) {
            out.add(new TextComponent("Two random implicits from other decks"));
        }

        Optional<DeckModifier<?>> implicitDeckModifier = ImplicitDeckModifiersConfig.getImplicitDeckModifier(CardDeckItem.getId(result));
        if(implicitDeckModifier.isPresent()) {
            DeckModifier<?> modifier = DeckModifier.ADAPTER.writeJson(implicitDeckModifier.get()).flatMap(DeckModifier.ADAPTER::readJson).orElse(null);

            if(modifier == null) {
                return;
            }

            modifier.onPopulate(ChunkRandom.any());

            modifier.addText(out, out.size(), TooltipFlag.Default.NORMAL, 0F);
        }
    }

    @Inject(method = "createOutput", at = @At(value = "TAIL"), cancellable = true)
    private void test(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack outputStack = cir.getReturnValue();
        Random random = new Random();
        if(outputStack.getItem() instanceof CardDeckItem) {
            ExpertiseTree tree = PlayerExpertisesData.get(crafter.getLevel()).getExpertises(crafter);

            float randomModChance = 0.0F;
            String forcedRollid = null;
            String poolId = "@default";
            boolean modified = false;
            CardDeck deck = CardDeckItem.getCardDeck(outputStack).orElse(null);

            if(deck == null) {
                return;
            }

            for(DeckMasterExpertise expertise : tree.getAll(DeckMasterExpertise.class, DeckMasterExpertise::isUnlocked)) {
                randomModChance += expertise.getChance();
                forcedRollid = expertise.getForcedRollId();
                poolId = expertise.getPoolId();
            }

            //Handle Deck Master expertise
            if(randomModChance >= random.nextFloat()) {
                DeckModifier<?> modifierType = ModConfigs.DECK_MODIFIERS.getRandom(poolId, ChunkRandom.ofNanoTime()).orElse(null);
                DeckModifier<?> modifier = DeckModifier.ADAPTER.writeJson(modifierType).flatMap(DeckModifier.ADAPTER::readJson).orElse(null);

                if(modifier == null) {
                    return;
                }

                if(forcedRollid != null) {
                    modifier.applyModifierRoll(forcedRollid);
                }

                modifier.onPopulate(ChunkRandom.ofNanoTime());

                deck.setSocketCount(deck.getSocketCount() + 1);
                deck.addModifier(modifier, ChunkRandom.ofNanoTime());
                modified = true;
            }



            Optional<DeckModifier<?>> implicitDeckModifier = ImplicitDeckModifiersConfig.getImplicitDeckModifier(CardDeckItem.getId(outputStack));
            //Special handling for this deck!
            if(CardDeckItem.getId(outputStack).equals("mystery")) {
                modified = true;
                for(int i = 0; i < 2; i++) {
                    DeckModifier<?> randomMod = ModConfigs.DECK_MODIFIERS.getRandom("@card_deck_implicits", ChunkRandom.ofNanoTime()).orElse(null);
                    if (randomMod == null) {
                        return;
                    }

                    DeckModifier<?> modifier = DeckModifier.ADAPTER.writeJson(randomMod).flatMap(DeckModifier.ADAPTER::readJson).orElse(null);
                    if(modifier == null) {
                        return;
                    }

                    modifier.onPopulate(ChunkRandom.ofNanoTime());

                    deck.addModifier(new ImplicitDeckModifier(modifier), ChunkRandom.ofNanoTime());
                }
            }
            else if(implicitDeckModifier.isPresent()) {
                DeckModifier<?> modifier = DeckModifier.ADAPTER.writeJson(implicitDeckModifier.get()).flatMap(DeckModifier.ADAPTER::readJson).orElse(null);

                if(modifier == null) {
                    return;
                }

                modifier.onPopulate(ChunkRandom.ofNanoTime());

                deck.addModifier(modifier, ChunkRandom.ofNanoTime());
                modified = true;
            }

            if(modified) {
                CardDeckItem.setCardDeck(outputStack, deck);
                cir.setReturnValue(outputStack);
            }
        }
    }
}

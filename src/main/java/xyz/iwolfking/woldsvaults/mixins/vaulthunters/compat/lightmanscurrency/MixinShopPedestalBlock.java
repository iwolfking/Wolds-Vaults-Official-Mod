package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.block.ShopPedestalBlock;
import iskallia.vault.block.entity.ShopPedestalBlockTile;
import iskallia.vault.config.ShopPedestalConfig;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicLootLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.ParadoxObjective;
import iskallia.vault.event.event.ShopPedestalPriceEvent;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CoinBlockItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.util.LootInitialization;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.helper.ShopPedestalHelper;
import xyz.iwolfking.woldsvaults.expertises.BlessedExpertise;
import xyz.iwolfking.woldsvaults.expertises.ShopRerollExpertise;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.List;
import java.util.Optional;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = ShopPedestalBlock.class, remap = false)
public abstract class MixinShopPedestalBlock extends Block implements EntityBlock, GameMasterBlock {
    @Unique
    private static final BooleanProperty REROLLED = BooleanProperty.create("rerolled");

    @Shadow @Final public static BooleanProperty ACTIVE;

    public MixinShopPedestalBlock(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = true)
    private void overrideDefaultBlockstate(CallbackInfo ci) {
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, true).setValue(REROLLED, false));
    }

    /**
     * @author iwolfking
     * @reason Handle shop pedestal to extend coin definitions
     */
    @Overwrite(remap = true) @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack c;
        if (worldIn.getBlockEntity(pos) instanceof ShopPedestalBlockTile tile) {
            if(ServerVaults.get(worldIn).isPresent()) {
                Optional<Vault> vaultOpt = ServerVaults.get(worldIn);

                if(vaultOpt.isPresent()) {
                    Vault vault = vaultOpt.get();

                    Objective.ObjList objectives = vault.get(Vault.OBJECTIVES).get(Objectives.LIST);
                    for(Objective obj : objectives) {
                        if(obj instanceof ParadoxObjective paradoxObjective) {
                            if(paradoxObjective.get(ParadoxObjective.TYPE).equals(ParadoxObjective.Type.BUILD)) {
                                return InteractionResult.FAIL;
                            }
                        }
                    }
                }
            }

            if(tile.isInitialized() && player.isShiftKeyDown()) {
                if(state.getValue(REROLLED) || player.hasEffect(ModEffects.REROLLED_TIMEOUT)) {
                    return InteractionResult.FAIL;
                }

                if(!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    ExpertiseTree expertises = PlayerExpertisesData.get(serverPlayer.getLevel()).getExpertises(player);
                    boolean hasExpertise = false;
                    for (ShopRerollExpertise expertise : expertises.getAll(ShopRerollExpertise.class, Skill::isUnlocked)) {
                        hasExpertise = true;
                        break;
                    }
                    if(!hasExpertise) {
                        return InteractionResult.FAIL;
                    }
                }

                Vault vault = ServerVaults.get(worldIn).orElse(null);
                if(vault == null) {
                    return InteractionResult.FAIL;
                }

                ShopPedestalConfig.ShopOffer offer = ShopPedestalHelper.generatePedestalOffer(state, worldIn, JavaRandom.ofNanoTime());

                if (offer != null && !offer.isEmpty()) {
                    ItemStack stack = LootInitialization.initializeVaultLoot(offer.offer(), vault, pos, JavaRandom.ofNanoTime());
                    tile.setOffer(stack, OverSizedItemStack.of(offer.currency().overSizedStack()));
                }

                if(!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(REROLLED, true), 3);
                }

                tile.setInitialized(true);
                tile.setChanged();
                worldIn.sendBlockUpdated(pos, state, state.setValue(REROLLED, true), 3);
                if(!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    ExpertiseTree expertises = PlayerExpertisesData.get(serverPlayer.getLevel()).getExpertises(player);
                    int rerollTimeout = 0;
                    for (ShopRerollExpertise expertise : expertises.getAll(ShopRerollExpertise.class, Skill::isUnlocked)) {
                        rerollTimeout += expertise.getRerollTimeout();
                    }
                    player.addEffect(new MobEffectInstance(ModEffects.REROLLED_TIMEOUT, rerollTimeout));
                }

            }
            else if (tile.isInitialized()) {
                c = tile.getOfferStack();
                if (!c.isEmpty()) {
                    ShopPedestalPriceEvent event = new ShopPedestalPriceEvent(player, c, tile.getCurrencyStack());
                    MinecraftForge.EVENT_BUS.post(event);
                    ItemStack currency = event.getCost();

                    return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map(itemHandler -> {
                        List<InventoryUtil.ItemAccess> allItems = List.of();
                        if (!player.isCreative()) {
                            allItems = InventoryUtil.findAllItems(player);
                            if(!(currency.getItem() instanceof CoinBlockItem)) {
                                if(!hasEnoughCurrency(allItems, currency)) {
                                    if (worldIn.isClientSide) {
                                        player.displayClientMessage(new TranslatableComponent("message.the_vault.shop_pedestal.fail", currency.getHoverName()), true);
                                    }

                                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                                }
                            }
                            else if(!woldsVaults$lightmansCurrencyExtract(player, currency)) {
                                if (worldIn.isClientSide) {
                                    player.displayClientMessage(new TranslatableComponent("message.the_vault.shop_pedestal.fail", currency.getHoverName()), true);
                                }

                                return InteractionResult.sidedSuccess(worldIn.isClientSide);
                            }
                        }


                        if (!worldIn.isClientSide) {
                            if (!player.isCreative()) {
                                if(!(currency.getItem() instanceof CoinBlockItem)) {
                                    deductItems(allItems, currency.getCount(), currency);
                                }
                                BlockState inactiveState = state.setValue(ACTIVE, false);
                                tile.setRemoved();
                                worldIn.setBlockAndUpdate(pos, inactiveState);
                            }

                            popResource(worldIn, player.getOnPos().above(), c.copy());
                            worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                        } else {
                            if (!player.getAbilities().instabuild) {
                                tile.setRemoved();
                            }

                            player.displayClientMessage(new TranslatableComponent("message.the_vault.shop_pedestal.purchase", c.getCount(), c.getHoverName(), currency.getCount(), currency.getHoverName()), true);
                        }

                        return InteractionResult.sidedSuccess(worldIn.isClientSide);
                    }).orElse(InteractionResult.PASS);
                }

                return InteractionResult.PASS;
            }
        }

        if (player.getAbilities().instabuild) {
            ItemStack o = player.getItemInHand(InteractionHand.MAIN_HAND);
            c = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!c.isEmpty() && !o.isEmpty()) {
                worldIn.setBlockAndUpdate(pos, state.setValue(ACTIVE, true));
                if (worldIn.getBlockEntity(pos) instanceof ShopPedestalBlockTile tile) {
                    tile.setOffer(o.copy(), OverSizedItemStack.of(c.copy()));
                    tile.setChanged();
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }

                worldIn.setBlockAndUpdate(pos, state.setValue(ACTIVE, false));
            }
        }

        return InteractionResult.PASS;
    }




    @Unique
    private boolean woldsVaults$lightmansCurrencyExtract(Player player, ItemStack costStack) {
        CoinValue cValue = CoinValue.fromItemOrValue(costStack.getItem(), costStack.getCount(), 81L);
        return (MoneyUtil.ProcessPayment(player.getInventory(), player, cValue));
    }

    private static boolean hasEnoughCurrency(List<InventoryUtil.ItemAccess> allItems, ItemStack currency) {
            int priceValue = currency.getCount();

            for(InventoryUtil.ItemAccess itemAccess : allItems) {
                if(itemAccess.getStack().getItem().equals(currency.getItem())) {
                    priceValue -= itemAccess.getStack().getCount();
                }

                if (priceValue <= 0) {
                    return true;
                }
            }

            return false;
    }

    private static int deductItems(List<InventoryUtil.ItemAccess> allItems, int priceValue, ItemStack currencyStack) {
        for(InventoryUtil.ItemAccess itemAccess : allItems) {
            ItemStack stack = itemAccess.getStack();
            if (stack.getItem() ==  currencyStack.getItem()) {
                int countToRemove = Math.min(priceValue, stack.getCount());
                if (countToRemove > 0) {
                    itemAccess.setStack(ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - countToRemove));
                    priceValue -= countToRemove;
                    if (priceValue <= 0) {
                        break;
                    }
                }
            }
        }

        return priceValue;
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"), remap = true)
    protected void addRerolledState(StateDefinition.Builder<Block, BlockState> pBuilder, CallbackInfo ci) {
        pBuilder.add(new Property[]{REROLLED});
    }
}

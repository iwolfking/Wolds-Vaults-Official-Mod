package xyz.iwolfking.woldsvaults.blocks.tiles;

import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.cucumber.util.Localizable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import xyz.iwolfking.woldsvaults.blocks.VaultInfuserBlock;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultInfuserContainer;
import xyz.iwolfking.woldsvaults.data.recipes.CachedInfuserRecipeData;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

public class VaultInfuserTileEntity extends BaseInventoryTileEntity implements MenuProvider {
    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private InfuserRecipe recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean ejecting = false;
    private boolean inputLimit = true;
    private final int processingSpeed;
    private final LazyOptional<IItemHandler> capability = LazyOptional.of(this::getInventory);

    public VaultInfuserTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VAULT_INFUSER_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
        this.inventory = createInventoryHandler(null);
        this.recipeInventory = new BaseItemStackHandler(2);
        if(CachedInfuserRecipeData.shouldCache() && this.level != null) {
            CachedInfuserRecipeData.cacheCatalysts(this.level);
            CachedInfuserRecipeData.cacheIngredients(this.level);
        }
        Block block = (VaultInfuserBlock) state.getBlock();
        if(block instanceof VaultInfuserBlock vaultInfuserBlock && vaultInfuserBlock.getProcessingSpeed() != 0) {
            processingSpeed = vaultInfuserBlock.getProcessingSpeed();
        }
        else {
            processingSpeed = 1;
        }

    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.materialCount = tag.getInt("MaterialCount");
        this.materialStack = ItemStack.of(tag.getCompound("MaterialStack"));
        this.progress = tag.getInt("Progress");
        this.ejecting = tag.getBoolean("Ejecting");
        this.inputLimit = tag.getBoolean("InputLimit");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.serializeNBT());
        tag.putInt("Progress", this.progress);
        tag.putBoolean("Ejecting", this.ejecting);
        tag.putBoolean("InputLimit", this.inputLimit);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!this.isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.capability);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Localizable.of("container.woldsvaults.infuser").build();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return VaultInfuserContainer.create(windowId, playerInventory, this::isUsableByPlayer, this.inventory, new SimpleContainerData(0), this.getBlockPos());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VaultInfuserTileEntity tile) {
        var mark = false;
        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);
        var catalyst = tile.inventory.getStackInSlot(2);

        tile.recipeInventory.setStackInSlot(0, tile.materialStack);
        tile.recipeInventory.setStackInSlot(1, catalyst);

        if (tile.recipe == null || !tile.recipe.matches(tile.recipeInventory)) {
            tile.recipe = (InfuserRecipe) level.getRecipeManager().getRecipeFor(ModRecipeTypes.INFUSER, tile.recipeInventory.toIInventory(), level).orElse(null);
            tile.progress = 0;
            mark = true;
        }

        if (!level.isClientSide()) {
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();
                    mark = true;
                }

                if (!tile.inputLimit || (tile.recipe != null && tile.materialCount < tile.recipe.getInputCount())) {
                    if (StackHelper.areStacksEqual(input, tile.materialStack)) {
                        int consumeAmount = input.getCount();
                        if (tile.inputLimit) {
                            consumeAmount = Math.min(consumeAmount, tile.recipe.getInputCount() - tile.materialCount);
                        }

                        input.shrink(consumeAmount);
                        tile.materialCount += consumeAmount;


                        mark = true;
                    }
                }
            }

            if (tile.recipe != null) {
                if (tile.materialCount >= tile.recipe.getInputCount()) {
                    if (tile.progress >= tile.recipe.getInfuseDuration()) {
                        var result = tile.recipe.assemble(tile.inventory);

                        if (StackHelper.canCombineStacks(result, output)) {
                            tile.updateResult(result);
                            tile.progress = 0;
                            tile.materialCount -= tile.recipe.getInputCount();
                            catalyst.shrink(1);
                            if (tile.materialCount <= 0) {
                                tile.materialStack = ItemStack.EMPTY;
                            }
                            mark = true;
                        }
                    } else {
                        tile.process(tile.recipe);
                        mark = true;}
                }
            }

            if (tile.ejecting) {
                if (tile.materialCount > 0 && !tile.materialStack.isEmpty() && (output.isEmpty() || StackHelper.areStacksEqual(tile.materialStack, output))) {
                    int addCount = Math.min(tile.materialCount, tile.materialStack.getMaxStackSize() - output.getCount());
                    if (addCount > 0) {
                        var toAdd = StackHelper.withSize(tile.materialStack, addCount, false);

                        tile.updateResult(toAdd);
                        tile.materialCount -= addCount;

                        if (tile.materialCount < 1) {
                            tile.materialStack = ItemStack.EMPTY;
                            tile.ejecting = false;
                        }

                        if (tile.progress > 0)
                            tile.progress = 0;

                        if (!mark)
                            mark = true;
                    }
                }
            }
        }

        if (mark) {
            tile.markDirtyAndDispatch();
        }
    }

    public static BaseItemStackHandler createInventoryHandler(Runnable onContentsChanged) {
        var inventory = new BaseItemStackHandler(3, onContentsChanged);

        inventory.setOutputSlots(0);
        inventory.setSlotValidator((slot, stack) -> {

            if(slot == 1 && CachedInfuserRecipeData.getIngredients().contains(stack.getItem())) {
                return true;
            }
            else return slot == 2 && CachedInfuserRecipeData.getCatalysts().contains(stack.getItem());
        });

        return inventory;
    }

    public ItemStack getMaterialStack() {
        return this.materialStack;
    }

    public boolean hasMaterialStack() {
        return !this.materialStack.isEmpty();
    }

    public int getMaterialCount() {
        return this.materialCount;
    }

    public boolean isEjecting() {
        return this.ejecting;
    }

    public void toggleEjecting() {
        if (this.materialCount > 0) {
            this.ejecting = !this.ejecting;
            this.markDirtyAndDispatch();
        }
    }

    public boolean isLimitingInput() {
        return this.inputLimit;
    }

    public void toggleInputLimit() {
        this.inputLimit = !this.inputLimit;
        this.markDirtyAndDispatch();
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean hasRecipe() {
        return this.recipe != null;
    }

    public InfuserRecipe getActiveRecipe() {
        return this.recipe;
    }

    public int getEnergyRequired() {
        return 0;
    }

    public int getInfuseDuration() {
        return this.recipe.getInfuseDuration();
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return this.recipe.getInputCount();

        return 0;
    }

    private void process(InfuserRecipe recipe) {
        int extract = this.processingSpeed;
        int difference = recipe.getInfuseDuration() - this.progress;
        if (difference < extract)
            extract = difference;

        this.progress += extract;
    }

    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, StackHelper.grow(result, stack.getCount()));
        }
    }
}

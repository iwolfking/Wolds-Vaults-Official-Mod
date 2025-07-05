package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.AlchemyIngredientItem;

public class BrewingAltarTileEntity extends BlockEntity {
    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(3, new ItemStack(ModItems.INGREDIENT_TEMPLATE));

    public BrewingAltarTileEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlocks.BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE, pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, ingredients);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        ContainerHelper.loadAllItems(pTag, ingredients);
    }


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag);
        return compoundtag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    public void sendUpdates() {
        if(this.level == null) return;

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
    }

    public NonNullList<ItemStack> getAllIngredients() {
        return ingredients;
    }

    public ItemStack getIngredient(int index) {
        return ingredients.get(index);
    }

    public void setIngredient(int index, ItemStack stack) {
        this.ingredients.set(index, stack);
        sendUpdates();
    }

    public void removeIngredient(int index) {
        this.ingredients.set(index, new ItemStack(ModItems.INGREDIENT_TEMPLATE));
        sendUpdates();
    }

    /**
     * adds an ingredient to the ingredient inventory
     *
     * @param stack Ingredient to be added
     * @return true if successfully added, false if not
     */
    public boolean addIngredient(ItemStack stack) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (getIngredient(i).getItem() == ModItems.INGREDIENT_TEMPLATE) {
                setIngredient(i, stack);
                return true;
            }
        }
        return false;
    }

    /**
     * removes the itemstack that was last added
     * @return the removed ItemStack, if there was none to remove, ItemStack.EMPTY is returned instead
     */
    public ItemStack removeLastIngredient() {
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            if (getIngredient(i).getItem() != ModItems.INGREDIENT_TEMPLATE) {
                ItemStack removedStack = getIngredient(i).copy();
                removeIngredient(i);
                return removedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * gets the itemstack that was last added
     * @return the ItemStack, if there was none, ItemStack.EMPTY is returned instead
     */
    public ItemStack getLastIngredient() {
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            if (getIngredient(i).getItem() != ModItems.INGREDIENT_TEMPLATE) {
                return getIngredient(i);
            }
        }
        return ItemStack.EMPTY;
    }
}

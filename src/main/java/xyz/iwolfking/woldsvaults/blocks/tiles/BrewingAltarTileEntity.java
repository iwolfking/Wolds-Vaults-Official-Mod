package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.block.entity.AlchemyTableTileEntity;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ClientboundAlchemyParticleMessage;
import iskallia.vault.network.message.ClientboundAlchemySecondParticleMessage;
import iskallia.vault.network.message.ClientboundTESyncMessage;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.BrewingAltar;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;

import java.util.List;
import java.util.Optional;

public class BrewingAltarTileEntity extends BlockEntity {
    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(3, new ItemStack(ModItems.INGREDIENT_TEMPLATE));
    private ItemStack catalyst = new ItemStack(ModItems.INGREDIENT_TEMPLATE); // If ingredient template -> then null

    private boolean brewing = false;
    private int cookingTimer = -1;
    private boolean hasExpired = false;
    private PercentageResult progressIncrease = new PercentageResult(-1F, -1F); // this is to prevent desync when the client has different configs for the objective


    public BrewingAltarTileEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlocks.BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE, pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, ingredients);

        pTag.putFloat("ProgressMin", progressIncrease.min());
        pTag.putFloat("ProgressMax", progressIncrease.max());
        pTag.putBoolean("Brewing", brewing);
        pTag.putInt("CookingTimer", cookingTimer);

        CompoundTag catalystTag = new CompoundTag();
        catalyst.save(catalystTag);
        pTag.put("Catalyst", catalystTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        ContainerHelper.loadAllItems(pTag, ingredients);

        if (pTag.contains("ProgressMin") && pTag.contains("ProgressMax")) {
            progressIncrease = new PercentageResult(pTag.getFloat("ProgressMin"), pTag.getFloat("ProgressMax"));
        } else {
            progressIncrease = new PercentageResult(-1F, -1F); // fallback
        }

        brewing = pTag.getBoolean("Brewing");
        cookingTimer = pTag.getInt("CookingTimer");
        if (pTag.get("Catalyst") instanceof CompoundTag t) {
            catalyst = ItemStack.of(t);
        } else {
            WoldsVaults.LOGGER.error("Catalyst Tag is not of type CompoundTag in BrewingAltarTileEntity. Bad things are happening");
        }

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

        progressIncrease = calculateObjectivePercentage();

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
    }

    public void setBrewing(boolean brewing) {
        this.brewing = brewing;
        this.cookingTimer = 60;
        sendUpdates();
    }

    public boolean isBrewing() {
        return brewing;
    }

    public void setHasExpired(boolean hasExpired) {
        this.hasExpired = hasExpired;
    }

    public boolean hasExpired() {
        return hasExpired;
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public Optional<CatalystItem.CatalystType> getCatalystType() {
        if (getCatalyst().getItem() instanceof CatalystItem item) {
            return Optional.of(item.getType());
        }
        return Optional.empty();
    }

    public void setCatalyst(ItemStack catalyst) {
            this.catalyst = catalyst;
            sendUpdates();
    }

    public boolean addCatalyst(ItemStack catalyst) {
        if (getCatalyst().getItem() == ModItems.INGREDIENT_TEMPLATE) {
            setCatalyst(catalyst);
            return true; // We updated the item.
        }

        return false; // There is already a Catalyst in the altar!
    }

    public ItemStack removeCatalyst() {
        ItemStack catalyst = getCatalyst().getItem() == ModItems.INGREDIENT_TEMPLATE ? null : getCatalyst().copy();
        setCatalyst(new ItemStack(ModItems.INGREDIENT_TEMPLATE));
        return catalyst;
    }

    // this also returns the filler items
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

    public void clearIngredients() {
        for (int i = 0; i < ingredients.size(); i++) {
            if (getIngredient(i).getItem() != ModItems.INGREDIENT_TEMPLATE) {
                setIngredient(i, ModItems.INGREDIENT_TEMPLATE.getDefaultInstance());
            }
        }
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

    public boolean isFilled() {
        for (int i = 0; i < ingredients.size(); i++) {
            if (getIngredient(i).getItem() == ModItems.INGREDIENT_TEMPLATE) {
                return false;
            }
        }
        return true;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(this.level != null){
            if (this.level.isClientSide()) return;
            if (!brewing) return;


            if (cookingTimer > 0) {

                if(cookingTimer == 60) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.BLOCKS, 0.35f, 1.5f);
                }
                if(cookingTimer == 60 - 5) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.BLOCKS, 0.35f, 1.5f);
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2f, 1f);
                }
                if(cookingTimer == 35) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5f, 1.5f);
                }
                if(cookingTimer == 20) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 0.5f, 1.5f);
                }
                if(cookingTimer == 19) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 0.45f, 2f);
                }

                int color = AlchemyIngredientItem.getMixedColor(ingredients);

                if(cookingTimer > 40 && cookingTimer < 60 - 2){

                    float f = 1 - ((cookingTimer - 40) / (float)(AlchemyTableTileEntity.CRAFTING_COOLDOWN - 40));
                    f = AlchemyTableTileEntity.ease(f);
                    float yOffset = (float)Math.sin(f * Math.PI / 1.5f) / 8;



                    ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new ClientboundAlchemyParticleMessage(this.getBlockPos(), Direction.NORTH, color, yOffset));
                }
                if(cookingTimer == 20) {
                    ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new ClientboundAlchemySecondParticleMessage(this.getBlockPos(), color));
                }

                cookingTimer--;
                return;
            }
            int newUses = Math.max(0, blockState.getValue(BrewingAltar.USES) - 1);
            BlockState newState = blockState.setValue(BrewingAltar.USES, newUses);
            level.setBlock(blockPos, newState, Block.UPDATE_CLIENTS);

            WoldCommonEvents.BREWING_ALTAR_BREW_EVENT.invoke(level, blockState, blockPos, this, List.copyOf(ingredients));
            brewing = false;
            clearIngredients();
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new ClientboundTESyncMessage(worldPosition, this.saveWithoutMetadata()));



            if (blockState.getValue(BrewingAltar.USES) == 0) {
                this.level.playSound(null, this.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.1f, 1f);
            }
        } else {

            if (!brewing) {
                return;
            }

            if (cookingTimer > 0) {
                cookingTimer--;
            }
        }
    }

    private PercentageResult calculateObjectivePercentage() {
        if (level == null) return new PercentageResult(-1F, -1F);
        if (level.isClientSide()) return new PercentageResult(-1F, -1F);
        if (ServerVaults.get(level).isEmpty()) return new PercentageResult(-1F, -1F);

        int vaultLevel = ServerVaults.get(level).get().get(Vault.LEVEL).get();
        AlchemyObjectiveConfig.Entry cfg = ModConfigs.ALCHEMY_OBJECTIVE.getConfig(vaultLevel);

        float min = 0F;
        float max = 0F;

        for (ItemStack i : ingredients) {
            if (i.getItem() instanceof AlchemyIngredientItem item) {
                PercentageResult res = cfg.getPercentageForType(item);
                min += res.min();
                max += res.max();
            }
        }

        return new PercentageResult(min, max);
    }

    public PercentageResult getProgressIncrease() {
        return progressIncrease;
    }

    public static class PercentageResult {
        private float min;
        private float max;

        public PercentageResult(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float min() {
            return min;
        }

        public float max() {
            return max;
        }

        public void setMin(float min) {
            this.min = min;
        }

        public void setMax(float max) {
            this.max = max;
        }



        public boolean isRange() {
            return min != max;
        }


    }
}

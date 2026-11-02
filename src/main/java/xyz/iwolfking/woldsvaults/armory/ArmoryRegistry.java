package xyz.iwolfking.woldsvaults.armory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlock;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryGodMenus;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryTreeMenus;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

import javax.annotation.Nullable;
import java.util.List;

/** Registers the armory blocks, items, block entity and menus. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ArmoryRegistry {
    public static ArmoryBlock ARMORY;
    public static ArmoryBlock CREATIVE_ARMORY;
    public static Item SKILLS_UPGRADE;
    public static Item GODS_UPGRADE;
    public static BlockEntityType<ArmoryBlockEntity> ARMORY_ENTITY;
    public static MenuType<ArmoryMenu> ARMORY_MENU;

    private ArmoryRegistry() {
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.Register<Block> event) {
        ARMORY = new ArmoryBlock(false);
        CREATIVE_ARMORY = new ArmoryBlock(true);
        event.getRegistry().register(ARMORY.setRegistryName(WoldsVaults.id("armory")));
        event.getRegistry().register(CREATIVE_ARMORY.setRegistryName(WoldsVaults.id("creative_armory")));
    }

    @SubscribeEvent
    public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new BlockItem(ARMORY, new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS).stacksTo(1))
                .setRegistryName(WoldsVaults.id("armory")));
        event.getRegistry().register(new BlockItem(CREATIVE_ARMORY, new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS)
                .stacksTo(1).rarity(Rarity.EPIC)).setRegistryName(WoldsVaults.id("creative_armory")));
        SKILLS_UPGRADE = new UpgradeItem("Load also sets abilities, talents and expertises.");
        GODS_UPGRADE = new UpgradeItem("Load also sets god constellation stars.");
        event.getRegistry().register(SKILLS_UPGRADE.setRegistryName(WoldsVaults.id("armory_skills_upgrade")));
        event.getRegistry().register(GODS_UPGRADE.setRegistryName(WoldsVaults.id("armory_gods_upgrade")));
    }

    @SubscribeEvent
    public static void blockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        ARMORY_ENTITY = BlockEntityType.Builder.of(ArmoryBlockEntity::new, ARMORY, CREATIVE_ARMORY).build(null);
        event.getRegistry().register(ARMORY_ENTITY.setRegistryName(WoldsVaults.id("armory")));
    }

    @SubscribeEvent
    public static void menus(RegistryEvent.Register<MenuType<?>> event) {
        ARMORY_MENU = IForgeMenuType.create(ArmoryMenu::client);
        event.getRegistry().register(ARMORY_MENU.setRegistryName(WoldsVaults.id("armory")));
        ArmoryTreeMenus.register(event);
        ArmoryGodMenus.register(event);
    }

    private static final class UpgradeItem extends Item {
        private final String text;

        private UpgradeItem(String text) {
            super(new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS).stacksTo(1).rarity(Rarity.RARE));
            this.text = text;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(new TextComponent("Armory upgrade").withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent(this.text).withStyle(ChatFormatting.GRAY));
        }
    }
}

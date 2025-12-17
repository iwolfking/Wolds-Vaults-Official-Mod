package xyz.iwolfking.woldsvaults.gui.server;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PlayerResearchesGUI extends SimpleGui {
    private int ticker = 0;
    private final UUID target;
    PlayerResearchesGUI.DisplayElement EMPTY = PlayerResearchesGUI.DisplayElement.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));
    public PlayerResearchesGUI(ServerPlayer player, UUID target) {
        super(MenuType.GENERIC_9x6, player, false);
        this.target = target;
    }

    private static void playClickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
    }
    public void updateDisplay() {
        int i = 0;
        List<String> targetResearches = getResearches(target);

        for(String research : targetResearches) {

            PlayerResearchesGUI.DisplayElement RESEARCH_ELEMENT = PlayerResearchesGUI.DisplayElement.of(new GuiElementBuilder(getItemByMod(research))
                    .setName(new TextComponent(research).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(new TextComponent("Unlocked.").withStyle(ChatFormatting.GOLD))
                    .hideFlags()
                    .setCallback((x,y,z) -> playClickSound(this.player)));
            this.setSlot(i, RESEARCH_ELEMENT.element);
            i += 1;

            if(i >= 54) {
                return;
            }
        }

    }

    public List<String> getResearches(UUID uuid) {
        ResearchTree playerResearches = PlayerResearchesData.get((ServerLevel) this.player.level).getResearches(uuid);
        return playerResearches.getResearchesDone();
    }

    public static Item getItemByMod(String modName) {
        switch(modName) {
            case "Easy Villagers":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("easy_villagers:auto_trader"));
            case "Botania Flux Field":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("botania:mana_fluxfield"));
            case "Soul Harvester":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("the_vault:vault_harvester"));
            case "Big Backpacks":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:netherite_backpack"));
            case "Backpacks":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:diamond_backpack"));
            case "Belts":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:gold_backpack"));
            case "Double Pouches":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:iron_backpack"));
            case "Better Wallets":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("lightmanscurrency:wallet_netherite"));
            case "Auto Feeding":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:feeding_upgrade"));
            case "Auto Refill":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:refill_upgrade"));
            case "Stack Upgrading":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:stack_upgrade_tier_4"));
            case "Pouches":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedbackpacks:backpack"));
            case "Junk Management":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("the_vault:vault_charm_controller"));
            case "Phytogenic Insulator":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("thermal:machine_insolator"));
            case "Digital Miner":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("mekanism:digital_miner"));
            case "Brews":
                return Items.BREWING_STAND;
            case "Mixtures":
                return Items.CAULDRON;
            case "Potions":
                return Items.GLASS_BOTTLE;
            case "Thermal Dynamos":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("thermal:dynamo_gourmand"));
            case "Altar Automation":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("vaultintegrations:altar_conduit"));
            case "Vault Compass":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("the_vault:vault_compass"));
            case "Mekanism QIO":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("mekanism:qio_drive_hyper_dense"));
            case "Create Ore Excavation":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("createoreexcavation:netherite_drill"));
            case "Automatic Genius":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("extrastorage:netherite_crafter"));
            case "Mekanism Generators":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("mekanismgenerators:gas_burning_generator"));
            case "Iron Generators":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("irongenerators:iron_generator"));
            case "Powah":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("powah:charged_snowball"));
            case "Thermal Expansion":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("thermal:machine_furnace"));
            case "Flux Networks":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("fluxnetworks:flux_plug"));
            case "Botany Pots":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("botanypots:terracotta_botany_pot"));
            case "Bonsai Pots":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("bonsaitrees3:bonsai_pot"));
            case "Cagerium":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("cagerium:cage"));
            case "Create Mechanical Extruder":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("create_mechanical_extruder:mechanical_extruder"));
            case "Create":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("create:cogwheel"));
            case "Dark Utilities":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("darkutils:vector_plate"));
            case "Industrial Foregoing":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("industrialforegoing:machine_frame_supreme"));
            case "Mekanism":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("mekanism:energy_tablet"));
            case "PneumaticCraft":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("pneumaticcraft:pressure_tube"));
            case "Botania":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("botania:apothecary_default"));
            case "Mob Spawners":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ispawner:spawner"));
            case "ComputerCraft":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("computercraft:computer_normal"));
            case "Pipez":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("pipez:item_pipe"));
            case "Modular Routers":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("modularrouters:modular_router"));
            case "Hexcasting":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("hexcasting:wand_oak"));
            case "LaserIO":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("laserio:laser_node"));
            case "Xnet":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("xnet:router"));
            case "Elevators":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("elevatorid:elevator_white"));
            case "Iron Furnaces":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ironfurnaces:iron_furnace"));
            case "Applied Energistics":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ae2:controller"));
            case "Integrated Dynamics":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("integrateddynamics:variable"));
            case "Refined Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("refinedstorage:improved_processor"));
            case "Iron Chests":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ironchests:iron_chest"));
            case "Drawers":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("storagedrawers:oak_full_drawers_1"));
            case "Sophisticated Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("sophisticatedstorage:controller"));
            case "Mining Gadgets":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("mininggadgets:mininggadget_simple"));
            case "Building Gadgets":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("buildinggadgets:gadget_building"));
            case "Trashcans":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("trashcans:item_trash_can"));
            case "Torch Master":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("torchmaster:mega_torch"));
            case "Waystones":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("waystones:waystone"));
            case "Toms Simple Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("toms_storage:ts.storage_terminal"));
            case "Cloud Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("cloudstorage:cloud_chest"));
            case "RFTools Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("rftoolsstorage:modular_storage"));
            case "Colossal Chests":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("colossalchests:chest_wall_wood"));
            case "Mega Cells":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("megacells:item_storage_cell_1m"));
            case "Extra Storage":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("extrastorage:neural_processor"));
            case "Botanical Machinery":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("botanicalmachinery:mana_battery"));
            case "Applied Mekanistics":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("appmek:chemical_cell_housing"));
            case "Applied Botanics":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("appbot:mana_cell_housing"));
            case "Create Crafts and Additions":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("createaddition:electric_motor"));
            case "Create Diesel Generators":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("createdieselgenerators:diesel_engine"));
            case "Time in a Bottle":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("tiab:time_in_a_bottle"));
            case "RFTools Utility":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("rftoolsutility:charged_porter"));
            case "Hostile Neural Networks":
                return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("hostilenetworks:deep_learner"));
            default:
                return Items.GREEN_CONCRETE;
        }
    }



    @Override
    public void onTick() {
        ticker++;
        if (ticker >= 20) {
            ticker = 0;
            updateDisplay();
        }

        super.onTick();
    }

    public record DisplayElement(@Nullable GuiElementInterface element, @Nullable Slot slot) {
        private static final PlayerResearchesGUI.DisplayElement EMPTY = PlayerResearchesGUI.DisplayElement.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));
        private static final PlayerResearchesGUI.DisplayElement FILLER = PlayerResearchesGUI.DisplayElement.of(
                new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(new TextComponent(""))
                        .hideFlags()
        );

        public static PlayerResearchesGUI.DisplayElement of(GuiElementInterface element) {
            return new PlayerResearchesGUI.DisplayElement(element, null);
        }

        public static PlayerResearchesGUI.DisplayElement of(GuiElementBuilderInterface<?> element) {
            return new PlayerResearchesGUI.DisplayElement(element.build(), null);
        }


        public static PlayerResearchesGUI.DisplayElement filler() {
            return FILLER;
        }

        public static PlayerResearchesGUI.DisplayElement empty() {
            return EMPTY;
        }
    }
}

package com.frequencygroups.mod.item;

import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.registry.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FrequencyGroups.MODID);

    public static final DeferredItem<IncompleteEmeraldSteelBarItem> INCOMPLETE_EMERALD_STEEL_BAR = ITEMS.registerItem(
            "incomplete_emerald_steel_bar", IncompleteEmeraldSteelBarItem::new, new Item.Properties()
                    .stacksTo(1)
                    .component(ModComponents.HITS.get(), 0)
    );

    public static final DeferredItem<Item> EMERALD_STEEL_BAR = ITEMS.registerSimpleItem(
            "emerald_steel_bar", new Item.Properties()
                    .stacksTo(64)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    );

    public static final DeferredItem<Item> ENCHANTED_EMERALD_APPLE = ITEMS.registerSimpleItem(
            "enchanted_emerald_apple", new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .food(ModFoodComponents.ENCHANTED_EMERALD_APPLE)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            );

    public static final DeferredItem<Item> EMERALD_APPLE = ITEMS.registerSimpleItem(
            "emerald_apple", new Item.Properties()
                    .rarity(Rarity.RARE)
                    .food(ModFoodComponents.EMERALD_APPLE)
            );

    public static final DeferredItem<Item> EMERALD_CARROT = ITEMS.registerSimpleItem(
            "emerald_carrot", new Item.Properties()
                    .food(ModFoodComponents.EMERALD_CARROT)
    );

    public static final DeferredItem<Item> BEGGARS_CROSSBOW = ITEMS.registerItem(
            "beggars_crossbow", BeggarsCrossbowItem::new, new Item.Properties()
                    .stacksTo(1)
    );


    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(EMERALD_STEEL_BAR);

            event.accept(ENCHANTED_EMERALD_APPLE);
            event.accept(EMERALD_APPLE);
            event.accept(EMERALD_CARROT);
        }
    }
}
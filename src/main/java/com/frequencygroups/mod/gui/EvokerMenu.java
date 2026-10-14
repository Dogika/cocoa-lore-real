package com.frequencygroups.mod.gui;

import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.core.component.DataComponents.LORE;

public class EvokerMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final String[] words = new String[]{"the", "elder", "scrolls", "klaatu", "berata", "niktu", "xyzzy", "bless", "curse", "light", "darkness", "fire", "air", "earth", "water", "hot", "dry", "cold", "wet", "ignite", "snuff", "embiggen", "twist", "shorten", "stretch", "fiddle", "destroy", "imbue", "galvanize", "enchant", "free", "limited", "range", "of", "towards", "inside", "sphere", "cube", "self", "other", "ball", "mental", "physical", "grow", "shrink", "demon", "elemental", "spirit", "animal", "creature", "beast", "humanoid", "undead", "fresh", "stale", "phnglui", "mglwnafh", "cthulhu", "rlyeh", "wgahnagl", "fhtagn", "baguette"};


    private static final Map<ResourceKey<Enchantment>, ResourceKey<Enchantment>> ALTERNATIVE_ENCHANT_MAP = Map.of(
            Enchantments.THORNS,       ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("barrier")),
                Enchantments.FROST_WALKER, ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("hot_footed")),
            Enchantments.FROST_WALKER, ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("hot_footed"))
    );

    private final ResultContainer result = new ResultContainer();

    public final SimpleContainer input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            EvokerMenu.this.slotsChanged(this);
        }
    };

    private final DataSlot isRecipeValid = DataSlot.standalone();

    private ItemStack lastBookProcessed = ItemStack.EMPTY;
    private ItemStack lastPaymentProcessed = ItemStack.EMPTY;
    private ItemStack currentOutputBook = ItemStack.EMPTY;

    public EvokerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public EvokerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.EVOKER_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        this.addSlot(new Slot(this.input, 0, 27, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ENCHANTED_BOOK);
            }
        });

        this.addSlot(new Slot(this.input, 1, 76, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.EMERALD_STEEL_BAR) || stack.is(Items.EMERALD);
            }
        });

        this.addSlot(new Slot(this.result, 2, 134, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                EvokerMenu.this.input.getItem(0).shrink(1);
                EvokerMenu.this.input.getItem(1).shrink(1);
                EvokerMenu.this.lastBookProcessed = ItemStack.EMPTY;
                EvokerMenu.this.lastPaymentProcessed = ItemStack.EMPTY;

                stack.set(DataComponents.STORED_ENCHANTMENTS, stack.get(DataComponents.STORED_ENCHANTMENTS).withTooltip(true));
                stack.set(LORE, ItemLore.EMPTY);
                super.onTake(player, stack);
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlot(this.isRecipeValid);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        ItemStack inputBook = this.input.getItem(0);
        ItemStack payment = this.input.getItem(1);

        if (inputBook.isEmpty() || !inputBook.is(Items.ENCHANTED_BOOK) || payment.isEmpty()) {
            this.result.setItem(0, ItemStack.EMPTY);
            this.isRecipeValid.set(0);
            this.lastBookProcessed = ItemStack.EMPTY;
            this.lastPaymentProcessed = ItemStack.EMPTY;
            return;
        }

        boolean bookChanged = !ItemStack.matches(this.lastBookProcessed, inputBook);
        boolean paymentChanged = !ItemStack.isSameItem(this.lastPaymentProcessed, payment);

        if (bookChanged || paymentChanged) {
            this.currentOutputBook = generateRolledBook(inputBook, payment);
            this.lastBookProcessed = inputBook.copy();
            this.lastPaymentProcessed = payment.copy();
        }

        this.result.setItem(0, this.currentOutputBook.copy());
        this.isRecipeValid.set(1);
        this.broadcastChanges();
    }

    private ItemStack generateRolledBook(ItemStack inputBook, ItemStack payment) {
        ItemStack outputStack = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments storedEnchants = inputBook.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable newEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        Registry<Enchantment> registry = this.player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Component> loreLines = new ArrayList<>();

        Style illagerStyle = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("minecraft", "illageralt")).withColor(ChatFormatting.GRAY);
        Style standardFont = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("minecraft", "default")).withColor(ChatFormatting.GRAY);

        boolean isPremium = payment.is(ModItems.EMERALD_STEEL_BAR);

        storedEnchants.entrySet().forEach(entry -> {
            Holder<Enchantment> holder = entry.getKey();
            int originalLevel = entry.getIntValue();
            Enchantment enchantment = holder.value();

            int newLevel = getWeightedRandomLevel(originalLevel, enchantment.getMinLevel(), enchantment.getMaxLevel(), isPremium);

            Optional<ResourceKey<Enchantment>> keyOpt = holder.unwrapKey();
            if (keyOpt.isPresent()) {
                ResourceKey<Enchantment> newKey = convertEnchantment(keyOpt.get(), isPremium);
                var optionalRef = registry.getHolder(newKey);
                Holder<Enchantment> newHolder = optionalRef.isPresent() ? optionalRef.get() : holder;

                newEnchants.set(newHolder, newLevel);
            }

            loreLines.add(Component.empty()
                    .append(Component.literal(Util.getRandom(words, player.getRandom())).withStyle(illagerStyle))
                    .append(" ")
                    .append(Component.translatable("enchantment.level." + newLevel).withStyle(standardFont)));
        });

        outputStack.set(DataComponents.STORED_ENCHANTMENTS, newEnchants.toImmutable().withTooltip(false));
        outputStack.set(LORE, new ItemLore(loreLines));

        return outputStack;
    }

    private int getWeightedRandomLevel(int currentLevel, int minLevel, int maxLevel, boolean isPremium) {
        if (minLevel >= maxLevel) return minLevel;

        int targetLevel = isPremium ? Math.min(currentLevel + 1, maxLevel) : currentLevel;

        double falloff = isPremium ? 1.5 : 3.0;

        double totalWeight = 0;
        double[] weights = new double[maxLevel - minLevel + 1];

        for (int i = minLevel; i <= maxLevel; i++) {
            int distance = Math.abs(i - targetLevel);
            double weight = 1.0 / Math.pow(falloff, distance);
            weights[i - minLevel] = weight;
            totalWeight += weight;
        }

        double randomVal = player.getRandom().nextDouble() * totalWeight;
        for (int i = minLevel; i <= maxLevel; i++) {
            randomVal -= weights[i - minLevel];
            if (randomVal <= 0) {
                return i;
            }
        }
        return currentLevel;
    }

    private ResourceKey<Enchantment> convertEnchantment(ResourceKey<Enchantment> key, boolean isPremium) {
        float roll = player.getRandom().nextFloat();

        if (key.equals(Enchantments.SHARPNESS)) {
            float bleedingChance = isPremium ? 0.15f : 0.02f;
            float dullnessChance = isPremium ? 0.15f : 0.3f;

            if (roll < bleedingChance) return ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("bleeding"));
            if (roll < bleedingChance + dullnessChance) return ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("dullness"));
            return Enchantments.SHARPNESS;
        }

        if (ALTERNATIVE_ENCHANT_MAP.containsKey(key)) {
            float altChance = isPremium ? 0.75f : 0.3f;
            if (roll < altChance) {
                return ALTERNATIVE_ENCHANT_MAP.get(key);
            }
        }

        return key;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            ItemStack inputBook = this.input.getItem(0);
            ItemStack payment = this.input.getItem(1);

            if (!payment.isEmpty() && !inputBook.isEmpty()) {
                payment.shrink(1);
                this.currentOutputBook = generateRolledBook(inputBook, payment);
                this.updateResult();
                return true;
            }
        }
        return false;
    }

    public boolean isRecipeValid() {
        return this.isRecipeValid.get() == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.input));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record GiveEnchantedBook(Item item, int level) implements DiceAction {
    public static final MapCodec<GiveEnchantedBook> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(GiveEnchantedBook::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("level", 1).forGetter(GiveEnchantedBook::level)
    ).apply(instance, GiveEnchantedBook::new));

    @Override
    public boolean canRun(DiceContext context) {
        return !ForgeRegistries.ENCHANTMENTS.isEmpty();
    }

    @Override
    public Result run(DiceContext context) {
        List<Enchantment> enchantments = List.copyOf(ForgeRegistries.ENCHANTMENTS.getValues());
        Enchantment enchantment = enchantments.get(context.random().nextInt(enchantments.size()));
        ItemStack stack = new ItemStack(item);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, level));
        GiveItem.drop(context.player(), stack, 1);
        return Result.DONE;
    }
}

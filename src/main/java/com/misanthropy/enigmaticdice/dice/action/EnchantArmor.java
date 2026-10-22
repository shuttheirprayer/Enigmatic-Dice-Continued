package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public record EnchantArmor(Enchantment enchantment, int level) implements DiceAction {
    public static final MapCodec<EnchantArmor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ForgeRegistries.ENCHANTMENTS.getCodec().fieldOf("enchantment").forGetter(EnchantArmor::enchantment),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("level", 1).forGetter(EnchantArmor::level)
    ).apply(instance, EnchantArmor::new));

    private static final List<EquipmentSlot> ARMOR = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    @Override
    public boolean canRun(DiceContext context) {
        return ARMOR.stream().map(slot -> context.player().getItemBySlot(slot)).anyMatch(this::accepts);
    }

    @Override
    public Result run(DiceContext context) {
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = context.player().getItemBySlot(slot);
            if (!accepts(stack)) continue;
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            enchantments.put(enchantment, level);
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
        return Result.DONE;
    }

    private boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && !EnchantmentHelper.getEnchantments(stack).containsKey(enchantment);
    }
}

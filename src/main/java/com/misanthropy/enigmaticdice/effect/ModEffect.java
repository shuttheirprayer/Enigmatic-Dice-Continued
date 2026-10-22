package com.misanthropy.enigmaticdice.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModEffect extends MobEffect {
    private final ModifierValue value;
    private final List<Runnable> lazyModifiers = new ArrayList<>();

    public ModEffect(MobEffectCategory category, int color) {
        this(category, color, null);
    }

    public ModEffect(MobEffectCategory category, int color, ModifierValue value) {
        super(category, color);
        this.value = value;
    }

    public ModEffect lazyModifier(Supplier<Attribute> attribute, String uuid, double amount, AttributeModifier.Operation operation) {
        lazyModifiers.add(() -> addAttributeModifier(attribute.get(), uuid, amount, operation));
        return this;
    }

    private void resolveModifiers() {
        if (lazyModifiers.isEmpty()) return;
        synchronized (lazyModifiers) {
            lazyModifiers.forEach(Runnable::run);
            lazyModifiers.clear();
        }
    }

    @Override
    public Map<Attribute, AttributeModifier> getAttributeModifiers() {
        resolveModifiers();
        return super.getAttributeModifiers();
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        resolveModifiers();
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        resolveModifiers();
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return value == null ? super.getAttributeModifierValue(amplifier, modifier) : value.get(amplifier, modifier);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @FunctionalInterface
    public interface ModifierValue {
        double get(int amplifier, AttributeModifier modifier);
    }
}

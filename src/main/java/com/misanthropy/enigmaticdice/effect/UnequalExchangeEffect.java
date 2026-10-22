package com.misanthropy.enigmaticdice.effect;

import com.misanthropy.enigmaticdice.Config;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class UnequalExchangeEffect extends ModEffect {
    private static final String HEALTH = "5f1d9b20-dfd6-4284-9d3a-004776a87bfd";
    private static final UUID HEALTH_ID = UUID.fromString(HEALTH);

    public UnequalExchangeEffect(MobEffectCategory category, int color) {
        super(category, color, UnequalExchangeEffect::value);
        addAttributeModifier(Attributes.MAX_HEALTH, HEALTH, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "27a72973-be25-44f0-9613-187812e4627c", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR, "44371e08-d7ae-4344-8cfa-95c9fd4825c2", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "a6fb771d-97de-4883-86a8-1ba2b8db59c2", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "68403af9-c76e-4b08-8196-f264d872876d", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static double value(int amplifier, AttributeModifier modifier) {
        double reduction = -Config.UNEQUAL_EXCHANGE_STAT_DEBUFF.get() * (amplifier + 1);
        return modifier.getId().equals(HEALTH_ID) ? Math.max(reduction, -0.8) : reduction;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}

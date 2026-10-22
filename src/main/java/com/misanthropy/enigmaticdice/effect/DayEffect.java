package com.misanthropy.enigmaticdice.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public class DayEffect extends ModEffect {
    private static final int PERIOD = 100;
    private static final double RADIUS = 150;

    private final Supplier<MobEffect> spread;
    private final int amplifier;
    private final boolean visible;

    public DayEffect(MobEffectCategory category, int color, Supplier<MobEffect> spread, int amplifier, boolean visible) {
        super(category, color);
        this.spread = spread;
        this.amplifier = amplifier;
        this.visible = visible;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % PERIOD == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity carrier, int carrierAmplifier) {
        if (carrier.level().isClientSide) return;
        MobEffect effect = spread.get();
        for (LivingEntity entity : carrier.level().getEntitiesOfClass(LivingEntity.class, carrier.getBoundingBox().inflate(RADIUS), LivingEntity::isAlive)) {
            entity.addEffect(new MobEffectInstance(effect, PERIOD * 2, amplifier, false, visible), carrier);
        }
    }
}

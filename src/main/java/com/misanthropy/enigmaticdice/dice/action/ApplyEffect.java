package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public record ApplyEffect(MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean curable) implements DiceAction {
    public static final MapCodec<ApplyEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(ApplyEffect::effect),
            Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("duration", 600).forGetter(ApplyEffect::duration),
            Codec.intRange(0, 127).optionalFieldOf("amplifier", 0).forGetter(ApplyEffect::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(ApplyEffect::ambient),
            Codec.BOOL.optionalFieldOf("visible", false).forGetter(ApplyEffect::visible),
            Codec.BOOL.optionalFieldOf("curable", true).forGetter(ApplyEffect::curable)
    ).apply(instance, ApplyEffect::new));

    @Override
    public boolean canRun(DiceContext context) {
        MobEffectInstance existing = context.player().getEffect(effect);
        return existing == null || existing.getAmplifier() < amplifier;
    }

    @Override
    public Result run(DiceContext context) {
        MobEffectInstance instance = new MobEffectInstance(effect, duration == -1 ? MobEffectInstance.INFINITE_DURATION : duration, amplifier, ambient, visible);
        if (!curable) instance.setCurativeItems(new ArrayList<>());
        context.player().removeEffect(effect);
        return context.player().addEffect(instance) ? Result.DONE : Result.FAILED;
    }
}

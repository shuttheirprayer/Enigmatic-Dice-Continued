package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.TimeAcceleration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.GameRules;

public record AccelerateTime(int multiplier, int duration) implements DiceAction {
    public static final MapCodec<AccelerateTime> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(2, 1000).optionalFieldOf("multiplier", 40).forGetter(AccelerateTime::multiplier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 6000).forGetter(AccelerateTime::duration)
    ).apply(instance, AccelerateTime::new));

    @Override
    public boolean canRun(DiceContext context) {
        return context.level().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) && !TimeAcceleration.isActive();
    }

    @Override
    public Result run(DiceContext context) {
        TimeAcceleration.start(context.server(), multiplier, duration);
        return Result.DONE;
    }
}

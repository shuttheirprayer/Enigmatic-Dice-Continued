package com.misanthropy.enigmaticdice.dice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public record DiceSettings(boolean enabled, int rarity, DiceCategory category, Optional<String> message, int messageVariants, int claimRadius) {
    public static final MapCodec<DiceSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(DiceSettings::enabled),
            ExtraCodecs.POSITIVE_INT.fieldOf("rarity").forGetter(DiceSettings::rarity),
            DiceCategory.CODEC.optionalFieldOf("category", DiceCategory.POSITIVE).forGetter(DiceSettings::category),
            Codec.STRING.optionalFieldOf("message").forGetter(DiceSettings::message),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("message_variants", 0).forGetter(DiceSettings::messageVariants),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("claim_radius", 0).forGetter(DiceSettings::claimRadius)
    ).apply(instance, DiceSettings::new));

    public double chance(float luck) {
        double base = 1.0 / rarity;
        int extraRolls = switch (category) {
            case POSITIVE -> luck > 0 ? (int) luck : 0;
            case NEGATIVE -> luck < 0 ? (int) -luck : 0;
            case NEUTRAL -> 0;
        };
        if (extraRolls == 0) return base;
        return 1.0 - (1.0 - base) * Math.pow(1.0 - 0.1 * base, extraRolls);
    }
}

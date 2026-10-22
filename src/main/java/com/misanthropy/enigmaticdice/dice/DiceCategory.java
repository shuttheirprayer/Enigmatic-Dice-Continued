package com.misanthropy.enigmaticdice.dice;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DiceCategory implements StringRepresentable {
    POSITIVE("positive"),
    NEUTRAL("neutral"),
    NEGATIVE("negative");

    public static final Codec<DiceCategory> CODEC = StringRepresentable.fromEnum(DiceCategory::values);

    private final String name;

    DiceCategory(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

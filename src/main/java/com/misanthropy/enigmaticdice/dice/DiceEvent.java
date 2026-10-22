package com.misanthropy.enigmaticdice.dice;

import net.minecraft.resources.ResourceLocation;

public record DiceEvent(ResourceLocation id, DiceSettings settings, DiceAction action) {
}

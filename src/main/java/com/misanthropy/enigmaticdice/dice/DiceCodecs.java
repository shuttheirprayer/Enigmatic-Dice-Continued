package com.misanthropy.enigmaticdice.dice;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public final class DiceCodecs {
    public static final Codec<CompoundTag> SNBT = Codec.STRING.comapFlatMap(DiceCodecs::parseSnbt, CompoundTag::toString);

    private DiceCodecs() {
    }

    private static DataResult<CompoundTag> parseSnbt(String snbt) {
        try {
            return DataResult.success(TagParser.parseTag(snbt));
        } catch (CommandSyntaxException exception) {
            return DataResult.error(() -> "Invalid NBT " + snbt + ": " + exception.getMessage());
        }
    }
}

package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceCodecs;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public record GiveItem(Item item, int count, Optional<CompoundTag> nbt) implements DiceAction {
    public static final MapCodec<GiveItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(GiveItem::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(GiveItem::count),
            DiceCodecs.SNBT.optionalFieldOf("nbt").forGetter(GiveItem::nbt)
    ).apply(instance, GiveItem::new));

    @Override
    public Result run(DiceContext context) {
        ItemStack stack = new ItemStack(item);
        nbt.ifPresent(tag -> stack.setTag(tag.copy()));
        drop(context.player(), stack, count);
        return Result.DONE;
    }

    public static void drop(ServerPlayer player, ItemStack template, int count) {
        int maxStack = Math.max(template.getMaxStackSize(), 1);
        while (count > 0) {
            ItemStack stack = template.copyWithCount(Math.min(count, maxStack));
            count -= stack.getCount();
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 1, player.getZ(), stack));
        }
    }
}

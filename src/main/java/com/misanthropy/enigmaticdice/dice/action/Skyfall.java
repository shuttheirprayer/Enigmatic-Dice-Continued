package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.SafeSpot;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public record Skyfall(int height, Item item) implements DiceAction {
    public static final MapCodec<Skyfall> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("height", 200).forGetter(Skyfall::height),
            ForgeRegistries.ITEMS.getCodec().optionalFieldOf("item", Items.WATER_BUCKET).forGetter(Skyfall::item)
    ).apply(instance, Skyfall::new));

    @Override
    public boolean canRun(DiceContext context) {
        ServerPlayer player = context.player();
        if (!player.level().dimension().equals(Level.OVERWORLD) || emptyHotbarSlot(player) < 0) return false;
        BlockPos target = BlockPos.containing(player.getX(), player.getY() + height, player.getZ());
        return SafeSpot.isOpen(player.level(), target) && SafeSpot.isOpen(player.level(), target.above());
    }

    @Override
    public Result run(DiceContext context) {
        ServerPlayer player = context.player();
        player.getInventory().setItem(emptyHotbarSlot(player), new ItemStack(item));
        player.teleportTo(player.serverLevel(), player.getX(), player.getY() + height, player.getZ(), player.getYRot(), player.getXRot());
        return Result.DONE;
    }

    private static int emptyHotbarSlot(ServerPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).isEmpty()) return i;
        }
        return -1;
    }
}

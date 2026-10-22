package com.misanthropy.enigmaticdice.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class MoonShard extends Item {
    public MoonShard(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.while_hotbar"));
            lines.add(Component.translatable("tooltip.enigmaticdice.moon_shard_1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.moon_shard_2"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.moon_shard_3"));
        });
    }
}

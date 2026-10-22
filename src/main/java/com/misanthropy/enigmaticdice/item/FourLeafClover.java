package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class FourLeafClover extends Item {
    private static final UUID LUCK_ID = UUID.fromString("31f427bc-d370-4bd0-8070-26cff86276e4");

    public FourLeafClover(Properties properties) {
        super(properties);
    }

    public static void updateLuck(Player player) {
        AttributeInstance luck = player.getAttribute(Attributes.LUCK);
        if (luck == null) return;
        int bonus = Math.min(count(player.getInventory()), Config.CLOVER_LUCK_CAP.get());
        AttributeModifier existing = luck.getModifier(LUCK_ID);
        if (existing != null && existing.getAmount() == bonus) return;
        if (existing != null) luck.removeModifier(LUCK_ID);
        if (bonus > 0) luck.addTransientModifier(new AttributeModifier(LUCK_ID, "Four-Leaf Clover luck", bonus, AttributeModifier.Operation.ADDITION));
    }

    private static int count(Inventory inventory) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof FourLeafClover) total += stack.getCount();
        }
        return total;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.four_leaf_clever"));
            lines.add(Component.translatable("tooltip.enigmaticdice.four_leaf_clever_cap", Config.CLOVER_LUCK_CAP.get()));
        });
    }
}

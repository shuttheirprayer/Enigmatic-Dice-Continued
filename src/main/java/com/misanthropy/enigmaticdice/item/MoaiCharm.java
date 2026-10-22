package com.misanthropy.enigmaticdice.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.enigmaticdice.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MoaiCharm extends Item implements ICurioItem {
    private static final UUID KNOCKBACK_ID = UUID.fromString("9b3ef174-e8eb-4ba7-a287-4575bdb540d0");

    public MoaiCharm(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(KNOCKBACK_ID, "enigmaticdice:moai_charm_knockback_resistance_bonus", Config.MOAI_CHARM_KNOCKBACK_RESISTANCE.get(), AttributeModifier.Operation.ADDITION));
        return attributes;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> lines.add(Component.translatable("tooltip.enigmaticdice.moai_charm")));
    }
}

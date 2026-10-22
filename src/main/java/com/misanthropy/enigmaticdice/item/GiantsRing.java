package com.misanthropy.enigmaticdice.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModAttributes;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class GiantsRing extends Item implements ICurioItem {
    private static final AttributeModifier STEP_HEIGHT = new AttributeModifier(UUID.fromString("979b021d-47b6-42dc-b68c-4c296aca0b01"), "enigmaticdice:giants_ring_step_height", 1, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SIZE = new AttributeModifier(UUID.fromString("b3d7b5e8-d381-45bf-8d9f-6291e7e5913d"), "enigmaticdice:giants_ring_size_scale", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final UUID HEALTH_ID = UUID.fromString("44aeda56-97d8-40d8-9c57-3727f50bea16");
    private static final UUID SPEED_ID = UUID.fromString("be2c96bc-2f43-471e-8cff-150ac8750b57");
    private static final UUID DAMAGE_ID = UUID.fromString("cc5c05c0-916a-4165-bd3f-61a6b9ccfab0");
    private static final int STOMP_COOLDOWN = 20;

    private final Map<LivingEntity, Integer> stompCooldowns = new WeakHashMap<>();

    public GiantsRing(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity().level().isClientSide || !(context.entity() instanceof Player player)) return;
        remove(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP_HEIGHT);
        remove(player.getAttribute(ModAttributes.SIZE_SCALE.get()), SIZE);
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level().isClientSide || !(context.entity() instanceof Player player)) return;
        ensure(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP_HEIGHT);
        ensure(player.getAttribute(ModAttributes.SIZE_SCALE.get()), SIZE);
        if (player.isSprinting() && !player.isSwimming()) stomp(player);
    }

    private void stomp(Player player) {
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) damage -= modifier.getAmount();
        }
        AABB box = player.getBoundingBox();
        AABB stompBox = new AABB(box.minX - 1, player.getY() - 0.1, box.minZ - 1, box.maxX + 1, player.getY() + 0.4, box.maxZ + 1);
        double playerVolume = volume(box);
        int tick = player.tickCount;
        for (LivingEntity victim : player.level().getEntitiesOfClass(LivingEntity.class, stompBox, e -> e != player && e.isAlive() && e.isPickable())) {
            if (tick < stompCooldowns.getOrDefault(victim, 0)) continue;
            if (playerVolume > volume(victim.getBoundingBox()) && Combat.canHarm(player, victim)) {
                victim.hurt(player.damageSources().playerAttack(player), (float) damage);
                stompCooldowns.put(victim, tick + STOMP_COOLDOWN);
            }
        }
    }

    private static double volume(AABB box) {
        return box.getXsize() * box.getYsize() * box.getZsize();
    }

    private static void ensure(@Nullable AttributeInstance instance, AttributeModifier modifier) {
        if (instance != null && !instance.hasModifier(modifier)) instance.addTransientModifier(modifier);
    }

    private static void remove(@Nullable AttributeInstance instance, AttributeModifier modifier) {
        if (instance != null) instance.removeModifier(modifier.getId());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.MAX_HEALTH, new AttributeModifier(HEALTH_ID, "enigmaticdice:giants_ring_health_bonus", Config.GIANTS_RING_MAX_HEALTH.get(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_ID, "enigmaticdice:giants_ring_speed_bonus", Config.GIANTS_RING_SPEED_BONUS.get(), AttributeModifier.Operation.MULTIPLY_BASE));
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_ID, "enigmaticdice:giants_ring_damage_bonus", Config.GIANTS_RING_ATTACK_DAMAGE.get(), AttributeModifier.Operation.ADDITION));
        return attributes;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.giants_ring_1"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.giants_ring_2"));
        }, Component.translatable("tooltip.enigmaticdice.giants_ring_0"));
    }
}

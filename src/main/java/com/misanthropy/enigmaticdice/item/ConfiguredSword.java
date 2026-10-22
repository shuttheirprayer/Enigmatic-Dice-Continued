package com.misanthropy.enigmaticdice.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.enigmaticdice.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

import java.util.function.DoubleSupplier;

public class ConfiguredSword extends SwordItem {
    private final DoubleSupplier damage;
    private final DoubleSupplier speed;
    private Multimap<Attribute, AttributeModifier> cached;
    private int cachedRevision = -1;

    public ConfiguredSword(DoubleSupplier damage, DoubleSupplier speed, Properties properties) {
        super(Tiers.NETHERITE, 0, 0, properties);
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) return super.getDefaultAttributeModifiers(slot);
        int revision = Config.revision();
        if (cached == null || cachedRevision != revision) {
            cached = baseModifiers().build();
            cachedRevision = revision;
        }
        return cached;
    }

    protected ImmutableMultimap.Builder<Attribute, AttributeModifier> baseModifiers() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage.getAsDouble() - 1, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", speed.getAsDouble() - 4, AttributeModifier.Operation.ADDITION));
        return builder;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}

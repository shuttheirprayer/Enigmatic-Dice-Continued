package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, EnigmaticDice.MOD_ID);

    public static final RegistryObject<Attribute> SIZE_SCALE = ATTRIBUTES.register("size_scale",
            () -> new RangedAttribute("attribute.enigmaticdice.size_scale", 1.0D, 0.1D, 20.0D).setSyncable(true));

    private ModAttributes() {
    }

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
        bus.addListener(ModAttributes::addToEntities);
    }

    private static void addToEntities(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, SIZE_SCALE.get());
        }
    }
}

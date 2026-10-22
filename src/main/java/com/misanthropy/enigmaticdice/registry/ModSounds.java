package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EnigmaticDice.MOD_ID);

    public static final RegistryObject<SoundEvent> UNEQUAL_EXCHANGE_HIT = register("unequal_exchange_hit");
    public static final RegistryObject<SoundEvent> SOUL_EATER_CHARGED_HIT = register("soul_eater_charged_hit");
    public static final RegistryObject<SoundEvent> PERMAFROST_FROZEN = register("permafrost_frozen");
    public static final RegistryObject<SoundEvent> PERMAFROST_UNFROZEN = register("permafrost_unfrozen");
    public static final RegistryObject<SoundEvent> CRUCIBLE_OF_RILE_PREATTACK = register("crucible_of_rile_preattack");
    public static final RegistryObject<SoundEvent> CRUCIBLE_OF_RILE_ATTACK = register("crucible_of_rile_attack");
    public static final RegistryObject<SoundEvent> CRUCIBLE_OF_RILE_USE = register("crucible_of_rile_use");
    public static final RegistryObject<SoundEvent> CRUCIBLE_OF_RILE_HELIX = register("crucible_of_rile_helix");
    public static final RegistryObject<SoundEvent> CRUCIBLE_OF_RILE_UNIQUE_KILL = register("crucible_of_rile_unique_kill");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(EnigmaticDice.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}

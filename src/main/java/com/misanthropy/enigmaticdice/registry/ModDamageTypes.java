package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> UNEQUAL_EXCHANGE = key("unequal_exchange");
    public static final ResourceKey<DamageType> UNEQUAL_EXCHANGE_SELF = key("unequal_exchange_self");
    public static final ResourceKey<DamageType> EXECUTE = key("execute");

    private ModDamageTypes() {
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), attacker);
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, EnigmaticDice.id(name));
    }
}

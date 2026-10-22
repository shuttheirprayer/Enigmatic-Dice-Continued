package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.effect.DayEffect;
import com.misanthropy.enigmaticdice.effect.ModEffect;
import com.misanthropy.enigmaticdice.effect.UnequalExchangeEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EnigmaticDice.MOD_ID);

    public static final RegistryObject<MobEffect> SOUL_EATER_CHARGED_HIT = EFFECTS.register("soul_eater_charged_hit",
            () -> new ModEffect(MobEffectCategory.BENEFICIAL, 0x5084d4));

    public static final RegistryObject<MobEffect> SOUL_EATER_HEALTH_BOOST = EFFECTS.register("soul_eater_health_boost",
            () -> new ModEffect(MobEffectCategory.BENEFICIAL, 0x72cee1));

    public static final RegistryObject<MobEffect> UNEQUAL_EXCHANGE_DEBUFFS = EFFECTS.register("unequal_exchange_debuffs",
            () -> new UnequalExchangeEffect(MobEffectCategory.HARMFUL, 0x411214));

    public static final RegistryObject<MobEffect> FROST_HIT = EFFECTS.register("frost_hit",
            () -> new ModEffect(MobEffectCategory.HARMFUL, 0x9ec2f6,
                    (amplifier, modifier) -> -Config.PERMAFROST_ATTACK_REDUCTION_PER_STACK.get() * (amplifier + 1))
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, "52276cff-92bc-4a7b-aaf3-56edcdd49521", -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.FLYING_SPEED, "3feb3b9b-a99c-42b3-9d2a-e53ca1ca5440", -1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> FROST_AURA = EFFECTS.register("frost_aura",
            () -> new ModEffect(MobEffectCategory.HARMFUL, 0x9ec2f6,
                    (amplifier, modifier) -> -Config.PERMAFROST_AURA_REDUCTION_FACTOR.get())
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, "a2f507ba-3631-4a91-ace0-41711c981a47", -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.FLYING_SPEED, "60c8804b-cb86-4efc-8a9a-63f7801731ac", -1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> CRUCIBLE_OF_RILE_ARMOR_BOOST = EFFECTS.register("crucible_of_rile_armor_boost",
            () -> new ModEffect(MobEffectCategory.BENEFICIAL, 0xc15507,
                    (amplifier, modifier) -> Config.CRUCIBLE_OF_RILE_ARMOR_BUFF_VALUE.get() * (amplifier + 1))
                    .addAttributeModifier(Attributes.ARMOR, "2fe31679-2a55-415d-9d49-9f1cc61e4731", 1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "e6481205-0401-4508-ace8-0ce73d48d2a9", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> DIVINE_SHIELD_INVULNERABILITY = EFFECTS.register("divine_shield_invulnerability",
            () -> new ModEffect(MobEffectCategory.BENEFICIAL, 0xffee7d));

    public static final RegistryObject<MobEffect> DAY_ON_THE_MOON = EFFECTS.register("day_on_the_moon",
            () -> new ModEffect(MobEffectCategory.BENEFICIAL, 0xccccff)
                    .lazyModifier(ForgeMod.ENTITY_GRAVITY, "954c16b3-1b50-4069-96c8-ab8b6d43f643", -0.06, AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<MobEffect> SHRUNK = EFFECTS.register("shrunk",
            () -> new ModEffect(MobEffectCategory.NEUTRAL, 0x8a6bbf, (amplifier, modifier) -> amplifier == 0 ? -0.5 : -0.9)
                    .lazyModifier(ModAttributes.SIZE_SCALE, "848682fe-ea22-4185-a838-146c328f6bc2", -1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> DAY_OF_INVISIBILITY = EFFECTS.register("day_of_invisibility",
            () -> new DayEffect(MobEffectCategory.NEUTRAL, 0x7f8392, () -> MobEffects.INVISIBILITY, 0, true));

    public static final RegistryObject<MobEffect> DAY_OF_TAG = EFFECTS.register("day_of_tag",
            () -> new DayEffect(MobEffectCategory.NEUTRAL, 0x33ebff, () -> MobEffects.MOVEMENT_SPEED, 2, false));

    public static final RegistryObject<MobEffect> DAY_OF_STRENGTH = EFFECTS.register("day_of_strength",
            () -> new DayEffect(MobEffectCategory.NEUTRAL, 0xffc700, () -> MobEffects.DAMAGE_BOOST, 1, false));

    public static final RegistryObject<MobEffect> MOON_DAY = EFFECTS.register("moon_day",
            () -> new DayEffect(MobEffectCategory.NEUTRAL, 0xccccff, DAY_ON_THE_MOON, 0, false));

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}

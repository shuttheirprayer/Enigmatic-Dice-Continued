package com.misanthropy.enigmaticdice;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_TABLES = BUILDER
            .comment("The list of chests in which the Enigmatic Die is added")
            .defineListAllowEmpty("lootTables", defaultLootTables(), Config::isResourceLocation);

    public static final ForgeConfigSpec.DoubleValue MOB_DROP_CHANCE = BUILDER
            .comment("Chance for Enigmatic Die to drop from a mob when killed by a player.",
                    "For example, a value of 0.005 means a 0.5% chance to drop.")
            .defineInRange("enigmaticDieDropChance", 0.005, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue MOB_DICE_TIME_INTERVAL = BUILDER
            .comment("Defines the time requirement (in minutes) for earning Enigmatic Dice from killing mobs.",
                    "This is NOT a cooldown.",
                    "For every X minutes the player spends in the world, they are allowed to obtain 1 die from mobs.",
                    "Example: If set to 10, then after 60 minutes of total playtime, the player may have earned up to 6 dice from mobs.",
                    "Set to 0 to disable the limitation entirely.")
            .defineInRange("mobDiceTimeInterval", 10, 0, 100000);

    public static final ForgeConfigSpec.DoubleValue BLOCK_DROP_CHANCE = BUILDER
            .comment("Chance for Enigmatic Die to drop from a block when broken by a player.",
                    "For example, a value of 0.001 means a 0.1% chance to drop.")
            .defineInRange("enigmaticDieBlockDropChance", 0.001, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue BLOCK_DICE_TIME_INTERVAL = BUILDER
            .comment("Defines the time requirement (in minutes) for earning Enigmatic Dice from breaking blocks.",
                    "This is NOT a cooldown.",
                    "For every X minutes the player spends in the world, they are allowed to obtain 1 die from blocks.",
                    "Example: If set to 10, then after 60 minutes of total playtime, the player may have earned up to 6 dice from blocks.",
                    "Set to 0 to disable the limitation entirely.")
            .defineInRange("blockDiceTimeInterval", 10, 0, 100000);

    public static final ForgeConfigSpec.DoubleValue CHEST_CHANCE = BUILDER
            .comment("Chance for Enigmatic Die to appear in a chest.",
                    "Value is between 0.0 and 1.0. For example, 0.1 means a 10% chance.")
            .defineInRange("enigmaticDieChestChance", 0.1, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue CLOVER_LUCK_CAP = BUILDER
            .comment("Each Four-Leaf Clover anywhere in the inventory gives +1 Luck.",
                    "This is the most Luck all carried clovers can give together.")
            .defineInRange("fourLeafCloverLuckCap", 40, 0, 1000);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_TARGET_HEALTH_REDUCTION = BUILDER
            .comment("Percentage of target's max health dealt as damage per hit by Unequal Exchange sword.",
                    "Example: 0.10 = 10% of target's max health.")
            .defineInRange("unequalExchangeTargetHealthReduction", 0.10, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_BOSS_HEALTH_REDUCTION = BUILDER
            .comment("Percentage of a boss's max health dealt as damage per hit by Unequal Exchange sword.",
                    "Bosses are entities in the forge:bosses entity type tag.")
            .defineInRange("unequalExchangeBossHealthReduction", 0.02, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_PLAYER_HEALTH_REDUCTION = BUILDER
            .comment("Percentage of player's max health dealt as damage to the wielder per hit by Unequal Exchange sword.",
                    "Example: 0.20 = 20% of player's max health.")
            .defineInRange("unequalExchangePlayerHealthReduction", 0.20, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_STAT_DEBUFF = BUILDER
            .comment("Percentage reduction of player's stats (armor, speed, etc.) per hit by Unequal Exchange sword.",
                    "Example: 0.20 = 20% reduction.")
            .defineInRange("unequalExchangeStatDebuff", 0.20, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue UNEQUAL_EXCHANGE_DEBUFF_DURATION = BUILDER
            .comment("Duration of the Unequal Exchange debuff (stat reduction) in seconds.",
                    "Each hit refreshes this timer.")
            .defineInRange("unequalExchangeDebuffDuration", 30, 0, 10000);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_ATTACK_DAMAGE = BUILDER
            .comment("Unequal Exchange Attack Damage.")
            .defineInRange("unequalExchangeAttackDamage", 1.0, 0.0, 100000.0);

    public static final ForgeConfigSpec.DoubleValue UNEQUAL_EXCHANGE_ATTACK_SPEED = BUILDER
            .comment("Unequal Exchange Attack Speed.")
            .defineInRange("unequalExchangeAttackSpeed", 1.6, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_CHARGED_ATTACK_DAMAGE_PER_HP = BUILDER
            .comment("Extra magic damage of Soul Eater's charged attack per HP spent.")
            .defineInRange("soulEaterChargedAttackDamagePerHP", 2.5, 0.0, 100.0);

    public static final ForgeConfigSpec.IntValue SOUL_EATER_CHARGE_DURATION = BUILDER
            .comment("The duration of the effect of a charged Soul Eater attack (in seconds).")
            .defineInRange("soulEaterChargeDuration", 60, 0, 100000);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_HEAL_PERCENT_ON_KILL = BUILDER
            .comment("Percent of killed enemy's max HP restored to the player.",
                    "Example: 0.1 = 10% of target's max HP.")
            .defineInRange("soulEaterHealPercentOnKill", 0.1, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_MAX_HEALTH_STEAL_PERCENT = BUILDER
            .comment("Percent of killed enemy's max HP added to player's max HP as bonus.",
                    "Example: 0.1 = +10% max HP of killed enemy.")
            .defineInRange("soulEaterMaxHealthStealPercent", 0.1, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue SOUL_EATER_MAX_HEALTH_BUFF_DURATION = BUILDER
            .comment("Duration (in seconds) of max health buff after killing an enemy.")
            .defineInRange("soulEaterMaxHealthBuffDuration", 300, 0, 100000);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_MAX_HEALTH_MULTIPLIER_LIMIT = BUILDER
            .comment("Maximum multiplier for player's max HP from Soul Eater.",
                    "Example: 1.0 = Player's max HP can be increased up to 100%.")
            .defineInRange("soulEaterMaxHealthMultiplierLimit", 1.0, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_ATTACK_DAMAGE = BUILDER
            .comment("Soul Eater Attack Damage.")
            .defineInRange("soulEaterAttackDamage", 10.0, 0.0, 100000.0);

    public static final ForgeConfigSpec.DoubleValue SOUL_EATER_ATTACK_SPEED = BUILDER
            .comment("Soul Eater Attack Speed.")
            .defineInRange("soulEaterAttackSpeed", 2.0, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue GIANTS_RING_ATTACK_DAMAGE = BUILDER
            .comment("Bonus attack damage granted by Giant's Ring.")
            .defineInRange("giantsRingAttackDamage", 3.0, 0.0, 1024.0);

    public static final ForgeConfigSpec.DoubleValue GIANTS_RING_MAX_HEALTH = BUILDER
            .comment("Bonus max health granted by Giant's Ring.")
            .defineInRange("giantsRingMaxHealth", 10.0, 0.0, 1024.0);

    public static final ForgeConfigSpec.DoubleValue GIANTS_RING_SPEED_BONUS = BUILDER
            .comment("Giant's Ring: movement speed bonus multiplier. (0.2 = +20% of base movement speed)")
            .defineInRange("giantsRingSpeedBonus", 0.2, -1.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue MOON_SHARD_GRAVITY_REDUCTION = BUILDER
            .comment("Gravity change applied while Moon Shard is in hotbar.",
                    "Default Minecraft gravity is 0.08.")
            .defineInRange("moonShardGravityReduction", -0.06, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue MOON_GRAVITY_REDUCTION = BUILDER
            .comment("Gravity change applied while holding the Moon item.",
                    "Default Minecraft gravity is 0.08.")
            .defineInRange("moonGravityReduction", -0.1, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue GRAVITY_CORE_GRAVITY_MULTIPLIER = BUILDER
            .comment("Multiplier applied to gravity when holding SHIFT with Gravity Core.")
            .defineInRange("gravityCoreGravityMultiplier", 5.0, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue GRAVITY_CORE_IMPACT_RADIUS_COEFFICIENT = BUILDER
            .comment("Impact radius coefficient when falling with Gravity Core.",
                    "Impact radius = fall speed * this value.")
            .defineInRange("gravityCoreImpactRadiusCoefficient", 1.0, 0.0, 10.0);

    public static final ForgeConfigSpec.DoubleValue GRAVITY_CORE_IMPACT_DAMAGE_COEFFICIENT = BUILDER
            .comment("Impact damage coefficient when falling with Gravity Core.",
                    "Damage = player's attack damage * fall speed * this value.")
            .defineInRange("gravityCoreImpactDamageCoefficient", 0.5, 0.0, 10.0);

    public static final ForgeConfigSpec.DoubleValue GRAVITY_CORE_JUMP_STRENGTH = BUILDER
            .comment("Jump strength boost applied when using Gravity Core.")
            .defineInRange("gravityCoreJumpStrength", 5.0, 0.0, 50.0);

    public static final ForgeConfigSpec.IntValue GRAVITY_CORE_COOLDOWN = BUILDER
            .comment("Cooldown in ticks for Gravity Core jump (20 ticks = 1 second).")
            .defineInRange("gravityCoreCooldown", 0, 0, 100000);

    public static final ForgeConfigSpec.DoubleValue RING_OF_AGILITY_CHANCE_SCALE = BUILDER
            .comment("Multiplier for movement speed in dodge chance formula.",
                    "Formula: dodgeChance = 1.0 - pow(0.99, movementSpeed * 100 * scale)")
            .defineInRange("ringOfAgilityChanceScale", 1.0, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue RING_OF_AGILITY_MAX_DODGE_CHANCE = BUILDER
            .comment("Maximum dodge chance.",
                    "Example: 0.3 = 30% maximum dodge chance.")
            .defineInRange("ringOfAgilityMaxDodgeChance", 0.3, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue DIVINE_SHIELD_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown duration of the Divine Shield in ticks. (1 second = 20 ticks)")
            .defineInRange("divineShieldCooldownTicks", 500, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue DIVINE_SHIELD_GIVE_EFFECT = BUILDER
            .comment("Whether to give the player the Divine Shield effect as an indicator that the item is not on cooldown.")
            .define("divineShieldGiveEffect", true);

    public static final ForgeConfigSpec.DoubleValue PERMAFROST_ATTACK_DAMAGE = BUILDER
            .comment("Permafrost Attack Damage.")
            .defineInRange("permafrostAttackDamage", 10.0, 0.0, 100000.0);

    public static final ForgeConfigSpec.DoubleValue PERMAFROST_ATTACK_SPEED = BUILDER
            .comment("Permafrost Attack Speed.")
            .defineInRange("permafrostAttackSpeed", 2.0, 0.0, 100.0);

    public static final ForgeConfigSpec.IntValue PERMAFROST_ATTACK_DEBUFF_DURATION = BUILDER
            .comment("Duration of the Permafrost debuff applied when hitting a target, in ticks. (1 second = 20 ticks)")
            .defineInRange("permafrostAttackDebuffDuration", 600, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue PERMAFROST_ATTACK_MAX_STACKS = BUILDER
            .comment("Maximum number of Permafrost stacks applied from attacks.")
            .defineInRange("permafrostAttackMaxStacks", 10, 1, 100);

    public static final ForgeConfigSpec.DoubleValue PERMAFROST_ATTACK_REDUCTION_PER_STACK = BUILDER
            .comment("Movement/Flying speed reduction per Permafrost stack applied from attacks (0.05 = 5% slower).")
            .defineInRange("permafrostAttackReductionPerStack", 0.05, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue PERMAFROST_AURA_RADIUS = BUILDER
            .comment("Radius of the Permafrost aura effect in blocks.")
            .defineInRange("permafrostAuraRadius", 5, 0, 32);

    public static final ForgeConfigSpec.IntValue PERMAFROST_AURA_DEBUFF_DURATION = BUILDER
            .comment("Duration of the Permafrost aura debuff in ticks. (1 second = 20 ticks)")
            .defineInRange("permafrostAuraDebuffDuration", 100, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue PERMAFROST_AURA_REDUCTION_FACTOR = BUILDER
            .comment("Movement/Flying speed reduction factor from Permafrost aura. (0.2 = 20% slower)")
            .defineInRange("permafrostAuraReductionFactor", 0.2, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_ATTACK_DAMAGE = BUILDER
            .comment("Crucible Of Rile Attack Damage.")
            .defineInRange("crucibleOfRileAttackDamage", 16.0, 0.0, 100000.0);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_ATTACK_SPEED = BUILDER
            .comment("Crucible Of Rile Attack Speed.")
            .defineInRange("crucibleOfRileAttackSpeed", 1.4, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_EXECUTE_THRESHOLD = BUILDER
            .comment("Crucible Of Rile: health threshold at or below which enemies can be executed.",
                    "Bosses (forge:bosses entity type tag) cannot be executed.")
            .defineInRange("crucibleOfRileExecuteThreshold", 5.0, 0.0, 100000.0);

    public static final ForgeConfigSpec.IntValue CRUCIBLE_OF_RILE_COOLDOWN = BUILDER
            .comment("Crucible Of Rile: ability cooldown in ticks. (1 second = 20 ticks)")
            .defineInRange("crucibleOfRileCooldown", 400, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_AGGRO_RADIUS = BUILDER
            .comment("Crucible Of Rile: aggro radius of the ability, in blocks.")
            .defineInRange("crucibleOfRileAggroRadius", 16.0, 0.0, 256.0);

    public static final ForgeConfigSpec.BooleanValue CRUCIBLE_OF_RILE_AGGRO_PLAYERS = BUILDER
            .comment("Crucible Of Rile: whether the ability should aggro players (forcing them to look at the item user).")
            .define("crucibleOfRileAggroPlayers", true);

    public static final ForgeConfigSpec.IntValue CRUCIBLE_OF_RILE_AGGRO_DURATION = BUILDER
            .comment("Crucible Of Rile: duration of the forced aggro (camera lock) effect in ticks. (1 second = 20 ticks)")
            .defineInRange("crucibleOfRileAggroDuration", 100, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_ARMOR_BUFF_VALUE = BUILDER
            .comment("Crucible Of Rile: armor buff multiplier applied by the active ability. (0.5 = +50%)")
            .defineInRange("crucibleOfRileArmorBuffValue", 0.5, 0.0, 100.0);

    public static final ForgeConfigSpec.IntValue CRUCIBLE_OF_RILE_ARMOR_BUFF_DURATION = BUILDER
            .comment("Crucible Of Rile: duration of the armor buff from the active ability, in ticks. (1 second = 20 ticks)")
            .defineInRange("crucibleOfRileArmorBuffDuration", 100, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue CRUCIBLE_OF_RILE_HITS_FOR_COUNTERATTACK = BUILDER
            .comment("Crucible Of Rile: number of hits the player must take before triggering a counterattack.")
            .defineInRange("crucibleOfRileHitsForCounterattack", 5, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_COUNTERATTACK_RADIUS = BUILDER
            .comment("Crucible Of Rile: radius of the counterattack, in blocks.")
            .defineInRange("crucibleOfRileCounterattackRadius", 3.5, 0.0, 64.0);

    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_OF_RILE_PERM_ARMOR_PER_UNIQUE = BUILDER
            .comment("Permanent armor bonus (percent as fraction) per unique killed enemy type. (0.01 = +1%)")
            .defineInRange("crucibleOfRilePermanentArmorPerUnique", 0.005, 0.0, 10.0);

    public static final ForgeConfigSpec.DoubleValue MOAI_CHARM_KNOCKBACK_RESISTANCE = BUILDER
            .comment("Moai Charm: knockback resistance bonus. (1.0 = full knockback immunity)")
            .defineInRange("moaiCharmKnockbackResistanceBonus", 1.0, 0.0, 1.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static volatile int revision;
    private static volatile Set<ResourceLocation> lootTables = Set.of();

    private Config() {
    }

    public static int revision() {
        return revision;
    }

    public static Set<ResourceLocation> lootTables() {
        return lootTables;
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent.Loading event) {
        refresh(event);
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        refresh(event);
    }

    private static void refresh(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        lootTables = LOOT_TABLES.get().stream().map(ResourceLocation::new).collect(Collectors.toUnmodifiableSet());
        revision++;
    }

    private static boolean isResourceLocation(Object value) {
        return value instanceof String string && ResourceLocation.isValidResourceLocation(string);
    }

    private static List<String> defaultLootTables() {
        List<String> tables = new ArrayList<>(List.of(
                "minecraft:chests/abandoned_mineshaft",
                "minecraft:chests/ancient_city",
                "minecraft:chests/ancient_city_ice_box",
                "minecraft:chests/bastion_bridge",
                "minecraft:chests/bastion_hoglin_stable",
                "minecraft:chests/bastion_other",
                "minecraft:chests/bastion_treasure",
                "minecraft:chests/buried_treasure",
                "minecraft:chests/desert_pyramid",
                "minecraft:chests/end_city_treasure",
                "minecraft:chests/igloo_chest",
                "minecraft:chests/jungle_temple",
                "minecraft:chests/nether_bridge",
                "minecraft:chests/pillager_outpost",
                "minecraft:chests/ruined_portal",
                "minecraft:chests/shipwreck_map",
                "minecraft:chests/shipwreck_supply",
                "minecraft:chests/shipwreck_treasure",
                "minecraft:chests/simple_dungeon",
                "minecraft:chests/spawn_bonus_chest",
                "minecraft:chests/stronghold_corridor",
                "minecraft:chests/stronghold_crossing",
                "minecraft:chests/stronghold_library",
                "minecraft:chests/underwater_ruin_big",
                "minecraft:chests/underwater_ruin_small",
                "minecraft:chests/village/village_armorer",
                "minecraft:chests/village/village_snowy_house",
                "minecraft:chests/village/village_temple",
                "minecraft:chests/village/village_toolsmith",
                "minecraft:chests/village/village_weaponsmith",
                "minecraft:chests/woodland_mansion"));
        if (ModList.get().isLoaded("born_in_chaos_v1")) {
            tables.addAll(List.of(
                    "minecraft:chests/basic_chest",
                    "minecraft:chests/chest_level_1",
                    "minecraft:chests/chest_level_2",
                    "minecraft:chests/chest_level_3",
                    "minecraft:chests/farm_drop",
                    "minecraft:chests/firewell_d",
                    "minecraft:chests/shater"));
        }
        if (ModList.get().isLoaded("iceandfire")) {
            tables.addAll(List.of(
                    "iceandfire:chest/cyclops_cave",
                    "iceandfire:chest/fire_dragon_female_cave",
                    "iceandfire:chest/fire_dragon_male_cave",
                    "iceandfire:chest/fire_dragon_roost",
                    "iceandfire:chest/graveyard",
                    "iceandfire:chest/hydra_cave",
                    "iceandfire:chest/ice_dragon_female_cave",
                    "iceandfire:chest/ice_dragon_male_cave",
                    "iceandfire:chest/ice_dragon_roost",
                    "iceandfire:chest/lightning_dragon_female_cave",
                    "iceandfire:chest/lightning_dragon_male_cave",
                    "iceandfire:chest/lightning_dragon_roost",
                    "iceandfire:chest/mausoleum_chest",
                    "iceandfire:chest/myrmex_desert_food_chest",
                    "iceandfire:chest/myrmex_jungle_food_chest",
                    "iceandfire:chest/myrmex_loot_chest",
                    "iceandfire:chest/myrmex_trash_chest",
                    "iceandfire:chest/village_scribe"));
        }
        if (ModList.get().isLoaded("call_of_yucutan")) {
            tables.addAll(List.of(
                    "call_of_yucutan:chest/crypt_chest_loot",
                    "call_of_yucutan:chest/overgrown_chest_loot"));
        }
        return tables;
    }
}

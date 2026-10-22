package com.misanthropy.enigmaticdice.dice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.dice.action.RunCommand;
import com.misanthropy.enigmaticdice.dice.action.Summon;
import com.misanthropy.enigmaticdice.item.FourLeafClover;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.registry.ModStats;
import com.misanthropy.enigmaticdice.util.Combat;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@GameTestHolder(EnigmaticDice.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DiceGameTests {
    private static final int VANILLA_EVENTS = 39;

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "dice-test")) {
            @Override
            public boolean isInvulnerableTo(DamageSource source) {
                return false;
            }
        };
        Vec3 spot = helper.absoluteVec(new Vec3(4.5, 1, 4.5));
        player.moveTo(spot.x, spot.y, spot.z);
        return player;
    }

    @GameTest(template = "empty")
    public static void eventsLoad(GameTestHelper helper) {
        helper.assertTrue(DiceEvents.all().size() == VANILLA_EVENTS, "Expected " + VANILLA_EVENTS + " events but loaded " + DiceEvents.all().size());
        helper.assertTrue(DiceEvents.get(EnigmaticDice.id("minecraft_explosion")) != null, "Explosion event missing");
        helper.assertTrue(DiceEvents.get(EnigmaticDice.id("alexscaves_nuclear_bomb")) == null, "Modded event loaded without its mod");
        helper.assertTrue(DiceEvents.get(EnigmaticDice.id("minecraft_give_modded_random_potion")) == null, "Disabled event loaded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void statsKeepTheirOriginalIds(GameTestHelper helper) {
        helper.assertTrue(new ResourceLocation("obtained_dice_from_block").equals(BuiltInRegistries.CUSTOM_STAT.getKey(ModStats.DICE_FROM_BLOCK)),
                "Block stat id changed, old worlds would lose their counts");
        helper.assertTrue(new ResourceLocation("obtained_dice_from_mob").equals(BuiltInRegistries.CUSTOM_STAT.getKey(ModStats.DICE_FROM_MOB)),
                "Mob stat id changed, old worlds would lose their counts");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chanceMatchesOriginalRetryRolls(GameTestHelper helper) {
        Random random = new Random(42);
        int trials = 400_000;
        for (int rarity : new int[]{2, 20, 1000}) {
            for (float luck : new float[]{0, 3, 7.9F, 40, -5}) {
                for (DiceCategory category : DiceCategory.values()) {
                    DiceSettings settings = new DiceSettings(true, rarity, category, Optional.empty(), 0, 0);
                    int hits = 0;
                    for (int i = 0; i < trials; i++) {
                        if (originalRoll(random, rarity, luck, category)) hits++;
                    }
                    double expected = settings.chance(luck);
                    double observed = hits / (double) trials;
                    double tolerance = 5 * Math.sqrt(expected * (1 - expected) / trials) + 1e-6;
                    helper.assertTrue(Math.abs(observed - expected) <= tolerance,
                            "rarity " + rarity + " luck " + luck + " " + category + ": expected " + expected + " observed " + observed);
                }
            }
        }
        helper.succeed();
    }

    private static boolean originalRoll(Random random, int rarity, float luck, DiceCategory category) {
        if (category == DiceCategory.NEUTRAL) return random.nextInt(rarity) == 0;
        int attempts = 1;
        int points = category == DiceCategory.POSITIVE && luck > 0 ? (int) luck : category == DiceCategory.NEGATIVE && luck < 0 ? (int) -luck : 0;
        for (int i = 0; i < points; i++) {
            if (random.nextFloat() < 0.1F) attempts++;
        }
        for (int i = 0; i < attempts; i++) {
            if (random.nextInt(rarity) == 0) return true;
        }
        return false;
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void everyEventRunsWithoutErrors(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Vec3 spot = player.position();
        int ran = 0;
        for (DiceEvent event : DiceEvents.all()) {
            player.getInventory().clearContent();
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            player.moveTo(spot.x, spot.y, spot.z);
            DiceContext context = new DiceContext(player, event, new HashSet<>());
            if (!event.action().canRun(context)) continue;
            DiceAction.Result result = event.action().run(context);
            helper.assertTrue(result != null, "Null result from " + event.id());
            ran++;
        }
        int total = ran;
        helper.assertTrue(total >= VANILLA_EVENTS - 6, "Only " + total + " events could run");
        helper.runAfterDelay(1100, helper::succeed);
    }

    @GameTest(template = "empty")
    public static void commandEventsRunAsTheRoller(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        DiceSettings settings = new DiceSettings(true, 1, DiceCategory.NEUTRAL, Optional.empty(), 0, 0);
        RunCommand tag = new RunCommand(List.of("tag @s add dice_command_test"), List.of());
        DiceContext context = new DiceContext(player, new DiceEvent(EnigmaticDice.id("test_command"), settings, tag), new HashSet<>());
        helper.assertTrue(tag.run(context) == DiceAction.Result.DONE, "Valid command should succeed");
        helper.assertTrue(player.getTags().contains("dice_command_test"), "Command did not run as the roller");
        RunCommand broken = new RunCommand(List.of(), List.of("not_a_real_command at all"));
        helper.assertTrue(broken.run(context) == DiceAction.Result.FAILED, "Invalid command should fail so the roll picks again");
        helper.assertTrue(RunCommand.CODEC.codec().parse(JsonOps.INSTANCE, new JsonObject()).error().isPresent(), "Empty command event should not load");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void summonOnSpawnRunsAsTheMob(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        DiceSettings settings = new DiceSettings(true, 1, DiceCategory.NEUTRAL, Optional.empty(), 0, 0);
        String json = "{\"spawns\":[{\"entity\":\"minecraft:zombie\",\"placement\":{\"type\":\"front\",\"distance\":2}}],\"on_spawn\":[\"tag @s add dice_on_spawn\"]}";
        Summon summon = Summon.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow(false, helper::fail);
        summon.run(new DiceContext(player, new DiceEvent(EnigmaticDice.id("test_on_spawn"), settings, summon), new HashSet<>()));
        helper.runAfterDelay(10, () -> {
            List<Zombie> zombies = helper.getLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(6), zombie -> zombie.getTags().contains("dice_on_spawn"));
            helper.assertTrue(zombies.size() == 1, "on_spawn command did not run as the summoned mob");
            helper.assertFalse(player.getTags().contains("dice_on_spawn"), "on_spawn command ran as the roller");
            zombies.forEach(Zombie::discard);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void rollerAlwaysPicksSomething(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Vec3 spot = player.position();
        for (int i = 0; i < 50; i++) {
            helper.assertTrue(DiceRoller.roll(player), "Roll " + i + " found nothing to do");
            player.removeAllEffects();
            player.moveTo(spot.x, spot.y, spot.z);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cloverLuckIsCapped(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(ModItems.FOUR_LEAF_CLOVER.get(), 64));
        player.getInventory().offhand.set(0, new ItemStack(ModItems.FOUR_LEAF_CLOVER.get(), 10));
        FourLeafClover.updateLuck(player);
        helper.assertTrue(player.getAttributeValue(Attributes.LUCK) == 40, "Luck should cap at 40 but was " + player.getAttributeValue(Attributes.LUCK));
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(ModItems.FOUR_LEAF_CLOVER.get(), 3));
        FourLeafClover.updateLuck(player);
        helper.assertTrue(player.getAttributeValue(Attributes.LUCK) == 3, "Three clovers should give 3 luck");
        player.getInventory().clearContent();
        FourLeafClover.updateLuck(player);
        helper.assertTrue(player.getAttributeValue(Attributes.LUCK) == 0, "No clovers should give no luck");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void divineShieldBlocksHitsButNotKill(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.reset();
            handler.setEquippedCurio("ring", 0, new ItemStack(ModItems.DIVINE_SHIELD.get()));
        });
        helper.assertTrue(Combat.isWearing(player, ModItems.DIVINE_SHIELD.get()), "Test setup could not equip the shield");
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(player, player.damageSources().mobAttack(zombie), 4)), "Shield should block a mob hit");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ModItems.DIVINE_SHIELD.get()), "Blocking should start the cooldown");
        player.getCooldowns().removeCooldown(ModItems.DIVINE_SHIELD.get());
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(player, player.damageSources().fellOutOfWorld(), 4)), "Shield must not block the void");
        player.kill();
        helper.assertTrue(player.isDeadOrDying(), "Shield must not stop /kill");
        helper.assertTrue(!player.getCooldowns().isOnCooldown(ModItems.DIVINE_SHIELD.get()), "/kill must not consume the shield");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void antimatterBreaksBedrockButNotPortalFrames(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setShiftKeyDown(true);
        BlockPos bedrock = new BlockPos(2, 1, 2);
        BlockPos frame = new BlockPos(6, 1, 6);
        helper.setBlock(bedrock, Blocks.BEDROCK);
        helper.setBlock(frame, Blocks.END_PORTAL_FRAME);
        for (BlockPos pos : List.of(bedrock, frame)) {
            BlockPos absolute = helper.absolutePos(pos);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ANTIMATTER.get()));
            ModItems.ANTIMATTER.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        }
        helper.assertBlockNotPresent(Blocks.BEDROCK, bedrock);
        helper.assertBlockPresent(Blocks.END_PORTAL_FRAME, frame);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unequalExchangeDebuffStacksAndCaps(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        double base = player.getAttributeBaseValue(Attributes.MAX_HEALTH);
        player.addEffect(new MobEffectInstance(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get(), 600, 1));
        helper.assertTrue(Math.abs(player.getMaxHealth() - base * 0.6) < 1e-4, "Two hits should leave 60% max health but got " + player.getMaxHealth());
        player.removeEffect(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get());
        player.addEffect(new MobEffectInstance(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get(), 600, 9));
        helper.assertTrue(Math.abs(player.getMaxHealth() - base * 0.2) < 1e-4, "Max health loss must cap at 80% but got " + player.getMaxHealth());
        player.removeEffect(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get());
        helper.assertTrue(Math.abs(player.getMaxHealth() - base) < 1e-4, "Max health must return after the debuff");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void shrinkingScalesTheHitbox(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        float width = player.getBbWidth();
        DiceEvent ant = DiceEvents.get(EnigmaticDice.id("minecraft_ant_size_shrink"));
        helper.assertTrue(ant != null && DiceRoller.trigger(player, ant), "Ant shrink should apply");
        helper.succeedWhen(() -> {
            player.doTick();
            helper.assertTrue(Math.abs(player.getBbWidth() - width * 0.1F) < 1e-3, "Width should be 10% but is " + player.getBbWidth());
        });
    }
}

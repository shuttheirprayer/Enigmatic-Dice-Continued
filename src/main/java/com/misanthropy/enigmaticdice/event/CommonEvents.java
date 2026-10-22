package com.misanthropy.enigmaticdice.event;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.command.DiceCommand;
import com.misanthropy.enigmaticdice.item.CrucibleOfRile;
import com.misanthropy.enigmaticdice.item.DivineShield;
import com.misanthropy.enigmaticdice.item.FourLeafClover;
import com.misanthropy.enigmaticdice.item.RingOfAgility;
import com.misanthropy.enigmaticdice.item.SoulEater;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import com.misanthropy.enigmaticdice.registry.ModStats;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            GravityHandler.tick(player);
            return;
        }
        if (player.level().isClientSide) return;
        if (player.tickCount % 10 == 0) FourLeafClover.updateLuck(player);
        if (player.tickCount % 20 == 0) SoulEater.expireHealthBoost(player);
    }

    @SubscribeEvent
    static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (entity.level().isClientSide || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (source.getEntity() != null && Combat.isWearing(entity, ModItems.RING_OF_AGILITY.get())
                && entity.getRandom().nextDouble() < RingOfAgility.dodgeChance(entity)) {
            event.setCanceled(true);
            return;
        }
        if (event.getAmount() > 0 && entity instanceof Player player && Combat.isWearing(player, ModItems.DIVINE_SHIELD.get())
                && ((DivineShield) ModItems.DIVINE_SHIELD.get()).tryBlock(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onLivingAttackLate(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (event.isCanceled() || entity.level().isClientSide || attacker == null || attacker == entity || event.getAmount() <= 1) return;
        if (entity.getMainHandItem().getItem() instanceof CrucibleOfRile) CrucibleOfRile.onHolderAttacked(entity);
    }

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer killer)) return;
        ItemStack weapon = killer.getMainHandItem();
        if (!(weapon.getItem() instanceof CrucibleOfRile)) return;
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (type != null) CrucibleOfRile.recordKill(weapon, type.toString(), event.getEntity());
    }

    @SubscribeEvent
    static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.DAY_ON_THE_MOON.get())
                || entity instanceof Player player && (Combat.isWearing(player, ModItems.GRAVITY_CORE.get()) || GravityHandler.negatesFall(player))) {
            event.setDamageMultiplier(0);
        }
    }

    @SubscribeEvent
    static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null || event.getEffectInstance().getEffect() != ModEffects.FROST_HIT.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity.isAlive()) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.PERMAFROST_UNFROZEN.get(), SoundSource.PLAYERS, 1F, 1.0F);
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Monster monster) || !(event.getSource().getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) return;
        if (player.getRandom().nextDouble() >= Config.MOB_DROP_CHANCE.get() || !canEarnDie(player, ModStats.DICE_FROM_MOB, Config.MOB_DICE_TIME_INTERVAL.get())) return;
        event.getDrops().add(new ItemEntity(monster.level(), monster.getX(), monster.getY(), monster.getZ(), new ItemStack(ModItems.ENIGMATIC_DIE.get())));
        player.awardStat(ModStats.DICE_FROM_MOB);
        player.displayClientMessage(Component.translatable("enigmaticdice.mob_drop." + player.getRandom().nextInt(4)), false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player) || player instanceof FakePlayer) return;
        if (!(event.getLevel() instanceof Level level)) return;
        if (player.getRandom().nextDouble() >= Config.BLOCK_DROP_CHANCE.get() || !canEarnDie(player, ModStats.DICE_FROM_BLOCK, Config.BLOCK_DICE_TIME_INTERVAL.get())) return;
        level.addFreshEntity(new ItemEntity(level, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, new ItemStack(ModItems.ENIGMATIC_DIE.get())));
        player.awardStat(ModStats.DICE_FROM_BLOCK);
        player.displayClientMessage(Component.translatable("enigmaticdice.block_break"), false);
    }

    private static boolean canEarnDie(ServerPlayer player, ResourceLocation stat, int intervalMinutes) {
        if (intervalMinutes == 0) return true;
        int playMinutes = player.getStats().getValue(Stats.CUSTOM.get(Stats.TOTAL_WORLD_TIME)) / (20 * 60);
        return player.getStats().getValue(Stats.CUSTOM.get(stat)) < playMinutes / intervalMinutes;
    }

    @SubscribeEvent
    static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) return;
        DisplayInfo display = event.getAdvancement().getDisplay();
        if (display == null || display.getFrame() == FrameType.TASK) return;
        player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), new ItemStack(ModItems.ENIGMATIC_DIE.get())));
        player.displayClientMessage(Component.translatable("enigmaticdice.advancement_earned." + player.getRandom().nextInt(5)), false);
    }

    @SubscribeEvent
    static void onLootTableLoad(LootTableLoadEvent event) {
        if (!Config.lootTables().contains(event.getName())) return;
        event.getTable().addPool(LootPool.lootPool()
                .name("enigmaticdice:enigmatic_die")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.ENIGMATIC_DIE.get()))
                .when(LootItemRandomChanceCondition.randomChance(Config.CHEST_CHANCE.get().floatValue()))
                .build());
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        DiceCommand.register(event.getDispatcher());
    }
}

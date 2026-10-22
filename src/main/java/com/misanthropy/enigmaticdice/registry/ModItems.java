package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.item.Antimatter;
import com.misanthropy.enigmaticdice.item.CrucibleOfRile;
import com.misanthropy.enigmaticdice.item.DivineShield;
import com.misanthropy.enigmaticdice.item.EnigmaticDie;
import com.misanthropy.enigmaticdice.item.FourLeafClover;
import com.misanthropy.enigmaticdice.item.GiantsRing;
import com.misanthropy.enigmaticdice.item.GravityCore;
import com.misanthropy.enigmaticdice.item.MoaiCharm;
import com.misanthropy.enigmaticdice.item.Moon;
import com.misanthropy.enigmaticdice.item.MoonShard;
import com.misanthropy.enigmaticdice.item.Permafrost;
import com.misanthropy.enigmaticdice.item.RingOfAgility;
import com.misanthropy.enigmaticdice.item.SoulEater;
import com.misanthropy.enigmaticdice.item.UnequalExchange;
import com.misanthropy.enigmaticdice.item.UnfinishedWeapon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnigmaticDice.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnigmaticDice.MOD_ID);

    public static final RegistryObject<Item> ENIGMATIC_DIE = ITEMS.register("enigmatic_die", () -> new EnigmaticDie(new Item.Properties()));
    public static final RegistryObject<Item> UNEQUAL_EXCHANGE = ITEMS.register("unequal_exchange", () -> new UnequalExchange(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> SOUL_EATER = ITEMS.register("soul_eater", () -> new SoulEater(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ANTIMATTER = ITEMS.register("antimatter", () -> new Antimatter(new Item.Properties()));
    public static final RegistryObject<Item> FOUR_LEAF_CLOVER = ITEMS.register("four_leaf_clever", () -> new FourLeafClover(new Item.Properties()));
    public static final RegistryObject<Item> GIANTS_RING = ITEMS.register("giants_ring", () -> new GiantsRing(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MOON_SHARD = ITEMS.register("moon_shard", () -> new MoonShard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GRAVITY_CORE = ITEMS.register("gravity_core", () -> new GravityCore(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MOON = ITEMS.register("moon", () -> new Moon(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RING_OF_AGILITY = ITEMS.register("ring_of_agility", () -> new RingOfAgility(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVINE_SHIELD = ITEMS.register("divine_shield", () -> new DivineShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PERMAFROST = ITEMS.register("permafrost", () -> new Permafrost(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> KYOMU = ITEMS.register("kyomu", () -> new UnfinishedWeapon(Tiers.NETHERITE, 5, -2F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> PHOENIX = ITEMS.register("phoenix", () -> new UnfinishedWeapon(Tiers.NETHERITE, 5, -2F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRUCIBLE_OF_RILE = ITEMS.register("crucible_of_rile", () -> new CrucibleOfRile(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> FIRE_CRYSTAL_GREATSWORD = ITEMS.register("fire_crystal_greatsword", () -> new UnfinishedWeapon(Tiers.NETHERITE, 11, -2.6F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> FIRE_CRYSTAL_SPEAR = ITEMS.register("fire_crystal_spear", () -> new UnfinishedWeapon(Tiers.NETHERITE, 5, -2F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> MOAI_CHARM = ITEMS.register("moai_charm", () -> new MoaiCharm(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("enigmaticdice_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ENIGMATIC_DIE.get()))
            .title(Component.translatable("creativetab.enigmaticdice_tab"))
            .displayItems((parameters, output) -> List.of(ENIGMATIC_DIE, UNEQUAL_EXCHANGE, SOUL_EATER, PERMAFROST, CRUCIBLE_OF_RILE,
                            FOUR_LEAF_CLOVER, ANTIMATTER, GIANTS_RING, MOON_SHARD, MOON, GRAVITY_CORE, RING_OF_AGILITY, DIVINE_SHIELD, MOAI_CHARM)
                    .forEach(item -> output.accept(item.get())))
            .build());

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
    }
}

package com.misanthropy.enigmaticdice;

import com.misanthropy.enigmaticdice.item.Antimatter;
import com.misanthropy.enigmaticdice.network.Network;
import com.misanthropy.enigmaticdice.registry.ModAttributes;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import com.misanthropy.enigmaticdice.registry.ModStats;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EnigmaticDice.MOD_ID)
public final class EnigmaticDice {
    public static final String MOD_ID = "enigmaticdice";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnigmaticDice() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(bus);
        ModSounds.register(bus);
        ModEffects.register(bus);
        ModAttributes.register(bus);
        bus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Network.register();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Antimatter.registerDispenserBehavior();
            ModStats.init();
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

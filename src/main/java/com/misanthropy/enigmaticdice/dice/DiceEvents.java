package com.misanthropy.enigmaticdice.dice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID)
public final class DiceEvents extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<ResourceLocation, DiceEvent> events = Map.of();

    private final ICondition.IContext conditions;

    private DiceEvents(ICondition.IContext conditions) {
        super(GSON, "dice_events");
        this.conditions = conditions;
    }

    public static Collection<DiceEvent> all() {
        return events.values();
    }

    @Nullable
    public static DiceEvent get(ResourceLocation id) {
        return events.get(id);
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DiceEvents(event.getConditionContext()));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, DiceEvent> loaded = new TreeMap<>();
        new TreeMap<>(files).forEach((id, element) -> {
            try {
                DiceEvent event = parse(id, GsonHelper.convertToJsonObject(element, "dice event"));
                if (event != null) loaded.put(id, event);
            } catch (RuntimeException exception) {
                EnigmaticDice.LOGGER.error("Skipping dice event {}: {}", id, exception.getMessage());
            }
        });
        events = Collections.unmodifiableMap(new LinkedHashMap<>(loaded));
        EnigmaticDice.LOGGER.info("Loaded {} dice events", loaded.size());
    }

    @Nullable
    private DiceEvent parse(ResourceLocation id, JsonObject json) {
        if (!CraftingHelper.processConditions(json, "conditions", conditions)) return null;
        DiceSettings settings = DiceSettings.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {
        });
        if (!settings.enabled()) return null;
        String type = GsonHelper.getAsString(json, "type");
        MapCodec<? extends DiceAction> codec = DiceActions.get(type);
        if (codec == null) throw new IllegalArgumentException("Unknown dice event type " + type);
        DiceAction action = codec.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {
        });
        return new DiceEvent(id, settings, action);
    }
}

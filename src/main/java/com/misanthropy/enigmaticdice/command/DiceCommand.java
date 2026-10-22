package com.misanthropy.enigmaticdice.command;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.dice.DiceCategory;
import com.misanthropy.enigmaticdice.dice.DiceEvent;
import com.misanthropy.enigmaticdice.dice.DiceEvents;
import com.misanthropy.enigmaticdice.dice.DiceRoller;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DiceCommand {
    private static final DynamicCommandExceptionType UNKNOWN_EVENT = new DynamicCommandExceptionType(id -> Component.translatable("commands.enigmaticdice.unknown_event", id));
    private static final String ODDS_FILE = "enigmatic_dice_odds.txt";
    private static final int[] ODDS_LUCK = {-100, -40, -20, -10, -5, -3, -2, -1, 0, 1, 2, 3, 5, 10, 20, 40, 100};

    private DiceCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("enigmaticdice")
                .then(Commands.literal("roll").requires(source -> source.hasPermission(2))
                        .executes(context -> roll(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> roll(context, EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("trigger").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("event", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(DiceEvents.all().stream().map(DiceEvent::id), builder))
                                .executes(context -> trigger(context, List.of(context.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> trigger(context, EntityArgument.getPlayers(context, "targets"))))))
                .then(Commands.literal("odds").requires(source -> source.hasPermission(2))
                        .executes(context -> odds(context.getSource(), context.getSource().getPlayerOrException().getLuck()))
                        .then(Commands.argument("luck", FloatArgumentType.floatArg(-1024, 1024))
                                .executes(context -> odds(context.getSource(), FloatArgumentType.getFloat(context, "luck")))))
                .then(Commands.literal("luck")
                        .executes(context -> luck(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player()).requires(source -> source.hasPermission(2))
                                .executes(context -> luck(context.getSource(), EntityArgument.getPlayer(context, "target"))))));
    }

    private static int roll(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        int rolled = 0;
        for (ServerPlayer player : targets) {
            if (DiceRoller.roll(player)) rolled++;
        }
        int count = rolled;
        context.getSource().sendSuccess(() -> Component.translatable("commands.enigmaticdice.roll", count, targets.size()), true);
        return count;
    }

    private static int trigger(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "event");
        DiceEvent event = DiceEvents.get(id);
        if (event == null) throw UNKNOWN_EVENT.create(id);
        int triggered = 0;
        for (ServerPlayer player : targets) {
            if (DiceRoller.trigger(player, event)) triggered++;
        }
        int count = triggered;
        if (count == 0) {
            context.getSource().sendFailure(Component.translatable("commands.enigmaticdice.trigger.failed", id));
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("commands.enigmaticdice.trigger", id, count, targets.size()), true);
        }
        return count;
    }

    private static int luck(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.translatable("commands.enigmaticdice.luck", player.getDisplayName(),
                String.format(Locale.ROOT, "%.2f", player.getLuck())).withStyle(ChatFormatting.GREEN), false);
        return (int) player.getLuck();
    }

    private static int odds(CommandSourceStack source, float luck) {
        Collection<DiceEvent> events = DiceEvents.all();
        Map<DiceCategory, Double> shares = categoryShares(events, luck);
        source.sendSuccess(() -> Component.translatable("commands.enigmaticdice.odds",
                String.format(Locale.ROOT, "%.2f", luck),
                percent(shares.get(DiceCategory.POSITIVE)), percent(shares.get(DiceCategory.NEUTRAL)), percent(shares.get(DiceCategory.NEGATIVE))), false);
        Path file = source.getServer().getWorldPath(LevelResource.ROOT).resolve(ODDS_FILE);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeOdds(writer, events);
            source.sendSuccess(() -> Component.translatable("commands.enigmaticdice.odds.file", ODDS_FILE), false);
        } catch (IOException exception) {
            EnigmaticDice.LOGGER.error("Could not write {}", file, exception);
            source.sendFailure(Component.translatable("commands.enigmaticdice.odds.file_failed", ODDS_FILE));
        }
        return events.size();
    }

    private static void writeOdds(Writer writer, Collection<DiceEvent> events) throws IOException {
        writer.write("Chance of each dice event per roll, in percent, by player luck.\n");
        writer.write("Events that cannot run at the moment of the roll are skipped and the rest scale up proportionally.\n\n");
        writer.write(String.format(Locale.ROOT, "%-48s%-10s", "event", "category"));
        for (int luck : ODDS_LUCK) writer.write(String.format(Locale.ROOT, "%9d", luck));
        writer.write("\n");
        double[] totals = new double[ODDS_LUCK.length];
        for (int i = 0; i < ODDS_LUCK.length; i++) {
            for (DiceEvent event : events) totals[i] += event.settings().chance(ODDS_LUCK[i]);
        }
        for (DiceEvent event : events) {
            writer.write(String.format(Locale.ROOT, "%-48s%-10s", event.id(), event.settings().category().getSerializedName()));
            for (int i = 0; i < ODDS_LUCK.length; i++) {
                writer.write(String.format(Locale.ROOT, "%9.3f", 100 * event.settings().chance(ODDS_LUCK[i]) / totals[i]));
            }
            writer.write("\n");
        }
        writer.write("\n");
        for (DiceCategory category : DiceCategory.values()) {
            writer.write(String.format(Locale.ROOT, "%-58s", "total " + category.getSerializedName()));
            for (int luck : ODDS_LUCK) writer.write(String.format(Locale.ROOT, "%9.3f", 100 * categoryShares(events, luck).get(category)));
            writer.write("\n");
        }
    }

    private static Map<DiceCategory, Double> categoryShares(Collection<DiceEvent> events, float luck) {
        Map<DiceCategory, Double> shares = new EnumMap<>(DiceCategory.class);
        for (DiceCategory category : DiceCategory.values()) shares.put(category, 0.0);
        double total = 0;
        for (DiceEvent event : events) {
            double chance = event.settings().chance(luck);
            shares.merge(event.settings().category(), chance, Double::sum);
            total += chance;
        }
        double sum = total;
        if (sum > 0) shares.replaceAll((category, value) -> value / sum);
        return shares;
    }

    private static String percent(double share) {
        return String.format(Locale.ROOT, "%.2f%%", share * 100);
    }
}

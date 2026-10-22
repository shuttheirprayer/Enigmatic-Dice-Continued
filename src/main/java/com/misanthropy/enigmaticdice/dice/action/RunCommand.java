package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public record RunCommand(List<String> commands, List<String> variants) implements DiceAction {
    public static final MapCodec<RunCommand> CODEC = RecordCodecBuilder.<RunCommand>mapCodec(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(RunCommand::commands),
            Codec.STRING.listOf().optionalFieldOf("variants", List.of()).forGetter(RunCommand::variants)
    ).apply(instance, RunCommand::new)).flatXmap(RunCommand::validate, RunCommand::validate);

    private static DataResult<RunCommand> validate(RunCommand action) {
        return action.commands.isEmpty() && action.variants.isEmpty()
                ? DataResult.error(() -> "A command event needs commands or variants")
                : DataResult.success(action);
    }

    @Override
    public Result run(DiceContext context) {
        CommandSourceStack source = context.player().createCommandSourceStack().withPermission(Commands.LEVEL_GAMEMASTERS).withSuppressedOutput();
        boolean ran = false;
        for (String command : commands) {
            ran |= execute(source, command);
        }
        if (!variants.isEmpty()) ran |= execute(source, variants.get(context.random().nextInt(variants.size())));
        return ran ? Result.DONE : Result.FAILED;
    }

    static boolean execute(CommandSourceStack source, String command) {
        String line = command.startsWith("/") ? command.substring(1) : command;
        Commands commands = source.getServer().getCommands();
        ParseResults<CommandSourceStack> parsed = commands.getDispatcher().parse(line, source);
        CommandSyntaxException error = Commands.getParseException(parsed);
        if (error != null) {
            EnigmaticDice.LOGGER.warn("Dice command '{}' is invalid: {}", line, error.getMessage());
            return false;
        }
        return commands.performCommand(parsed, line) > 0;
    }
}

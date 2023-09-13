package dev.maykol.bot.command.data;

import dev.maykol.bot.command.annotation.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandData {

    private final Object object;
    private final Command command;

    private final MethodData[] methodData;

}

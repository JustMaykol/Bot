package dev.maykol.bot.command.data;

import dev.maykol.bot.command.annotation.Name;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParameterData {

    private final Name name;

    private final String string;
    private final Class<?> clazz;

}

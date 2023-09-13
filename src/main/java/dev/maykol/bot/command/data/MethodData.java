package dev.maykol.bot.command.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class MethodData {

    private final Method method;

    private final ParameterData[] parameterData;

}

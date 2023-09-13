package dev.maykol.bot.command;

import dev.maykol.bot.CommandManager;
import dev.maykol.bot.command.adapter.Adapter;
import dev.maykol.bot.command.context.Context;
import dev.maykol.bot.command.data.CommandData;
import dev.maykol.bot.command.data.MethodData;
import dev.maykol.bot.command.data.ParameterData;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class CommandExecutor {

    private final String[] args;

    private final String label;

    private final CommandData commandData;
    private final Context context;

    public void execute() {
        for (MethodData methodData : commandData.getMethodData()) {
            second:
            {
                if (methodData.getMethod().getDeclaringClass().equals(commandData.getObject().getClass())) {
                    if (methodData.getParameterData().length - 1 > args.length) {
                        break second;
                    }

                    for (MethodData otherMethodData : commandData.getMethodData()) {
                        if (!otherMethodData.equals(methodData)) {
                            if (methodData.getParameterData().length == otherMethodData.getParameterData().length && methodData.getParameterData()[0].getClazz().equals(Context.class)) {
                                break second;
                            }

                            if (args.length != methodData.getParameterData().length - 1 && args.length - methodData.getParameterData().length > args.length - otherMethodData.getParameterData().length) {
                                break second;
                            }
                        }
                    }

                    if (methodData.getParameterData().length > 0 && (methodData.getParameterData()[0].getClazz().equals(Context.class))) {
                        final List<Object> arguments = new ArrayList<>();
                        final ParameterData[] parameters = methodData.getParameterData();

                        arguments.add(context);

                        for (int i = 1; i < parameters.length; ++i) {
                            final ParameterData parameterData = parameters[i];
                            final Adapter<?> adapter = CommandManager.INSTANCE.getAdapter(parameterData.getClazz());

                            if (adapter == null) {
                                arguments.add(null);
                            } else {
                                Object object;

                                if (i == parameters.length - 1) {
                                    object = adapter.parse(context, StringUtils.join(args, " ", i - 1,
                                            args.length));
                                } else {
                                    object = adapter.parse(context, args[i - 1]);
                                }

                                arguments.add(object);
                            }
                        }

                        if (arguments.size() == parameters.length) {
                            if (commandData.getCommand().log()) {
                                Nitrogen.INSTANCE.getLogger().info(
                                        MessageFormat.format(
                                                "{0} executed: {1}{2} {3}",
                                                context.getUser().getAsTag(),
                                                Locale.PREFIX.getAsString(),
                                                label,
                                                Joiner.on(" ").skipNulls().join(args)
                                        )
                                );
                            }

                            try {
                                methodData.getMethod().invoke(commandData.getObject(), arguments.toArray());
                            } catch (Exception exception) {
                                // ignored
                            }

                            return;
                        }
                    }
                }
            }
        }

        context.getChannel().sendMessage(
                getUsage()
        ).complete().delete().queueAfter(10, TimeUnit.SECONDS);
    }

    private String getUsage() {
        final Map<Integer, List<String>> arguments = new HashMap<>();
        final StringBuilder builder = new StringBuilder("Usage: ").append(Locale.PREFIX.getAsString()).append(label);

        for (MethodData methodData : commandData.getMethodData()) {
            final ParameterData[] parameterDataArray = methodData.getParameterData();

            for (int i = 1; i < parameterDataArray.length; i++) {
                final List<String> argument = arguments.getOrDefault(i - 1, new ArrayList<>());
                final ParameterData parameterData = parameterDataArray[i];

                if (parameterData.getName() != null) {
                    argument.add(parameterData.getName().value().toLowerCase());
                } else {
                    String name = parameterData.getString();

                    if (!argument.contains(name)) {
                        argument.add(name);
                    }
                }

                arguments.put(i - 1, argument);
            }
        }

        for (int i = 0; i < arguments.size(); i++) {
            final List<String> argument = arguments.get(i);

            if (argument != null) {
                builder.append(" <").append(StringUtils.join(argument, "/")).append(">");
            }
        }

        return builder.toString();
    }
}

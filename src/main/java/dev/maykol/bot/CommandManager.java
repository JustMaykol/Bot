package dev.maykol.bot;

import dev.maykol.bot.command.CommandExecutor;
import dev.maykol.bot.command.adapter.Adapter;
import dev.maykol.bot.command.annotation.Command;
import dev.maykol.bot.command.annotation.Name;
import dev.maykol.bot.command.context.Context;
import dev.maykol.bot.command.data.CommandData;
import dev.maykol.bot.command.data.MethodData;
import dev.maykol.bot.command.data.ParameterData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public enum CommandManager {

    INSTANCE;

    private final Map<Class<?>, Adapter<?>> adapterStorage = new ConcurrentHashMap<>();
    private final Map<String, CommandData> commandDataStorage = new ConcurrentHashMap<>();

    public void addCommand(Object object) {
        final Command command = object.getClass().getAnnotation(Command.class);

        if (command == null) return;

        final List<MethodData> methodDataList = new ArrayList<>();

        for (Method method : object.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                if (Context.class.isAssignableFrom(method.getParameters()[0].getType())) {
                    final ParameterData[] parameterData = new ParameterData[method.getParameters().length];

                    for (int i = 0; i < method.getParameterCount(); i++) {
                        final Parameter parameter = method.getParameters()[i];
                        parameterData[i] = new ParameterData(parameter.getAnnotation(Name.class), parameter.getName(), parameter.getType());
                    }

                    methodDataList.add(new MethodData(method, parameterData));
                }
            }
        }

        final CommandData commandData = new CommandData(object, command, methodDataList.toArray(new MethodData[0]));

        for (String label : getLabel(object.getClass(), new ArrayList<>())) {
            commandDataStorage.put(label.toLowerCase(), commandData);
        }

        for (Class<?> clazz : object.getClass().getDeclaredClasses()) {
            if (clazz.getSuperclass().equals(object.getClass())) {
                try {
                    addCommand(clazz.getDeclaredConstructor(object.getClass()).newInstance(object));
                } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private List<String> getLabel(Class<?> clazz, List<String> list) {
        final List<String> toReturn = new ArrayList<>();
        final Class<?> superClazz = clazz.getSuperclass();

        if (superClazz != null) {
            final Command command = superClazz.getAnnotation(Command.class);

            if (command != null) {
                list = getLabel(superClazz, list);
            }
        }

        final Command command = clazz.getAnnotation(Command.class);

        if (command == null) {
            return list;
        }

        if (list.isEmpty()) {
            toReturn.addAll(Arrays.asList(command.label()));
        } else {
            for (String prefix : list) {
                for (String label : command.label()) {
                    toReturn.add(prefix + " " + label);
                }
            }
        }

        return toReturn;
    }

    public void execute(MessageReceivedEvent event) {
        final Message message = event.getMessage();

        final String contentRaw = message.getContentRaw();
        final String[] messageSplit = contentRaw.substring(1).split("\\s+");

        CommandData commandData = null;
        String label = null;

        message.delete().queue();

        for (int remaining = messageSplit.length; remaining > 0; --remaining) {
            label = StringUtils.join(messageSplit, " ", 0, remaining);

            if (commandDataStorage.containsKey(label.toLowerCase())) {
                commandData = commandDataStorage.get(label.toLowerCase());
                break;
            }
        }

        if (commandData != null) {
            String[] labelSplit = label.split(" ");
            String[] args = new String[0];

            if (messageSplit.length != labelSplit.length) {
                int numArgs = messageSplit.length - labelSplit.length;
                args = new String[numArgs];
                System.arraycopy(messageSplit, labelSplit.length, args, 0, numArgs);
            }

            final CommandExecutor executor = new CommandExecutor(args, label.toLowerCase(), commandData,
                    new Context(event));

            if (commandData.getCommand().async()) {
                ForkJoinPool.commonPool().execute(executor::execute);
            } else {
                executor.execute();
            }
        }
    }

    public void addAdapter(Class<?> clazz, Adapter<?> adapter) {
        adapterStorage.put(clazz, adapter);
    }

    public Adapter<?> getAdapter(Class<?> clazz) {
        return adapterStorage.get(clazz);
    }
}

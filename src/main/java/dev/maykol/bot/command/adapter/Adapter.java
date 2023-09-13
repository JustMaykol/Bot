package dev.maykol.bot.command.adapter;

import dev.maykol.bot.command.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Adapter<T> {

    @Nullable
    T parse(@NotNull Context ctx, @NotNull String input);

}

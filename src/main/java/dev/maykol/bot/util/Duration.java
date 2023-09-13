package dev.maykol.bot.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Duration {

    private static final ThreadLocal<StringBuilder> BUILDER = ThreadLocal.withInitial(StringBuilder::new);

    public static long fromString(String input) {
        long duration = 0;

        Matcher matcher = Pattern.compile("\\d+\\D+").matcher(input);

        while (matcher.find()) {
            String[] group = matcher.group().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

            String type = group[1];
            long value = Long.parseLong(group[0]);

            switch (type) {
                case "s":
                    duration += value;
                    break;
                case "m":
                    duration += (value * 60);
                    break;
                case "h":
                    duration += (value * 3600);
                    break;
                case "d":
                    duration += (value * 86400);
                    break;
            }
        }

        if (duration == 0L) {
            return -1L;
        } else {
            return duration * 1000L;
        }
    }

}

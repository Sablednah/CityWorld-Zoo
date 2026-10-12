package com.sablednah.cityworldzoo.neoforge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Player-facing output; '&' codes become real styles, never section signs (the console reads getString()). */
public final class Feedback {

    public static Component colored(String text) {
        MutableComponent out = Component.empty();
        StringBuilder run = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            ChatFormatting code = (c == '&' || c == '§') && i + 1 < text.length()
                    ? ChatFormatting.getByCode(text.charAt(i + 1)) : null;
            if (code == null) { run.append(c); continue; }
            if (!run.isEmpty()) { out.append(Component.literal(run.toString()).withStyle(style)); run.setLength(0); }
            style = code == ChatFormatting.RESET ? Style.EMPTY
                    : (isFormatting(code) ? style.applyFormat(code) : Style.EMPTY.withColor(code));
            i++;
        }
        if (!run.isEmpty()) out.append(Component.literal(run.toString()).withStyle(style));
        return out;
    }

    /** Ours, because 26.2 stripped ChatFormatting to a bare enum and isFormat() went with it. */
    private static boolean isFormatting(ChatFormatting code) {
        return code == ChatFormatting.OBFUSCATED || code == ChatFormatting.BOLD
                || code == ChatFormatting.STRIKETHROUGH || code == ChatFormatting.UNDERLINE
                || code == ChatFormatting.ITALIC;
    }

    private Feedback() {}
}

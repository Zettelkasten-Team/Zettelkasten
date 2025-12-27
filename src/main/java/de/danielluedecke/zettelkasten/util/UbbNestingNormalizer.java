package de.danielluedecke.zettelkasten.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import de.danielluedecke.zettelkasten.util.Constants;

public final class UbbNestingNormalizer {

    private static final String TAG_M_OPEN_PREFIX = "[m ";
    private static final String TAG_M_CLOSE = "[/m]";
    private static final Map<String, TagSpec> TAGS = new LinkedHashMap<>();

    static {
        TAGS.put("c", new TagSpec("[c]", "[/c]"));
        TAGS.put("m", new TagSpec(null, TAG_M_CLOSE));
    }

    private UbbNestingNormalizer() {}

    public static String normalize(String rawUbb) {
        if (rawUbb == null || rawUbb.isEmpty()) {
            return rawUbb;
        }

        StringBuilder out = new StringBuilder(rawUbb.length());
        Deque<Tag> stack = new ArrayDeque<>();
        int i = 0;
        while (i < rawUbb.length()) {
            char ch = rawUbb.charAt(i);
            if (ch == '[') {
                TagSpec cSpec = TAGS.get("c");
                if (rawUbb.startsWith(cSpec.openToken, i)) {
                    stack.push(new Tag("c"));
                    out.append(cSpec.openToken);
                    i += cSpec.openToken.length();
                    continue;
                }
                if (rawUbb.startsWith(cSpec.closeToken, i)) {
                    handleClose(out, stack, "c");
                    i += cSpec.closeToken.length();
                    continue;
                }
                if (rawUbb.startsWith(TAG_M_CLOSE, i)) {
                    handleClose(out, stack, "m");
                    i += TAG_M_CLOSE.length();
                    continue;
                }
                if (rawUbb.startsWith(TAG_M_OPEN_PREFIX, i)) {
                    int end = rawUbb.indexOf(']', i + 3);
                    if (end != -1) {
                        String openTag = rawUbb.substring(i, end + 1);
                        stack.push(new Tag("m"));
                        out.append(openTag);
                        i = end + 1;
                        continue;
                    }
                }
            }
            out.append(ch);
            i++;
        }

        while (!stack.isEmpty()) {
            out.append(closeTagFor(stack.pop()));
        }

        return out.toString();
    }

    private static void handleClose(StringBuilder out, Deque<Tag> stack, String type) {
        if (stack.isEmpty()) {
            logStrayClose(type);
            return;
        }

        if (!containsType(stack, type)) {
            logStrayClose(type);
            return;
        }

        while (!stack.isEmpty() && !stack.peek().type.equals(type)) {
            out.append(closeTagFor(stack.pop()));
        }
        if (!stack.isEmpty() && stack.peek().type.equals(type)) {
            out.append(closeTagFor(stack.pop()));
        }
    }

    private static boolean containsType(Deque<Tag> stack, String type) {
        for (Tag tag : stack) {
            if (tag.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static String closeTagFor(Tag tag) {
        return closeTagFor(tag.type);
    }

    private static String closeTagFor(String type) {
        TagSpec spec = TAGS.get(type);
        return spec != null ? spec.closeToken : "";
    }

    private static void logStrayClose(String type) {
        String tag = closeTagFor(type);
        Constants.zknlogger.log(Level.FINE, "UbbNestingNormalizer dropped stray close {0}", tag);
    }

    private static final class Tag {
        private final String type;

        private Tag(String type) {
            this.type = type;
        }
    }

    private static final class TagSpec {
        private final String openToken;
        private final String closeToken;

        private TagSpec(String openToken, String closeToken) {
            this.openToken = openToken;
            this.closeToken = closeToken;
        }
    }
}

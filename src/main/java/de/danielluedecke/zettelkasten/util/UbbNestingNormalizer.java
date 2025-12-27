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
        return normalizeWithStats(rawUbb).text;
    }

    public static NormalizeResult normalizeWithStats(String rawUbb) {
        if (rawUbb == null || rawUbb.isEmpty()) {
            return new NormalizeResult(rawUbb, false, 0, 0);
        }

        StringBuilder out = new StringBuilder(rawUbb.length());
        Deque<Tag> stack = new ArrayDeque<>();
        int droppedStrayCloses = 0;
        int autoClosed = 0;
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
                    CloseStats stats = handleClose(out, stack, "c");
                    droppedStrayCloses += stats.droppedStray;
                    autoClosed += stats.autoClosed;
                    i += cSpec.closeToken.length();
                    continue;
                }
                if (rawUbb.startsWith(TAG_M_CLOSE, i)) {
                    CloseStats stats = handleClose(out, stack, "m");
                    droppedStrayCloses += stats.droppedStray;
                    autoClosed += stats.autoClosed;
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
            autoClosed++;
        }

        boolean changed = droppedStrayCloses > 0 || autoClosed > 0;
        return new NormalizeResult(out.toString(), changed, droppedStrayCloses, autoClosed);
    }

    private static CloseStats handleClose(StringBuilder out, Deque<Tag> stack, String type) {
        int droppedStray = 0;
        int autoClosed = 0;
        if (stack.isEmpty()) {
            logStrayClose(type);
            droppedStray++;
            return new CloseStats(droppedStray, autoClosed);
        }

        if (!containsType(stack, type)) {
            logStrayClose(type);
            droppedStray++;
            return new CloseStats(droppedStray, autoClosed);
        }

        while (!stack.isEmpty() && !stack.peek().type.equals(type)) {
            out.append(closeTagFor(stack.pop()));
            autoClosed++;
        }
        if (!stack.isEmpty() && stack.peek().type.equals(type)) {
            out.append(closeTagFor(stack.pop()));
        }
        return new CloseStats(droppedStray, autoClosed);
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

    private static final class CloseStats {
        private final int droppedStray;
        private final int autoClosed;

        private CloseStats(int droppedStray, int autoClosed) {
            this.droppedStray = droppedStray;
            this.autoClosed = autoClosed;
        }
    }

    public static final class NormalizeResult {
        public final String text;
        public final boolean changed;
        public final int droppedStrayCloses;
        public final int autoClosed;

        private NormalizeResult(String text, boolean changed, int droppedStrayCloses, int autoClosed) {
            this.text = text;
            this.changed = changed;
            this.droppedStrayCloses = droppedStrayCloses;
            this.autoClosed = autoClosed;
        }
    }
}

package de.danielluedecke.zettelkasten.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UbbNestingValidator {

    private UbbNestingValidator() {}

    public static Result validate(String rawUbb) {
        if (rawUbb == null || rawUbb.isEmpty()) {
            return Result.valid();
        }
        Deque<RawTag> stack = new ArrayDeque<>();
        int i = 0;
        while (i < rawUbb.length()) {
            char ch = rawUbb.charAt(i);
            if (ch == '[') {
                if (rawUbb.startsWith("[c]", i)) {
                    stack.push(new RawTag("c", i));
                    i += 3;
                    continue;
                }
                if (rawUbb.startsWith("[/c]", i)) {
                    Result result = handleClose(stack, "c", i, "[/c]");
                    if (!result.valid) {
                        return result;
                    }
                    i += 4;
                    continue;
                }
                if (rawUbb.startsWith("[/m]", i)) {
                    Result result = handleClose(stack, "m", i, "[/m]");
                    if (!result.valid) {
                        return result;
                    }
                    i += 4;
                    continue;
                }
                if (rawUbb.startsWith("[m ", i)) {
                    int end = rawUbb.indexOf(']', i + 3);
                    if (end != -1) {
                        stack.push(new RawTag("m", i));
                        i = end + 1;
                        continue;
                    }
                }
            }
            i++;
        }

        if (!stack.isEmpty()) {
            RawTag open = stack.peek();
            return Result.invalid("unclosed [" + open.type + "]", open.pos, open.type);
        }
        return Result.valid();
    }

    private static Result handleClose(Deque<RawTag> stack, String type, int pos, String closeTag) {
        if (stack.isEmpty()) {
            return Result.invalid("stray " + closeTag, pos, type);
        }
        if (stack.peek().type.equals(type)) {
            stack.pop();
            return Result.valid();
        }
        if (!containsType(stack, type)) {
            return Result.invalid("stray " + closeTag, pos, type);
        }
        RawTag top = stack.peek();
        return Result.invalid("crossing close " + closeTag + " while [" + top.type + "] is open", pos, type);
    }

    private static boolean containsType(Deque<RawTag> stack, String type) {
        for (RawTag tag : stack) {
            if (tag.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static final class RawTag {
        private final String type;
        private final int pos;

        private RawTag(String type, int pos) {
            this.type = type;
            this.pos = pos;
        }
    }

    public static final class Result {
        public final boolean valid;
        public final String message;
        public final int rawPos;
        public final String tagType;

        private Result(boolean valid, String message, int rawPos, String tagType) {
            this.valid = valid;
            this.message = message;
            this.rawPos = rawPos;
            this.tagType = tagType;
        }

        public static Result valid() {
            return new Result(true, null, -1, null);
        }

        public static Result invalid(String message, int rawPos, String tagType) {
            return new Result(false, message, rawPos, tagType);
        }
    }
}

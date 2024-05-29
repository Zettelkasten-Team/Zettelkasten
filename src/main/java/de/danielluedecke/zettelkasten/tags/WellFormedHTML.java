package de.danielluedecke.zettelkasten.tags;

import java.util.Stack;

public class WellFormedHTML {

    public static boolean isWellFormed(String[] tags) {
        Stack<String> stack = new Stack<>();

        for (String tag : tags) {
            if (isOpenTag(tag)) {
                stack.push(tag);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                String openTag = stack.pop();
                if (!getCloseTag(openTag).equals(tag)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    private static boolean isOpenTag(String tag) {
        return !tag.startsWith("/");
    }

    private static String getCloseTag(String openTag) {
        return "/" + openTag;
    }
}

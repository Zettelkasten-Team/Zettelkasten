package de.danielluedecke.zettelkasten.tags;

import java.util.Stack;

public class WellFormedTags {

    public static boolean isWellFormed(Tag[] tags) {
        Stack<Tag> stack = new Stack<>();

        for (Tag tag : tags) {
            if (tag.isOpenTag()) {
                stack.push(tag);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                Tag openTag = stack.pop();
                if (!openTag.getCloseTag().equals(tag.getTag())) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        // Example usage
        Tag[] tags1 = { new HTMLTag("<html>"), new HTMLTag("<body>"), new HTMLTag("</body>"), new HTMLTag("</html>") };
        System.out.println(isWellFormed(tags1));  // Output: true

        Tag[] tags2 = { new HTMLTag("<html>"), new HTMLTag("<body>"), new HTMLTag("</html>"), new HTMLTag("</body>") };
        System.out.println(isWellFormed(tags2));  // Output: false

        Tag[] tags3 = { new UBBTag("[b]"), new UBBTag("[i]"), new UBBTag("[/i]"), new UBBTag("[/b]") };
        System.out.println(isWellFormed(tags3));  // Output: true

        Tag[] tags4 = { new UBBTag("[b]"), new UBBTag("[i]"), new UBBTag("[/b]"), new UBBTag("[/i]") };
        System.out.println(isWellFormed(tags4));  // Output: false
    }
}

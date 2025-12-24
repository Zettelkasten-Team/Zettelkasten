package de.danielluedecke.zettelkasten.walks;

import java.util.HashSet;
import java.util.Set;

public final class PathValidator {

    private PathValidator() {}

    /**
     * Validates that the path has distinct nodes and all adjacent pairs
     * satisfy the predicate. Null/empty paths are invalid.
     */
    public static boolean isNarrativePath(int[] path, IntNarrativeEdgePredicate predicate) {
        if (path == null || predicate == null) {
            return false;
        }
        if (path.length == 0) {
            return false;
        }
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < path.length; i++) {
            int entry = path[i];
            if (!seen.add(entry)) {
                return false;
            }
            if (i > 0 && !predicate.isNarrativeEdge(path[i - 1], entry)) {
                return false;
            }
        }
        return true;
    }
}

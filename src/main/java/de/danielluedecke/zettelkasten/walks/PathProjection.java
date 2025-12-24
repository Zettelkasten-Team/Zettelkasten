package de.danielluedecke.zettelkasten.walks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PathProjection {

    private PathProjection() {}

    /**
     * Projects a history snapshot into a simple path ending at activeIndex.
     * activeIndex is a strict index into historySnapshot; out-of-range throws.
     */
    public static int[] simplePathFromHistory(int[] historySnapshot, int activeIndex, ProjectionMode mode) {
        if (historySnapshot == null) {
            throw new IllegalArgumentException("historySnapshot must not be null");
        }
        if (activeIndex < 0 || activeIndex >= historySnapshot.length) {
            throw new IllegalArgumentException("activeIndex out of range: " + activeIndex);
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }

        switch (mode) {
            case KEEP_FIRST_OCCURRENCE:
                return keepFirstOccurrence(historySnapshot, activeIndex);
            case KEEP_LAST_OCCURRENCE:
                return keepLastOccurrence(historySnapshot, activeIndex);
            case CUT_ON_REPEAT:
                return cutOnRepeat(historySnapshot, activeIndex);
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private static int[] keepFirstOccurrence(int[] historySnapshot, int activeIndex) {
        List<Integer> path = new ArrayList<>();
        for (int i = 0; i <= activeIndex; i++) {
            int entry = historySnapshot[i];
            if (!contains(path, entry)) {
                path.add(entry);
            }
        }
        return toIntArray(path);
    }

    private static int[] keepLastOccurrence(int[] historySnapshot, int activeIndex) {
        List<Integer> reversed = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int i = activeIndex; i >= 0; i--) {
            int entry = historySnapshot[i];
            if (seen.add(entry)) {
                reversed.add(entry);
            }
        }
        Collections.reverse(reversed);
        return toIntArray(reversed);
    }

    private static int[] cutOnRepeat(int[] historySnapshot, int activeIndex) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i <= activeIndex; i++) {
            int entry = historySnapshot[i];
            if (seen.contains(entry)) {
                break;
            }
            seen.add(entry);
            path.add(entry);
        }
        return toIntArray(path);
    }

    private static boolean contains(List<Integer> list, int value) {
        for (int entry : list) {
            if (entry == value) {
                return true;
            }
        }
        return false;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}

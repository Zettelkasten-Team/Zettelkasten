package de.danielluedecke.zettelkasten.walks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HistorySegmenter {

    private HistorySegmenter() {}

    /**
     * Returns contiguous windows ending at activeIndex, sizes 1..maxWindowSize (smallest-first).
     */
    public static List<int[]> candidateWindows(int[] historySnapshot, int activeIndex, int maxWindowSize) {
        if (historySnapshot == null) {
            throw new IllegalArgumentException("historySnapshot must not be null");
        }
        if (activeIndex < 0 || activeIndex >= historySnapshot.length) {
            throw new IllegalArgumentException("activeIndex out of range: " + activeIndex);
        }
        if (maxWindowSize < 1) {
            throw new IllegalArgumentException("maxWindowSize must be >= 1");
        }

        int maxSize = Math.min(maxWindowSize, activeIndex + 1);
        List<int[]> windows = new ArrayList<>();
        for (int size = 1; size <= maxSize; size++) {
            int start = activeIndex - size + 1;
            int[] window = Arrays.copyOfRange(historySnapshot, start, activeIndex + 1);
            windows.add(window);
        }
        return windows;
    }
}

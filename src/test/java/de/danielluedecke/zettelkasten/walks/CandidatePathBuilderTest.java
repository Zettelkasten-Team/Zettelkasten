package de.danielluedecke.zettelkasten.walks;

import de.danielluedecke.zettelkasten.data.History;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class CandidatePathBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void pathProjectionRejectsNegativeActiveIndex() {
        PathProjection.simplePathFromHistory(new int[] { 1 }, -1, ProjectionMode.KEEP_FIRST_OCCURRENCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void pathProjectionRejectsTooLargeActiveIndex() {
        PathProjection.simplePathFromHistory(new int[] { 1 }, 1, ProjectionMode.KEEP_FIRST_OCCURRENCE);
    }

    @Test
    public void pathProjectionKeepFirstOccurrence() {
        int[] history = new int[] { 1, 2, 3, 2, 4 };
        int[] projected = PathProjection.simplePathFromHistory(history, 4, ProjectionMode.KEEP_FIRST_OCCURRENCE);
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, projected);
    }

    @Test
    public void pathProjectionKeepLastOccurrence() {
        int[] history = new int[] { 1, 2, 3, 2, 4 };
        int[] projected = PathProjection.simplePathFromHistory(history, 4, ProjectionMode.KEEP_LAST_OCCURRENCE);
        assertArrayEquals(new int[] { 1, 3, 2, 4 }, projected);
    }

    @Test
    public void pathProjectionCutOnRepeat() {
        int[] history = new int[] { 1, 2, 3, 2, 4 };
        int[] projected = PathProjection.simplePathFromHistory(history, 4, ProjectionMode.CUT_ON_REPEAT);
        assertArrayEquals(new int[] { 1, 2, 3 }, projected);
    }

    @Test
    public void pathValidatorRejectsNullOrEmpty() {
        assertFalse(PathValidator.isNarrativePath(null, IntNarrativeEdges.ALWAYS_TRUE));
        assertFalse(PathValidator.isNarrativePath(new int[0], IntNarrativeEdges.ALWAYS_TRUE));
        assertFalse(PathValidator.isNarrativePath(new int[] { 1 }, null));
    }

    @Test
    public void pathValidatorDetectsDuplicatesAndPredicateViolations() {
        assertFalse(PathValidator.isNarrativePath(new int[] { 1, 2, 1 }, IntNarrativeEdges.ALWAYS_TRUE));
        assertFalse(PathValidator.isNarrativePath(new int[] { 1, 2, 2, 3 }, IntNarrativeEdges.NO_SELF_LOOPS));
        assertTrue(PathValidator.isNarrativePath(new int[] { 1, 2, 3 }, IntNarrativeEdges.ALWAYS_TRUE));
    }

    @Test
    public void historySegmenterExtractsWindows() {
        int[] history = new int[] { 1, 2, 3, 4 };
        List<int[]> windows = HistorySegmenter.candidateWindows(history, 3, 3);
        assertEquals(3, windows.size());
        assertArrayEquals(new int[] { 4 }, windows.get(0));
        assertArrayEquals(new int[] { 3, 4 }, windows.get(1));
        assertArrayEquals(new int[] { 2, 3, 4 }, windows.get(2));
    }

    @Test
    public void candidateBuilderSelectsFullPathForSimpleHistory() {
        int[] history = new int[] { 1, 2, 3, 4 };
        Optional<int[]> candidate = CandidatePathBuilder.bestCandidatePath(
                history,
                3,
                4,
                ProjectionMode.KEEP_LAST_OCCURRENCE,
                IntNarrativeEdges.ALWAYS_TRUE);
        assertTrue(candidate.isPresent());
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, candidate.get());
    }

    @Test
    public void candidateBuilderRespectsKeepLastOccurrence() {
        int[] history = new int[] { 1, 2, 3, 2, 4 };
        Optional<int[]> candidate = CandidatePathBuilder.bestCandidatePath(
                history,
                4,
                5,
                ProjectionMode.KEEP_LAST_OCCURRENCE,
                IntNarrativeEdges.ALWAYS_TRUE);
        assertTrue(candidate.isPresent());
        assertArrayEquals(new int[] { 1, 3, 2, 4 }, candidate.get());
    }

    @Test
    public void candidateBuilderTieBreaksBySuffixMatch() {
        int[] history = new int[] { 1, 2, 1, 3 };
        Optional<int[]> candidate = CandidatePathBuilder.bestCandidatePath(
                history,
                3,
                4,
                ProjectionMode.KEEP_FIRST_OCCURRENCE,
                IntNarrativeEdges.ALWAYS_TRUE);
        assertTrue(candidate.isPresent());
        assertArrayEquals(new int[] { 2, 1, 3 }, candidate.get());
    }

    @Test
    public void historyWalkAdapterBuildsCandidatePath() {
        History history = new History(5);
        history.addToHistory(1);
        history.addToHistory(2);
        history.addToHistory(3);
        history.historyBack();
        history.addToHistory(4);

        Optional<int[]> candidate = HistoryWalkAdapter.candidatePathP(
                history,
                5,
                ProjectionMode.KEEP_LAST_OCCURRENCE);
        assertTrue(candidate.isPresent());
        assertArrayEquals(new int[] { 1, 2, 4 }, candidate.get());
    }
}

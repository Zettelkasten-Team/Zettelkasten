package de.danielluedecke.zettelkasten.walks;

@FunctionalInterface
public interface IntNarrativeEdgePredicate {
    boolean isNarrativeEdge(int from, int to);
}

package de.danielluedecke.zettelkasten.walks;

public final class IntNarrativeEdges {

    private IntNarrativeEdges() {}

    public static final IntNarrativeEdgePredicate ALWAYS_TRUE = (from, to) -> true;
    public static final IntNarrativeEdgePredicate NO_SELF_LOOPS = (from, to) -> from != to;
}

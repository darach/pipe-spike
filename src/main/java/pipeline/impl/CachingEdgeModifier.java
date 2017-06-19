/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl;

import com.hazelcast.jet.Edge;

/**
 * Caches a Jet Edge for specialization when defining Operators or
 * creating connections between the connectable vertices of a DAG
 * encapsulated by a pipeline
 */
/*package-local*/ class CachingEdgeModifier {
    protected Edge edge;

    public Edge apply(Edge e) {
        edge = e;
        return e;
    }
}

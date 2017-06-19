/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import com.hazelcast.jet.Edge;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Pipelines are composed from reusable templates. Templates are DAGs.
 * As a template may be used multiply to define a useful pipeline each
 * instance needs a handle or reference. The binding context is a handle
 * for such an instance and allows connections ( edges ) to be defined
 * between an instance and its host or parent pipeline.
 */

public interface BindingContext {
    /**
     * Get the pipeline that owns this binding.
     * @return
     */
    Pipeline parent();

    /**
     * Connect two vertices in the pipeline and return their port. The
     * vertices are connected on ordinal 0.
     *
     * @param fromVertex The vertex being connected
     * @param toVertex The vertex being connected to
     * @return The port through which the vertices are connected
     */
    Port connect(@Nonnull final String fromVertex, @Nonnull final String toVertex);

    /**
     * Connect two vertices in the pipeline and return their port. The
     * DAG edge underlying the port can be specialised. The vertices are
     * connected on ordinal 0
     *
     * @param fromVertex The vertex being connected
     * @param toVertex The vertex being connected to
     * @param edgeModifier A possibly null edge modifier to specialise this ports configuration
     * @return The port through which the vertices are connected
     */
    Port connect(@Nonnull final String fromVertex, @Nonnull final String toVertex, Function<Edge,Edge> edgeModifier);

    /**
     * Connect two vertices in the pipeline and return their port. The
     * DAG edge underlying the port can be specialised. The vertices are
     * connected on the specified ordinals
     *
     * @param fromVertex The vertex being connected
     * @param fromOrdinal The vertex ordinal being connected
     * @param toVertex The vertex being connected to
     * @param toOrdinal The vertex ordinal being connected to
     * @param edgeModifier A possibly null edge modifier to specialise this ports configuration
     * @return The port through which the vertices are connected
     */
    Port connect(@Nonnull final String fromVertex, int fromOrdinal, @Nonnull final String toVertex, int toOrdinal, Function<Edge,Edge> edgeModifier);

}

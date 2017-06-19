/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.jet.Processor;
import com.hazelcast.jet.ResettableSingletonTraverser;
import com.hazelcast.jet.Vertex;
import com.hazelcast.jet.function.DistributedSupplier;
import com.hazelcast.jet.impl.processor.TransformP;

import javax.annotation.Nonnull;

/**
 * Utility functions useful in pipeline construction
 *
 */
public class PipelineUtil {

    /**
     * A processor that passes through its inputs as outputs
     * @param <T> The input and output type
     * @return The processor
     */
    @Nonnull
    public static <T> DistributedSupplier<Processor> passthrough() {
        return () -> {
            final ResettableSingletonTraverser<T> trav = new ResettableSingletonTraverser<>();
            return new TransformP<>(item -> {
                trav.accept((T) item);
                return trav;
            });
        };
    }

    /**
     * Creates a list of vertices from an interable over vertices
     *
     * @param iterable The iterable over vertices to be collected
     * @return A list of vertices
     */
    public static List<Vertex> vertices(final Iterable<Vertex> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }
}

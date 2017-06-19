/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Edge;
import com.hazelcast.jet.Vertex;
import pipeline.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Default boilerplate implementation of a binding context.
 */
public class AbstractBindingContext implements BindingContext {
    private final Pipeline parent;
    private final DAG dag;
    private final Pipeline fromPipeline;
    private final String alias;
    private final DAG fromDag;
    private int ordinal = 0;

    public AbstractBindingContext(Pipeline parent, Pipeline fromPipeline, String alias) {
        this.parent = parent;
        this.dag = parent.dag();
        this.fromPipeline = fromPipeline;
        this.alias = alias;
        this.fromDag = fromPipeline.dag();
        cloneTemplateIntoHost();
    }

    @Override
    public Pipeline parent() {
        return parent;
    }

    private void cloneTemplateIntoHost() {
        final DAG fd = fromPipeline.dag();
        final DAG td = dag;


        if ( fd == td ) {
            return;
        }

        // We're not self-binding, so we need to clone the pipeline / template
        // First pass - clone vertices
        //
        for (Vertex from : fd) {
            // Compute Fully qualified vertex name
            final String path = from(from);
            dag.newVertex(path, from.getSupplier()).localParallelism(from.getLocalParallelism());
        }

        // Second pass. Clone edges
        //
        for (Vertex from : fromDag) {
            for (Edge fromEdge : fromDag.getInboundEdges(from.getName())) {
                final Vertex originSource = fromEdge.getSource();
                final Vertex originDest = fromEdge.getDestination();
                final Vertex targetOrigin = to(originSource,parent);
                final Vertex targetDest = to(originDest,parent);
                // NOTE CHECK & specialize - IFF source ordinal == target ordinal == 0
                // NOTE CHECK & correct - partitioning, distributed, isolation etc..
                // System.out.println("Cloning inbound Edge: " + from(originSource) + " -> " + from(originDest));
                dag.edge(
                    Edge.from(targetOrigin,fromEdge.getSourceOrdinal())
                    .to(targetDest,fromEdge.getDestOrdinal())
                    .setConfig(fromEdge.getConfig())
                );
            }
        }
    }

    @Override
    public Port connect(@Nonnull String fromVertex, @Nonnull String toVertex) {
        return connect(fromVertex, 0, toVertex, 0, (e) -> e);
    }

    @Override
    public Port connect(@Nonnull String fromVertex, @Nonnull String toVertex, Function<Edge,Edge> edgeModifier) {
        return connect(fromVertex, 0, toVertex, 0, edgeModifier);
    }

    @Override
    public Port connect(@Nonnull String fromVertex, int fromOrdinal, @Nonnull String toVertex, int toOrdinal, @Nonnull Function<Edge,Edge> edgeModifier) {
        final List<Edge> fo = fromPipeline.dag().getOutboundEdges(fromVertex);
        final List<Edge> ti = dag.getInboundEdges(toVertex);

        if ( fo.size() > 0 && ti.size() > 0 ) {
            throw new IllegalArgumentException("Cannot bind already bound vertices");
        }

        if ( !"$".equals(parent.name()) ) {
            throw new IllegalArgumentException("Cannot connect instances to templates");
        } else {
//            Vertex fv = dag.getVertex(fromVertex);
//            Vertex tv =
            System.out.println("Connecting: " + dag.getVertex(from(fromVertex)) + " -> " + to(toVertex));
            CachingEdgeModifier sneakyEdge = new CachingEdgeModifier();
            dag.edge(sneakyEdge.apply(edgeModifier.apply(Edge.from(dag.getVertex(from(fromVertex)),fromOrdinal).to(to(toVertex),toOrdinal))));
            Port port = SimplePort.newPort(this.parent, sneakyEdge.edge, "port" + ordinal++);

            dag.iterator(); // Force validation as side-effect
            return port;
        }


    }

    private Vertex to(String name) {
        if ( "$".equalsIgnoreCase(parent.path()) ) {
            return dag.getVertex(name);
        } else {
            return parent.dag().getVertex(from(name));
        }
    }

    private Vertex to(Vertex origin, Pipeline to) {
        return to.dag().getVertex(from(origin));
    }

    private String from(Vertex from) {
        if ( "$".equals(fromPipeline.path()) ) {
            return from.getName();
        } else {
            return from(from.getName());
        }
    }

    private String from(String from) {
        if ( "$".equals(fromPipeline.path()) ) {
            return from;
        } else {
            String part = (alias == null) ? fromPipeline.name() : alias;
            return parent.path() + "." + part + "." + from;
        }
    }
}
